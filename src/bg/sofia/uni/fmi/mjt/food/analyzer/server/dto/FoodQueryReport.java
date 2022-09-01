package bg.sofia.uni.fmi.mjt.food.analyzer.server.dto;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FoodQueryReport {
    private final List<FoodProduct> foods;

    public FoodQueryReport() {
        super();
        foods = new ArrayList<>();
    }

    public FoodQueryReport(List<FoodProduct> foods) {
        this.foods = foods;
    }

    public List<FoodProduct> getFoods() {
        return Collections.unmodifiableList(this.foods);
    }
}
