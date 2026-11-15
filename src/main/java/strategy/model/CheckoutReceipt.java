package strategy.model;

import builder.product.Money;
import java.math.BigDecimal;
import java.util.List;

public record CheckoutReceipt(List<OrderLine> lines, BigDecimal subtotal,
                              BigDecimal discount, BigDecimal total, String paymentMessage) {
    public CheckoutReceipt {
        lines = List.copyOf(lines);
        subtotal = Money.amount(subtotal);
        discount = Money.amount(discount);
        total = Money.amount(total);
        if (lines.isEmpty() || paymentMessage == null || paymentMessage.isBlank()) {
            throw new IllegalArgumentException("Receipt requires items and payment confirmation");
        }
        BigDecimal calculated = lines.stream().map(OrderLine::total).reduce(Money.ZERO, BigDecimal::add);
        if (subtotal.compareTo(calculated) != 0 || discount.compareTo(subtotal) > 0
                || total.compareTo(subtotal.subtract(discount)) != 0) {
            throw new IllegalArgumentException("Inconsistent receipt totals");
        }
    }
}
