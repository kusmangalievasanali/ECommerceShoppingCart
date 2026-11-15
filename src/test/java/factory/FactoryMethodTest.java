package factory;

import builder.product.Product;
import factory.creators.ProductFactory;
import factory.creators.LaptopFactory;
import factory.creators.HeadphonesFactory;
import factory.products.StandardLaptop;
import factory.products.WirelessHeadphones;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class FactoryMethodTest {
    private Product create(ProductFactory factory) { return factory.createProduct(); }

    @Test
    void abstractCreatorSelectsBothConcreteProducts() {
        assertInstanceOf(StandardLaptop.class, create(new LaptopFactory()));
        assertInstanceOf(WirelessHeadphones.class, create(new HeadphonesFactory()));
    }

    @Test
    void separateFactoryCallsCreateIndependentObjectsWithStableSku() {
        Product first = create(new LaptopFactory());
        Product second = create(new LaptopFactory());
        assertNotSame(first, second);
        assertEquals(first.getSku(), second.getSku());
    }
}
