package observer.subject;

import observer.observers.ProductObserver;

public interface ProductSubject {
    void addObserver(ProductObserver observer);
    void removeObserver(ProductObserver observer);
}
