package builder;

import builder.builders.CustomPCBuilder;
import builder.director.PCDirector;
import builder.product.CustomPC;
import builder.product.Product;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class BuilderTest {
    private CustomPCBuilder builder() {
        return new CustomPCBuilder().setSku("PC").setName("Custom PC")
                .setCpu("CPU").setGpu("GPU").setRamGb(16).setStorageGb(512)
                .setPrice(new BigDecimal("1000"));
    }

    @Test
    void realComponentsAreConfigurableAndBuiltProductsAreIndependent() {
        CustomPCBuilder builder = builder();
        CustomPC first = builder.build();
        CustomPC second = builder.setRamGb(64).setCpu("Another CPU").build();
        assertEquals(16, first.getRamGb());
        assertEquals("CPU", first.getCpu());
        assertEquals(64, second.getRamGb());
        assertEquals("Another CPU", second.getCpu());
    }

    @Test
    void bothDirectorRecipesProduceDifferentConfigurations() {
        PCDirector director = new PCDirector();
        Product gaming = director.buildGamingPC(new CustomPCBuilder());
        Product work = director.buildWorkstation(new CustomPCBuilder());
        assertNotEquals(gaming.getSku(), work.getSku());
        assertTrue(gaming.getDescription().contains("RTX 4070"));
        assertTrue(work.getDescription().contains("64 GB RAM"));
    }

    @Test
    void invalidConfigurationAndNegativePriceAreRejected() {
        assertThrows(IllegalArgumentException.class, () -> builder().setRamGb(0).build());
        assertThrows(IllegalArgumentException.class, () -> builder().setGpu(" ").build());
        assertThrows(IllegalArgumentException.class,
                () -> builder().setPrice(new BigDecimal("-0.001")).build());
        assertThrows(IllegalArgumentException.class, () -> new CustomPCBuilder().build());
    }
}
