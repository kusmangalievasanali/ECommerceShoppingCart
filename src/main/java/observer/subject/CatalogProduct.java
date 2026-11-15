package observer.subject;

import builder.product.Product;
import java.math.BigDecimal;

public final class CatalogProduct implements Product {
    private final Product configuration;
    private BigDecimal price;
    private int stock;

    CatalogProduct(Product configuration, int stock) {
        this.configuration = configuration;
        this.price = configuration.getPrice();
        this.stock = stock;
    }

    @Override
    public String getSku() { return configuration.getSku(); }

    @Override
    public String getName() { return configuration.getName(); }

    @Override
    public String getDescription() { return configuration.getDescription(); }

    @Override
    public BigDecimal getPrice() { return price; }

    public int getStock() { return stock; }

    void updatePrice(BigDecimal price) { this.price = price; }
    void updateStock(int stock) { this.stock = stock; }
}
