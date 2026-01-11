package com.heronix.edu.client.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Hardware-based device identification
 * Generates a unique, persistent device ID based on hardware characteristics
 *
 * This eliminates the need for registration codes - the device itself becomes the credential
 */
public class HardwareIdentifier {

    private static final Logger logger = LoggerFactory.getLogger(HardwareIdentifier.class);
    private static String cachedDeviceId = null;

    /**
     * Generate a unique device ID based on hardware characteristics
     *
     * This ID is persistent across application restarts and uniquely identifies
     * the physical device, preventing device sharing and enhancing security.
     *
     * @return Unique device identifier (SHA-256 hash of hardware properties)
     */
    public static String generateDeviceId() {
        if (cachedDeviceId != null) {
            return cachedDeviceId;
        }

        try {
            List<String> identifiers = new ArrayList<>();

            // 1. CPU ID (Windows/Linux/Mac)
            String cpuId = getCpuId();
            if (cpuId != null && !cpuId.isEmpty()) {
                identifiers.add("CPU:" + cpuId);
            }

            // 2. Motherboard Serial Number (Windows/Linux)
            String motherboardSerial = getMotherboardSerial();
            if (motherboardSerial != null && !motherboardSerial.isEmpty()) {
                identifiers.add("MB:" + motherboardSerial);
            }

            // 3. Primary MAC Address
            String macAddress = getPrimaryMacAddress();
            if (macAddress != null && !macAddress.isEmpty()) {
                identifiers.add("MAC:" + macAddress);
            }

            // 4. Computer Name/Hostname
            String hostname = InetAddress.getLocalHost().getHostName();
            if (hostname != null && !hostname.isEmpty()) {
                identifiers.add("HOST:" + hostname);
            }

            // 5. System UUID (Linux/Mac)
            String systemUuid = getSystemUuid();
            if (systemUuid != null && !systemUuid.isEmpty()) {
                identifiers.add("UUID:" + systemUuid);
            }

            // Fallback: If no hardware IDs found, use a combination of system properties
            if (identifiers.isEmpty()) {
                logger.warn("No hardware identifiers found, using system properties fallback");
                identifiers.add("OS:" + System.getProperty("os.name"));
                identifiers.add("USER:" + System.getProperty("user.name"));
                identifiers.add("ARCH:" + System.getProperty("os.arch"));
            }

            // Sort for consistency
            Collections.sort(identifiers);

            // Combine all identifiers
            String combined = String.join("|", identifiers);

            // Hash to create a fixed-length device ID
            cachedDeviceId = hashString(combined);

            logger.info("Generated device ID from {} hardware identifiers", identifiers.size());
            logger.debug("Device ID components: {}", identifiers);

            return cachedDeviceId;

        } catch (Exception e) {
            logger.error("Error generating device ID", e);
            // Fallback to a UUID-based ID
            cachedDeviceId = "FALLBACK-" + java.util.UUID.randomUUID().toString();
            return cachedDeviceId;
        }
    }

    /**
     * Get CPU ID using platform-specific commands
     */
    private static String getCpuId() {
        try {
            String os = System.getProperty("os.name").toLowerCase();

            if (os.contains("win")) {
                // Windows: Use WMIC to get CPU ProcessorId
                return executeCommand("wmic cpu get ProcessorId");
            } else if (os.contains("mac")) {
                // macOS: Use system_profiler to get CPU serial
                return executeCommand("system_profiler SPHardwareDataType | grep 'Serial Number'");
            } else if (os.contains("nix") || os.contains("nux")) {
                // Linux: Use dmidecode to get CPU serial
                return executeCommand("sudo dmidecode -t processor | grep ID");
            }
        } catch (Exception e) {
            logger.debug("Could not get CPU ID", e);
        }
        return null;
    }

    /**
     * Get motherboard serial number
     */
    private static String getMotherboardSerial() {
        try {
            String os = System.getProperty("os.name").toLowerCase();

            if (os.contains("win")) {
                // Windows: Use WMIC to get motherboard serial
                return executeCommand("wmic baseboard get serialnumber");
            } else if (os.contains("nix") || os.contains("nux")) {
                // Linux: Use dmidecode
                return executeCommand("sudo dmidecode -s baseboard-serial-number");
            } else if (os.contains("mac")) {
                // macOS: Use system_profiler
                return executeCommand("system_profiler SPHardwareDataType | grep 'Serial Number'");
            }
        } catch (Exception e) {
            logger.debug("Could not get motherboard serial", e);
        }
        return null;
    }

    /**
     * Get primary network interface MAC address
     */
    private static String getPrimaryMacAddress() {
        try {
            // Get the primary network interface
            InetAddress localHost = InetAddress.getLocalHost();
            NetworkInterface network = NetworkInterface.getByInetAddress(localHost);

            if (network != null) {
                byte[] mac = network.getHardwareAddress();

                if (mac != null) {
                    StringBuilder sb = new StringBuilder();
                    for (int i = 0; i < mac.length; i++) {
                        sb.append(String.format("%02X", mac[i]));
                        if (i < mac.length - 1) {
                            sb.append("-");
                        }
                    }
                    return sb.toString();
                }
            }

            // Fallback: Get first available MAC address
            var interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface iface = interfaces.nextElement();
                if (!iface.isLoopback() && iface.isUp()) {
                    byte[] mac = iface.getHardwareAddress();
                    if (mac != null && mac.length > 0) {
                        StringBuilder sb = new StringBuilder();
                        for (int i = 0; i < mac.length; i++) {
                            sb.append(String.format("%02X", mac[i]));
                            if (i < mac.length - 1) {
                                sb.append("-");
                            }
                        }
                        return sb.toString();
                    }
                }
            }

        } catch (Exception e) {
            logger.debug("Could not get MAC address", e);
        }
        return null;
    }

    /**
     * Get system UUID (Linux/Mac)
     */
    private static String getSystemUuid() {
        try {
            String os = System.getProperty("os.name").toLowerCase();

            if (os.contains("nix") || os.contains("nux")) {
                // Linux: Use dmidecode
                return executeCommand("sudo dmidecode -s system-uuid");
            } else if (os.contains("mac")) {
                // macOS: Use ioreg
                return executeCommand("ioreg -rd1 -c IOPlatformExpertDevice | grep IOPlatformUUID");
            }
        } catch (Exception e) {
            logger.debug("Could not get system UUID", e);
        }
        return null;
    }

    /**
     * Execute a system command and return output
     */
    private static String executeCommand(String command) {
        try {
            Process process = Runtime.getRuntime().exec(command);
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

            StringBuilder output = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line.trim()).append(" ");
            }

            process.waitFor();
            reader.close();

            // Clean up the output
            String result = output.toString().trim();

            // Remove common labels/headers
            result = result.replaceAll("ProcessorId|SerialNumber|Serial Number|ID|UUID", "").trim();
            result = result.replaceAll("\\s+", " ").trim();

            return result.isEmpty() ? null : result;

        } catch (Exception e) {
            logger.debug("Command execution failed: {}", command, e);
            return null;
        }
    }

    /**
     * Hash a string using SHA-256
     */
    private static String hashString(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes("UTF-8"));

            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }

            return hexString.toString();

        } catch (Exception e) {
            logger.error("Error hashing string", e);
            return input; // Fallback to unhashed input
        }
    }

    /**
     * Get a human-readable summary of the device hardware
     */
    public static String getDeviceHardwareSummary() {
        StringBuilder summary = new StringBuilder();
        summary.append("Device Hardware Information:\n");
        summary.append("OS: ").append(System.getProperty("os.name")).append(" ")
               .append(System.getProperty("os.version")).append("\n");
        summary.append("Architecture: ").append(System.getProperty("os.arch")).append("\n");
        summary.append("Hostname: ");
        try {
            summary.append(InetAddress.getLocalHost().getHostName());
        } catch (Exception e) {
            summary.append("Unknown");
        }
        summary.append("\n");

        String macAddress = getPrimaryMacAddress();
        if (macAddress != null) {
            summary.append("MAC Address: ").append(macAddress).append("\n");
        }

        summary.append("Device ID: ").append(generateDeviceId());

        return summary.toString();
    }

    /**
     * Clear the cached device ID (for testing purposes)
     */
    public static void clearCache() {
        cachedDeviceId = null;
    }
}
