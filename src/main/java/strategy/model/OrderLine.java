package strategy.model;

import builder.product.Money;
import java.math.BigDecimal;

public record OrderLine(String sku, String description, int quantity, BigDecimal unitPrice) {
    public OrderLine {
        if (sku == null || sku.isBlank() || description == null || description.isBlank()) {
            throw new IllegalArgumentException("Order line requires SKU and description");
        }
        if (quantity <= 0) { throw new IllegalArgumentException("Quantity must be positive"); }
        unitPrice = Money.amount(unitPrice);
    }

    public BigDecimal total() { return unitPrice.multiply(BigDecimal.valueOf(quantity)); }
}
