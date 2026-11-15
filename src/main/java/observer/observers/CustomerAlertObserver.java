package observer.observers;

import observer.events.EventType;
import observer.events.ProductEvent;
import java.util.Objects;
import java.util.function.Consumer;

public final class CustomerAlertObserver implements ProductObserver {
    private final Consumer<String> output;

    public CustomerAlertObserver(Consumer<String> output) { this.output = Objects.requireNonNull(output); }

    @Override
    public void onProductChanged(ProductEvent event) {
        String message = event.type() == EventType.PRICE_CHANGED
                ? "base price KZT " + event.oldPrice() + " -> " + event.newPrice()
                : "stock " + event.oldStock() + " -> " + event.newStock();
        output.accept("CUSTOMER ALERT: " + event.sku() + " | " + message);
    }
}
