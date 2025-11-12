import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class ReliableUDPServer {

    public static void main(String[] args) {
        // use port 8080 if no argument is given
        int port = (args.length > 0) ? Integer.parseInt(args[0]) : 8080;

        try (DatagramSocket socket = new DatagramSocket(port)) {
            System.out.println("reliable UDP server running on port " + port + "...");
            byte[] buffer = new byte[1024];
            int lastSeq = -1; // store the last received sequence number

            while (true) {
                // wait to receive a UDP datagram
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);

                // convert the received bytes into a UTF-8 string
                String received = new String(packet.getData(), 0, packet.getLength(), "UTF-8");

                // "seq:message"
                String[] parts = received.split(":", 2);
                int seq = Integer.parseInt(parts[0]);
                String message = parts.length > 1 ? parts[1] : "";

                // check if packets are missing or duplicated
                if (seq > lastSeq + 1) {
                    System.out.println("missing packets detected (expected " + (lastSeq + 1) + ")");
                } else if (seq <= lastSeq) {
                    System.out.println("duplicate or out-of-order packet seq=" + seq);
                }

                // print received message
                InetAddress clientAddr = packet.getAddress();
                int clientPort = packet.getPort();
                System.out.println("received seq=" + seq + " from " + clientAddr + ":" + clientPort + " -> " + message);
                lastSeq = seq;

                // send ack back to the client
                String ackMsg = "ACK:" + seq;
                byte[] ackData = ackMsg.getBytes("UTF-8");
                DatagramPacket ackPacket = new DatagramPacket(ackData, ackData.length, clientAddr, clientPort);
                socket.send(ackPacket);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
