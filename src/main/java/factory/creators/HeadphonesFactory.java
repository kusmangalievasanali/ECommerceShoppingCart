package factory.creators;

import builder.product.Product;
import factory.products.WirelessHeadphones;

public final class HeadphonesFactory extends ProductFactory {
    @Override
    public Product createProduct() { return new WirelessHeadphones(); }
}
