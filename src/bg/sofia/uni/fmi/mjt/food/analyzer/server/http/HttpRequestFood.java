package bg.sofia.uni.fmi.mjt.food.analyzer.server.http;

import bg.sofia.uni.fmi.mjt.food.analyzer.server.dto.FoodProduct;
import bg.sofia.uni.fmi.mjt.food.analyzer.server.dto.FoodQueryReport;
import bg.sofia.uni.fmi.mjt.food.analyzer.server.dto.SingleProductInfo;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class HttpRequestFood {
    public static FoodQueryReport getFoodProduct(String productUrl) {
        HttpClient client = HttpClient.newHttpClient();

        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(productUrl)).build();

        try {
            String jsonResponse = client.send(request, HttpResponse.BodyHandlers.ofString()).body();
            System.out.println(jsonResponse);
            Gson gson = new Gson();

            FoodQueryReport result = gson.fromJson(jsonResponse, new TypeToken<FoodQueryReport>() { }.getType());

            return result;
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public static FoodProduct getFoodProductById(String productUrl) {
        HttpClient client = HttpClient.newHttpClient();

        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(productUrl)).build();

        try {
            String jsonResponse = client.send(request, HttpResponse.BodyHandlers.ofString()).body();
            System.out.println(jsonResponse);
            Gson gson = new Gson();

            FoodProduct result = gson.fromJson(jsonResponse, new TypeToken<FoodProduct>() { }.getType());
            SingleProductInfo info = gson.fromJson(jsonResponse, new TypeToken<SingleProductInfo>() { }.getType());
            result.setPublishedDate(info.getPublicationDate());

            return result;
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
