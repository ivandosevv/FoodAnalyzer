package bg.sofia.uni.fmi.mjt.food.analyzer.client;

import org.junit.jupiter.api.Test;

import static bg.sofia.uni.fmi.mjt.food.analyzer.client.FoodAnalyzerClient.getFromImg;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class ClientTest {
    @Test
    public void testImageFromWeb() {
        String input = "--img=https://github.com/fmi/java-course/raw/master/course-projects/images" +
            "/upc-barcode.gif";

        assertEquals(getFromImg(input.split("=")), "009800146130");
    }
}
