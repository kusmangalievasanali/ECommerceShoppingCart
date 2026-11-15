package strategy.context;

public interface StockReservation extends AutoCloseable {
    void commit();
    @Override
    void close();
}
