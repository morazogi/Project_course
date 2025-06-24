package InfrastructureLayer;

import DomainLayer.IPayment;
import ServiceLayer.ErrorLogger;
import org.springframework.stereotype.Repository;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Repository
public class ProxyPayment implements IPayment {
    public ProxyPayment() {
    }

    public String processPayment(Double payment, String creditCardNumber, String expirationDate, String backNumber, String Id, String name) throws Exception {
        try {
            HttpClient client = HttpClient.newHttpClient();

            String json = "action_type=handshake";

            HttpRequest request = HttpRequest.newBuilder().uri(URI.create("https://damp-lynna-wsep-1984852e.koyeb.app")).header("Content-Type", "application/x-www-form-urlencoded").header("Authorization", "Bearer YOUR_API_KEY").POST(HttpRequest.BodyPublishers.ofString(json)).build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.body().equals("OK")) {
                client = HttpClient.newHttpClient();

                String[] dateleft = expirationDate.split("\\\\");
                String[] dateright = expirationDate.split("/");
                String[] datemiddle = expirationDate.split("\\.");

                if (dateleft.length == 2) {
                    json = "action_type=pay" +
                            "&amount=" + payment.toString() +
                            "&currency=USD" +
                            "&card_number=" + creditCardNumber +
                            "&month=" + dateleft[0] +
                            "&year=" + dateleft[1] +
                            "&holder=" + name +
                            "&cvv=" + backNumber +
                            "&id=" + Id;
                } else if (dateright.length == 2) {
                    json = "action_type=pay" +
                            "&amount=" + payment.toString() +
                            "&currency=USD" +
                            "&card_number=" + creditCardNumber +
                            "&month=" + dateright[0] +
                            "&year=" + dateright[1] +
                            "&holder=" + name +
                            "&cvv=" + backNumber +
                            "&id=" + Id;

                } else if (datemiddle.length == 2) {
                    json = "action_type=pay" +
                            "&amount=" + payment.toString() +
                            "&currency=USD" +
                            "&card_number=" + creditCardNumber +
                            "&month=" + datemiddle[0] +
                            "&year=" + datemiddle[1] +
                            "&holder=" + name +
                            "&cvv=" + backNumber +
                            "&id=" + Id;

                } else {
                    throw new Exception("invalid date");
                }

                request = HttpRequest.newBuilder().uri(URI.create("https://damp-lynna-wsep-1984852e.koyeb.app")).header("Content-Type", "application/x-www-form-urlencoded").header("Authorization", "Bearer YOUR_API_KEY").POST(HttpRequest.BodyPublishers.ofString(json)).build();

                response = client.send(request, HttpResponse.BodyHandlers.ofString());

                if (Integer.valueOf(response.body()) == -1) {
                    throw new Exception("payment unsuccessful");
                } else {
                    return response.body();
                }

            } else {
                throw new Exception("could not connect to payment system");
            }
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public String cancelPayment(String Id) throws Exception {
        try {
            HttpClient client = HttpClient.newHttpClient();

            String json = "action_type=cancel_pay" +
                          "&transaction_id=" + Id;

            HttpRequest request = HttpRequest.newBuilder().uri(URI.create("https://damp-lynna-wsep-1984852e.koyeb.app")).header("Content-Type", "application/x-www-form-urlencoded").POST(HttpRequest.BodyPublishers.ofString(json)).build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                if (Integer.valueOf(response.body()) == -1) {
                    throw new Exception("cancel payment unsucessful");
                } else {
                    return "cancel payment sucessful";
                }

            } else {
                throw new Exception("could not connect to payment system");
            }
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

}