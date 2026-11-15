package factory.products;

import builder.product.AbstractProduct;
import java.math.BigDecimal;

public final class WirelessHeadphones extends AbstractProduct {
    public WirelessHeadphones() { super("HEADPHONES-001", "Wireless Headphones", new BigDecimal("65000")); }

    @Override
    public String getDescription() { return "Bluetooth over-ear headphones"; }
}
