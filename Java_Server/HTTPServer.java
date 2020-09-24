import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;
import java.nio.file.*;

/**
 * Java program to create a multi-threaded HTTP Server
 * 
 * @author Shelby Huffman
 */
public class HTTPServer {
    // publicly available variables
    public static final int PORT = 8080;
    public static final String successStatusCode = "200 OK"; // Used for Get, Head, Post, and Trace methods
    public static final String createdStatusCode = "201 Created"; // Used for Post requests
    public static final String notFoundStatusCode = "404 Not Found"; // Used when server cannot find requested resource

    // Create thread pool
    private static final int MAX_NO_THREADS = 3;
    private static ExecutorService threadPool = Executors.newFixedThreadPool(MAX_NO_THREADS);
    public static int counter = 0;

    public static void main(String argv[]) throws IOException {
        // Attemp to create a server on port 8080
        try (ServerSocket server = new ServerSocket(PORT)) {
            // If the server successfully starts, we are now listening to connect to the
            // port 8080
            System.out.println("Server Started.\nListening for connection on port " + PORT + " . . .");

            // The program will utilize this while loop to run indefinetly until the user
            // shuts the server down
            while (true) {
                // Attempt to accept the client. Listening for a TCP connection request
                Socket clientSocket = server.accept();
                if (counter < 3) {
                    counter++;

                    Thread thread = new Thread(new HttpRequest(clientSocket));
                    threadPool.execute(thread);
                }
            }
        }
    }
}

final class HttpRequest implements Runnable {
    private static String htmlFile = "./client/indexfake.html";
    private static String cssFile = "./notFoundHTML/stlyes.css";
    private static String htmlFileTwo = "./client/genericfake.html";
    private static int round = 1;
    final static String CRLF = "\r\n";
    private Socket socket;

    // Constructor
    public HttpRequest(Socket clientSocket) {
        this.socket = clientSocket;
    }

    @Override
    public void run() {
        try {
            System.out.println(Thread.currentThread().getName() + ", executing run() method!");
            processRequest();
            System.out.println("Request Processed");
        } catch (Exception ex) {
            System.out.println(ex);
        }
    }

    private void processRequest() throws IOException {
        System.out.println("Connection opened. (" + new Date() + ")");

        BufferedReader socketInputReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        String requestLine = socketInputReader.readLine();

        // Display the request line
        System.out.println("Request Line : " + requestLine);

        // Display the header lines
        String headerLine = null;
        while ((headerLine = socketInputReader.readLine()).length() != 0) {
            System.out.println(headerLine);
        }
        Path filePath = null;
        if (round == 1) {
            filePath = Paths.get(htmlFile);
        } else if (round == 2) {
            filePath = Paths.get(htmlFileTwo);
        } else if (round == 3) {
            filePath = Paths.get(cssFile);
        }
        round++;

        Path notFoundPath = Paths.get("./NotFoundHTML/notFound.html");

        // Construct the response message.
        if (Files.exists(filePath)) {
            String contentType = "Content-type: " + guessContentType(filePath) + CRLF;
            System.out.println(filePath);
            System.out.println("Content Type is " + contentType);
            sendResponse(socket, HTTPServer.successStatusCode, contentType, Files.readAllBytes(filePath));
        } else {
            String contentType = "Content-type: " + guessContentType(notFoundPath) + CRLF;
            sendResponse(socket, HTTPServer.notFoundStatusCode, contentType, Files.readAllBytes(notFoundPath));
        }
    }

    /**
     * Optimizing the process of sending a reponse to the browser
     */
    private static void sendResponse(Socket clientSocket, String status, String contentType, byte[] content) {
        try {
            OutputStream clientOutput = clientSocket.getOutputStream();
            clientOutput.write(("HTTP/1.1 \r\n" + status).getBytes());
            clientOutput.write(("ContentType: " + contentType).getBytes());
            clientOutput.write(content);
            clientOutput.write(CRLF.getBytes());
            clientOutput.flush();
            clientOutput.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * We need to tell the browser what kind of content we are sending.
     */
    private static String guessContentType(Path filePath) throws IOException {
        return Files.probeContentType(filePath);
    }
}