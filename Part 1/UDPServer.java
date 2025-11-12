import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class UDPServer {

    private int port;

    // Constructor with port number
    public UDPServer(int port) {
        this.port = port;
    }

    // Constructor default (port 8080)
    public UDPServer() {
        this.port = 8080;
    }

    // Iniciate the server
    public void launch() {
        try (DatagramSocket socket = new DatagramSocket(port)) {
            System.out.println("UDP Server running on port " + port + "...");

            byte[] buffer = new byte[1024];

            while (true) {
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet); // Espera receber mensagem

                String message = new String(packet.getData(), 0, packet.getLength(), "UTF-8");
                InetAddress clientAddress = packet.getAddress();
                int clientPort = packet.getPort();

                System.out.println("Received from " + clientAddress + ":" + clientPort + " -> " + message);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String toString() {
        return "UDPServer running on port " + port;
    }

    public static void main(String[] args) {
        int port = (args.length > 0) ? Integer.parseInt(args[0]) : 8080;
        UDPServer server = new UDPServer(port);
        server.launch();
    }
}
