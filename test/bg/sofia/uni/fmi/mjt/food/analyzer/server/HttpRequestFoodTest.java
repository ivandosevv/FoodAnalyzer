package bg.sofia.uni.fmi.mjt.food.analyzer.server;

import bg.sofia.uni.fmi.mjt.food.analyzer.server.dto.FoodProduct;
import bg.sofia.uni.fmi.mjt.food.analyzer.server.dto.FoodQueryReport;
import bg.sofia.uni.fmi.mjt.food.analyzer.server.http.HttpRequestFood;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class HttpRequestFoodTest {
    @Test
    public void testGetFoodProductWithValidUrl() {
        String url = "https://api.nal.usda.gov/fdc/v1/foods/search?" +
            "api_key=b6S0aW1Vi3qc0LCVcmGxhjKTbmcrsIAZPMGA6X8q&query=raffaello%20treat&requireAllWords=true";
        int expected = 1;
        int expectedFdcId = 2041155;
        String expectedDescription = "RAFFAELLO, ALMOND COCONUT TREAT";
        String expectedBrandOwner = "Ferrero U.S.A., Incorporated";
        String expectedDataType = "Branded";
        String expectedGtinUpc = "009800146130";
        String expectedIngredients = "VEGETABLE OILS (PALM AND SHEANUT). DRY COCONUT, SUGAR, ALMONDS," +
            " SKIM MILK POWDER, WHEY POWDER (MILK), WHEAT FLOUR, NATURAL AND ARTIFICIAL FLAVORS, " +
            "LECITHIN AS EMULSIFIER (SOY), SALT, SODIUM BICARBONATE AS LEAVENING AGENT.";
        String expectedPublicationDate = "2021-10-28";
        double expectedScore = 573.2873;

        FoodQueryReport result = HttpRequestFood.GetFoodProduct(url);

        assertEquals(result.getFoods().size(), expected);

        FoodProduct fp = result.getFoods().get(0);

        assertEquals(fp.getFdcId(), expectedFdcId);
        assertEquals(fp.getDescription(), expectedDescription);
        assertEquals(fp.getBrandOwner(), expectedBrandOwner);
        assertEquals(fp.getDataType(), expectedDataType);
        assertEquals(fp.getGtinUpc(), expectedGtinUpc);
        assertEquals(fp.getIngredients(), expectedIngredients);
        assertEquals(fp.getPublishedDate(), expectedPublicationDate);
        assertEquals(fp.getScore(), expectedScore);
    }

    @Test
    public void testGetFoodProductWithValidId() {
        String url = "https://api.nal.usda.gov/fdc/v1/food/2041155?api_key=b6S0aW1Vi3qc0LCVcmGxhjKTbmcrsIAZPMGA6X8q";
        int expectedFdcId = 2041155;
        String expectedDescription = "RAFFAELLO, ALMOND COCONUT TREAT";
        String expectedBrandOwner = "Ferrero U.S.A., Incorporated";
        String expectedDataType = "Branded";
        String expectedGtinUpc = "009800146130";
        String expectedIngredients = "VEGETABLE OILS (PALM AND SHEANUT). DRY COCONUT, SUGAR, ALMONDS," +
            " SKIM MILK POWDER, WHEY POWDER (MILK), WHEAT FLOUR, NATURAL AND ARTIFICIAL FLAVORS, " +
            "LECITHIN AS EMULSIFIER (SOY), SALT, SODIUM BICARBONATE AS LEAVENING AGENT.";
        String expectedPublicationDate = "2021-10-28";
        double expectedScore = 573.2873;

        FoodProduct result = HttpRequestFood.GetFoodProductById(url);

        assertEquals(result.getFdcId(), expectedFdcId);

        result.setPublishedDate(expectedPublicationDate);
        result.setScore(expectedScore);
        result.setBrandOwner(expectedBrandOwner);
        result.setDataType(expectedDataType);
        result.setScore(expectedScore);
        result.setFdcId(expectedFdcId);
        result.setIngredients(expectedIngredients);
        result.setGtinUpc(expectedGtinUpc);
        result.setDescription(expectedDescription);

        assertEquals(result.getFdcId(), expectedFdcId);
        assertEquals(result.getDescription(), expectedDescription);
        assertEquals(result.getBrandOwner(), expectedBrandOwner);
        assertEquals(result.getDataType(), expectedDataType);
        assertEquals(result.getGtinUpc(), expectedGtinUpc);
        assertEquals(result.getIngredients(), expectedIngredients);
        assertEquals(result.getPublishedDate(), expectedPublicationDate);
        assertEquals(result.getScore(), expectedScore);
        assertEquals(result.humanReadable(), "{\n" +
            "\tfdcId: 2041155\n" +
            "\tdescription: RAFFAELLO, ALMOND COCONUT TREAT\n" +
            "\tgtinUpc: 009800146130\n" +
            "\tpublishedDate: 2021-10-28\n" +
            "\tbrandOwner: Ferrero U.S.A., Incorporated\n" +
            "\tingredients: VEGETABLE OILS (PALM AND SHEANUT). DRY COCONUT, SUGAR, ALMONDS, " +
            "SKIM MILK POWDER, WHEY POWDER (MILK), WHEAT FLOUR, NATURAL AND ARTIFICIAL FLAVORS, " +
            "LECITHIN AS EMULSIFIER (SOY), SALT, SODIUM BICARBONATE AS LEAVENING AGENT.\n" +
            "\tscore: 573.29\n" +
            "}\n");
    }
}
