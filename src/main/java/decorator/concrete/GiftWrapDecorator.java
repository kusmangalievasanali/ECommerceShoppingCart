package decorator.concrete;

import builder.product.Product;
import decorator.base.ProductDecorator;
import java.math.BigDecimal;

public final class GiftWrapDecorator extends ProductDecorator {
    private static final BigDecimal FEE = new BigDecimal("1500.00");

    public GiftWrapDecorator(Product product) { super(product); }

    @Override
    public String getDescription() { return wrappedProduct.getDescription() + ", gift wrapped"; }

    @Override
    public BigDecimal getPrice() { return wrappedProduct.getPrice().add(FEE); }
}
