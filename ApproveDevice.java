import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class ApproveDevice {
    public static void main(String[] args) throws Exception {
        String loginJson = "{\"username\":\"admin1\",\"password\":\"admin123\"}";

        // Login as admin
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest loginRequest = HttpRequest.newBuilder()
            .uri(URI.create("http://localhost:8081/api/auth/login"))
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(loginJson))
            .build();

        HttpResponse<String> loginResponse = client.send(loginRequest, HttpResponse.BodyHandlers.ofString());
        String token = loginResponse.body().split("\"token\":\"")[1].split("\"")[0];

        System.out.println("Got token: " + token.substring(0, 20) + "...");

        // Approve device
        String deviceId = "5cd8b36b025392a98c022d95e9d47702a8ebf1a0bb5514d05568fabbd8701ccf";
        HttpRequest approveRequest = HttpRequest.newBuilder()
            .uri(URI.create("http://localhost:8081/api/admin/devices/" + deviceId + "/approve"))
            .header("Authorization", "Bearer " + token)
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString("{}"))
            .build();

        HttpResponse<String> approveResponse = client.send(approveRequest, HttpResponse.BodyHandlers.ofString());
        System.out.println("Approve response: " + approveResponse.statusCode());
        System.out.println(approveResponse.body());
    }
}
