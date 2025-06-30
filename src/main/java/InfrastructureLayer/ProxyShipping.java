package InfrastructureLayer;

import DomainLayer.IShipping;
import DomainLayer.Store;
import org.springframework.stereotype.Repository;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;

@Repository
public class ProxyShipping implements IShipping {
    public ProxyShipping() {}
    public String processShipping(String state, String city, String address, Map<String, Integer> products, String name, String zip) throws Exception {
        try {
            HttpClient client = HttpClient.newHttpClient();

            String json = "action_type=handshake";

            HttpRequest request = HttpRequest.newBuilder().uri(URI.create("https://damp-lynna-wsep-1984852e.koyeb.app")).header("Content-Type", "application/x-www-form-urlencoded").POST(HttpRequest.BodyPublishers.ofString(json)).build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            System.out.println(response);
            if (response.body().equals("OK")) {
                client = HttpClient.newHttpClient();

                    json = "action_type=supply" +
                            "&name=" + name +
                            "&city=" + city +
                            "&address=" + address +
                            "&country=" + state +
                            "&zip=" + zip;

                request = HttpRequest.newBuilder().uri(URI.create("https://damp-lynna-wsep-1984852e.koyeb.app")).header("Content-Type", "application/x-www-form-urlencoded").POST(HttpRequest.BodyPublishers.ofString(json)).build();

                response = client.send(request, HttpResponse.BodyHandlers.ofString());

                if (Integer.valueOf(response.body()) == -1) {
                    throw new Exception("shipping unsuccessful");
                } else {
                    return response.body();
                }

            } else {
                throw new Exception("could not connect to shipping system");
            }
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public String cancelShipping(String Id) throws Exception {
        try {
            HttpClient client = HttpClient.newHttpClient();

            String json = "action_type=cancel_supply" +
                    "&transaction_id=" + Id;

            HttpRequest request = HttpRequest.newBuilder().uri(URI.create("https://damp-lynna-wsep-1984852e.koyeb.app")).header("Content-Type", "application/x-www-form-urlencoded").POST(HttpRequest.BodyPublishers.ofString(json)).build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                if (Integer.valueOf(response.body()) == -1) {
                    throw new Exception("cancel supply unsucessful");
                } else {
                    return "cancel supply sucessful";
                }

            } else {
                throw new Exception("could not connect to shipping system");
            }
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

}
