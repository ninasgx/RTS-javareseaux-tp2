import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;

public class UDPServer {

    public static final int DEFAULT_PORT = 8080;
    private static final int MAX_BYTES = 1024;

    private final int port;
    private DatagramSocket socket;

    public UDPServer(int port) {
        this.port = port;
    }

    public UDPServer() {
        this(DEFAULT_PORT);
    }

    public void launch() {
        try {
            socket = new DatagramSocket(new InetSocketAddress(port));
            System.out.println("[UDPServer] Listening on port " + port + " ...");

            byte[] buf = new byte[MAX_BYTES];

            while (true) {
                DatagramPacket packet = new DatagramPacket(buf, buf.length);
                socket.receive(packet);

                int length = packet.getLength();
                String client = packet.getAddress().getHostAddress() + ":" + packet.getPort();
                System.out.println("[UDPServer] Received bytes: " + length);

                if (length >= MAX_BYTES) {
                    System.out.println("[UDPServer][WARNING] Message exceeded " + MAX_BYTES + " bytes and may have been truncated.");
                }

                String msg = new String(packet.getData(), 0, length, StandardCharsets.UTF_8);
                System.out.println("[" + client + "] " + msg);

                // Parse sequence number if present: "<seq>:<payload>"
                String ackSeq = parseSeq(msg);
                if (ackSeq != null) {

                    // Extract payload (text after ':')
                    String payload = "";
                    int colon = msg.indexOf(':');
                    if (colon >= 0 && colon + 1 < msg.length()) {
                        payload = msg.substring(colon + 1);
                    }

                    // Normalize payload: remove CR/LF and trim spaces
                    String payloadTrim = payload.replace("\r", "").replace("\n", "").trim();

                    String ack = "ACK " + ackSeq;
                    byte[] ackData = ack.getBytes(StandardCharsets.UTF_8);
                    DatagramPacket ackPacket = new DatagramPacket(
                            ackData, ackData.length, packet.getAddress(), packet.getPort());

                    // Intentionally drop ACK when payload == "testloss"
                    if (payloadTrim.equals("testloss")) {
                        System.out.println("[UDPServer] (Simulated) drop ACK for seq " + ackSeq + " (payload == testloss)");
                        // Do not send ACK
                    } else {
                        socket.send(ackPacket);
                        System.out.println("[UDPServer] Sent " + ack + " to " + client);
                    }
                }
            }

        } catch (SocketException se) {
            System.err.println("[UDPServer] Socket error: " + se.getMessage());
        } catch (IOException ioe) {
            System.err.println("[UDPServer] I/O error: " + ioe.getMessage());
        } finally {
            if (socket != null && !socket.isClosed()) {
                socket.close();
                System.out.println("[UDPServer] Socket closed.");
            }
        }
    }

    private String parseSeq(String msg) {
        int idx = msg.indexOf(':');
        if (idx <= 0) {
            return null;
        }
        String left = msg.substring(0, idx).trim();
        for (int i = 0; i < left.length(); i++) {
            if (!Character.isDigit(left.charAt(i))) {
                return null;
            }
        }
        return left;
    }

    @Override
    public String toString() {
        boolean bound = (socket != null && socket.isBound());
        String local = (socket != null && socket.isBound())
                ? socket.getLocalAddress().getHostAddress() + ":" + socket.getLocalPort()
                : "unbound";
        return "UDPServer{port=" + port + ", bound=" + bound + ", local=" + local + "}";
    }

    public static void main(String[] args) {
        int port = DEFAULT_PORT;
        if (args != null && args.length > 0) {
            try {
                port = Integer.parseInt(args[0]);
            } catch (NumberFormatException nfe) {
                System.err.println("[UDPServer] Invalid port, using default " + DEFAULT_PORT);
            }
        }
        UDPServer server = new UDPServer(port);
        System.out.println(server.toString());
        server.launch();
    }
}
