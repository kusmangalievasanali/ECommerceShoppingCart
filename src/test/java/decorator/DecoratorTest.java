package decorator;

import builder.product.Product;
import decorator.concrete.ExpressDeliveryDecorator;
import decorator.concrete.ExtendedWarrantyDecorator;
import decorator.concrete.GiftWrapDecorator;
import factory.products.StandardLaptop;
import observer.subject.CatalogProduct;
import observer.subject.ProductCatalogNotifier;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class DecoratorTest {
    @Test
    void reportedBasePriceAndDecoratedPriceHaveConsistentSemantics() {
        ProductCatalogNotifier catalog = new ProductCatalogNotifier();
        CatalogProduct listing = catalog.register(new StandardLaptop(), 2);
        Product wrapped = new ExpressDeliveryDecorator(new ExtendedWarrantyDecorator(new GiftWrapDecorator(listing)));
        assertEquals(new BigDecimal("433500.00"), wrapped.getPrice());
        catalog.changePrice(listing.getSku(), new BigDecimal("390000"));
        assertEquals(new BigDecimal("390000.00"), listing.getPrice());
        assertEquals(new BigDecimal("403500.00"), wrapped.getPrice());
        assertEquals(listing.getSku(), wrapped.getSku());
        assertTrue(wrapped.getDescription().contains("express delivery"));
    }

    @Test
    void decoratorOrderDoesNotChangeAdditiveFees() {
        Product base = new StandardLaptop();
        Product one = new GiftWrapDecorator(new ExtendedWarrantyDecorator(base));
        Product two = new ExtendedWarrantyDecorator(new GiftWrapDecorator(base));
        assertEquals(one.getPrice(), two.getPrice());
        assertEquals(new BigDecimal("420000.00"), base.getPrice());
    }
}
