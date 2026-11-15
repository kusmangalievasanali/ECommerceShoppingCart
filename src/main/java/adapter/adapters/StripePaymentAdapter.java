package adapter.adapters;

import adapter.adaptees.StripeSDK;
import adapter.target.PaymentProcessor;
import java.math.BigDecimal;
import java.util.Objects;

public final class StripePaymentAdapter implements PaymentProcessor {
    private final StripeSDK sdk;

    public StripePaymentAdapter(StripeSDK sdk) { this.sdk = Objects.requireNonNull(sdk); }

    @Override
    public String pay(BigDecimal amount) { return sdk.chargeCard(amount, "KZT"); }
}
