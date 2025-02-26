import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class TCPClient {
    
    public TCPClient() {
    }

    public byte[] askServer(String hostname, int port, byte [] toServerBytes) throws IOException {
        Socket socket = new Socket(hostname, port);
        System.out.println("Server connected successfully!");

        OutputStream outputStream = socket.getOutputStream();
        InputStream inputStream = socket.getInputStream();
        ByteArrayOutputStream serverMessage = new ByteArrayOutputStream();

        outputStream.write(toServerBytes);
        outputStream.flush();

        byte[] buffer = new byte[1024];
        int bytes;

        while ((bytes = inputStream.read(buffer)) != -1) {
            serverMessage.write(buffer, 0, bytes);
        }

        inputStream.close();
        outputStream.close();
        socket.close();
        return serverMessage.toByteArray();
    }

    public byte[] askServer(String hostname, int port) throws IOException {
        Socket socket = new Socket(hostname, port);
        System.out.println("Server connected successfully!");

        InputStream inputStream = socket.getInputStream();
        ByteArrayOutputStream serverMessage = new ByteArrayOutputStream();

        byte[] buffer = new byte[1024];

        int bytes;

        while ((bytes = inputStream.read(buffer)) != -1) {
            serverMessage.write(buffer, 0, bytes);
        }

        inputStream.close();
        socket.close();
        return serverMessage.toByteArray();
    }
}
