package strategy.context;

import java.util.Map;

public interface InventoryGateway {
    StockReservation reserve(Map<String, Integer> quantities);
}
