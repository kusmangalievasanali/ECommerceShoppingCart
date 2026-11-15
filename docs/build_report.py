"""Regenerate REPORT.pdf after editing report_metadata.json. Requires reportlab."""
from pathlib import Path
from xml.sax.saxutils import escape
import json
import re
import os
import textwrap

from reportlab.lib import colors
from reportlab.lib.enums import TA_CENTER
from reportlab.lib.pagesizes import A4
from reportlab.lib.styles import ParagraphStyle, getSampleStyleSheet
from reportlab.lib.utils import ImageReader
from reportlab.pdfbase import pdfmetrics
from reportlab.pdfbase.ttfonts import TTFont
from reportlab.platypus import (
    SimpleDocTemplate, Paragraph, Spacer, PageBreak, Image, Table, TableStyle,
    Preformatted, KeepTogether,
)

HERE = Path(__file__).resolve().parent
ROOT = HERE.parent

def find_font(filename):
    roots = [HERE / 'fonts', Path('/usr/share/fonts/truetype/dejavu')]
    for root in roots:
        candidate = root / filename
        if candidate.exists():
            return str(candidate)
    raise FileNotFoundError('Place DejaVu fonts in docs/fonts; see docs/fonts/README.md')

for name, filename in [
    ('DV', 'DejaVuSans.ttf'), ('DV-Bold', 'DejaVuSans-Bold.ttf'),
    ('DVMono', 'DejaVuSansMono.ttf'),
]:
    pdfmetrics.registerFont(TTFont(name, find_font(filename)))
pdfmetrics.registerFontFamily('DV', normal='DV', bold='DV-Bold', italic='DV', boldItalic='DV-Bold')

styles = getSampleStyleSheet()
styles.add(ParagraphStyle(name='BodyRU', fontName='DV', fontSize=9.5, leading=14.5, spaceAfter=8))
styles.add(ParagraphStyle(name='H1RU', fontName='DV-Bold', fontSize=18, leading=24, spaceAfter=16))
styles.add(ParagraphStyle(name='H2RU', fontName='DV-Bold', fontSize=12, leading=17, spaceBefore=12, spaceAfter=8))
styles.add(ParagraphStyle(name='CellRU', fontName='DV', fontSize=8, leading=11))
styles.add(ParagraphStyle(name='CaptionRU', fontName='DV', fontSize=8, leading=11, textColor=colors.HexColor('#475569'), spaceAfter=14))
styles.add(ParagraphStyle(name='CodeRU', fontName='DVMono', fontSize=6.8, leading=9, spaceAfter=10))
styles.add(ParagraphStyle(name='BulletRU', parent=styles['BodyRU'], leftIndent=13, firstLineIndent=-10))

PAGE_W, PAGE_H = A4
MARGIN = 44
WIDTH = PAGE_W - MARGIN * 2
story = []

def inline(value):
    value = escape(value)
    value = re.sub(r'\*\*(.+?)\*\*', r'<b>\1</b>', value)
    value = re.sub(r'`([^`]+)`', r'<font name="DVMono">\1</font>', value)
    return value

def para(value, style='BodyRU'):
    return Paragraph(inline(value), styles[style])

def add_image(filename, caption):
    path = HERE / filename
    iw, ih = ImageReader(str(path)).getSize()
    scale = min(WIDTH / iw, 610 / ih, 1)
    image = Image(str(path), width=iw * scale, height=ih * scale)
    story.append(KeepTogether([image, Spacer(1, 6), para(caption, 'CaptionRU')]))

def add_table(lines):
    rows = [[cell.strip() for cell in line.strip().strip('|').split('|')] for line in lines]
    rows = [row for row in rows if not all(re.fullmatch(r'[:\- ]+', cell) for cell in row)]
    count = len(rows[0])
    if count == 2:
        widths = [WIDTH * 0.23, WIDTH * 0.77]
    elif count == 3:
        widths = [WIDTH * 0.23, WIDTH * 0.23, WIDTH * 0.54]
    else:
        widths = [WIDTH / count] * count
    data = [[para(cell, 'CellRU') for cell in row] for row in rows]
    table = Table(data, colWidths=widths, repeatRows=1, hAlign='LEFT')
    table.setStyle(TableStyle([
        ('BACKGROUND', (0, 0), (-1, 0), colors.HexColor('#e2e8f0')),
        ('GRID', (0, 0), (-1, -1), 0.4, colors.HexColor('#cbd5e1')),
        ('VALIGN', (0, 0), (-1, -1), 'TOP'),
        ('LEFTPADDING', (0, 0), (-1, -1), 7),
        ('RIGHTPADDING', (0, 0), (-1, -1), 7),
        ('TOPPADDING', (0, 0), (-1, -1), 7),
        ('BOTTOMPADDING', (0, 0), (-1, -1), 7),
    ]))
    story.extend([table, Spacer(1, 12)])

metadata = json.loads((HERE / 'report_metadata.json').read_text(encoding='utf-8'))
text = (HERE / 'REPORT.md').read_text(encoding='utf-8')
for key in ['group', 'teacher', 'repository', 'deadline']:
    text = text.replace('{{' + key.upper() + '}}', str(metadata[key]))
text = text.replace('{{TEAM_MEMBERS}}', '; '.join(metadata['team_members']))

lines = text.splitlines()
i = 0
first_heading = True
while i < len(lines):
    line = lines[i].strip()
    if not line:
        i += 1
        continue
    if line == '{{JAVA_SOURCE_APPENDIX}}':
        sources = sorted((ROOT / 'src/main/java').rglob('*.java'),
                         key=lambda p: (p.name != 'Main.java', str(p)))
        for source in sources:
            story.append(para(str(source.relative_to(ROOT)), 'H2RU'))
            story.append(Preformatted(source.read_text(encoding='utf-8').rstrip(), styles['CodeRU']))
        i += 1
        continue
    if line.startswith('# '):
        if not first_heading:
            story.append(PageBreak())
        first_heading = False
        story.append(para(line[2:], 'H1RU'))
        i += 1
        continue
    if line.startswith('## '):
        story.append(para(line[3:], 'H2RU'))
        i += 1
        continue
    if line.startswith('```'):
        block = []
        i += 1
        while i < len(lines) and not lines[i].startswith('```'):
            block.append(lines[i])
            i += 1
        story.append(Preformatted('\n'.join(block), styles['CodeRU']))
        i += 1
        continue
    match = re.fullmatch(r'!\[(.*?)\]\((.*?)\)', line)
    if match:
        add_image(match.group(2), match.group(1))
        i += 1
        continue
    if line.startswith('|'):
        block = []
        while i < len(lines) and lines[i].strip().startswith('|'):
            block.append(lines[i])
            i += 1
        add_table(block)
        continue
    if line.startswith('- '):
        story.append(para('• ' + line[2:], 'BulletRU'))
        i += 1
        continue
    block = [line]
    i += 1
    while i < len(lines) and lines[i].strip() and not lines[i].startswith(('#', '|', '!', '-', '```', '{{')):
        block.append(lines[i].strip())
        i += 1
    story.append(para(' '.join(block)))

def decorate(canvas, doc):
    canvas.saveState()
    canvas.setFont('DV', 7)
    canvas.setFillColor(colors.HexColor('#64748b'))
    canvas.drawString(MARGIN, PAGE_H - 25, 'E-Commerce Shopping Cart · Final Project')
    canvas.drawRightString(PAGE_W - MARGIN, 24, str(doc.page))
    canvas.restoreState()

doc = SimpleDocTemplate(str(HERE / 'REPORT.pdf'), pagesize=A4,
                        leftMargin=MARGIN, rightMargin=MARGIN,
                        topMargin=45, bottomMargin=42,
                        title='E-Commerce Shopping Cart — Final Project Report',
                        author='; '.join(metadata['team_members']))
doc.build(story, onFirstPage=decorate, onLaterPages=decorate)
print('Created', HERE / 'REPORT.pdf')
