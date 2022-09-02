package bg.sofia.uni.fmi.mjt.food.analyzer.server;

import bg.sofia.uni.fmi.mjt.food.analyzer.server.cache.FoodCache;
import bg.sofia.uni.fmi.mjt.food.analyzer.server.dto.FoodProduct;
import bg.sofia.uni.fmi.mjt.food.analyzer.server.dto.FoodQueryReport;
import bg.sofia.uni.fmi.mjt.food.analyzer.server.http.HttpRequestFood;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.LuminanceSource;
import com.google.zxing.NotFoundException;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.Result;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class FoodAnalyzerServer {
    private static final String DELIMITER = "%20";
    private static final String API_KEY = "PUT_API_KEY";
    private static final String URL_BY_FOOD_NAME =
        "https://api.nal.usda.gov/fdc/v1/foods/search?query=%s&requireAllWords=true&api_key="
            + API_KEY;

    private static final String URL_BY_FDC_ID = "https://api.nal.usda.gov/fdc/v1/food/%s?api_key=" +
        API_KEY;

    private static final String URL_BY_GTIN_UPC = "https://api.nal.usda.gov/fdc/v1/foods/search?api_key="
        + API_KEY + "&query=%s";

    private static final List<FoodProduct> FOODS_CACHE = new ArrayList<>();

    private static final int LOCALHOST = 8080;

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

        FoodCache.loadCache(FOODS_CACHE);
        try (ServerSocket serverSocket = new ServerSocket(LOCALHOST)) {
            //System.out.println("SUCCESS!!");

            //Socket socket = serverSocket.accept();

            switch (command.toLowerCase()) {
                case "get-food": {
                    return getFoodByName(splitInput, filename);
                }
                case "get-food-report": {
                    return getFoodReport(splitInput, filename);
                }
                case "get-food-by-barcode": {
                    return getFoodByBarcode(splitInput, filename);
                }
            }

        } catch (IOException e) {
            System.err.println("Invalid input! Enter a valid command!");
        }

        return null;
    }

    private static FoodQueryReport getFoodByName(String[] input, String filename) {
        String foodName = String.join(DELIMITER, input);

        String resultUrl = String.format(URL_BY_FOOD_NAME, foodName);

        FoodQueryReport result = HttpRequestFood.getFoodProduct(resultUrl);

        for (FoodProduct curr : result.getFoods()) {
            addNewProduct(curr, filename);
            System.out.println(curr.humanReadable());
        }

        return result;
    }

    private static FoodQueryReport getFoodReport(String[] input, String filename) {
        String fdcId = input[0];

        String resultUrl = String.format(URL_BY_FDC_ID, fdcId);

        FoodProduct result = HttpRequestFood.getFoodProductById(resultUrl);

        addNewProduct(result, filename);
        System.out.println(result.humanReadable());

        List<FoodProduct> foods = new ArrayList<>();
        foods.add(result);
        return new FoodQueryReport(foods);
    }

    private static FoodQueryReport getFoodByBarcode(String[] input, String filename) {
        FoodQueryReport result = new FoodQueryReport();

        if (input.length > 1) {
            final int first = 1;

            String[] firstProperInput = input[0].split("=");
            String[] secondProperInput = input[1].split("=");

            if (firstProperInput[0].equalsIgnoreCase("--code")) {
                result = getFoodQueryByCode(firstProperInput[first], filename);
            } else if (secondProperInput[0].equalsIgnoreCase("--code")) {
                result = getFoodQueryByCode(secondProperInput[first], filename);
            } else if (firstProperInput[0].equalsIgnoreCase("--img")) {
                result = getFoodQueryByImg(firstProperInput[first], input[0], filename);
            } else if (secondProperInput[0].equalsIgnoreCase("--img")) {
                result = getFoodQueryByImg(secondProperInput[first], input[first], filename);
            }
        } else {
            String[] properInput = input[0].split("=");
            if (properInput[0].equalsIgnoreCase("--img")) {
                result = getFoodQueryByImg(properInput[1], input[0], filename);
            } else if (properInput[0].equalsIgnoreCase("--code")) {
                result = getFoodQueryByCode(properInput[1], filename);
            }
        }

        return result;
    }

    private static FoodQueryReport getFoodQueryByCode(String gtinUpc, String filename) {
        FoodQueryReport result = new FoodQueryReport();
        //String gtinUpc = properInput[1];

        String resultUrl = String.format(URL_BY_FOOD_NAME, gtinUpc);

        result = HttpRequestFood.getFoodProduct(resultUrl);

        for (FoodProduct curr : result.getFoods()) {
            addNewProduct(curr, filename);
            System.out.println(curr.humanReadable());
        }

        return result;
    }

    private static FoodQueryReport getFoodQueryByImg(String path, String imageQuery, String filename) {
        FoodQueryReport result = new FoodQueryReport();
        final int substringStart = 6;
        try {
            String imageUrl = imageQuery.substring(substringStart);
            File file = null;
            if (path.substring(0, substringStart - 1).equalsIgnoreCase("https")) {
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
                String resultUrl = String.format(URL_BY_GTIN_UPC, decodedText);
                System.out.println(resultUrl);

                result = HttpRequestFood.getFoodProduct(resultUrl);

                for (FoodProduct curr : result.getFoods()) {
                    addNewProduct(curr, filename);
                    System.out.println(curr.humanReadable());
                }
            }
        } catch (IOException e) {
            System.err.println("Could not decode Barcode");
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
                FOODS_CACHE.add(foodProduct);
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
                final int gtinIndex = 3;
                String currGtinUpc = properties[gtinIndex];

                if (!currGtinUpc.equals(keyWords[0])) {
                    containsGtinUpc = false;
                }

                if (!currFdcId.equals(keyWords[0])) {
                    containsFdcId = false;
                }

                for (String keyWord : keyWords) {
                    if (!currDescription.contains(keyWord.toLowerCase())) {
                        containsDescription = false;
                        break;
                    }
                }

                if (containsFdcId || containsDescription || containsGtinUpc) {
                    int index = 0;
                    FoodProduct toReturn = new FoodProduct(Integer.parseInt(properties[index++]), properties[index++],
                        properties[index++], properties[index++], properties[index++], properties[index++],
                        properties[index++], Double.parseDouble(properties[index]));

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
