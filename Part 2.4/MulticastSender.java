import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.MulticastSocket;

/*
 * multicast sender
 * - sends datagrams to a multicast group
 * - all receivers that joined the group will receive the message
 * To compile: javac MulticastReceiver.java MulticastSender.java
 * TO RUN IT after compiling it java MulticastSender 230.0.0.1 8888 "hello!"
 */

public class MulticastSender {
    public static void main(String[] args) {
        if (args.length < 3) {
            System.out.println("usage: java MulticastSender <group> <port> <message>");
            return;
        }

        String group = args[0];
        int port = Integer.parseInt(args[1]);
        String message = args[2];

        try (MulticastSocket socket = new MulticastSocket()) {
            InetAddress groupAddr = InetAddress.getByName(group);
            NetworkInterface loopback = NetworkInterface.getByInetAddress(InetAddress.getByName("127.0.0.1"));
            socket.setNetworkInterface(loopback);  // force local interface

            byte[] data = message.getBytes("UTF-8");
            DatagramPacket packet = new DatagramPacket(data, data.length, groupAddr, port);
            socket.send(packet);
            System.out.println("📤 multicast message sent to group " + group + ": " + message);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
