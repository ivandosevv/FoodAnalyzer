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

    @Override
    public String toString() {
        StringBuffer buf = new StringBuffer();

        for (FoodProduct food: foods) {
            buf.append(food.humanReadable());
            buf.append(System.lineSeparator());
        }

        return buf.toString();
    }
}
