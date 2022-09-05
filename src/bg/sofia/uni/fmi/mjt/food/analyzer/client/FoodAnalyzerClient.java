package bg.sofia.uni.fmi.mjt.food.analyzer.client;

import bg.sofia.uni.fmi.mjt.food.analyzer.server.FoodAnalyzerServer;
import com.google.zxing.*;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Scanner;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.LuminanceSource;
import com.google.zxing.NotFoundException;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.Result;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;

public class FoodAnalyzerClient {
    private static final String HOST = "localhost";
    private static final int PORT = 7777;
    //private static final String FILENAME = "resources/products.txt";
    private static final int BUFFER_SIZE = 2048;
    private static ByteBuffer buffer = ByteBuffer.allocateDirect(BUFFER_SIZE);


    public static void main(String[] args) {
        execute();
    }

    public static int execute() {
        try (SocketChannel socketChannel = SocketChannel.open();
             Scanner scanner = new Scanner(System.in)) {

            socketChannel.connect(new InetSocketAddress(HOST, PORT));

            System.out.println("Connected to the server.");

            while (true) {
                System.out.print("Enter message: ");
                String message = scanner.nextLine(); // read a line from the console
                String[] splitInput = message.split(" ");
                String command = splitInput[0];

                if ("quit".equalsIgnoreCase(command) || "disconnect".equalsIgnoreCase(command)) {
                    break;
                }

                if (command.equalsIgnoreCase("get-food-by-barcode")) {
                    if (splitInput.length > 2) {
                        if (splitInput[1].contains("--code")) {
                            message = splitInput[0] + " " + splitInput[1];
                        } else if (splitInput[2].contains("--code")) {
                            message = splitInput[0] + " " + splitInput[2];
                        } else if (splitInput[1].contains("--img")) {
                            String[] splitImgQuery = splitInput[1].split("=");
                            message = splitInput[0] + " --code=" + getFromImg(splitImgQuery);
                        } else if (splitInput[2].contains("--img")) {
                            String[] splitImgQuery = splitInput[2].split("=");
                            message = splitInput[0] + " --code=" + getFromImg(splitImgQuery);
                        }
                    } else {
                        if (splitInput[1].contains("--img")) {
                            String[] splitImgQuery = splitInput[1].split("=");
                            message = splitInput[0] + " --code=" + getFromImg(splitImgQuery);
                        }
                    }
                }

                System.out.println("Sending message <" + message + "> to the server...");

                buffer.clear(); // switch to writing mode
                buffer.put(message.getBytes()); // buffer fill
                buffer.flip(); // switch to reading mode
                socketChannel.write(buffer); // buffer drain

                buffer.clear(); // switch to writing mode
                socketChannel.read(buffer); // buffer fill
                buffer.flip(); // switch to reading mode

                byte[] byteArray = new byte[buffer.remaining()];
                buffer.get(byteArray);
                String reply = new String(byteArray, "UTF-8"); // buffer drain

                // if buffer is a non-direct one, is has a wrapped array and we can get it
                //String reply = new String(buffer.array(), 0, buffer.position(), "UTF-8"); // buffer drain

                System.out.println("The server replied <" + reply + ">");
            }
        } catch (IOException e) {
            throw new RuntimeException("There is a problem with the network communication", e);
        }

        return 0;
    }

    public static String getFromImg(String[] splitImgQuery) {
        String decodedText = "";
        final int subStr = 5;

        try {
            if (splitImgQuery[1].substring(0, subStr).equalsIgnoreCase("https")) {
                //We're using an online link
                URL url = new URL(splitImgQuery[1]);
                BufferedImage img = ImageIO.read(url);
                File file = new File("resources/barcode.gif");
                ImageIO.write(img, "gif", file);
                decodedText = decodeBarcode(file);
            } else {
                File file = new File(splitImgQuery[1]);
                decodedText = decodeBarcode(file);
            }
        } catch (IOException e) {
            throw new RuntimeException("Cannot decode barcode");
        }


        return decodedText;
    }

    public static String decodeBarcode(File barcodeImage) throws IOException {
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
}
