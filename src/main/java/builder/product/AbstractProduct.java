package builder.product;

import java.math.BigDecimal;

public abstract class AbstractProduct implements Product {
    private final String sku;
    private final String name;
    private final BigDecimal price;

    protected AbstractProduct(String sku, String name, BigDecimal price) {
        this.sku = requireText(sku, "SKU");
        this.name = requireText(name, "Product name");
        this.price = Money.amount(price);
    }

    @Override
    public final String getSku() { return sku; }

    @Override
    public final String getName() { return name; }

    @Override
    public final BigDecimal getPrice() { return price; }

    protected static String requireText(String value, String label) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(label + " is required");
        }
        return value.trim();
    }
}
