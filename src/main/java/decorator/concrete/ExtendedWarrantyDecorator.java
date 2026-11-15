package decorator.concrete;

import builder.product.Product;
import decorator.base.ProductDecorator;
import java.math.BigDecimal;

public final class ExtendedWarrantyDecorator extends ProductDecorator {
    private static final BigDecimal FEE = new BigDecimal("9000.00");

    public ExtendedWarrantyDecorator(Product product) { super(product); }

    @Override
    public String getDescription() { return wrappedProduct.getDescription() + ", extended warranty"; }

    @Override
    public BigDecimal getPrice() { return wrappedProduct.getPrice().add(FEE); }
}
