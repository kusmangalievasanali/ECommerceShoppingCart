package adapter.adaptees;

import builder.product.Money;
import java.math.BigDecimal;


public final class StripeSDK {
    public String chargeCard(BigDecimal amount, String currency) {
        if (!"KZT".equals(currency)) { throw new IllegalArgumentException("Only KZT is supported"); }
        return "Stripe charged " + Money.positiveAmount(amount) + " " + currency;
    }
}
