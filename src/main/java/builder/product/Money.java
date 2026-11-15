package builder.product;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

public final class Money {
    public static final BigDecimal ZERO = new BigDecimal("0.00");
    private Money() { }

    public static BigDecimal amount(BigDecimal value) {
        Objects.requireNonNull(value, "Amount is required");
        if (value.signum() < 0) {
            throw new IllegalArgumentException("Amount cannot be negative");
        }
        return value.setScale(2, RoundingMode.HALF_UP);
    }

    public static BigDecimal positiveAmount(BigDecimal value) {
        BigDecimal amount = amount(value);
        if (amount.signum() == 0) {
            throw new IllegalArgumentException("Payment amount must be positive");
        }
        return amount;
    }
}
