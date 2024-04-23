package tallestred.piglinproliferation.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CodeUtilities {

    /**
     * Casts an object to given type if they are compatible, returns null if not.
     *
     * @param object the object to try casting
     * @param castType the type to try casting to
     * */
    public static <T> T castOrNull(Object object, Class<T> castType) {
        return castType.isInstance(object) ? castType.cast(object) : null;
    }

    /**
     * <p>Converts a string formatted in snake case to its equivalent plaintext capitalised form.</p>
     * <p>Example: {@code hello_world -> Hello World}</p>
     *
     * @param raw the snake case string to convert
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
     * Converts an array of elements to a list of arbitrary type.
     * Only intended for use in cases where you'd be casting even though you know the elements are valid, and fails
     * at runtime if they're not; be careful!
     *
     * @param elements the objects to convert - must be of valid type for the output or this method will fail
     * */
    @SuppressWarnings("unchecked") //Properly handled
    public static <T> List<T> castElementsToList(Object... elements) {
        try {
            List<Object> list = new ArrayList<>();
            Collections.addAll(list, elements);
            return (List<T>) list;
        } catch (ClassCastException e) {
            throw new IllegalArgumentException("Cannot cast the input list to the output type! If you are a player, report to the Piglin Proliferation github");
        }
    }

    /**
     * Rounds a double to a specified number of decimal places.
     *
     * @author jpdymond from StackOverflow
     *
     * @param value the number to round
     * @param precision the number of decimal places to round to
     * */
    public static double round(double value, int precision) {
        int scale = (int) Math.pow(10, precision);
        return (double) Math.round(value * scale) / scale;
    }

    /**
     * Caps an integer value within a given range
     *
     * @author Barry Staes from StackOverflow
     * */
    public static int capToRange(int value, int min, int max) {
        return Math.min(Math.max(value, min), max);
    }

    public static double capToRange(double value, double min, double max) {
        return Math.min(Math.max(value, min), max);
    }

    /**
     * Converts a number of ticks into a double representing a decimal value for seconds.
     * Rounds to 2dp in case of floating-point error.
     *
     * @param ticks the number of ticks to convert
     * */
    public static double ticksToSeconds(int ticks) {
        return round(ticks / 20f, 2);
    }

    public static String doubleToString(double value) {
        return value % 1 == 0 ? Integer.toString((int) value) : Double.toString(value);
    }
}
