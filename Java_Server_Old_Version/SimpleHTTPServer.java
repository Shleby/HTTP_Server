import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;

/**
 * Java program to create a simple HTTP Server
 * 
 * @author Shelby Huffman
 */
public class SimpleHTTPServer {
    private static final int PORT = 8080;
    private static final String successStatusCode = "200 OK"; // Used for Get, Head, Post, and Trace methods
    private static final String createdStatusCode = "201 Created"; // Used for Post requests
    private static final String notFoundStatusCode = "404 Not Found"; // Used when server cannot find requested resource

    public static void main(String[] args) throws IOException {

        // Create a server on port 8080
        final ServerSocket server = new ServerSocket(PORT);

        System.out.println("Server Started.\nListening for connection on port " + PORT + " . . .");

        // Create an infinite loop for our server to run
        while (true) {
            // Attempt to accept client connection to the server
            try (final Socket clientSocket = server.accept()) {
                System.out.println("Connection opened. (" + new Date() + ")");

                // Read the input stream
                InputStreamReader isr = new InputStreamReader(clientSocket.getInputStream());
                BufferedReader reader = new BufferedReader(isr);
                String line = reader.readLine();

                // Print out the response
                while (!line.isEmpty()) {
                    System.out.println(line);
                    line = reader.readLine();
                }

                // Create today's Date
                Date today = new Date();

                Path filePath = Paths.get("./client/index.html");

                if (Files.exists(filePath)) {
                    String contentType = guessContentType(filePath);
                    sendResponse(clientSocket, successStatusCode, contentType, Files.readAllBytes(filePath));
                } else {
                    // 404
                    byte[] contentNotFound = "<h1>Not found : (</h1>".getBytes();
                    sendResponse(clientSocket, notFoundStatusCode, "text/html", contentNotFound);
                }
            } catch (IOException ex) {
                System.err.println("Server Connection error : " + ex.getMessage());
            }
        }
    }

    /**
     * Optimizing the process of sending a reponse to the browser
     * 
     * @param client
     * @param status
     * @param contentType
     * @param content
     * @throws IOException
     */
    private static void sendResponse(Socket client, String status, String contentType, byte[] content)
            throws IOException {
        OutputStream clientOutput = client.getOutputStream();
        clientOutput.write(("HTTP/1.1 \r\n" + status).getBytes());
        clientOutput.write(("ContentType: " + contentType + "\r\n").getBytes());
        clientOutput.write("\r\n".getBytes());
        clientOutput.write(content);
        clientOutput.write("\r\n\r\n".getBytes());
        clientOutput.flush();
        client.close();
    }

    /**
     * We need to tell the browser what kind of content we are sending.
     * 
     * @param filePath
     * @return the type of content we are sending
     * @throws IOException
     */
    private static String guessContentType(Path filePath) throws IOException {
        return Files.probeContentType(filePath);
    }
}
