package observer.observers;

import observer.events.ProductEvent;
import strategy.context.ShoppingCart;
import java.util.Objects;
import java.util.function.Consumer;

public final class CartSyncObserver implements ProductObserver {
    private final ShoppingCart cart;
    private final Consumer<String> output;

    public CartSyncObserver(ShoppingCart cart, Consumer<String> output) {
        this.cart = Objects.requireNonNull(cart);
        this.output = Objects.requireNonNull(output);
    }

    @Override
    public void onProductChanged(ProductEvent event) {
        boolean relevant = cart.getItems().stream()
                .anyMatch(item -> item.product().getSku().equals(event.sku()));
        if (relevant) {
            output.accept("CART SYNC: " + event.type() + " | actual subtotal KZT "
                    + cart.getSubtotal() + " | total KZT " + cart.getTotal());
        }
    }
}
