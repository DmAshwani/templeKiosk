package in.dataman.util;//package in.dataman.util;
//
//import okhttp3.OkHttpClient;
//import okhttp3.Request;
//import okhttp3.Response;
//import org.json.JSONArray;
//
//public class HindiTransliterationUtil {
//    private static final OkHttpClient client = new OkHttpClient();
//
//    public static String transliterateWord(String word) {
//        try {
//            String url = "https://inputtools.google.com/request?text=" + word + "&itc=hi-t-i0-und&num=1";
//            Request request = new Request.Builder().url(url).build();
//            try (Response response = client.newCall(request).execute()) {
//                String responseBody = response.body().string();
//
//                JSONArray outerArray = new JSONArray(responseBody);
//                String status = outerArray.getString(0);
//
//                if ("SUCCESS".equals(status)) {
//                    JSONArray suggestions = outerArray
//                            .getJSONArray(1)
//                            .getJSONArray(0)
//                            .getJSONArray(1);
//                    return suggestions.getString(0); // best suggestion
//                }
//            }
//        } catch (Exception e) {
//            System.err.println("Failed to transliterate word: " + word);
//        }
//        return word; // fallback to original if failed
//    }
//
//    public static void main(String[] args) {
//        String input = "Kesar Barfi -200 gram";
//        String[] words = input.split(" ");
//        StringBuilder hindiSentence = new StringBuilder();
//
//        for (String word : words) {
//            String hindi = transliterateWord(word);
//            hindiSentence.append(hindi).append(" ");
//        }
//
//        System.out.println("Input: " + input);
//        System.out.println("Hindi: " + hindiSentence.toString().trim());
//    }
//
//
//}



import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.json.JSONArray;


public class HindiTransliterationUtil {

    private static final OkHttpClient client = new OkHttpClient();

    public static String transliterateSentence(String input) {
        String[] words = input.split(" ");
        StringBuilder hindiSentence = new StringBuilder();

        for (String word : words) {
            hindiSentence.append(transliterateWord(word)).append(" ");
        }

        return hindiSentence.toString().trim();
    }

    private static String transliterateWord(String word) {
        try {
            // Remove punctuation if needed (e.g., comma, hyphen)
            String cleanWord = word.replaceAll("[^a-zA-Z0-9]", "");
            String url = "https://inputtools.google.com/request?text=" + cleanWord + "&itc=hi-t-i0-und&num=1";
            Request request = new Request.Builder().url(url).build();
            try (Response response = client.newCall(request).execute()) {
                String responseBody = response.body().string();
                JSONArray outerArray = new JSONArray(responseBody);
                String status = outerArray.getString(0);

                if ("SUCCESS".equals(status)) {
                    JSONArray suggestions = outerArray
                            .getJSONArray(1)
                            .getJSONArray(0)
                            .getJSONArray(1);
                    return word.replace(cleanWord, suggestions.getString(0)); // maintain symbols
                }
            }
        } catch (Exception e) {
            System.err.println("Failed to transliterate word: " + word);
        }
        return word; // fallback
    }

//    public static void main(String[] args) {
//        String input = "Kesar Barfi -200 gram";
//        String hindi = transliterateSentence(input);
//
//        System.out.println("Input: " + input);
//        System.out.println("Hindi: " + hindi);
//    }
}
