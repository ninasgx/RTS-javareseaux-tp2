import java.io.BufferedReader;
import java.io.Console;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;

public class UDPClient {

    private static final int MAX_RETRIES = 3;       // resend times on timeout
    private static final int ACK_TIMEOUT_MS = 1000; // 1s

    // print summary in one place (we'll call it on EOF and in finally)
    private static void printSummary(int sent, int acked, int retransmissions) {
        int lost = sent - acked;
        double lossRate = sent == 0 ? 0.0 : (100.0 * lost / sent);
        System.out.println("[UDPClient] Summary -> sent=" + sent + ", acked=" + acked
                + ", lost(no-ack)=" + lost + ", retransmissions=" + retransmissions
                + ", lossRate=" + String.format("%.2f", lossRate) + "%");
        System.out.flush();
    }

    public static void main(String[] args) {
        if (args == null || args.length < 2) {
            System.err.println("Usage: java UDPClient <serverHost> <port>");
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
            br = new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8));
            System.out.println("[UDPClient] Console unavailable; using STDIN reader. (Ctrl-D/Ctrl-Z to end)");
        } else {
            System.out.println("[UDPClient] Type lines to send. (Ctrl-D to end)");
        }

        int seq = 0;
        int sent = 0;
        int acked = 0;
        int retransmissions = 0;

        try (DatagramSocket socket = new DatagramSocket()) {
            socket.setSoTimeout(ACK_TIMEOUT_MS);
            InetAddress serverAddr = InetAddress.getByName(host);
            System.out.println("[UDPClient] Sending to " + host + ":" + port + " (Ctrl-D/Ctrl-Z end)");

            while (true) {
                String line = (console != null) ? console.readLine() : br.readLine();
                if (line == null) {
                    System.out.println("[UDPClient] EOF detected. Exiting...");
                    // Print summary here (first time) to ensure you see it immediately
                    printSummary(sent, acked, retransmissions);
                    break;
                }

                seq++;
                String message = seq + ":" + line;
                byte[] data = message.getBytes(StandardCharsets.UTF_8);

                boolean gotAck = false;
                int attempts = 0;

                while (!gotAck && attempts <= MAX_RETRIES) {
                    attempts++;
                    sent++;
                    DatagramPacket packet = new DatagramPacket(data, data.length, serverAddr, port);
                    socket.send(packet);

                    try {
                        byte[] ackBuf = new byte[128];
                        DatagramPacket ack = new DatagramPacket(ackBuf, ackBuf.length);
                        socket.receive(ack);
                        String ackMsg = new String(ack.getData(), 0, ack.getLength(), StandardCharsets.UTF_8).trim();
                        if (ackMsg.equals("ACK " + seq)) {
                            gotAck = true;
                            acked++;
                            System.out.println("[UDPClient] Received " + ackMsg);
                        } else {
                            System.out.println("[UDPClient] Unexpected ACK content: " + ackMsg);
                        }
                    } catch (SocketTimeoutException te) {
                        if (attempts <= MAX_RETRIES) {
                            retransmissions++;
                            System.out.println("[UDPClient] ACK timeout for seq " + seq + ", retry " + attempts + "/" + MAX_RETRIES);
                        }
                    }
                }

                if (!gotAck) {
                    System.out.println("[UDPClient][WARNING] No ACK for seq " + seq + " after " + MAX_RETRIES + " retries.");
                }
            }

        } catch (IOException ioe) {
            System.err.println("[UDPClient] I/O error: " + ioe.getMessage());
        } finally {
           
        }
    }
}
