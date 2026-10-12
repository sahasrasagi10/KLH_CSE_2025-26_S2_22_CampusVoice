package algorithms;

import java.util.*;

public class BipartiteMatching {

    private final int feedbackCount;
    private final int slotCount;
    private final boolean[][] graph;
    private final int[] matchedFeedback;

    public BipartiteMatching(int feedbackCount, int slotCount) {
        this.feedbackCount = feedbackCount;
        this.slotCount = slotCount;
        this.graph = new boolean[feedbackCount][slotCount];
        this.matchedFeedback = new int[slotCount];
        Arrays.fill(matchedFeedback, -1);
    }

    public void connect(int feedback, int slot) {
        if (feedback >= 0 && feedback < feedbackCount && slot >= 0 && slot < slotCount) {
            graph[feedback][slot] = true;
        }
    }

    public int maximumMatching() {
        int matches = 0;
        for (int feedback = 0; feedback < feedbackCount; feedback++) {
            boolean[] visited = new boolean[slotCount];
            if (findMatch(feedback, visited)) {
                matches++;
            }
        }
        return matches;
    }

    private boolean findMatch(int feedback, boolean[] visited) {
        for (int slot = 0; slot < slotCount; slot++) {
            if (!graph[feedback][slot] || visited[slot]) continue;
            visited[slot] = true;

            if (matchedFeedback[slot] == -1 || findMatch(matchedFeedback[slot], visited)) {
                matchedFeedback[slot] = feedback;
                return true;
            }
        }
        return false;
    }

    public int[] getMatches() {
        return Arrays.copyOf(matchedFeedback, matchedFeedback.length);
    }
}
