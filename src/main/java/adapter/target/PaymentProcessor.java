package adapter.target;

import java.math.BigDecimal;


public interface PaymentProcessor {
    String pay(BigDecimal amount);
}
