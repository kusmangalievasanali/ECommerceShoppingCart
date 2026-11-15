package factory.products;

import builder.product.AbstractProduct;
import java.math.BigDecimal;

public final class StandardLaptop extends AbstractProduct {
    public StandardLaptop() { super("LAPTOP-001", "Standard Laptop", new BigDecimal("420000")); }

    @Override
    public String getDescription() { return "14-inch laptop for everyday work"; }
}
