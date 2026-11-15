package decorator.concrete;

import builder.product.Product;
import decorator.base.ProductDecorator;
import java.math.BigDecimal;

public final class ExpressDeliveryDecorator extends ProductDecorator {
    private static final BigDecimal FEE = new BigDecimal("3000.00");

    public ExpressDeliveryDecorator(Product product) { super(product); }

    @Override
    public String getDescription() { return wrappedProduct.getDescription() + ", express delivery"; }

    @Override
    public BigDecimal getPrice() { return wrappedProduct.getPrice().add(FEE); }
}
