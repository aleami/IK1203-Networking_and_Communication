import java.net.*;

public class ConcHTTPAsk {
    public static void main(String[] args) throws Exception {
        if (args.length < 1) {
            System.err.println("Error: Port argument is missing!");
            return;
        }

        int port = Integer.parseInt(args[0]);
        ServerSocket serverSocket = new ServerSocket(port);

        while (true) {
            Socket clientSocket = serverSocket.accept();
            System.out.println("New client connected: " + clientSocket.getInetAddress());

            Thread clientThread = new Thread(new MyRunnable(clientSocket));
            clientThread.start();
        }
    }
}
