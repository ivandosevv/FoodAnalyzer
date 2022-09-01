package bg.sofia.uni.fmi.mjt.food.analyzer.server.cache;

import bg.sofia.uni.fmi.mjt.food.analyzer.server.dto.FoodProduct;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
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
            int index = 0;
            String[] splitLine = line.split("&&");
            int fdcId = Integer.parseInt(splitLine[index++]);
            String description = splitLine[index++];
            String dataType = splitLine[index++];
            String gtinUpc = splitLine[index++];
            String publishedDate = splitLine[index++];
            String brandOwner = splitLine[index++];
            String ingredients = splitLine[index++];
            double score = Double.parseDouble(splitLine[index++]);

            FoodProduct currFood = new FoodProduct(fdcId, description, dataType, gtinUpc, publishedDate, brandOwner,
                ingredients, score);

            foodsCache.add(currFood);
        }
    }
}
