package strategy.context;

import adapter.target.PaymentProcessor;
import builder.product.Money;
import builder.product.Product;
import strategy.model.CartItem;
import strategy.model.CheckoutReceipt;
import strategy.model.OrderLine;
import strategy.strategies.DiscountStrategy;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class ShoppingCart {
    private final List<CartItem> items = new ArrayList<>();
    private final InventoryGateway inventory;
    private DiscountStrategy discountStrategy;

    public ShoppingCart(DiscountStrategy strategy, InventoryGateway inventory) {
        this.inventory = Objects.requireNonNull(inventory, "Inventory is required");
        setDiscountStrategy(strategy);
    }

    public void setDiscountStrategy(DiscountStrategy strategy) {
        discountStrategy = Objects.requireNonNull(strategy, "Discount strategy is required");
    }

    public void addProduct(Product product) { addProduct(product, 1); }

    public void addProduct(Product product, int quantity) { items.add(new CartItem(product, quantity)); }

    public void removeItem(int index) { items.remove(index); }

    public List<CartItem> getItems() { return List.copyOf(items); }

    public boolean isEmpty() { return items.isEmpty(); }

    public BigDecimal getSubtotal() {
        return items.stream().map(CartItem::total).reduce(Money.ZERO, BigDecimal::add);
    }

    public BigDecimal getTotal() { return calculateTotal(getSubtotal()); }

    public CheckoutReceipt checkout(PaymentProcessor paymentProcessor) {
        Objects.requireNonNull(paymentProcessor, "Payment processor is required");
        if (items.isEmpty()) { throw new IllegalStateException("Cannot checkout an empty cart"); }

        List<OrderLine> lines = items.stream().map(item -> new OrderLine(
                item.product().getSku(), item.product().getDescription(),
                item.quantity(), item.product().getPrice())).toList();
        BigDecimal subtotal = lines.stream().map(OrderLine::total).reduce(Money.ZERO, BigDecimal::add);
        BigDecimal total = calculateTotal(subtotal);
        Map<String, Integer> quantities = new LinkedHashMap<>();
        for (OrderLine line : lines) {
            quantities.merge(line.sku(), line.quantity(), Math::addExact);
        }

        try (StockReservation reservation = inventory.reserve(quantities)) {
            String paymentMessage = total.signum() == 0
                    ? "No payment required: zero-total order" : paymentProcessor.pay(total);
            CheckoutReceipt receipt = new CheckoutReceipt(
                    lines, subtotal, subtotal.subtract(total), total, paymentMessage);
            reservation.commit();
            items.clear();
            return receipt;
        }
    }

    private BigDecimal calculateTotal(BigDecimal subtotal) {
        BigDecimal total = Money.amount(discountStrategy.applyDiscount(subtotal));
        if (total.compareTo(subtotal) > 0) {
            throw new IllegalArgumentException("Discount strategy cannot increase the subtotal");
        }
        return total;
    }
}
