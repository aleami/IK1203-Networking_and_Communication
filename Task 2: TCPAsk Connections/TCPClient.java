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
        }

        OutputStream outputStream = socket.getOutputStream();
        InputStream inputStream = socket.getInputStream();
        ByteArrayOutputStream serverMessage = new ByteArrayOutputStream();

        outputStream.write(toServerBytes);
        outputStream.flush();

        if (shutdown) {
            System.out.println("Shutdown...");
            socket.shutdownOutput();
        }

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
            System.out.println("Timeout...");
        }

        inputStream.close();
        outputStream.close();
        socket.close();
        return serverMessage.toByteArray();
    }
}
