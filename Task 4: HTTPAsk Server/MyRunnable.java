import java.net.*;
import java.io.*;

public class MyRunnable implements Runnable {
    private final Socket socket;
    private String hostname = null;
    private int port = 0;
    private String string = "";
    private boolean shutdown = false;
    private Integer timeout = null;
    private Integer limit = null;

    public MyRunnable(Socket socket) {
        this.socket = socket;
    }

    public void run() {
        try {
            InputStream inputStream = socket.getInputStream();
            OutputStream outputStream = socket.getOutputStream();
            ByteArrayOutputStream dataOut = new ByteArrayOutputStream();

            byte[] buffer = new byte[1024];
            int bytes;

            // Read first line of HTTP request
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

            // Check the request format
            if (!line.startsWith("GET") || !line.contains("HTTP/1.1")) {
                answer(outputStream, "400 Bad Request", "Bad Request".getBytes());
                socket.close();
                return;
            }

            String[] reqSplit = line.split(" ");

            if (reqSplit.length < 2 || !reqSplit[1].startsWith("/ask")) {
                answer(outputStream, "404 Not Found", "Not Found".getBytes());
                socket.close();
                return;
            }

            String arg = "";
            if (reqSplit[1].length() > 5) {
                arg = reqSplit[1].substring(5);
            }
            if (!arg.isEmpty()) {
                parseArgs(arg);
            } else {
                answer(outputStream, "400 Bad Request", "Bad request".getBytes());
                socket.close();
                return;
            }

            if (port <= 0 || hostname == null || hostname.isEmpty()) {
                answer(outputStream, "400 Bad Request", "Invalid request".getBytes());
                socket.close();
                return;
            }

            byte[] serverMessage;

            try {
                TCPClient client = new TCPClient(shutdown, timeout, limit);
                serverMessage = client.askServer(hostname, port, string.getBytes());
                if (serverMessage == null || serverMessage.length == 0) {
                    answer(outputStream, "400 Bad Request", "Bad request.".getBytes());
                } else {
                    answer(outputStream, "200 OK", serverMessage);
                }
            } catch (Exception e) {
                System.err.println("Error: " + e.getMessage());
                answer(outputStream, "400 Bad Request", "Bad request!".getBytes());
                socket.close();
                return;
            }

            // Handle server shutdown
            if (shutdown) {
                System.out.println("Server is shutting down...");
                answer(outputStream, "200 OK", "Server shutdown...".getBytes());
                socket.close();
                System.exit(0);
            }

            socket.close();

        } catch (IOException e) {
            System.err.println("Error: " + e.getMessage());
        }
    }

    private void parseArgs(String inputData) {
        for (String arg : inputData.split("&")) {
            String[] keyVal = arg.split("=", 2);
            if (keyVal.length == 2) {
                switch (keyVal[0]) {
                    case "shutdown":
                        shutdown = Boolean.parseBoolean(keyVal[1]);
                        break;
                    case "timeout":
                        timeout = Integer.parseInt(keyVal[1]);
                        break;
                    case "limit":
                        limit = Integer.parseInt(keyVal[1]);
                        break;
                    case "string":
                        string = keyVal[1] + "\n";
                        break;
                    case "hostname":
                        hostname = keyVal[1];
                        break;
                    case "port":
                        port = Integer.parseInt(keyVal[1]);
                        break;
                }
            }
        }
    }

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
}
