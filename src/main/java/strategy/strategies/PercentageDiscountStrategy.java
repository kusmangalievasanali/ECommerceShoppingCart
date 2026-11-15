package strategy.strategies;

import builder.product.Money;
import java.math.BigDecimal;
import java.util.Objects;

public final class PercentageDiscountStrategy implements DiscountStrategy {
    private final BigDecimal percentage;

    public PercentageDiscountStrategy(BigDecimal percentage) {
        Objects.requireNonNull(percentage, "Percentage is required");
        if (percentage.signum() < 0 || percentage.compareTo(new BigDecimal("100")) > 0) {
            throw new IllegalArgumentException("Percentage must be between 0 and 100");
        }
        this.percentage = percentage;
    }

    @Override
    public BigDecimal applyDiscount(BigDecimal subtotal) {
        BigDecimal validSubtotal = Money.amount(subtotal);
        BigDecimal discount = Money.amount(validSubtotal.multiply(percentage).movePointLeft(2));
        return validSubtotal.subtract(discount);
    }
}
