package decorator.base;

import builder.product.Product;
import java.util.Objects;

public abstract class ProductDecorator implements Product {
    protected final Product wrappedProduct;

    protected ProductDecorator(Product product) {
        wrappedProduct = Objects.requireNonNull(product, "Wrapped product is required");
    }

    @Override
    public String getSku() { return wrappedProduct.getSku(); }

    @Override
    public String getName() { return wrappedProduct.getName(); }
}
