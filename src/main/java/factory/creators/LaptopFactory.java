package factory.creators;

import builder.product.Product;
import factory.products.StandardLaptop;

public final class LaptopFactory extends ProductFactory {
    @Override
    public Product createProduct() { return new StandardLaptop(); }
}
