import adapter.adaptees.KaspiPayAPI;
import adapter.adaptees.StripeSDK;
import adapter.adapters.KaspiPaymentAdapter;
import adapter.adapters.StripePaymentAdapter;
import builder.builders.CustomPCBuilder;
import builder.director.PCDirector;
import builder.product.Product;
import decorator.concrete.ExpressDeliveryDecorator;
import decorator.concrete.ExtendedWarrantyDecorator;
import decorator.concrete.GiftWrapDecorator;
import factory.creators.HeadphonesFactory;
import factory.creators.LaptopFactory;
import factory.creators.ProductFactory;
import observer.observers.CartSyncObserver;
import observer.observers.CustomerAlertObserver;
import observer.subject.CatalogProduct;
import observer.subject.ProductCatalogNotifier;
import strategy.context.ShoppingCart;
import strategy.model.CheckoutReceipt;
import strategy.strategies.NoDiscountStrategy;
import strategy.strategies.PercentageDiscountStrategy;
import strategy.strategies.PromoCodeDiscountStrategy;
import java.math.BigDecimal;

public final class Main {
    private Main() { }

    public static void main(String[] args) {
        System.out.println("E-COMMERCE SHOPPING CART | Java 17 | KZT");
        System.out.println("\n[1] BUILDER: real PC configuration and two director recipes");
        PCDirector director = new PCDirector();
        Product pc = director.buildGamingPC(new CustomPCBuilder());
        Product workstation = director.buildWorkstation(new CustomPCBuilder());
        System.out.println(pc.getDescription());
        System.out.println(workstation.getDescription());

        System.out.println("\n[2] FACTORY METHOD: two creator subclasses");
        Product laptop = create(new LaptopFactory());
        Product headphones = create(new HeadphonesFactory());
        System.out.println(laptop.getName() + ": " + laptop.getPrice());
        System.out.println(headphones.getName() + ": " + headphones.getPrice());

        ProductCatalogNotifier catalog = new ProductCatalogNotifier();
        CatalogProduct pcListing = catalog.register(pc, 3);
        CatalogProduct laptopListing = catalog.register(laptop, 2);
        CatalogProduct headphonesListing = catalog.register(headphones, 4);
        ShoppingCart cart = new ShoppingCart(new NoDiscountStrategy(), catalog);

        System.out.println("\n[3] DECORATOR: wrapping, warranty and delivery per unit");
        cart.addProduct(addServices(pcListing));
        cart.addProduct(addServices(laptopListing));
        cart.addProduct(addServices(headphonesListing));
        System.out.println("Services per item: 1500.00 + 9000.00 + 3000.00 = 13500.00");
        System.out.println("Initial subtotal: " + cart.getSubtotal());

        System.out.println("\n[4] OBSERVER: actual cart exists before subscribing");
        CustomerAlertObserver customer = new CustomerAlertObserver(System.out::println);
        CartSyncObserver cartSync = new CartSyncObserver(cart, System.out::println);
        catalog.addObserver(customer);
        catalog.addObserver(cartSync);
        catalog.changePrice(laptop.getSku(), new BigDecimal("390000"));
        catalog.changePrice(laptop.getSku(), new BigDecimal("395000"));
        catalog.changePrice(laptop.getSku(), new BigDecimal("390000"));
        catalog.restock(headphones.getSku(), 1);

        System.out.println("\n[5] STRATEGY: no discount, percentage and promo code");
        System.out.println("No discount: " + cart.getTotal());
        cart.setDiscountStrategy(new PercentageDiscountStrategy(new BigDecimal("5")));
        System.out.println("With 5% discount: " + cart.getTotal());
        cart.setDiscountStrategy(new PromoCodeDiscountStrategy("STUDENT15"));
        System.out.println("With STUDENT15: " + cart.getTotal());

        System.out.println("\n[6] ADAPTER: Kaspi checkout, committed stock and cart clearing");
        CheckoutReceipt receipt = cart.checkout(new KaspiPaymentAdapter(new KaspiPayAPI()));
        printReceipt(receipt);
        System.out.println("Remaining stock PC/laptop/headphones: " + pcListing.getStock()
                + "/" + laptopListing.getStock() + "/" + headphonesListing.getStock());
        System.out.println("Cart empty: " + cart.isEmpty());

        System.out.println("\n[ADAPTER] Second provider: Stripe");
        ShoppingCart second = new ShoppingCart(new NoDiscountStrategy(), catalog);
        second.addProduct(addServices(headphonesListing));
        System.out.println(second.checkout(new StripePaymentAdapter(new StripeSDK())).paymentMessage());

        System.out.println("\n[VALIDATION] Insufficient stock preserves cart and avoids payment");
        ShoppingCart unavailable = new ShoppingCart(new NoDiscountStrategy(), catalog);
        unavailable.addProduct(laptopListing, 99);
        try {
            unavailable.checkout(amount -> { throw new AssertionError("Payment must not be called"); });
            throw new AssertionError("Unavailable order must be rejected");
        } catch (IllegalStateException expected) {
            System.out.println("Rejected: " + expected.getMessage());
            System.out.println("Cart retained: " + !unavailable.isEmpty());
        }

        System.out.println("\n[VALIDATION] Failed payment rolls back reserved stock");
        ShoppingCart failed = new ShoppingCart(new NoDiscountStrategy(), catalog);
        failed.addProduct(laptopListing);
        try {
            failed.checkout(amount -> { throw new IllegalStateException("Simulated provider failure"); });
            throw new AssertionError("Provider failure must propagate");
        } catch (IllegalStateException expected) {
            System.out.println(expected.getMessage() + " | laptop stock: " + laptopListing.getStock());
            System.out.println("Cart retained: " + !failed.isEmpty());
        }

        System.out.println("\n[VALIDATION] 100% discount: order succeeds without a bank charge");
        ShoppingCart free = new ShoppingCart(new PercentageDiscountStrategy(new BigDecimal("100")), catalog);
        free.addProduct(headphonesListing);
        CheckoutReceipt freeReceipt = free.checkout(amount -> {
            throw new AssertionError("Zero-total order must not call a payment provider");
        });
        System.out.println(freeReceipt.paymentMessage() + " | total: " + freeReceipt.total());
        catalog.removeObserver(customer);
        catalog.removeObserver(cartSync);
        System.out.println("\nALL SIX PATTERNS DEMONSTRATED SUCCESSFULLY.");
    }

    private static Product create(ProductFactory factory) { return factory.createProduct(); }

    private static Product addServices(Product product) {
        return new ExpressDeliveryDecorator(new ExtendedWarrantyDecorator(new GiftWrapDecorator(product)));
    }

    private static void printReceipt(CheckoutReceipt receipt) {
        System.out.println("--- IMMUTABLE RECEIPT ---");
        receipt.lines().forEach(line -> System.out.println(line.sku() + " x" + line.quantity()
                + " @ " + line.unitPrice() + " = " + line.total()));
        System.out.println("Subtotal: " + receipt.subtotal());
        System.out.println("Discount: " + receipt.discount());
        System.out.println("TOTAL KZT: " + receipt.total());
        System.out.println(receipt.paymentMessage());
    }
}
