package bg.sofia.uni.fmi.mjt.food.analyzer.server;

import bg.sofia.uni.fmi.mjt.food.analyzer.server.cache.FoodCache;
import bg.sofia.uni.fmi.mjt.food.analyzer.server.dto.FoodProduct;
import bg.sofia.uni.fmi.mjt.food.analyzer.server.dto.FoodQueryReport;
import bg.sofia.uni.fmi.mjt.food.analyzer.server.http.HttpRequestFood;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;

import java.io.File;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
        String input = "get-food beef noodle soup";

        /*FoodQueryReport food = HttpRequestFood.GetFoodProduct("https://api.nal.usda.gov/fdc/v1/foods/" +
            "search?query=christmas&requireAllWords=true&api_key=b6S0aW1Vi3qc0LCVcmGxhjKTbmcrsIAZPMGA6X8q");*/
        int expected = 50;

        assertEquals(FoodAnalyzerServer.startServer(input, filename).getFoods().size(), expected);
    }

    @Test
    public void testGetFoodThatHasAlreadyBeenAdded() {
        String input = "get-food beef noodle soup";

        int expected = 50;

        assertEquals(FoodAnalyzerServer.startServer(input, filename).getFoods().size(), expected);
        assertEquals(FoodAnalyzerServer.startServer(input, filename), null);
    }

    @Test
    public void testGetFoodReportValid() {
        String input = "get-food-report 415987";

        int expected = 1;

        assertEquals(FoodAnalyzerServer.startServer(input, filename).getFoods().size(), expected);
    }

    @Test
    public void testGetFoodByBarcodeValid() {
        String input = "get-food-by-barcode --code=07622210164018";
        int expected = 1;

        assertEquals(FoodAnalyzerServer.startServer(input, filename).getFoods().size(), expected);
    }

    @Test
    public void testGetFoodByBarcodeImageValid() {
        String input = "get-food-by-barcode --img=https://raw.githubusercontent.com/fmi/java-course/" +
            "master/course-projects/images/upc-barcode.gif";

        int expected = 1;

        assertEquals(FoodAnalyzerServer.startServer(input, filename).getFoods().size(), expected);
    }

    @Test
    public void testGetFoodByBarcodeAndImageValid() {
        String firstInput = "get-food-by-barcode --img=https://raw.githubusercontent.com/fmi/java-course/master/" +
            "course-projects/images/upc-barcode.gif --code=009800146130";

        int expected = 1;
        assertEquals(FoodAnalyzerServer.startServer(firstInput, filename).getFoods().size(), expected);
    }

    @Test
    public void testGetFoodByBarcodeAndImageValidReversed() {
        String secondInput = "get-food-by-barcode --code=009800146130 --img=https://raw.githubusercontent.com/" +
            "fmi/java-course/master/course-projects/images/upc-barcode.gif";

        int expected = 1;
        assertEquals(FoodAnalyzerServer.startServer(secondInput, filename).getFoods().size(), expected);
    }

    @Test
    public void testGetFoodByImageAndSomethingElseReversed() {
        String firstInput = "get-food-by-barcode --img=https://raw.githubusercontent.com/fmi/java-course/master/" +
            "course-projects/images/upc-barcode.gif --somethingElse=somethingElse";

        int expected = 1;
        assertEquals(FoodAnalyzerServer.startServer(firstInput, filename).getFoods().size(), expected);
    }

    @Test
    public void testGetFoodByImageAndSomethingElseValidReversed() {
        String secondInput = "get-food-by-barcode --somethingElse=somethingElse --img=https://raw.githubusercontent." +
            "com/fmi/java-course/master/course-projects/images/upc-barcode.gif";

        int expected = 1;

        assertEquals(FoodAnalyzerServer.startServer(secondInput, filename).getFoods().size(), expected);
    }
}
