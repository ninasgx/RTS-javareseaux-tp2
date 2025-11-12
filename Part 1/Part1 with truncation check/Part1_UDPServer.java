import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;

public class Part1_UDPServer {

    public static final int DEFAULT_PORT = 8080;
    private static final int MAX_BYTES = 1024;

    private final int port;
    private DatagramSocket socket;

    public Part1_UDPServer(int port) {
        this.port = port;
    }

    public Part1_UDPServer() {
        this(DEFAULT_PORT);
    }

    public void launch() {
        try {
            socket = new DatagramSocket(new InetSocketAddress(port));
            System.out.println("[UDPServer] Listening on port " + port + " ...");

            // single-threaded loop (as required)
            while (true) {
                byte[] buf = new byte[MAX_BYTES]; // 1024-byte buffer (truncation beyond this)
                DatagramPacket packet = new DatagramPacket(buf, buf.length);
                socket.receive(packet);

                int length = packet.getLength(); // actual received length (<= 1024)
                String client = packet.getAddress().getHostAddress() + ":" + packet.getPort();

                // decode as UTF-8
                String msg = new String(packet.getData(), 0, length, StandardCharsets.UTF_8);

                if (length >= MAX_BYTES) {
                    System.out.println("[UDPServer][INFO] Received " + length + " bytes (buffer is " + MAX_BYTES + "); data beyond this was truncated.");
                }

                System.out.println("[" + client + "] " + msg);
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

    @Override
    public String toString() {
        boolean bound = (socket != null && socket.isBound());
        String local = (socket != null && socket.isBound())
                ? socket.getLocalAddress().getHostAddress() + ":" + socket.getLocalPort()
                : "unbound";
        return "Part1_UDPServer{port=" + port + ", bound=" + bound + ", local=" + local + "}";
    }

    public static void main(String[] args) {
        int port = DEFAULT_PORT;
        if (args != null && args.length > 0) {
            try {
                port = Integer.parseInt(args[0]);
            } catch (NumberFormatException nfe) {
                System.err.println("[UDPServer] Invalid port. Using default " + DEFAULT_PORT);
            }
        }
        Part1_UDPServer server = new Part1_UDPServer(port);
        System.out.println(server.toString());
        server.launch();
    }
}
