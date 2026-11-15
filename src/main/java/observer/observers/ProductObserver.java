package observer.observers;

import observer.events.ProductEvent;

@FunctionalInterface
public interface ProductObserver {
    void onProductChanged(ProductEvent event);
}
