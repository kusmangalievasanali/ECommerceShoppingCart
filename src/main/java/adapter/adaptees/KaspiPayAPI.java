package adapter.adaptees;

import builder.product.Money;
import java.math.BigDecimal;


public final class KaspiPayAPI {
    public String makePayment(BigDecimal amount) {
        return "Kaspi payment completed: " + Money.positiveAmount(amount) + " KZT";
    }
}
