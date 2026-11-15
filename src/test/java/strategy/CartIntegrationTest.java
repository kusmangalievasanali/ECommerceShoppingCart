package strategy;

import adapter.adaptees.KaspiPayAPI;
import adapter.adapters.KaspiPaymentAdapter;
import builder.builders.CustomPCBuilder;
import builder.director.PCDirector;
import builder.product.Product;
import decorator.concrete.ExpressDeliveryDecorator;
import decorator.concrete.ExtendedWarrantyDecorator;
import decorator.concrete.GiftWrapDecorator;
import factory.creators.LaptopFactory;
import factory.creators.HeadphonesFactory;
import observer.events.ProductEvent;
import observer.subject.CatalogProduct;
import observer.subject.ProductCatalogNotifier;
import strategy.context.ShoppingCart;
import strategy.model.CheckoutReceipt;
import strategy.strategies.NoDiscountStrategy;
import strategy.strategies.PercentageDiscountStrategy;
import strategy.strategies.PromoCodeDiscountStrategy;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import static org.junit.jupiter.api.Assertions.*;

class CartIntegrationTest {
    private Product decorate(Product product) {
        return new ExpressDeliveryDecorator(new ExtendedWarrantyDecorator(new GiftWrapDecorator(product)));
    }

    @Test
    void allSixPatternsProduceCorrectReceiptStockAndImmutablePriceSnapshot() {
        ProductCatalogNotifier catalog = new ProductCatalogNotifier();
        Product pc = new PCDirector().buildGamingPC(new CustomPCBuilder());
        CatalogProduct pcListing = catalog.register(pc,3);
        CatalogProduct laptop = catalog.register(new LaptopFactory().createProduct(),2);
        CatalogProduct headphones = catalog.register(new HeadphonesFactory().createProduct(),4);
        List<ProductEvent> events = new ArrayList<>();
        catalog.addObserver(events::add);
        ShoppingCart cart = new ShoppingCart(new PromoCodeDiscountStrategy("STUDENT15"),catalog);
        cart.addProduct(decorate(pcListing));
        cart.addProduct(decorate(laptop));
        cart.addProduct(decorate(headphones));
        catalog.changePrice(laptop.getSku(),new BigDecimal("390000"));
        CheckoutReceipt receipt = cart.checkout(new KaspiPaymentAdapter(new KaspiPayAPI()));
        assertEquals(new BigDecimal("1345500.00"),receipt.subtotal());
        assertEquals(new BigDecimal("201825.00"),receipt.discount());
        assertEquals(new BigDecimal("1143675.00"),receipt.total());
        assertEquals(4,events.size());
        assertEquals(2,pcListing.getStock());
        assertEquals(1,laptop.getStock());
        assertEquals(3,headphones.getStock());
        assertTrue(cart.isEmpty());
        catalog.changePrice(laptop.getSku(),new BigDecimal("100000"));
        assertEquals(new BigDecimal("403500.00"),receipt.lines().get(1).unitPrice());
        assertEquals(new BigDecimal("1143675.00"),receipt.total());
        assertThrows(UnsupportedOperationException.class,() -> receipt.lines().clear());
    }

    @Test
    void providerFailureRestoresStockAndCartAndDoesNotEmitCommittedStockEvent() {
        ProductCatalogNotifier catalog = new ProductCatalogNotifier();
        CatalogProduct product = catalog.register(new LaptopFactory().createProduct(),2);
        List<ProductEvent> events = new ArrayList<>();
        catalog.addObserver(events::add);
        ShoppingCart cart = new ShoppingCart(new NoDiscountStrategy(),catalog);
        cart.addProduct(product);
        assertThrows(IllegalStateException.class,
                () -> cart.checkout(amount -> { throw new IllegalStateException("Provider failed"); }));
        assertEquals(2,product.getStock());
        assertFalse(cart.isEmpty());
        assertTrue(events.isEmpty());
    }

    @Test
    void zeroTotalOrderConsumesStockWithoutCallingPaymentProvider() {
        ProductCatalogNotifier catalog = new ProductCatalogNotifier();
        CatalogProduct product = catalog.register(new LaptopFactory().createProduct(),2);
        ShoppingCart cart = new ShoppingCart(new PercentageDiscountStrategy(new BigDecimal("100")),catalog);
        cart.addProduct(product);
        AtomicInteger calls = new AtomicInteger();
        CheckoutReceipt receipt = cart.checkout(amount -> { calls.incrementAndGet(); return "charged"; });
        assertEquals(new BigDecimal("0.00"),receipt.total());
        assertEquals(0,calls.get());
        assertEquals(1,product.getStock());
        assertTrue(cart.isEmpty());
    }

    @Test
    void repeatedSkuAcrossDecorationsCannotOversellAndNeverCallsProvider() {
        ProductCatalogNotifier catalog = new ProductCatalogNotifier();
        CatalogProduct product = catalog.register(new LaptopFactory().createProduct(),2);
        ShoppingCart cart = new ShoppingCart(new NoDiscountStrategy(),catalog);
        cart.addProduct(product,2);
        cart.addProduct(new GiftWrapDecorator(product),1);
        AtomicInteger calls = new AtomicInteger();
        assertThrows(IllegalStateException.class,
                () -> cart.checkout(amount -> { calls.incrementAndGet(); return "charged"; }));
        assertEquals(0,calls.get());
        assertEquals(2,product.getStock());
        assertEquals(2,cart.getItems().size());
    }

    @Test
    void decimalMoneyDoesNotHaveBinaryFloatingPointRoundingErrors() {
        ProductCatalogNotifier catalog = new ProductCatalogNotifier();
        Product first = new CustomPCBuilder().setSku("A").setName("A").setCpu("C").setGpu("G")
                .setRamGb(1).setStorageGb(1).setPrice(new BigDecimal("0.10")).build();
        Product second = new CustomPCBuilder().setSku("B").setName("B").setCpu("C").setGpu("G")
                .setRamGb(1).setStorageGb(1).setPrice(new BigDecimal("0.20")).build();
        ShoppingCart cart = new ShoppingCart(new NoDiscountStrategy(),catalog);
        cart.addProduct(catalog.register(first,1));
        cart.addProduct(catalog.register(second,1));
        assertEquals(new BigDecimal("0.30"),cart.getTotal());
    }

    @ParameterizedTest
    @CsvSource({"100, 5, 95.00", "0.05, 10, 0.04", "100, 100, 0.00", "100, 0, 100.00"})
    void percentageStrategyHasExplicitCentRounding(String subtotal,String percent,String expected) {
        assertEquals(new BigDecimal(expected),new PercentageDiscountStrategy(new BigDecimal(percent))
                .applyDiscount(new BigDecimal(subtotal)));
    }

    @Test
    void promoNormalizationAndInvalidPromoOrPercentAreHandled() {
        assertEquals(new BigDecimal("85.00"),new PromoCodeDiscountStrategy(" student15 ")
                .applyDiscount(new BigDecimal("100")));
        assertThrows(IllegalArgumentException.class,() -> new PromoCodeDiscountStrategy("BAD"));
        assertThrows(IllegalArgumentException.class,() -> new PercentageDiscountStrategy(new BigDecimal("101")));
        assertThrows(IllegalArgumentException.class,() -> new PercentageDiscountStrategy(new BigDecimal("-1")));
    }

    @Test
    void emptyCartAndInvalidQuantityAreRejected() {
        ProductCatalogNotifier catalog = new ProductCatalogNotifier();
        ShoppingCart cart = new ShoppingCart(new NoDiscountStrategy(),catalog);
        assertThrows(IllegalStateException.class,() -> cart.checkout(amount -> "receipt"));
        assertThrows(IllegalArgumentException.class,
                () -> cart.addProduct(new LaptopFactory().createProduct(),0));
    }

    @Test
    void invalidCustomStrategyIsRejectedBeforeAnyStockOrPaymentSideEffect() {
        ProductCatalogNotifier catalog = new ProductCatalogNotifier();
        CatalogProduct product = catalog.register(new LaptopFactory().createProduct(),2);
        ShoppingCart cart = new ShoppingCart(subtotal -> subtotal.add(BigDecimal.ONE),catalog);
        cart.addProduct(product);
        assertThrows(IllegalArgumentException.class,() -> cart.checkout(amount -> "receipt"));
        assertEquals(2,product.getStock());
        assertFalse(cart.isEmpty());
    }

    @Test
    void blankPaymentConfirmationTriggersReservationRollback() {
        ProductCatalogNotifier catalog = new ProductCatalogNotifier();
        CatalogProduct product = catalog.register(new LaptopFactory().createProduct(),2);
        ShoppingCart cart = new ShoppingCart(new NoDiscountStrategy(),catalog);
        cart.addProduct(product);
        assertThrows(IllegalArgumentException.class,() -> cart.checkout(amount -> " "));
        assertEquals(2,product.getStock());
        assertFalse(cart.isEmpty());
    }

    @Test
    void quantityOverflowIsRejectedBeforeReservingStock() {
        ProductCatalogNotifier catalog = new ProductCatalogNotifier();
        CatalogProduct product = catalog.register(new LaptopFactory().createProduct(),2);
        ShoppingCart cart = new ShoppingCart(new NoDiscountStrategy(),catalog);
        cart.addProduct(product,Integer.MAX_VALUE);
        cart.addProduct(product,1);
        assertThrows(ArithmeticException.class,() -> cart.checkout(amount -> "receipt"));
        assertEquals(2,product.getStock());
    }
}
