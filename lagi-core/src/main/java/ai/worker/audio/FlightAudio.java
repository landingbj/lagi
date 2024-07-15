package ai.worker.audio;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class FlightAudio {
    private static final Map<Character, Set<String>> FLIGHT_CODE_ORGANIZED_MAP;
    private static final Map<Character, Set<String>> FLIGHT_NUM_ORGANIZED_MAP;
    private static final Map<String, String> INPUT_REPLACEMENTS = new ConcurrentHashMap<>();
    private static final Map<String, String> FLIGHT_REPLACEMENTS = new ConcurrentHashMap<>();

    private static final String FLIGHT_REGEX = "[A-Z0-9]{2}\\d{3,4}";

    private static final Pattern FLIGHT_PATTERN = Pattern.compile(FLIGHT_REGEX);

    static {
        List<String> flightNumbers = readFlightNumberJson();
        FLIGHT_CODE_ORGANIZED_MAP = organizeFlightCode(flightNumbers);
        FLIGHT_NUM_ORGANIZED_MAP = organizeFlightNumbers(flightNumbers);
        INPUT_REPLACEMENTS.put(" ", "");
        INPUT_REPLACEMENTS.put("-", "");
        INPUT_REPLACEMENTS.put("零", "0");
        INPUT_REPLACEMENTS.put("一", "1");
        INPUT_REPLACEMENTS.put("二", "2");
        INPUT_REPLACEMENTS.put("三", "3");
        INPUT_REPLACEMENTS.put("四", "4");
        INPUT_REPLACEMENTS.put("五", "5");
        INPUT_REPLACEMENTS.put("六", "6");
        INPUT_REPLACEMENTS.put("七", "7");
        INPUT_REPLACEMENTS.put("八", "8");
        INPUT_REPLACEMENTS.put("九", "9");
        INPUT_REPLACEMENTS.put("IPHONE", "FN");
        INPUT_REPLACEMENTS.put("HOE", "HO1");
        INPUT_REPLACEMENTS.put("雷刺翁", "HO");
        INPUT_REPLACEMENTS.put("雷刺翁用", "HO1");
        INPUT_REPLACEMENTS.put("生日", "CZ");

        FLIGHT_REPLACEMENTS.put("82", "8L");
        FLIGHT_REPLACEMENTS.put("31", "3L");
        FLIGHT_REPLACEMENTS.put("CJ", "CZ");
        FLIGHT_REPLACEMENTS.put("JS", "GS");
        FLIGHT_REPLACEMENTS.put("ZS", "GS");
        FLIGHT_REPLACEMENTS.put("SU", "HU");
        FLIGHT_REPLACEMENTS.put("KL", "KN");
        FLIGHT_REPLACEMENTS.put("SO", "HO");
    }

    public static String replaceStrings(String input, Map<String, String> replacements) {
        for (Map.Entry<String, String> entry : replacements.entrySet()) {
            input = input.toUpperCase().replace(entry.getKey(), entry.getValue());
        }
        return input;
    }

    public static String replaceCode(String input, Map<String, String> replacements) {
        int endIndex = 2;
        String subStr = input.substring(0, endIndex);
        input = input.toUpperCase();
        if (replacements.containsKey(subStr)) {
            StringBuilder sb = new StringBuilder(input);
            input = sb.replace(0, endIndex, replacements.get(subStr)).toString();
            return input;
        }
        return input;
    }

    public static String correctFlightNumber(String text) {
        String tmpText = replaceStrings(text, INPUT_REPLACEMENTS);
        String flightNumber = extractFlightNumber(tmpText);
        if (flightNumber == null) {
            return text;
        }
        String tmpFlightNumber = replaceCode(flightNumber, FLIGHT_REPLACEMENTS);
        String correctedFlightNumber = getCorrectedFlightNumber(tmpFlightNumber);
        String result;
        if (correctedFlightNumber != null) {
            result = tmpText.replace(flightNumber, correctedFlightNumber);
        } else {
            result = tmpText.replace(flightNumber, tmpFlightNumber);
        }
        return result;
    }

    public static String extractFlightNumber(String text) {
        String result = null;
        Matcher matcher = FLIGHT_PATTERN.matcher(text);

        while (matcher.find()) {
            result = matcher.group();
        }
        return result;
    }

    private static Map<Character, Set<String>> organizeFlightCode(List<String> flightNumbers) {
        Map<Character, Set<String>> map = new ConcurrentHashMap<>();
        for (String flightNumber : flightNumbers) {
            for (char ch : flightNumber.toCharArray()) {
                map.computeIfAbsent(ch, new Function<Character, Set<String>>() {
                    @Override
                    public Set<String> apply(Character k) {
                        return new HashSet<>();
                    }
                }).add(flightNumber);
            }
        }
        return map;
    }

    private static Map<Character, Set<String>> organizeFlightNumbers(List<String> flightNumbers) {
        Map<Character, Set<String>> map = new ConcurrentHashMap<>();
        for (String flightNumber : flightNumbers) {
            for (int i = 2; i < flightNumber.length(); i++) {
                char ch = flightNumber.charAt(i);
                map.computeIfAbsent(ch, new Function<Character, Set<String>>() {
                    @Override
                    public Set<String> apply(Character k) {
                        return new HashSet<>();
                    }
                }).add(flightNumber);
            }
        }
        return map;
    }

    private static List<String> readFlightNumberJson() {
        String respath = "/flight_number.json";
        String content = "{}";

        try (InputStream in = FlightAudio.class.getResourceAsStream(respath);) {
            content = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8)).lines().collect(Collectors.joining("\n"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        Type listType = new TypeToken<List<String>>() {
        }.getType();
        List<String> result = new Gson().fromJson(content, listType);

        respath = "/flight_number_extra.json";

        try (InputStream in = FlightAudio.class.getResourceAsStream(respath);) {
            content = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8)).lines().collect(Collectors.joining("\n"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        List<String> extraData = new Gson().fromJson(content, listType);
        result.addAll(extraData);

        return result;
    }

    public static String getCorrectedFlightNumber(String input) {
        char[] chars = input.toCharArray();
        List<Set<String>> charSetList = new ArrayList<>();
        for (int i = 0; i < chars.length; i++) {
            char c = chars[i];
            Set<String> charSet;
            if (i < 2) {
                charSet = FLIGHT_CODE_ORGANIZED_MAP.get(c);
            } else {
                charSet = FLIGHT_NUM_ORGANIZED_MAP.get(c);
            }
            if (charSet != null) {
                charSetList.add(new HashSet<>(charSet));
            }
        }
        Set<String> setResult = charSetList.get(0);
        for (int i = 1; i < charSetList.size(); i++) {
            setResult.retainAll(charSetList.get(i));
        }
        Map<String, Integer> resultMap = new HashMap<>();
        for (String s : setResult) {
            if (isSubsequence(input, s)) {
                resultMap.put(s, getChoicePriority(s));
            }
        }
        if (resultMap.isEmpty()) {
            return null;
        }
        String result = findMaxValueKey(resultMap);
        return result;
    }

    public static String findMaxValueKey(Map<String, Integer> map) {
        String maxKey = null;
        Integer maxValue = Integer.MIN_VALUE;
        for (Map.Entry<String, Integer> entry : map.entrySet()) {
            if (entry.getValue().compareTo(maxValue) > 0) {
                maxValue = entry.getValue();
                maxKey = entry.getKey();
            }
        }
        return maxKey;
    }

    private static boolean isSubsequence(String s, String t) {
        if (s.isEmpty())
            return true;
        int indexS = 0, indexT = 0;

        while (indexT < t.length()) {
            if (s.charAt(indexS) == t.charAt(indexT)) {
                indexS++;
                if (indexS == s.length())
                    return true;
            }
            indexT++;
        }
        return false;
    }

    private static int getChoicePriority(String choice) {
        int indexOf4 = choice.indexOf('4');
        int maxPriority = 100;
        if (indexOf4 == 3 || indexOf4 == 4) {
            return maxPriority;
        }
        int indexOf5 = choice.indexOf('5');
        if (indexOf5 > 1) {
            return maxPriority / indexOf5;
        }
        return 0;
    }

    public static void main(String[] args) {
        // 创建和启动5个线程
        String[] inputs = {"CA1029", "CA-5116", "MU2314", "CZ 1245", "CZ1245", "HU-1256", "HU7943", "CA5116",
                "HU1256", "MF1245", "MF9872", "FM6821", "FM6111", "ZH1255", "ZH8253", "3U1245", "3U8752", "SC9166",
                "SC8621", "HO1244", "H O E 2 4 4", "HO9124", "KN0171", "KN0124", "FN", "iPhone", "244", "FN1244",
                "FN5512", "8L1241", "8L 1241", "8L2511", "CJ1245", "SU1256", "MF125", "Zs1 2 5", "ZS8253", "JS8253",
                "3U125", "318752", "雷刺翁1244", "雷刺翁用二四四", "SO9124", "KL 017", "iPhone 1244", "iPhone5512", "821241",
                "航班号8L995"};
        // String[] inputs = {"8L995", "3U125"};
        for (String input : inputs) {
            final long startTime = System.currentTimeMillis();
            String result = FlightAudio.correctFlightNumber(input);
            final long endTime = System.currentTimeMillis();

            System.out.println(input + ", " + result + ", Total execution time: " + (endTime - startTime));
            System.out.println();
        }
    }
}
