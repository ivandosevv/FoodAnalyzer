package bg.sofia.uni.fmi.mjt.food.analyzer.server;

import bg.sofia.uni.fmi.mjt.food.analyzer.server.dto.SingleProductInfo;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SingleProductInfoTest {
    private static SingleProductInfo product;

    @BeforeAll
    private static void setUp() {
        product = new SingleProductInfo(1461113, "2021-03-19", -57.52143);
    }

    @Test
    public void testSingleProductInfoProperties() {
        int fdcId = 1461114;
        String publicationDate = "2021-03=20";
        double value = -58;

        product.setCalories(value);
        product.setFdcId(fdcId);
        product.setPublicationDate(publicationDate);

        assertEquals(product.getFdcId(), fdcId);
        assertEquals(product.getPublicationDate(), publicationDate);
        assertEquals(product.getCalories(), value);
    }
}
