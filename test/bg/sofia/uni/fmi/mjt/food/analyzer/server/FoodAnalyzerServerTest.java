
package bg.sofia.uni.fmi.mjt.food.analyzer.server;

import bg.sofia.uni.fmi.mjt.food.analyzer.server.cache.FoodCache;
import bg.sofia.uni.fmi.mjt.food.analyzer.server.dto.FoodProduct;
import bg.sofia.uni.fmi.mjt.food.analyzer.server.dto.FoodQueryReport;
import bg.sofia.uni.fmi.mjt.food.analyzer.server.http.HttpRequestFood;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;

import java.io.File;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
//import org.mockito.junit.jupiter.MockitoExtension;

//@ExtendWith(MockitoExtension.class)
public class FoodAnalyzerServerTest {
    private static String filename = "resources/demo.txt";

    @BeforeEach
    public void setUp() {
        try {
            File file = new File(filename);
            file.createNewFile();
            System.out.println("File: " + file);
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    @AfterEach
    public void exit() {
        File file = new File("resources/demo.txt");
        if (file.delete()) {
            System.out.println("Deleted the file: " + file.getName());
        } else {
            System.out.println("Failed to delete the file.");
        }
    }

    @Test
    public void testGetFoodByValidName() {
        String input = "get-food christmas";

        assertTrue(FoodAnalyzerServer.startServer(input, filename).toLowerCase().contains("christmas"));
    }

    @Test
    public void testGetFoodThatHasAlreadyBeenAdded() {
        String input = "get-food beef noodle soup";

        assertTrue(FoodAnalyzerServer.startServer(input, filename).toLowerCase().contains("soup"));
        assertTrue(FoodAnalyzerServer.startServer(input, filename).toLowerCase().contains("soup"));
    }

    @Test
    public void testGetFoodReportValid() {
        String input = "get-food-report 415987";

        assertTrue(FoodAnalyzerServer.startServer(input, filename).toLowerCase().contains("415987"));
        assertTrue(FoodAnalyzerServer.startServer(input, filename).toLowerCase().contains("415987"));
    }

    @Test
    public void testGetFoodByBarcodeValid() {
        String input = "get-food-by-barcode --code=07622210164018";

        assertTrue(FoodAnalyzerServer.startServer(input, filename).toLowerCase().contains("07622210164018"));
    }

    @Test
    public void testGetFoodByBarcodeAndImageValid() {
        String firstInput = "get-food-by-barcode --img=https://raw.githubusercontent.com/fmi/java-course/master/" +
            "course-projects/images/upc-barcode.gif --code=009800146130";

        assertTrue(FoodAnalyzerServer.startServer(firstInput, filename).toLowerCase().contains("009800146130"));
        assertTrue(FoodAnalyzerServer.startServer(firstInput, filename).toLowerCase().contains("009800146130"));
    }

    @Test
    public void testGetFoodByBarcodeAndImageValidReversed() {
        String secondInput = "get-food-by-barcode --code=009800146130 --img=https://raw.githubusercontent.com/" +
            "fmi/java-course/master/course-projects/images/upc-barcode.gif";

        assertTrue(FoodAnalyzerServer.startServer(secondInput, filename).contains("009800146130"));
    }
}
