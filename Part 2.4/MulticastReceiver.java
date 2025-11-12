import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.NetworkInterface;

/*
 * multicast receiver
 * - joins a multicast group and listens for datagrams
 * - several receivers can join the same group and receive the same message
 * To compile: javac MulticastReceiver.java MulticastSender.java
 * TO RUN IT after compiling it: java MulticastReceiver 230.0.0.1 8888
 */

public class MulticastReceiver {
    public static void main(String[] args) {
        String group = (args.length > 0) ? args[0] : "224.0.0.3";  // local multicast address
        int port = (args.length > 1) ? Integer.parseInt(args[1]) : 8888;

        try (MulticastSocket socket = new MulticastSocket(port)) {
            InetAddress groupAddr = InetAddress.getByName(group);
            NetworkInterface loopback = NetworkInterface.getByInetAddress(InetAddress.getByName("127.0.0.1"));

            socket.setNetworkInterface(loopback);  // force loopback interface
            socket.joinGroup(groupAddr);
            System.out.println("📡 joined multicast group " + group + " on port " + port + " (loopback interface)");

            byte[] buffer = new byte[1024];

            while (true) {
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);
                String msg = new String(packet.getData(), 0, packet.getLength(), "UTF-8");
                System.out.println("📥 received: " + msg + " from " + packet.getAddress().getHostAddress());
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}