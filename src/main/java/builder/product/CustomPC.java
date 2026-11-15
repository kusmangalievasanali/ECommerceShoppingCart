package builder.product;

import java.math.BigDecimal;


public final class CustomPC extends AbstractProduct {
    private final String cpu;
    private final String gpu;
    private final int ramGb;
    private final int storageGb;

    public CustomPC(String sku, String name, String cpu, String gpu,
                    int ramGb, int storageGb, BigDecimal price) {
        super(sku, name, price);
        this.cpu = requireText(cpu, "CPU");
        this.gpu = requireText(gpu, "GPU");
        if (ramGb <= 0 || storageGb <= 0) {
            throw new IllegalArgumentException("RAM and storage must be positive");
        }
        this.ramGb = ramGb;
        this.storageGb = storageGb;
    }

    public String getCpu() { return cpu; }
    public String getGpu() { return gpu; }
    public int getRamGb() { return ramGb; }
    public int getStorageGb() { return storageGb; }

    @Override
    public String getDescription() {
        return "%s [%s, %s, %d GB RAM, %d GB SSD]"
                .formatted(getName(), cpu, gpu, ramGb, storageGb);
    }
}
