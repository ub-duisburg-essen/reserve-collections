package unidue.rc.ui.services;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.tapestry5.services.URLEncoder;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Optional;

/**
 * Custom implementation of tapestrys <code>URLEncoder</code> to allow more characters in urls, not just letters numbers
 * and a small amount of additional characters.
 */
public class CustomURLEncoderImpl implements URLEncoder {

    static final String ENCODED_NULL = "$N";
    static final String ENCODED_BLANK = "$B";

    /**
     * Bit set indicating which character are safe to pass through (when
     * encoding or decoding) as-is. All other characters are encoded as a
     * kind
     * of unicode escape.
     */
    private final BitSet safeForInput = new BitSet(128);
    private final BitSet safeForOutput = new BitSet(128);

    /**
     * Array of characters that must be escaped to make use of them in a url
     */
    private final Pair<Character, String>[] codingReference = new Pair[]{
            Pair.of(' ', "%20"),
            Pair.of('!', "%21"),
            Pair.of('"', "%22"),
            Pair.of('#', "%23"),
            Pair.of('$', "%24"),
            Pair.of('%', "%25"),
            Pair.of('&', "%26"),
            Pair.of('\'', "%27"),
            Pair.of('(', "%28"),
            Pair.of(')', "%29"),
            Pair.of('*', "%2A"),
            Pair.of('+', "%2B"),
            Pair.of('/', "%2F")
    };

    {

        markSafeForInput("aàâäbcçĉdeéèêëfgĝhĥiïîjĵklmnoôöpqrsŝtuùûüvwxyzæœ");
        markSafeForInput("AÀÂÄBCÇĈDEÉÈÊËFGĜHĤIÏÎĤJĴKLMNOÔÖPQRSŜTUÙÛÜVWXYZÆŒ");
        markSafeForInput("01234567890-_.:,'");

        markSafeForOuput("abcdefghijklmnopqrstuvwxyz");
        markSafeForOuput("ABCDEFGHIJKLMNOPQRSTUVWXYZ");
        markSafeForOuput("01234567890-_.:,'");
    }

    private void markSafeForInput(String s) {
        for (char ch : s.toCharArray()) {
            safeForInput.set(ch);
        }
    }

    private void markSafeForOuput(String s) {
        for (char ch : s.toCharArray()) {
            safeForOutput.set(ch);
        }
    }

    public String encode(String input) {
        if (input == null)
            return ENCODED_NULL;

        if (input.equals(""))
            return ENCODED_BLANK;

        boolean dirty = false;

        int length = input.length();

        StringBuilder output = new StringBuilder(length * 2);

        for (int i = 0; i < length; i++) {
            char ch = input.charAt(i);
            int chAsInt = ch;

            if (ch == '$') {
                output.append("$$");
                dirty = true;
            } else if (safeForOutput.get(chAsInt)) {
                output.append(ch);
            } else {

                try {
                    String result = java.net.URLEncoder.encode(new String(input), "UTF-8");
                    return result;
                } catch (UnsupportedEncodingException e) {
                    throw new IllegalArgumentException(e);
                }
            }
        }

        return dirty
               ? output.toString()
               : input;
    }

    public String decode(String input) {


        if (input.equals(ENCODED_NULL))
            return null;

        if (input.equals(ENCODED_BLANK))
            return "";

        boolean dirty = false;

        int length = input.length();

        StringBuilder output = new StringBuilder(length * 2);

        for (int i = 0; i < length; i++) {
            char ch = input.charAt(i);
            Optional<String> mapping = getMapping(ch);

            // if $ is found check for $$ or $\d{4}
            if (ch == '$') {
                dirty = true;

                // if double $ is present just skip and mark as dirty
                if (i + 1 < length && input.charAt(i + 1) == '$') {
                    output.append('$');
                    i++;
                } else if (i + 4 < length) {
                    // try to read hex value
                    String hex = input.substring(i + 1, i + 5);

                    try {
                        int unicode = Integer.parseInt(hex, 16);

                        output.append((char) unicode);
                        i += 4;
                    } catch (NumberFormatException ex) {
                        // Ignore.
                    }
                } else {

                    throw new IllegalArgumentException(
                            String.format(
                                    "Input string '%s' is not valid; the '$' character at position %d should be followed by another '$' or a four digit hex number (a unicode value).",
                                    input, i + 1));
                }
            } else if (mapping.isPresent()) {
                output.append(mapping.get());
            } else if (!safeForInput.get(ch)) {
                throw new IllegalArgumentException(
                        String.format(
                                "Input string '%s' is not valid; the character '%s' at position %d is not valid.",
                                input, ch, i + 1));
            } else {

                output.append(ch);
            }

        }

        return dirty
               ? output.toString()
               : input;
    }

    private Optional<String> getMapping(char ch) {
        return Arrays.stream(codingReference)
                .filter(p -> p.getLeft().equals(ch))
                .map(p -> p.getRight())
                .findFirst();
    }

}
