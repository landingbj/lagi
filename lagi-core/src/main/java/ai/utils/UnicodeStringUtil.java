package ai.utils;

public class UnicodeStringUtil {


    public static String replaceSlashUWithBackslashU(String input) {
        if (input == null) {
            return null;
        }
        return input.replace("/u", "\\u");
    }

    public static String decodeUnicode(String input) {
        StringBuilder output = new StringBuilder();
        int length = input.length();
        for (int i = 0; i < length; i++) {
            char c = input.charAt(i);
            if (c == '\\' && i + 1 < length && input.charAt(i + 1) == 'u') {
                String unicode = input.substring(i + 2, i + 6);
                try {
                    output.append((char) Integer.parseInt(unicode, 16));
                    i += 5;
                } catch (NumberFormatException e) {
                    output.append(c);
                }
            } else {
                output.append(c);
            }
        }
        return output.toString();
    }

    public static void main(String[] args) {
        String unicodeString = "/u5c0f/u4fe1/u6700/u8fd1/u5b66/u4e60/u4e86";
        String unicodeString2 = "/u62b1/u6b49";
        String decodedString1 = decodeUnicode(replaceSlashUWithBackslashU(unicodeString));
        String decodedString2 = decodeUnicode(replaceSlashUWithBackslashU(unicodeString2));
        System.out.println("Decoded String 1: " + decodedString1);
        System.out.println("Decoded String 2: " + decodedString2);
    }
}
