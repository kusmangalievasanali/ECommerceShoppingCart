# E-Commerce Shopping Cart — project

Six patterns: Builder, Factory Method, Decorator, Adapter, Observer, Strategy.
Currency: KZT. Payments are educational simulations with no network calls.

## IntelliJ IDEA: open and run

1. Extract the archive into a new folder. Open `pom.xml` as a Maven project.
2. Set Project SDK and Maven Runner JRE to JDK 17 or newer.
3. Reload Maven. Run `src/main/java/Main.java` with the green triangle.
4. Run Maven Lifecycle → verify to execute all automated tests.

To update your existing Git repository, back it up first. Keep its `.git` directory,
replace the old `src` tree with this archive's `src`, and copy the root files and docs.
Do not retain the old `src/Main.java` or old pattern folders beside `src/main/java`.
Do not copy a new `.git` directory over your existing repository.

## Terminal

Windows PowerShell:

```powershell
.\mvnw.cmd clean verify
java -jar target/ecommerce-shopping-cart-1.0.0.jar
```

macOS / Linux:

```sh
chmod +x mvnw
./mvnw clean verify
java -jar target/ecommerce-shopping-cart-1.0.0.jar
```

JDK 17+ is required. Maven Wrapper downloads Maven on its first use. Maven Central
access is required to download test dependencies. The built application runs offline.

## Source hierarchy

```text
src/main/java/
  Main.java
  builder/{builders,director,product}/
  factory/{creators,products}/
  decorator/{base,concrete}/
  adapter/{adaptees,adapters,target}/
  observer/{events,observers,subject}/
  strategy/{context,model,strategies}/
src/test/java/
  builder/ factory/ decorator/ adapter/ observer/ strategy/
```

Exactly six top-level packages. Main is the only production class outside them.

## Business rules

- Every concrete configuration has a SKU, name and immutable base definition.
  Builder configures CPU, GPU, RAM and SSD. The merchant supplies the final base
  price for the SKU; component-price calculation is outside this prototype.
- The catalog wraps configurations with live base prices and integer stock.
  Only catalog operations mutate prices or stock, keeping Observer events consistent.
- Cart items use catalog views or decorators over them. A price change immediately
  affects a cart not yet checked out. Event prices always mean base catalog prices.
- Gift wrapping 1500.00, warranty 9000.00 and express delivery 3000.00 KZT are
  charged per unit. All services are included before percentage discounts.
- BigDecimal and HALF_UP are used. Base amounts are rounded to two decimal places;
  percentage discount amounts are rounded before subtracting from subtotal.
- Discount policies: no discount, 0–100 percent, SAVE10 and STUDENT15 promo codes.
- A zero-total order consumes stock and clears the cart without calling a bank.
- Repeated SKUs across decorated and undecorated lines share inventory quantities.
- All requested quantities are checked before reserving any stock. Payment failure
  or invalid confirmation rolls back the reservation and preserves the cart.
- Committed stock changes notify observers. Failed reservations do not publish
  completed-sale events. One failing observer does not block other subscribers.
- The catalog permits one active reservation. Other catalog mutations are blocked
  until it commits or closes, preventing overlapping rollback corruption.
- Receipts preserve immutable price/description snapshots; future price changes
  do not alter completed receipts.

This is a single-threaded, in-memory teaching model. There is no real payment,
database, email, persistence, production transaction manager or GUI. Product and
discount implementations are trusted application components. Concurrent checkout,
provider idempotency and distributed payment recovery require further development.

## Documentation and final submission

- `docs/REPORT.pdf`: Introduction, Body, Conclusion, Further Work, screenshots,
  UML and complete production Java source appendix.
- `docs/uml/`: editable PlantUML and rendered PNG diagrams.
- `docs/screenshots/`: screenshots of actual captured console and test output.
- `docs/DEFENSE_RU.md`: theory and demonstration guide.
- `docs/TEAM_WORKFLOW_RU.md`: genuine contribution workflow.
- `docs/REQUIREMENTS_CHECKLIST.md`: completed technical criteria and external actions.
- `docs/CHANGES_RU.md`: changes from the uploaded archive.

Each student must personally implement/understand at least two patterns and have
genuine commits. A prepared archive does not prove contribution. Add your real
Git repository link and submit PDF + source ZIP individually to Moodle before the
actual deadline, then defend the project. No deadline date was provided in the PDF.

Report metadata can be filled in `docs/report_metadata.json`. To rebuild the PDF:
create a Python virtual environment, install `docs/requirements.txt`, then run
`python docs/build_report.py`. Python is optional and not needed to run Java.
