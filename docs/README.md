# Документы проекта

`REPORT.pdf` — полный технический отчёт. Он включает обязательные разделы,
классовые диаграммы, последовательность, снимки фактического консольного вывода
и весь production-код Java.

Данные команды и Git-ссылку заполните в `report_metadata.json`, затем пересоберите
отчёт. `REPORT.md` — редактируемый текст, `build_report.py` — сборщик PDF.

Из корня проекта:

```sh
python -m venv .venv
```

Windows PowerShell:

```powershell
.\.venv\Scripts\python.exe -m pip install -r docs/requirements.txt
.\.venv\Scripts\python.exe docs/build_report.py
```

Linux/macOS:

```sh
.venv/bin/python -m pip install -r docs/requirements.txt
.venv/bin/python docs/build_report.py
```

Для приложения Java Python и ReportLab не нужны. Шрифты для PDF приложены
в `fonts`, лицензия находится рядом.

`uml` содержит PlantUML и PNG. При изменении интерфейсов обновите диаграммы.
`screenshots` содержит изображения фактического вывода JAR и Maven, показанного
в браузерном просмотрщике консоли. Raw-логи — `demo-output.txt`, `test-summary.txt`,
`build-output.txt`. При доработке кода повторите запуск и обновите доказательства.

`CHANGES_RU.md` описывает исправления; `REQUIREMENTS_CHECKLIST.md` перечисляет все
критерии; `TEAM_WORKFLOW_RU.md` — реальные вклады команды; `DEFENSE_RU.md` — теория.
