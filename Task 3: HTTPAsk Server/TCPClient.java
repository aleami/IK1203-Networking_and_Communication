import java.net.*;
import java.io.*;

public class TCPClient {
    boolean shutdown;
    Integer timeout;
    Integer limit;


    public TCPClient(boolean shutdown, Integer timeout, Integer limit) {
        this.shutdown = shutdown;
        this.timeout = timeout;
        this.limit = limit;
    }

    public byte[] askServer(String hostname, int port, byte [] toServerBytes) throws IOException {
        Socket socket = new Socket(hostname, port);
        System.out.println("Server connected successfully!");

        if (timeout != null) {
            socket.setSoTimeout(timeout);
            System.out.println("Timeout set to: " + timeout + " ms");
        } else {
            socket.setSoTimeout(10000);
            System.out.println("Default timeout set to 10000 ms");
        }

        OutputStream outputStream = socket.getOutputStream();
        InputStream inputStream = socket.getInputStream();
        ByteArrayOutputStream serverMessage = new ByteArrayOutputStream();

        System.out.println("Sending data to server: " + new String(toServerBytes));
        outputStream.write(toServerBytes);
        outputStream.flush();

        byte[] buffer = new byte[1024];
        int bytes;
        int dataCount = 0;

        try {
            while ((bytes = inputStream.read(buffer)) != -1 && (limit == null || dataCount < limit)) {
                int outputBytes = bytes;
                if (limit != null && dataCount + bytes > limit) {
                    outputBytes = limit - dataCount;
                }

                serverMessage.write(buffer, 0, outputBytes);
                dataCount += outputBytes;
            }
        } catch (SocketTimeoutException e) {
            System.out.println("Timeout!");
        }

        byte[] endMessage = serverMessage.toByteArray();

        if (endMessage.length > 0) {
            System.out.println("Server response received: " + new String(endMessage));
        } else {
            System.out.println("Server response: <No Data Received>");
        }

        if (shutdown) {
            System.out.println("Output stream Shutdown...");
            socket.shutdownOutput();
        }

        inputStream.close();
        outputStream.close();
        socket.close();
        System.out.println("Connection closed......");
        return endMessage;
    }
}
