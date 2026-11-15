package adapter;

import adapter.adaptees.KaspiPayAPI;
import adapter.adaptees.StripeSDK;
import adapter.adapters.KaspiPaymentAdapter;
import adapter.adapters.StripePaymentAdapter;
import adapter.target.PaymentProcessor;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class PaymentAdapterTest {
    @Test
    void bothAdaptersTranslateTargetCallToTheirOwnProvider() {
        PaymentProcessor kaspi = new KaspiPaymentAdapter(new KaspiPayAPI());
        PaymentProcessor stripe = new StripePaymentAdapter(new StripeSDK());
        assertEquals("Kaspi payment completed: 100.25 KZT", kaspi.pay(new BigDecimal("100.25")));
        assertEquals("Stripe charged 100.25 KZT", stripe.pay(new BigDecimal("100.25")));
    }

    @Test
    void providerRejectsZeroNegativeAndUnsupportedCurrency() {
        PaymentProcessor kaspi = new KaspiPaymentAdapter(new KaspiPayAPI());
        PaymentProcessor stripe = new StripePaymentAdapter(new StripeSDK());
        assertThrows(IllegalArgumentException.class, () -> kaspi.pay(BigDecimal.ZERO));
        assertThrows(IllegalArgumentException.class, () -> stripe.pay(new BigDecimal("-1")));
        assertThrows(IllegalArgumentException.class,
                () -> new StripeSDK().chargeCard(BigDecimal.ONE,"USD"));
    }
}
