package ai.medusa.utils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class LCS {

    public static Set<String> findLongestCommonSubstrings(String s1, String s2, int threshold) {
        Set<String> result = new HashSet<>();
        int m = s1.length();
        int n = s2.length();
        int[][] dp = new int[m + 1][n + 1];
        List<Integer> endPoints = new ArrayList<>();
        List<Integer> lengthList = new ArrayList<>();
        for (int i = 1; i <= m; i++) {
            for (int j = 1; j <= n; j++) {
                if (s1.charAt(i - 1) == s2.charAt(j - 1)) {
                    dp[i][j] = dp[i - 1][j - 1] + 1;
                    if (i < m && j < n && s1.charAt(i) == s2.charAt(j)) {
                        continue;
                    }
                    if (dp[i][j] >= threshold) {
                        endPoints.add(i);
                        lengthList.add(dp[i][j]);
                    }
                }
            }
        }
        for (int i = 0; i < endPoints.size(); i++) {
            int end = endPoints.get(i);
            int length = lengthList.get(i);
            result.add(s1.substring(end - length, end));
        }
        return result;
    }

    public static double getLcsRatio(List<String> strList1, List<String> strList2, int threshold) {
        String str1 = String.join("", strList1);
        String str2 = String.join("", strList2);

        int lcsLength = 0;
        Set<String> lcsSet = findLongestCommonSubstrings(str1, str2, threshold);
        for (String lcs : lcsSet) {
            lcsLength += lcs.length();
        }
        int maxLength = Math.max(str1.length(), str2.length());
        return (double) lcsLength / maxLength;
    }

    public static double getLcsRatio(String str, Set<String> strSet) {
        int lcs = 0;
        for (String sub : strSet) {
            lcs += sub.length();
        }
        return (double) lcs / str.length();
    }

    public static void main(String[] args) {
        int threshold = 3;
        String s1 = "bbbabcde51233";
        String s2 = "bbabcdf612335";
        Set<String> lcsList = LCS.findLongestCommonSubstrings(s1, s2, threshold);
        System.out.println("Common substrings with length at least " + threshold + ": " + lcsList);
    }
}
