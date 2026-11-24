import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;


public class GeminiAI {

    public static String generateDescription(String productName, String category) {

        String apiKey = System.getenv("GEMINI_API_KEY");

        if (apiKey == null || apiKey.isEmpty()) {
            return "Gemini API key not found.";
        }

        try {
            String prompt = "Write a short, attractive product description for "
                    + productName + " in the category " + category
                    + ". Keep it simple and suitable for a farmer-to-customer marketplace.";

            String json = "{"
                    + "\"contents\":[{"
                    + "\"parts\":[{"
                    + "\"text\":\"" + escapeJson(prompt) + "\""
                    + "}]"
                    + "}]"
                    + "}";

            String url = "https://generativelanguage.googleapis.com/v1beta/models/"
                    + "gemini-3.8-flash:generateContent?key=" + apiKey;

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            HttpClient client = HttpClient.newHttpClient();

            HttpResponse<String> response =
                    client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                System.out.println("Gemini Error: " + response.body());
                return "Unable to generate description.";
            }

            String body = response.body();

            int textStart = body.indexOf("\"text\":");

            if (textStart != -1) {
                textStart += 7;

                int startQuote = body.indexOf("\"", textStart);
                int endQuote = body.indexOf("\"", startQuote + 1);

                if (startQuote != -1 && endQuote != -1) {
                    return body.substring(startQuote + 1, endQuote)
                            .replace("\\n", "\n")
                            .replace("\\\"", "\"");
                }
            }

            return "Description generated successfully.";

        } catch (Exception e) {
            e.printStackTrace();
            return "Error connecting to Gemini.";
        }
    }

    private static String escapeJson(String text) {
        return text.replace("\\", "\\\\")
                   .replace("\"", "\\\"");
    }
    
}