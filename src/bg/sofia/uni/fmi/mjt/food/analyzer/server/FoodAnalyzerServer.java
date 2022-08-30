package bg.sofia.uni.fmi.mjt.food.analyzer.server;

import bg.sofia.uni.fmi.mjt.food.analyzer.server.cache.FoodCache;
import bg.sofia.uni.fmi.mjt.food.analyzer.server.dto.FoodProduct;
import bg.sofia.uni.fmi.mjt.food.analyzer.server.dto.FoodQueryReport;
import bg.sofia.uni.fmi.mjt.food.analyzer.server.dto.SingleProductInfo;
import bg.sofia.uni.fmi.mjt.food.analyzer.server.http.HttpRequestFood;
import com.google.zxing.*;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;
import org.apache.commons.io.FileUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class FoodAnalyzerServer {
    private static final String DELIMITER = "%20";
    private static final String API_KEY = "b6S0aW1Vi3qc0LCVcmGxhjKTbmcrsIAZPMGA6X8q";
    private static String urlByFoodName =
        "https://api.nal.usda.gov/fdc/v1/foods/search?query=%s&requireAllWords=true&api_key="
            + API_KEY;

    private static String urlByFdcId = "https://api.nal.usda.gov/fdc/v1/food/%s?api_key=" +
        API_KEY;

    private static String urlByGtinUpc = "https://api.nal.usda.gov/fdc/v1/foods/search?api_key="
        + API_KEY + "&query=%s";

    private static List<FoodProduct> foodsCache = new ArrayList<>();

    public static FoodQueryReport startServer(String input, String filename) {
        String[] splitInput = input.split(" ");
        String command = splitInput[0];
        splitInput = removeFirstElement(splitInput);

        String validInfoAboutProduct;
        if (command.equalsIgnoreCase("get-food-by-barcode")) {
            String[] gtinUpcArray = new String[1];
            gtinUpcArray[0] = splitInput[0].split("=")[1];
            validInfoAboutProduct = getInfoAboutProduct(gtinUpcArray, filename);
        } else {
            validInfoAboutProduct = getInfoAboutProduct(splitInput, filename);
        }

        if (!validInfoAboutProduct.equals("")) {
            System.out.println(validInfoAboutProduct);
            return null;
        }

        FoodCache.loadCache(foodsCache);
        try (ServerSocket serverSocket = new ServerSocket(8080)) {
            //System.out.println("SUCCESS!!");

            //Socket socket = serverSocket.accept();

            switch (command.toLowerCase()) {
                case "get-food": { return getFoodByName(splitInput, filename); }
                case "get-food-report": { return getFoodReport(splitInput, filename); }
                case "get-food-by-barcode": { return getFoodByBarcode(splitInput, filename); }
            }

        } catch (IOException e) {
            System.err.println("Invalid input! Enter a valid command!");
        }

        return null;
    }

    private static FoodQueryReport getFoodByName(String[] input, String filename) {
        String foodName = String.join(DELIMITER, input);

        String resultUrl = String.format(urlByFoodName, foodName);

        FoodQueryReport result = HttpRequestFood.GetFoodProduct(resultUrl);

        for (FoodProduct curr : result.getFoods()) {
            addNewProduct(curr, filename);
            System.out.println(curr.humanReadable());
        }

        return result;
    }

    private static FoodQueryReport getFoodReport(String[] input, String filename) {
        String fdcId = input[0];

        String resultUrl = String.format(urlByFdcId, fdcId);

        FoodProduct result = HttpRequestFood.GetFoodProductById(resultUrl);

        addNewProduct(result, filename);
        System.out.println(result.humanReadable());

        List<FoodProduct> foods = new ArrayList<>();
        foods.add(result);
        return new FoodQueryReport(foods);
    }

    private static FoodQueryReport getFoodByBarcode(String[] input, String filename) {
        FoodQueryReport result = new FoodQueryReport();

        if (input.length > 1) {

        } else {
            String[] properInput = input[0].split("=");
            if (properInput[0].toLowerCase().equals("--img")) {
                try {
                    String imageUrl = input[0].substring(6);
                    File file = null;
                    if (properInput[1].substring(0,5).equalsIgnoreCase("https")) {
                        //We're using an online link
                        URL url = new URL(imageUrl);
                        BufferedImage img = ImageIO.read(url);
                        file = new File("resources/barcode.gif");
                        ImageIO.write(img, "gif", file);
                    } else {
                        file = new File(imageUrl);
                    }

                    String decodedText = decodeBarcode(file);

                    if (decodedText == null) {
                        System.out.println("No QR code could be found.");
                    } else {
                        String resultUrl = String.format(urlByGtinUpc, decodedText);
                        System.out.println(resultUrl);

                        result = HttpRequestFood.GetFoodProduct(resultUrl);

                        for (FoodProduct curr : result.getFoods()) {
                            addNewProduct(curr, filename);
                            System.out.println(curr.humanReadable());
                        }
                    }
                } catch (IOException e) {
                    System.err.println("Could not decode Barcode");
                }
            } else if (properInput[0].toLowerCase().equals("--code")) {
                String gtinUpc = properInput[1];

                String resultUrl = String.format(urlByFoodName, gtinUpc);

                result = HttpRequestFood.GetFoodProduct(resultUrl);

                for (FoodProduct curr : result.getFoods()) {
                    addNewProduct(curr, filename);
                    System.out.println(curr.humanReadable());
                }
            }
        }

        return result;
    }

    private static String[] removeFirstElement(String[] myArr) {
        String[] newArr = new String[myArr.length - 1];

        for (int i = 1; i < myArr.length; i++) {
            newArr[i - 1] = myArr[i];
        }

        return newArr;
    }

    private static String decodeBarcode(File barcodeImage) throws IOException {
        BufferedImage bufferedImage = ImageIO.read(barcodeImage);
        LuminanceSource source = new BufferedImageLuminanceSource(bufferedImage);
        BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));

        try {
            Result result = new MultiFormatReader().decode(bitmap);
            return result.getText();
        } catch (NotFoundException e) {
            System.out.println("There is no QR code in the image");
            return null;
        }
    }

    public static FoodProduct addNewProduct(FoodProduct foodProduct, String filename) {
        try (FileWriter writer = new FileWriter(filename, true)) {
            synchronized (writer) {
                foodsCache.add(foodProduct);
                writer.write(foodProduct.toString());
                System.out.println("Successfully added: " + foodProduct.getDescription());

            }
        } catch (IOException e) {
            System.err.println("Could not read given file.");
        }

        return foodProduct;
    }

    public static String getInfoAboutProduct(String[] keyWords, String filename) {
        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            String line = reader.readLine();
            while (line != null) {
                boolean containsFdcId = true;
                boolean containsDescription = true;
                boolean containsGtinUpc = true;
                String[] properties = line.split("&&");
                String currDescription = properties[1].toLowerCase();
                String currFdcId = properties[0];
                String currGtinUpc = properties[3];

                if (!currGtinUpc.equals(keyWords[0])) {
                    containsGtinUpc = false;
                }

                if (!currFdcId.equals(keyWords[0])) {
                    containsFdcId = false;
                }

                for (int i = 0; i < keyWords.length; i++) {
                    if (!currDescription.contains(keyWords[i].toLowerCase())) {
                        containsDescription = false;
                        break;
                    }
                }

                if (containsFdcId || containsDescription || containsGtinUpc) {
                    FoodProduct toReturn = new FoodProduct(Integer.parseInt(properties[0]), properties[1],
                        properties[2], properties[3], properties[4], properties[5], properties[6],
                        Double.parseDouble(properties[7]));

                    return toReturn.humanReadable();
                }

                line = reader.readLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return "";
    }
}
