package algorithms;

import java.util.*;

public class MaxFlow {

    private int bfs(int[][] capacity, int[][] flow, int source, int sink, int[] parent) {
        Arrays.fill(parent, -1);
        parent[source] = -2;

        int[] pathCapacity = new int[capacity.length];
        pathCapacity[source] = Integer.MAX_VALUE;

        Queue<Integer> queue = new LinkedList<>();
        queue.offer(source);

        while (!queue.isEmpty()) {
            int current = queue.poll();

            for (int next = 0; next < capacity.length; next++) {
                int residual = capacity[current][next] - flow[current][next];

                if (parent[next] == -1 && residual > 0) {
                    parent[next] = current;
                    pathCapacity[next] = Math.min(pathCapacity[current], residual);

                    if (next == sink) {
                        return pathCapacity[next];
                    }

                    queue.offer(next);
                }
            }
        }

        return 0;
    }

    public int edmondsKarp(int[][] capacity, int source, int sink) {
        int n = capacity.length;
        int[][] flow = new int[n][n];
        int[] parent = new int[n];
        int maxFlow = 0;

        int newFlow;

        while ((newFlow = bfs(capacity, flow, source, sink, parent)) > 0) {
            maxFlow += newFlow;

            int current = sink;
            while (current != source) {
                int previous = parent[current];
                flow[previous][current] += newFlow;
                flow[current][previous] -= newFlow;
                current = previous;
            }
        }

        return maxFlow;
    }

    public List<String> getAssignments(
            int[][] capacity,
            int source,
            int issueStart,
            int issueCount,
            int departmentStart,
            int departmentCount,
            int sink) {

        int n = capacity.length;
        int[][] flow = new int[n][n];
        int[] parent = new int[n];

        while (bfs(capacity, flow, source, sink, parent) > 0) {
            int current = sink;
            int pathFlow = Integer.MAX_VALUE;

            while (current != source) {
                int previous = parent[current];
                pathFlow = Math.min(pathFlow, capacity[previous][current] - flow[previous][current]);
                current = previous;
            }

            current = sink;
            while (current != source) {
                int previous = parent[current];
                flow[previous][current] += pathFlow;
                flow[current][previous] -= pathFlow;
                current = previous;
            }
        }

        List<String> assignments = new ArrayList<>();

        for (int i = 0; i < issueCount; i++) {
            int issueNode = issueStart + i;

            for (int j = 0; j < departmentCount; j++) {
                int departmentNode = departmentStart + j;

                if (flow[issueNode][departmentNode] > 0) {
                    assignments.add(
                            "Issue " + (i + 1)
                                    + " -> Department " + (j + 1)
                                    + " (capacity used: "
                                    + flow[issueNode][departmentNode] + ")"
                    );
                }
            }
        }

        return assignments;
    }
}
