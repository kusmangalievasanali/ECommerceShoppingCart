package strategy.strategies;

import builder.product.Money;
import java.math.BigDecimal;

public final class NoDiscountStrategy implements DiscountStrategy {
    @Override
    public BigDecimal applyDiscount(BigDecimal subtotal) { return Money.amount(subtotal); }
}
