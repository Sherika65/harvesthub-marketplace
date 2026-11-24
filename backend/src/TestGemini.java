public class TestGemini {
    public static void main(String[] args) {

        String result = GeminiAI.generateDescription(
                "Tomato",
                "Vegetable"
        );

        System.out.println("Gemini Response:");
        System.out.println(result);
    }
}