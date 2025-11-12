import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

/*
 * TO RUN IT:
 * - We have fixed buffers:
 *   small=64, large=2048, huge=65507
 * - choose the buffer with arg #2: small | large | huge
 *   to test: java ReliableUDPServer_2_2 <port> <small|large|huge>
 *       (one test shown in the report pdf)
 */

public class ReliableUDPServer_2_2 {

    public static void main(String[] args) {
        int port = (args.length > 0) ? Integer.parseInt(args[0]) : 8080;
        String which = (args.length > 1) ? args[1].toLowerCase() : "large";

        // test different buffer sizes as requested
        byte[] smallBuffer = new byte[64];     // too small
        byte[] largeBuffer = new byte[2048];   // standard
        byte[] hugeBuffer  = new byte[65507];  // max udp payload

        byte[] active;
        switch (which) {
            case "small": active = smallBuffer; break;
            case "huge":  active = hugeBuffer;  break;
            default:      active = largeBuffer; which = "large";
        }

        try (DatagramSocket socket = new DatagramSocket(port)) {
            System.out.println("udp server on port " + port + " using " + which + " buffer (" + active.length + " bytes)");

            while (true) {
                // create a packet with the selected buffer
                DatagramPacket packet = new DatagramPacket(active, active.length);
                socket.receive(packet);

                InetAddress clientAddr = packet.getAddress();
                int clientPort = packet.getPort();
                int receivedLen = packet.getLength();

                System.out.println("\n📦 from " + clientAddr.getHostAddress() + ":" + clientPort);
                System.out.println("  -> bytes received: " + receivedLen);
                System.out.println("  -> buffer capacity: " + active.length);

                // preview the first bytes so we can see content
                int preview = Math.min(receivedLen, 60);
                String head = new String(packet.getData(), 0, preview, "UTF-8");
                System.out.println("  -> payload (first " + preview + " bytes): " + head.replace("\n", "\\n"));

                // simple truncation heuristic: if received == buffer size,
                // the datagram may have been larger and got truncated
                if (receivedLen == active.length) {
                    System.out.println("⚠ truncation likely: message may be larger than buffer");
                } else {
                    System.out.println("✅ no truncation observed");
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}