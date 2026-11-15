package observer.events;

import java.math.BigDecimal;

public record ProductEvent(String sku, EventType type, BigDecimal oldPrice,
                           BigDecimal newPrice, int oldStock, int newStock) { }
