package observer;

import factory.products.StandardLaptop;
import factory.products.WirelessHeadphones;
import observer.events.EventType;
import observer.events.ProductEvent;
import observer.observers.ProductObserver;
import observer.observers.CartSyncObserver;
import observer.subject.CatalogProduct;
import observer.subject.ProductCatalogNotifier;
import decorator.concrete.GiftWrapDecorator;
import strategy.context.ShoppingCart;
import strategy.context.StockReservation;
import strategy.strategies.NoDiscountStrategy;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class CatalogObserverTest {
    private ProductCatalogNotifier catalog;
    private CatalogProduct laptop;

    @BeforeEach
    void setUp() {
        catalog = new ProductCatalogNotifier();
        laptop = catalog.register(new StandardLaptop(), 2);
    }

    @Test
    void decreasesIncreasesAndRestockingPublishCorrectEvents() {
        List<ProductEvent> events = new ArrayList<>();
        catalog.addObserver(events::add);
        catalog.changePrice(laptop.getSku(), new BigDecimal("390000"));
        catalog.changePrice(laptop.getSku(), new BigDecimal("430000"));
        catalog.restock(laptop.getSku(), 3);
        assertEquals(3,events.size());
        assertEquals(new BigDecimal("420000.00"),events.get(0).oldPrice());
        assertEquals(new BigDecimal("390000.00"),events.get(0).newPrice());
        assertEquals(new BigDecimal("430000.00"),events.get(1).newPrice());
        assertEquals(EventType.STOCK_CHANGED,events.get(2).type());
        assertEquals(2,events.get(2).oldStock());
        assertEquals(5,events.get(2).newStock());
    }

    @Test
    void unchangedPriceDuplicateSubscriptionAndUnsubscriptionAreHandled() {
        List<ProductEvent> events = new ArrayList<>();
        ProductObserver observer = events::add;
        catalog.addObserver(observer);
        catalog.addObserver(observer);
        catalog.changePrice(laptop.getSku(),new BigDecimal("420000"));
        assertTrue(events.isEmpty());
        catalog.changePrice(laptop.getSku(),new BigDecimal("400000"));
        assertEquals(1,events.size());
        catalog.removeObserver(observer);
        catalog.restock(laptop.getSku(),1);
        assertEquals(1,events.size());
    }

    @Test
    void failingObserverDoesNotBlockOthersOrUndoState() {
        List<ProductEvent> events = new ArrayList<>();
        catalog.addObserver(event -> { throw new IllegalStateException("Observer failed"); });
        catalog.addObserver(events::add);
        assertDoesNotThrow(() -> catalog.changePrice(laptop.getSku(),new BigDecimal("400000")));
        assertEquals(1,events.size());
        assertEquals(1,catalog.getNotificationErrors().size());
        assertEquals(new BigDecimal("400000.00"),laptop.getPrice());
    }

    @Test
    void cartSyncReadsActualDecoratedCart() {
        ShoppingCart cart = new ShoppingCart(new NoDiscountStrategy(),catalog);
        cart.addProduct(new GiftWrapDecorator(laptop));
        List<String> output = new ArrayList<>();
        catalog.addObserver(new CartSyncObserver(cart,output::add));
        catalog.changePrice(laptop.getSku(),new BigDecimal("390000"));
        assertEquals(new BigDecimal("391500.00"),cart.getSubtotal());
        assertTrue(output.get(0).contains("391500.00"));
    }

    @Test
    void reservationRollbackDoesNotPublishACompletedStockChange() {
        List<ProductEvent> events = new ArrayList<>();
        catalog.addObserver(events::add);
        try (StockReservation reservation = catalog.reserve(Map.of(laptop.getSku(),1))) {
            assertEquals(1,laptop.getStock());
            assertTrue(events.isEmpty());
        }
        assertEquals(2,laptop.getStock());
        assertTrue(events.isEmpty());
    }

    @Test
    void committingReservationPublishesExactlyOnceAndCannotCommitTwice() {
        List<ProductEvent> events = new ArrayList<>();
        catalog.addObserver(events::add);
        try (StockReservation reservation = catalog.reserve(Map.of(laptop.getSku(),1))) {
            reservation.commit();
            assertThrows(IllegalStateException.class,reservation::commit);
        }
        assertEquals(1,laptop.getStock());
        assertEquals(1,events.size());
        assertEquals(2,events.get(0).oldStock());
        assertEquals(1,events.get(0).newStock());
    }

    @Test
    void multiProductReservationValidatesAllBeforeMutatingAny() {
        CatalogProduct empty = catalog.register(new WirelessHeadphones(),0);
        assertThrows(IllegalStateException.class,
                () -> catalog.reserve(Map.of(laptop.getSku(),1,empty.getSku(),1)));
        assertEquals(2,laptop.getStock());
        assertEquals(0,empty.getStock());
    }

    @Test
    void invalidMutationsAreRejectedWithoutChangingState() {
        assertThrows(IllegalArgumentException.class,() -> catalog.restock(laptop.getSku(),0));
        assertThrows(IllegalArgumentException.class,() -> catalog.changePrice(laptop.getSku(),new BigDecimal("-1")));
        assertThrows(IllegalArgumentException.class,() -> catalog.register(new StandardLaptop(),1));
        assertThrows(IllegalArgumentException.class,() -> catalog.reserve(Map.of("UNKNOWN",1)));
        assertThrows(IllegalArgumentException.class,() -> catalog.reserve(Map.of(laptop.getSku(),0)));
        assertEquals(2,laptop.getStock());
        assertEquals(new BigDecimal("420000.00"),laptop.getPrice());
    }

    @Test
    void overlappingReservationsAndReentrantMutationCannotCorruptRollback() {
        try (StockReservation reservation = catalog.reserve(Map.of(laptop.getSku(),1))) {
            assertThrows(IllegalStateException.class,() -> catalog.reserve(Map.of(laptop.getSku(),1)));
            assertThrows(IllegalStateException.class,() -> catalog.restock(laptop.getSku(),1));
            assertThrows(IllegalStateException.class,() -> catalog.changePrice(laptop.getSku(),BigDecimal.ONE));
        }
        assertEquals(2,laptop.getStock());
        assertDoesNotThrow(() -> catalog.restock(laptop.getSku(),1));
        assertEquals(3,laptop.getStock());
    }

    @Test
    void stockOverflowDoesNotChangeStoredStock() {
        ProductCatalogNotifier full = new ProductCatalogNotifier();
        CatalogProduct product = full.register(new StandardLaptop(),Integer.MAX_VALUE);
        assertThrows(ArithmeticException.class,() -> full.restock(product.getSku(),1));
        assertEquals(Integer.MAX_VALUE,product.getStock());
    }
}
