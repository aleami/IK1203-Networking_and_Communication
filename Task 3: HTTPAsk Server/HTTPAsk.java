import java.net.*;
import java.io.*;

public class HTTPAsk {
    static String hostname = null;
    static int Port = 0;
    static String string = "";
    static boolean shutdown = false;
    static Integer timeout = null;
    static Integer limit = null;


     //Handles and processes parameters from the HTTP request.
    private static void parseArgs(String inputData) {
        for (String arg : inputData.split("&")) {
            String[] keyVal = arg.split("=", 2);
            if (keyVal.length == 2) {
                switch (keyVal[0]) {
                    case "shutdown":
                        shutdown = Boolean.parseBoolean(keyVal[1]);
                        break;
                    case "timeout":
                        try {
                            timeout = Integer.parseInt(keyVal[1]);
                        } catch (NumberFormatException e) {
                            throw new IllegalArgumentException("Invalid timeout");
                        }
                        break;
                    case "limit":
                        try {
                            limit = Integer.parseInt(keyVal[1]);
                        } catch (NumberFormatException e) {
                            throw new IllegalArgumentException("Invalid limit");
                        }
                        break;
                    case "string":
                        string = keyVal[1] + "\n";
                        break;
                    case "hostname":
                        hostname = keyVal[1];
                        break;
                    case "port":
                        try {
                            Port = Integer.parseInt(keyVal[1]);
                        } catch (NumberFormatException e) {
                            throw new IllegalArgumentException("Invalid port");
                        }
                        break;
                }
            }
        }
    }


     //Sends an HTTP response
    private static void answer(OutputStream out, String reply, byte[] data) throws IOException {
        String text;
        if (data != null && data.length > 0) {
            text = new String(data);
        } else {
            text = "";
        }
        String message = "HTTP/1.1 " + reply + "\r\n" +
                "Content-Type: text/plain\r\n" +
                "Content-Length: " + text.length() + "\r\n" +
                "Connection: close\r\n" +
                "\r\n" +
                text +
                "\r\n";

        System.out.println("Message: " + message);
        out.write(message.getBytes());
        out.flush();
    }

    //Starts the HTTP server and handles connections.
    public static void main(String[] args) throws Exception {
        if (args.length < 1) {
            System.err.println("Error: Port argument is missing!");
            return;
        }

        int port = Integer.parseInt(args[0]);
        ServerSocket serverSocket = new ServerSocket(port);

        while (true) {
            Socket socket = serverSocket.accept();
            System.out.println("Client connected: " + socket.getInetAddress());
            InputStream inputStream = socket.getInputStream();
            OutputStream outputStream = socket.getOutputStream();
            ByteArrayOutputStream dataOut = new ByteArrayOutputStream();

            byte[] buffer = new byte[1024];
            int bytes;

            while ((bytes = inputStream.read(buffer)) != -1) {
                dataOut.write(buffer, 0, bytes);
                String curr = dataOut.toString();
                if (curr.contains("\r\n")) {
                    break;
                }
            }

            String dataReq = dataOut.toString();
            int end = dataReq.indexOf("\r\n");

            String line;

            if (end != -1) {
                line = dataReq.substring(0, end);
            } else {
                line = dataReq;
            }

            System.out.println("Request line: " + line);

            // Check the request format
            if (!line.startsWith("GET") || !line.contains("HTTP/1.1")) {
                answer(outputStream, "400 Bad Request", "Bad Request".getBytes());
                socket.close();
                continue;
            }

            String[] reqSplit = line.split(" ");

            //Confirms that the request URL matches the expected format
            String reqPath = reqSplit[1];
            if (!reqPath.startsWith("/ask")) {
                answer(outputStream, "404 Not Found", "Not Found".getBytes());
                socket.close();
                continue;
            }

            // Extract query parameters and parse them
            String arg = reqSplit[1].substring(5);
            if (!arg.isEmpty()) {
                parseArgs(arg);
            } else {
                answer(outputStream, "400 Bad Request", "Bad request".getBytes());
                socket.close();
                continue;
            }

            // Checking if hostname and port are set
            if (Port <= 0 || hostname == null || hostname.isEmpty()) {
                answer(outputStream, "400 Bad Request", "Invalid request".getBytes());
                socket.close();
                continue;
            }


            String httpReq = "GET / HTTP/1.1\r\n" +
                    "Host: " + hostname + ":" + Port + "\r\n" +
                    "Connection: close\r\n\r\n";

            byte[] toServerBytes = httpReq.getBytes();
            System.out.println(httpReq);

            byte[] serverMessage;

            try {
                TCPClient client = new TCPClient(false, timeout, limit);
                serverMessage = client.askServer(hostname, Port, toServerBytes);
                if (serverMessage == null || serverMessage.length == 0) {
                    answer(outputStream, "400 Bad request", "Bad request.".getBytes());
                } else {
                    answer(outputStream, "200 OK", serverMessage);
                }
            } catch (Exception e) {
                System.err.println("Error" + e.getMessage());
                answer(outputStream, "400 Not Found", "Bad request!".getBytes());
                socket.close();
                continue;
            }

            // Handle server shutdown
            if (shutdown) {
                System.out.println("Server is shutting down...");
                answer(outputStream, "200 OK", "Server shutdown...".getBytes());
                serverSocket.close();
                System.exit(0);
            }

            // Close connections
            inputStream.close();
            outputStream.close();
            socket.close();
        }
    }
}
