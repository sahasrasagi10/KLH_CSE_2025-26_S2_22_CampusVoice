package algorithms;

public class LevenshteinDistance {

    public int distance(String a, String b) {

        a = normalize(a);
        b = normalize(b);

        int n = a.length();
        int m = b.length();

        int[][] dp = new int[n + 1][m + 1];

        for (int i = 0; i <= n; i++) {
            dp[i][0] = i;
        }

        for (int j = 0; j <= m; j++) {
            dp[0][j] = j;
        }

        for (int i = 1; i <= n; i++) {

            for (int j = 1; j <= m; j++) {

                int cost;

                if (a.charAt(i - 1) == b.charAt(j - 1)) {
                    cost = 0;
                } else {
                    cost = 1;
                }

                dp[i][j] = Math.min(
                        Math.min(
                                dp[i - 1][j] + 1,
                                dp[i][j - 1] + 1),
                        dp[i - 1][j - 1] + cost);
            }
        }

        return dp[n][m];
    }

    public double similarity(String a, String b) {

        a = normalize(a);
        b = normalize(b);

        if (a.equals(b)) {
            return 1.0;
        }

        if (a.isEmpty() && b.isEmpty()) {
            return 1.0;
        }

        int maxLength = Math.max(a.length(), b.length());

        if (maxLength == 0) {
            return 1.0;
        }

        int editDistance = distance(a, b);

        return 1.0 - ((double) editDistance / maxLength);
    }

    private String normalize(String text) {

        if (text == null) {
            return "";
        }

        return text
                .toLowerCase()
                .replaceAll("[^a-z0-9\\s]", " ")
                .replaceAll("\\s+", " ")
                .trim();
    }
}