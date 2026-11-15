package observer.subject;

import builder.product.Money;
import builder.product.Product;
import observer.events.EventType;
import observer.events.ProductEvent;
import observer.observers.ProductObserver;
import strategy.context.InventoryGateway;
import strategy.context.StockReservation;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public final class ProductCatalogNotifier implements ProductSubject, InventoryGateway {
    private final Map<String, CatalogProduct> products = new LinkedHashMap<>();
    private final Set<ProductObserver> observers = new LinkedHashSet<>();
    private final List<String> notificationErrors = new ArrayList<>();
    private boolean reservationOpen;

    public CatalogProduct register(Product product, int stock) {
        requireNoReservation();
        Objects.requireNonNull(product, "Product is required");
        if (stock < 0) { throw new IllegalArgumentException("Stock cannot be negative"); }
        CatalogProduct listing = new CatalogProduct(product, stock);
        if (products.putIfAbsent(product.getSku(), listing) != null) {
            throw new IllegalArgumentException("Duplicate SKU: " + product.getSku());
        }
        return listing;
    }

    public CatalogProduct getProduct(String sku) {
        CatalogProduct product = products.get(sku);
        if (product == null) { throw new IllegalArgumentException("Unknown SKU: " + sku); }
        return product;
    }

    @Override
    public void addObserver(ProductObserver observer) {
        observers.add(Objects.requireNonNull(observer, "Observer is required"));
    }

    @Override
    public void removeObserver(ProductObserver observer) { observers.remove(observer); }

    public List<String> getNotificationErrors() { return List.copyOf(notificationErrors); }

    public void changePrice(String sku, BigDecimal newPrice) {
        requireNoReservation();
        CatalogProduct product = getProduct(sku);
        BigDecimal validPrice = Money.amount(newPrice);
        BigDecimal oldPrice = product.getPrice();
        if (oldPrice.compareTo(validPrice) == 0) { return; }
        product.updatePrice(validPrice);
        publish(new ProductEvent(sku, EventType.PRICE_CHANGED, oldPrice, validPrice,
                product.getStock(), product.getStock()));
    }

    public void restock(String sku, int quantity) {
        requireNoReservation();
        if (quantity <= 0) { throw new IllegalArgumentException("Quantity must be positive"); }
        CatalogProduct product = getProduct(sku);
        int oldStock = product.getStock();
        product.updateStock(Math.addExact(oldStock, quantity));
        publish(stockEvent(product, oldStock));
    }

    @Override
    public StockReservation reserve(Map<String, Integer> requested) {
        requireNoReservation();
        Map<String, Integer> quantities = new LinkedHashMap<>(requested);
        if (quantities.isEmpty()) { throw new IllegalArgumentException("Reservation cannot be empty"); }
        for (Map.Entry<String, Integer> entry : quantities.entrySet()) {
            if (entry.getValue() == null || entry.getValue() <= 0) {
                throw new IllegalArgumentException("Quantity must be positive");
            }
            if (getProduct(entry.getKey()).getStock() < entry.getValue()) {
                throw new IllegalStateException("Insufficient stock for " + entry.getKey());
            }
        }
        Map<String, Integer> previousStocks = new LinkedHashMap<>();
        reservationOpen = true;
        quantities.forEach((sku, quantity) -> {
            CatalogProduct product = getProduct(sku);
            previousStocks.put(sku, product.getStock());
            product.updateStock(product.getStock() - quantity);
        });
        return new CatalogReservation(previousStocks);
    }

    private ProductEvent stockEvent(CatalogProduct product, int oldStock) {
        return new ProductEvent(product.getSku(), EventType.STOCK_CHANGED,
                product.getPrice(), product.getPrice(), oldStock, product.getStock());
    }

    private void requireNoReservation() {
        if (reservationOpen) {
            throw new IllegalStateException("Catalog mutation is blocked during an active reservation");
        }
    }

    private void publish(ProductEvent event) {
        for (ProductObserver observer : List.copyOf(observers)) {
            try {
                observer.onProductChanged(event);
            } catch (RuntimeException exception) {
                notificationErrors.add(exception.toString());
            }
        }
    }

    private final class CatalogReservation implements StockReservation {
        private final Map<String, Integer> previousStocks;
        private boolean active = true;

        private CatalogReservation(Map<String, Integer> previousStocks) {
            this.previousStocks = previousStocks;
        }

        @Override
        public void commit() {
            if (!active) { throw new IllegalStateException("Reservation is already closed"); }
            active = false;
            List<ProductEvent> events = previousStocks.entrySet().stream()
                    .map(entry -> stockEvent(getProduct(entry.getKey()), entry.getValue())).toList();
            try {
                events.forEach(ProductCatalogNotifier.this::publish);
            } finally {
                reservationOpen = false;
            }
        }

        @Override
        public void close() {
            if (active) {
                previousStocks.forEach((sku, stock) -> getProduct(sku).updateStock(stock));
                active = false;
                reservationOpen = false;
            }
        }
    }
}
