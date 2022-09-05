package bg.sofia.uni.fmi.mjt.food.analyzer.server;

import bg.sofia.uni.fmi.mjt.food.analyzer.server.cache.FoodCache;
import bg.sofia.uni.fmi.mjt.food.analyzer.server.dto.FoodProduct;
import bg.sofia.uni.fmi.mjt.food.analyzer.server.dto.FoodQueryReport;
import bg.sofia.uni.fmi.mjt.food.analyzer.server.http.HttpRequestFood;

import java.io.FileWriter;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.*;

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
    private static Map<String, FoodProduct> foodsCacheName = new HashMap<>();
    private static Map<Integer, FoodProduct> foodsCacheFdc = new HashMap<>();
    private static Map<String, FoodProduct> foodsCacheGtin = new HashMap<>();
    public static final int SERVER_PORT = 7777;
    private static final String SERVER_HOST = "localhost";
    private static final int BUFFER_SIZE = 2048;

    public static void main(String[] args) {
        execute();
    }

    public static void execute() {
        try (ServerSocketChannel serverSocketChannel = ServerSocketChannel.open()) {
            serverSocketChannel.bind(new InetSocketAddress(SERVER_HOST, SERVER_PORT));
            serverSocketChannel.configureBlocking(false);

            Selector selector = Selector.open();
            serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);

            ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);

            while (true) {
                int readyChannels = selector.select();
                if (readyChannels == 0) {
                    continue;
                }
                Set<SelectionKey> selectedKeys = selector.selectedKeys();
                Iterator<SelectionKey> keyIterator = selectedKeys.iterator();

                while (keyIterator.hasNext()) {
                    SelectionKey key = keyIterator.next();
                    if (key.isReadable()) {
                        SocketChannel sc = (SocketChannel) key.channel();

                        buffer.clear();
                        int r = sc.read(buffer);
                        if (r < 0) {
                            System.out.println("Client has closed the connection");
                            sc.close();
                            continue;
                        }
                        buffer.flip();

                        byte[] clientBytes = new byte[buffer.remaining()];
                        buffer.get(clientBytes);

                        String clientInput = new String(clientBytes, StandardCharsets.UTF_8);
                        String result = startServer(clientInput, "resources/products.txt");

                        if (result.length() > BUFFER_SIZE) {
                            String[] toGet = result.split(System.lineSeparator());
                            StringBuilder sb = new StringBuilder();

                            for (String curr : toGet) {
                                if (sb.length() + curr.length() + System.lineSeparator().length() < BUFFER_SIZE) {
                                    sb.append(curr);
                                    sb.append(System.lineSeparator());
                                } else {
                                    break;
                                }
                            }
                            result = sb.toString();
                        }
                        buffer.clear();
                        buffer.put(result.getBytes(StandardCharsets.UTF_8));
                        buffer.flip();
                        sc.write(buffer);
                    } else if (key.isAcceptable()) {
                        ServerSocketChannel sockChannel = (ServerSocketChannel) key.channel();
                        SocketChannel accept = sockChannel.accept();
                        accept.configureBlocking(false);
                        accept.register(selector, SelectionKey.OP_READ);
                    }

                    keyIterator.remove();
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("There is a problem with the server socket", e);
        }
    }

    public static String startServer(String input, String filename) {
        String[] splitInput = input.split(" ");
        String command = splitInput[0];
        splitInput = removeFirstElement(splitInput);

        String validInfoAboutProduct;
        if (command.equalsIgnoreCase("get-food-by-barcode")) {
            String[] gtinUpcArray = new String[1];
            gtinUpcArray[0] = splitInput[0].split("=")[1];
            validInfoAboutProduct = getInfoAboutProductByGtin(gtinUpcArray[0], filename);
        } else if (command.equalsIgnoreCase("get-food-report")) {
            validInfoAboutProduct = getInfoAboutProductByFdc(Integer.parseInt(splitInput[0]), filename);
        } else {
            validInfoAboutProduct = getInfoAboutProductByName(splitInput, filename);
        }

        if (!validInfoAboutProduct.equals("")) {
            System.out.println(validInfoAboutProduct);
            return validInfoAboutProduct;
        }

        FoodCache.loadCache(foodsCacheFdc, foodsCacheGtin, foodsCacheName);

        switch (command.toLowerCase()) {
            case "get-food": {
                return getFoodByName(splitInput, filename).toString();
            }
            case "get-food-report": {
                return getFoodReport(splitInput, filename).toString();
            }
            case "get-food-by-barcode": {
                return getFoodByBarcode(splitInput, filename).toString();
            }
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
            }
        } else {
            String[] properInput = input[0].split("=");
            if (properInput[0].equalsIgnoreCase("--code")) {
                result = getFoodQueryByCode(properInput[1], filename);
            }
        }
        return result;
    }

    private static FoodQueryReport getFoodQueryByCode(String gtinUpc, String filename) {
        String resultUrl = String.format(URL_BY_FOOD_NAME, gtinUpc);

        FoodQueryReport result = HttpRequestFood.getFoodProduct(resultUrl);

        for (FoodProduct curr : result.getFoods()) {
            addNewProduct(curr, filename);
            System.out.println(curr.humanReadable());
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

    public static FoodProduct addNewProduct(FoodProduct foodProduct, String filename) {
        try (FileWriter writer = new FileWriter(filename, true)) {
            synchronized (writer) {
                foodsCacheFdc.putIfAbsent(foodProduct.getFdcId(), foodProduct);
                foodsCacheName.putIfAbsent(foodProduct.getDescription(), foodProduct);
                foodsCacheGtin.putIfAbsent(foodProduct.getGtinUpc(), foodProduct);
                writer.write(foodProduct.toString());
                System.out.println("Successfully added: " + foodProduct.getDescription());

            }
        } catch (IOException e) {
            System.err.println("Could not read given file.");
        }

        return foodProduct;
    }

    public static String getInfoAboutProductByGtin(String keyWord, String filename) {
        if (foodsCacheGtin.containsKey(keyWord)) {
            return foodsCacheGtin.get(keyWord).humanReadable();
        }

        return "";
    }

    public static String getInfoAboutProductByFdc(Integer keyWord, String filename) {
        if (foodsCacheFdc.containsKey(keyWord)) {
            return foodsCacheFdc.get(keyWord).humanReadable();
        }

        return "";
    }

    public static String getInfoAboutProductByName(String[] keyWords, String filename) {
        int counter = 0;

        for (Map.Entry<String, FoodProduct> entry: foodsCacheName.entrySet()) {
            for (String key: keyWords) {
                if (entry.getKey().toLowerCase().contains(key.toLowerCase())) {
                    counter++;
                }
            }

            if (counter == keyWords.length) {
                return entry.getValue().humanReadable();
            }
        }

        return "";
    }
}
