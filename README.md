# E-Commerce Shopping Cart

A Java 17 console application for configuring products, managing a shopping cart,
applying discounts and processing simulated payments.

The project combines six design patterns in a single shopping workflow.

## Team

| Team member | Responsibility |
|---|---|
| Damir | Builder and Factory Method |
| Nurali | Decorator and Adapter |
| Asanali | Observer and Strategy |

## Design patterns

| Category | Pattern | Application |
|---|---|---|
| Creational | Builder | Step-by-step configuration of custom PCs |
| Creational | Factory Method | Creation of laptops and headphones |
| Structural | Decorator | Gift wrapping, extended warranty and express delivery |
| Structural | Adapter | Common payment interface for Kaspi and Stripe simulations |
| Behavioral | Observer | Notifications about price and inventory changes |
| Behavioral | Strategy | Interchangeable discount algorithms |

## Requirements

- JDK 17 or newer
- Internet connection for the first Maven build

Maven Wrapper is included, so a separate Maven installation is not required.

## Run in IntelliJ IDEA

1. Open the project's `pom.xml` as a Maven project.
2. Select JDK 17 or newer as the Project SDK.
3. Reload Maven dependencies.
4. Run `src/main/java/Main.java`.

`Main` demonstrates product creation, additional services, price and stock
notifications, discount calculation and checkout through both payment adapters.

## Build and test

### Windows PowerShell

```powershell
.\mvnw.cmd clean verify
```

### macOS / Linux

```sh
chmod +x mvnw
./mvnw clean verify
```

The test suite contains 33 test cases covering the patterns, monetary calculations,
inventory reservations, payment failures and input validation.

Test reports are saved in:

```text
target/surefire-reports/
```

## Run the packaged application

After a successful build:

```sh
java -jar target/ecommerce-shopping-cart-1.0.0.jar
```

The application runs offline. Kaspi and Stripe payments are local simulations.

## Project structure

```text
src/
├── main/
│   └── java/
│       ├── Main.java
│       ├── builder/
│       │   ├── builders/
│       │   ├── director/
│       │   └── product/
│       ├── factory/
│       │   ├── creators/
│       │   └── products/
│       ├── decorator/
│       │   ├── base/
│       │   └── concrete/
│       ├── adapter/
│       │   ├── adaptees/
│       │   ├── adapters/
│       │   └── target/
│       ├── observer/
│       │   ├── events/
│       │   ├── observers/
│       │   └── subject/
│       └── strategy/
│           ├── context/
│           ├── model/
│           └── strategies/
└── test/
    └── java/
        ├── builder/
        ├── factory/
        ├── decorator/
        ├── adapter/
        ├── observer/
        └── strategy/
```

The source code has six top-level pattern packages.
`Main` is the only production class outside these packages.

## Business rules

- All prices are expressed in KZT.
- Monetary calculations use `BigDecimal` with two decimal places and
  `HALF_UP` rounding.
- Custom PCs have configurable CPU, GPU, RAM and storage.
  The base price is supplied for each configuration.
- Gift wrapping costs 1500.00 KZT, extended warranty costs 9000.00 KZT,
  and express delivery costs 3000.00 KZT per unit.
- Discounts apply to the subtotal, including additional services.
- Available discount policies include no discount, percentage discounts,
  and the `SAVE10` and `STUDENT15` promo codes.
- Catalog events report base prices. Decorated prices include service fees.
- Price increases, price decreases and stock changes notify subscribed observers.
- Quantities of the same SKU are combined before inventory reservation.
- Failed payments restore reserved stock and preserve the cart.
- Successful checkout commits stock changes and clears the cart.
- Orders with a zero total are completed without calling a payment provider.
- Completed receipts preserve their original prices and descriptions.

The application stores data in memory and processes operations sequentially.

## Documentation

The `docs/` directory contains the project report and supporting materials:

- PDF project report
- `uml/` — PlantUML source files and rendered diagrams
- `screenshots/` — application output and test results
