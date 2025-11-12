import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

/*
 * TO RUN IT:
 * - sends a single datagram with a chosen payload size
 * - use sizes to probe server buffers: like 1400, 3000, 65000
 *   to test: java ReliableUDPClient_2_2 <host> <port> <payload-size> 
 *      (one test shown in the report pdf)
 */

public class ReliableUDPClient_2_2 {

    public static void main(String[] args) {
        if (args.length < 3) {
            System.out.println("usage: java ReliableUDPClient_2_2 <host> <port> <payload-size>");
            System.out.println("example: java ReliableUDPClient_2_2 localhost 8080 1400");
            return;
        }

        String host = args[0];
        int port = Integer.parseInt(args[1]);
        int payloadSize = Integer.parseInt(args[2]);

        try (DatagramSocket socket = new DatagramSocket()) {
            InetAddress addr = InetAddress.getByName(host);

            // build a payload of given size filled with 'A'
            byte[] data = new byte[payloadSize];
            for (int i = 0; i < data.length; i++) data[i] = 'A';

            System.out.println("sending udp datagram: payload=" + data.length + " bytes");
            DatagramPacket packet = new DatagramPacket(data, data.length, addr, port);
            socket.send(packet);
            System.out.println("✅ sent");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
