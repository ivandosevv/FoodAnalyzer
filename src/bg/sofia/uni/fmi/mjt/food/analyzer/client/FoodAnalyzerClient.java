package bg.sofia.uni.fmi.mjt.food.analyzer.client;

import bg.sofia.uni.fmi.mjt.food.analyzer.server.FoodAnalyzerServer;
import java.util.Scanner;

public class FoodAnalyzerClient {
    private static final String HOST = "localhost";
    private static final int PORT = 8080;
    private static final String FILENAME = "resources/products.txt";

    public static void execute() {
        try (Scanner scanner = new Scanner(System.in)) {
            System.out.println(String.format("Server successfully connected to %s:%d", HOST, PORT));

            while (true) {
                String input = scanner.nextLine();
                String[] splitInput = input.split(" ");
                String command = splitInput[0];

                if (command.equalsIgnoreCase("disconnect") || command.equalsIgnoreCase("quit")) {
                    break;
                } else if (command.equals("get-food") || command.equals("get-food-report") ||
                    command.equals("get-food-by-barcode")) {
                    if (splitInput.length < 2) {
                        System.out.println("Not enough arguments were passed");
                    } else {
                        FoodAnalyzerServer.startServer(input, FILENAME);
                    }
                } else {
                    System.out.println("Invalid command");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        execute();
    }
}
