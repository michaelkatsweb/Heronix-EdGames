import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.*;
import java.util.*;

public class NetworkMonitor {
    private long lastRxBytes = 0;
    private long lastTxBytes = 0;
    private long lastTimestamp = System.currentTimeMillis();

    // Object pooling - reuse objects instead of creating new ones
    private final NetworkStats cachedStats = new NetworkStats(0, 0, 0, 0);
    private final List<Connection> connectionPool = new ArrayList<>(100);

    public List<NetworkInterface> getNetworkInterfaces() {
        List<NetworkInterface> interfaces = new ArrayList<>();
        try {
            Enumeration<java.net.NetworkInterface> nets =
                java.net.NetworkInterface.getNetworkInterfaces();

            while (nets.hasMoreElements()) {
                java.net.NetworkInterface netInterface = nets.nextElement();
                if (netInterface.isUp() && !netInterface.isLoopback()) {
                    interfaces.add(new NetworkInterface(
                        netInterface.getName(),
                        netInterface.getDisplayName(),
                        getIPAddresses(netInterface),
                        netInterface.isUp()
                    ));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return interfaces;
    }

    private List<String> getIPAddresses(java.net.NetworkInterface netInterface) {
        List<String> addresses = new ArrayList<>();
        Enumeration<InetAddress> inetAddresses = netInterface.getInetAddresses();
        while (inetAddresses.hasMoreElements()) {
            InetAddress addr = inetAddresses.nextElement();
            addresses.add(addr.getHostAddress());
        }
        return addresses;
    }

    public NetworkStats getNetworkStats() {
        try {
            String os = System.getProperty("os.name").toLowerCase();
            long rxBytes = 0;
            long txBytes = 0;

            if (os.contains("win")) {
                rxBytes = getWindowsNetworkBytes(true);
                txBytes = getWindowsNetworkBytes(false);
            } else {
                long[] bytes = getUnixNetworkBytes();
                rxBytes = bytes[0];
                txBytes = bytes[1];
            }

            long currentTime = System.currentTimeMillis();
            double timeDiff = (currentTime - lastTimestamp) / 1000.0;

            double rxSpeed = timeDiff > 0 ? (rxBytes - lastRxBytes) / timeDiff : 0;
            double txSpeed = timeDiff > 0 ? (txBytes - lastTxBytes) / timeDiff : 0;

            lastRxBytes = rxBytes;
            lastTxBytes = txBytes;
            lastTimestamp = currentTime;

            // Reuse cached object instead of creating new one
            cachedStats.update(rxBytes, txBytes, rxSpeed, txSpeed);
            return cachedStats;
        } catch (Exception e) {
            e.printStackTrace();
            return new NetworkStats(0, 0, 0, 0);
        }
    }

    private long getWindowsNetworkBytes(boolean receive) {
        try {
            ProcessBuilder pb = new ProcessBuilder(
                "powershell", "-Command",
                "Get-NetAdapterStatistics | Measure-Object -Property " +
                (receive ? "ReceivedBytes" : "SentBytes") + " -Sum | Select-Object -ExpandProperty Sum"
            );
            Process process = pb.start();
            BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream())
            );
            String line = reader.readLine();
            reader.close();
            return line != null ? Long.parseLong(line.trim()) : 0;
        } catch (Exception e) {
            return 0;
        }
    }

    private long[] getUnixNetworkBytes() {
        long rxBytes = 0;
        long txBytes = 0;
        try {
            Process process = Runtime.getRuntime().exec("cat /proc/net/dev");
            BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream())
            );

            String line;
            while ((line = reader.readLine()) != null) {
                if (line.contains(":") && !line.contains("lo:")) {
                    String[] parts = line.trim().split("\\s+");
                    if (parts.length >= 9) {
                        rxBytes += Long.parseLong(parts[1]);
                        txBytes += Long.parseLong(parts[9]);
                    }
                }
            }
            reader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new long[]{rxBytes, txBytes};
    }

    public List<Connection> getActiveConnections() {
        List<Connection> connections = new ArrayList<>();
        try {
            String os = System.getProperty("os.name").toLowerCase();
            Process process;

            if (os.contains("win")) {
                process = Runtime.getRuntime().exec("netstat -ano");
            } else {
                process = Runtime.getRuntime().exec("netstat -tunapl");
            }

            BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream())
            );

            String line;
            while ((line = reader.readLine()) != null) {
                Connection conn = parseConnectionLine(line, os);
                if (conn != null) {
                    connections.add(conn);
                }
            }
            reader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return connections;
    }

    private Connection parseConnectionLine(String line, String os) {
        try {
            line = line.trim();
            if (line.isEmpty() || line.startsWith("Active") ||
                line.startsWith("Proto") || line.startsWith("(")) {
                return null;
            }

            String[] parts = line.split("\\s+");
            if (parts.length >= 4) {
                String protocol = parts[0];
                String localAddress = parts[1];
                String foreignAddress = parts[2];
                String state = parts.length > 3 ? parts[3] : "UNKNOWN";

                return new Connection(protocol, localAddress, foreignAddress, state);
            }
        } catch (Exception e) {
            // Skip malformed lines
        }
        return null;
    }

    public static class NetworkInterface {
        public final String name;
        public final String displayName;
        public final List<String> ipAddresses;
        public final boolean isUp;

        public NetworkInterface(String name, String displayName,
                               List<String> ipAddresses, boolean isUp) {
            this.name = name;
            this.displayName = displayName;
            this.ipAddresses = ipAddresses;
            this.isUp = isUp;
        }
    }

    public static class NetworkStats {
        public long totalReceived;
        public long totalSent;
        public double receiveSpeed;
        public double sendSpeed;

        public NetworkStats(long totalReceived, long totalSent,
                           double receiveSpeed, double sendSpeed) {
            this.totalReceived = totalReceived;
            this.totalSent = totalSent;
            this.receiveSpeed = receiveSpeed;
            this.sendSpeed = sendSpeed;
        }

        // Update method for object pooling
        public void update(long totalReceived, long totalSent,
                          double receiveSpeed, double sendSpeed) {
            this.totalReceived = totalReceived;
            this.totalSent = totalSent;
            this.receiveSpeed = receiveSpeed;
            this.sendSpeed = sendSpeed;
        }
    }

    public static class Connection {
        public final String protocol;
        public final String localAddress;
        public final String foreignAddress;
        public final String state;

        public Connection(String protocol, String localAddress,
                         String foreignAddress, String state) {
            this.protocol = protocol;
            this.localAddress = localAddress;
            this.foreignAddress = foreignAddress;
            this.state = state;
        }
    }
}
