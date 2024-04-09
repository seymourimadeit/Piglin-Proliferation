package tallestred.piglinproliferation;

import java.util.ArrayList;
import java.util.List;

public class CodeUtilities {
    /**
     * Converts
     * */
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

    /**
     * Converts a list to a new, mutable list of a specified type. Throws an error if casting fails.
     *
     * @param list the list of unknown type to convert
     * */
    @SuppressWarnings("unchecked")
    public static <T> List<T> convertToMutableListOrThrow(List<?> list) {
        try {
            return (List<T>) new ArrayList<>(list);
        } catch (ClassCastException e) {
            throw new IllegalArgumentException("Cannot cast the input list to the output type! If you are a player, report to the Piglin Proliferation github");
        }
    }
}
