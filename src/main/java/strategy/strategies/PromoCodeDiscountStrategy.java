package strategy.strategies;

import java.math.BigDecimal;
import java.util.Locale;
import java.util.Map;

public final class PromoCodeDiscountStrategy implements DiscountStrategy {
    private static final Map<String, BigDecimal> CODES = Map.of(
            "SAVE10", new BigDecimal("10"), "STUDENT15", new BigDecimal("15"));
    private final PercentageDiscountStrategy delegate;

    public PromoCodeDiscountStrategy(String promoCode) {
        String normalized = promoCode == null ? "" : promoCode.trim().toUpperCase(Locale.ROOT);
        BigDecimal percent = CODES.get(normalized);
        if (percent == null) { throw new IllegalArgumentException("Invalid promo code"); }
        delegate = new PercentageDiscountStrategy(percent);
    }

    @Override
    public BigDecimal applyDiscount(BigDecimal subtotal) { return delegate.applyDiscount(subtotal); }
}
