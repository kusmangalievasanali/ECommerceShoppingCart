package adapter.adapters;

import adapter.adaptees.KaspiPayAPI;
import adapter.target.PaymentProcessor;
import java.math.BigDecimal;
import java.util.Objects;

public final class KaspiPaymentAdapter implements PaymentProcessor {
    private final KaspiPayAPI api;

    public KaspiPaymentAdapter(KaspiPayAPI api) { this.api = Objects.requireNonNull(api); }

    @Override
    public String pay(BigDecimal amount) { return api.makePayment(amount); }
}
