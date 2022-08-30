package bg.sofia.uni.fmi.mjt.food.analyzer.server.cache;

import bg.sofia.uni.fmi.mjt.food.analyzer.server.dto.FoodProduct;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FoodCache {
    private static final String CACHE_PATH = "resources/products.txt";

    private FoodCache() {

    }

    public static void loadCache(List<FoodProduct> foodsCache) {
        List<String> linesResult = new ArrayList<>();

        try (Stream<String> currLines = Files.lines(Paths.get(CACHE_PATH))) {
            linesResult = currLines.collect(Collectors.toList());
        } catch (IOException e) {
            System.err.println("Could not find " + CACHE_PATH);
        }

        //List<FoodProduct> foodsResult = new ArrayList<>();

        for (String line: linesResult) {
            String[] splitLine = line.split("&&");
            int fdcId = Integer.parseInt(splitLine[0]);
            String description = splitLine[1];
            String dataType = splitLine[2];
            String gtinUpc = splitLine[3];
            String publishedDate = splitLine[4];
            String brandOwner = splitLine[5];
            String ingredients = splitLine[6];
            double score = Double.parseDouble(splitLine[7]);

            FoodProduct currFood = new FoodProduct(fdcId, description, dataType, gtinUpc, publishedDate, brandOwner,
                ingredients, score);

            foodsCache.add(currFood);
        }
    }
}
