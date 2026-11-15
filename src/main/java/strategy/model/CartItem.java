package strategy.model;

import builder.product.Money;
import builder.product.Product;
import java.math.BigDecimal;
import java.util.Objects;

public record CartItem(Product product, int quantity) {
    public CartItem {
        Objects.requireNonNull(product, "Product is required");
        if (quantity <= 0) { throw new IllegalArgumentException("Quantity must be positive"); }
    }

    public BigDecimal total() {
        return Money.amount(product.getPrice()).multiply(BigDecimal.valueOf(quantity));
    }
}
