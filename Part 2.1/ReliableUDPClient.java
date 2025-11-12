import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Random;

public class ReliableUDPClient {

    public static void main(String[] args) {
        // check for command-line arguments
        if (args.length < 2) {
            System.out.println("usage: java ReliableUDPClient <hostname> <port>");
            return;
        }

        String hostname = args[0];
        int port = Integer.parseInt(args[1]);

        try (DatagramSocket socket = new DatagramSocket()) {
            socket.setSoTimeout(500); // timeout waiting for ACK in milliseconds
            InetAddress serverAddr = InetAddress.getByName(hostname);
            Random random = new Random();

            int totalMessages = 10;      // total number of packets to send
            double lossProbability = 0.2; // simulate 20% packet loss

            int sent = 0;
            int acked = 0;

            for (int seq = 0; seq < totalMessages; seq++) {
                // "seq:content"
                String msg = seq + ":Hello from client message " + seq;
                byte[] data = msg.getBytes("UTF-8");

                // simulate random packet loss, we do not send some packets
                if (random.nextDouble() < lossProbability) {
                    System.out.println("🚫 simulated packet loss (seq=" + seq + ")");
                    continue;
                }

                // send the UDP packet to the server
                DatagramPacket packet = new DatagramPacket(data, data.length, serverAddr, port);
                socket.send(packet);
                sent++;
                System.out.println("📤 sent seq=" + seq);

                // wait for acknowledgment
                byte[] ackBuffer = new byte[1024];
                DatagramPacket ackPacket = new DatagramPacket(ackBuffer, ackBuffer.length);

                try {
                    socket.receive(ackPacket); // wait for ACK from server
                    String ack = new String(ackPacket.getData(), 0, ackPacket.getLength(), "UTF-8");

                    // verify if the ACK matches the sent sequence
                    if (ack.equals("ACK:" + seq)) {
                        System.out.println("✅ received " + ack);
                        acked++;
                    }
                } catch (Exception e) {
                    // no ACK received, timeout
                    System.out.println("⏰ no ACK for seq=" + seq + " (timeout)");
                }

                // short delay between packets
                Thread.sleep(300);
            }

            // print a simple transmission summary
            System.out.println("\n📊 summary: sent=" + sent + ", acked=" + acked + ", lost=" + (sent - acked));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
