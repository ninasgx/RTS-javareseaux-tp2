import java.io.BufferedReader;
import java.io.Console;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;

public class Part1_UDPClient {

    public static void main(String[] args) {
        if (args == null || args.length < 2) {
            System.err.println("Usage: java Part1_UDPClient <serverHost> <port>");
            return;
        }

        String host = args[0];
        int port;
        try {
            port = Integer.parseInt(args[1]);
        } catch (NumberFormatException nfe) {
            System.err.println("Invalid port: " + args[1]);
            return;
        }

        Console console = System.console();
        BufferedReader br = null;
        if (console == null) {
            // VS Code / IDE often returns null console: fallback to STDIN
            br = new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8));
            System.out.println("[UDPClient] Console unavailable; using STDIN reader. (Ctrl-D/Ctrl-Z to end)");
        } else {
            System.out.println("[UDPClient] Type lines to send. (Ctrl-D to end)");
        }

        try (DatagramSocket socket = new DatagramSocket()) {
            InetAddress serverAddr = InetAddress.getByName(host);

            while (true) {
                String line;
                if (console != null) {
                    line = console.readLine();
                } else {
                    line = br.readLine();
                }
                if (line == null) {
                    System.out.println("[UDPClient] EOF detected. Exiting...");
                    break;
                }

                byte[] data = line.getBytes(StandardCharsets.UTF_8); // UTF-8 encode
                DatagramPacket packet = new DatagramPacket(data, data.length, serverAddr, port);
                socket.send(packet);

                // optional: warn if likely to be truncated by server
                if (data.length > 1024) {
                    System.out.println("[UDPClient][INFO] Sent " + data.length + " bytes; server buffer is typically 1024, so it may truncate.");
                }
            }
        } catch (IOException ioe) {
            System.err.println("[UDPClient] I/O error: " + ioe.getMessage());
        }
    }
}
