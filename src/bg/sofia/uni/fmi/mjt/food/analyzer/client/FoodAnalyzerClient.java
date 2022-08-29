package bg.sofia.uni.fmi.mjt.food.analyzer.client;

import bg.sofia.uni.fmi.mjt.food.analyzer.server.FoodAnalyzerServer;
import bg.sofia.uni.fmi.mjt.food.analyzer.server.http.HttpRequestFood;
import com.google.zxing.*;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.UnknownHostException;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Locale;
import java.util.Scanner;

import org.apache.commons.io.FileUtils;

public class FoodAnalyzerClient {
    private static String HOST = "localhost";
    private static int PORT = 8080;

    public static void main(String[] args) {
        PrintWriter writer = null;
        try (Scanner scanner = new Scanner(System.in)) {
            /*Socket socket = new Socket(HOST, PORT);
            writer = new PrintWriter(socket.getOutputStream(), true);*/

            System.out.println(String.format("Server successfully connected to %s:%d", HOST, PORT));

            while (true) {
                String input = scanner.nextLine();
                String[] splitInput = input.split(" ");
                String command = splitInput[0];

                if (command.equals("disconnect") || command.equals("quit")) {
                    //writer.println(input);
                    break;
                } else if (command.equals("get-food") || command.equals("get-food-report") ||
                command.equals("get-food-by-barcode")) {
                    if (splitInput.length < 2) {
                        System.out.println("Not enough arguments were passed");
                    } else {
                        FoodAnalyzerServer.startServer(input);
                    }
                } else {
                    System.out.println("Invalid command");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
