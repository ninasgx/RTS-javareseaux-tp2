import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

/*
 * simple UDP client for connectionless test
 * TO USE IT: java UDPClient_2_3 localhost 8080 "from client 1"
 */

public class UDPClient_2_3 {
    public static void main(String[] args) {
        if (args.length < 3) {
            System.out.println("usage: java UDPClient_2_3 <host> <port> <message>");
            return;
        }

        String host = args[0];
        int port = Integer.parseInt(args[1]);
        String message = args[2];

        try (DatagramSocket socket = new DatagramSocket()) {
            InetAddress serverAddr = InetAddress.getByName(host);
            byte[] data = message.getBytes("UTF-8");
            DatagramPacket packet = new DatagramPacket(data, data.length, serverAddr, port);
            socket.send(packet);
            System.out.println("📤 sent message: " + message);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
