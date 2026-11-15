package builder.product;

import java.math.BigDecimal;

public interface Product {
    String getSku();
    String getName();
    String getDescription();
    BigDecimal getPrice();
}
