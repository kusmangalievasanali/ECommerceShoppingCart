package builder.builders;

import builder.product.Product;
import java.math.BigDecimal;

public interface ProductBuilder {
    ProductBuilder setSku(String sku);
    ProductBuilder setName(String name);
    ProductBuilder setCpu(String cpu);
    ProductBuilder setGpu(String gpu);
    ProductBuilder setRamGb(int ramGb);
    ProductBuilder setStorageGb(int storageGb);
    ProductBuilder setPrice(BigDecimal price);
    Product build();
}
