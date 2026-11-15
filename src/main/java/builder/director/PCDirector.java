package builder.director;

import builder.builders.ProductBuilder;
import builder.product.Product;
import java.math.BigDecimal;

public final class PCDirector {
    public Product buildGamingPC(ProductBuilder builder) {
        return builder.setSku("PC-GAMING").setName("Custom Gaming PC")
                .setCpu("Intel Core i7").setGpu("GeForce RTX 4070")
                .setRamGb(32).setStorageGb(1024)
                .setPrice(new BigDecimal("850000")).build();
    }

    public Product buildWorkstation(ProductBuilder builder) {
        return builder.setSku("PC-WORK").setName("Custom Workstation")
                .setCpu("AMD Ryzen 7").setGpu("Integrated graphics")
                .setRamGb(64).setStorageGb(2048)
                .setPrice(new BigDecimal("720000")).build();
    }
}
