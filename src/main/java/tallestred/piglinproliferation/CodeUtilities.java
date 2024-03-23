package tallestred.piglinproliferation;

public class CodeUtilities {
    public static String snakeCaseToEnglish(String raw) {
        String[] parts = raw.split("_");
        StringBuilder output = new StringBuilder();
        for (String s : parts) {
            output.append(s.substring(0, 1).toUpperCase());
            output.append(s.substring(1));
            output.append(" ");
        }
        return output.toString().trim();
    }
}
