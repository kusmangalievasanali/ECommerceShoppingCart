package builder.builders;

import builder.product.CustomPC;
import java.math.BigDecimal;

public final class CustomPCBuilder implements ProductBuilder {
    private String sku;
    private String name;
    private String cpu;
    private String gpu;
    private int ramGb;
    private int storageGb;
    private BigDecimal price;

    @Override
    public CustomPCBuilder setSku(String sku) {
        this.sku = sku;
        return this;
    }

    @Override
    public CustomPCBuilder setName(String name) {
        this.name = name;
        return this;
    }

    @Override
    public CustomPCBuilder setCpu(String cpu) {
        this.cpu = cpu;
        return this;
    }

    @Override
    public CustomPCBuilder setGpu(String gpu) {
        this.gpu = gpu;
        return this;
    }

    @Override
    public CustomPCBuilder setRamGb(int ramGb) {
        this.ramGb = ramGb;
        return this;
    }

    @Override
    public CustomPCBuilder setStorageGb(int storageGb) {
        this.storageGb = storageGb;
        return this;
    }

    @Override
    public CustomPCBuilder setPrice(BigDecimal price) {
        this.price = price;
        return this;
    }

    @Override
    public CustomPC build() {
        return new CustomPC(sku, name, cpu, gpu, ramGb, storageGb, price);
    }
}
