import java.io.Console;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class UDPClient {

    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println("Usage: java UDPClient <hostname> <port>");
            return;
        }

        String hostname = args[0];
        int port = Integer.parseInt(args[1]);

        try (DatagramSocket socket = new DatagramSocket()) {
            InetAddress serverAddress = InetAddress.getByName(hostname);
            Console console = System.console();

            if (console == null) {
                System.out.println("No console available. Run from a terminal!");
                return;
            }

            System.out.println("Enter messages (Ctrl+C to quit):");

            while (true) {
                String message = console.readLine("> ");
                byte[] data = message.getBytes("UTF-8");

                DatagramPacket packet = new DatagramPacket(data, data.length, serverAddress, port);
                socket.send(packet);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

