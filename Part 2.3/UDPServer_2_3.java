import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

/*
 * simple UDP server to demonstrate connectionless communication
 * TO USE IT: java UDPServer_2_3 8080
 */

public class UDPServer_2_3 {
    public static void main(String[] args) {
        int port = (args.length > 0) ? Integer.parseInt(args[0]) : 8080;

        try (DatagramSocket socket = new DatagramSocket(port)) {
            System.out.println("UDP server running on port " + port + " (connectionless mode)");

            byte[] buffer = new byte[1024];

            while (true) {
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);

                InetAddress addr = packet.getAddress();
                int portFrom = packet.getPort();
                String message = new String(packet.getData(), 0, packet.getLength(), "UTF-8");

                System.out.println("📩 received from " + addr.getHostAddress() + ":" + portFrom + " -> " + message);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
