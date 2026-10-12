package algorithms;

import java.util.*;

public class PriorityOptimizer {

    public static class Issue {
        private final String feedbackId;
        private final Set<String> students;
        private final int upvotes;

        public Issue(String feedbackId, Set<String> students, int upvotes) {
            this.feedbackId = feedbackId;
            this.students = new HashSet<>(students);
            this.upvotes = upvotes;
        }

        public String getFeedbackId() {
            return feedbackId;
        }

        public Set<String> getStudents() {
            return students;
        }

        public int getUpvotes() {
            return upvotes;
        }
    }

    public List<Issue> greedyMaximumCoverage(List<Issue> issues, int limit) {
        List<Issue> remaining = new ArrayList<>(issues);
        List<Issue> selected = new ArrayList<>();
        Set<String> coveredStudents = new HashSet<>();

        while (!remaining.isEmpty() && selected.size() < limit) {
            Issue best = null;
            int bestGain = -1;

            for (Issue issue : remaining) {
                int gain = 0;

                for (String student : issue.getStudents()) {
                    if (!coveredStudents.contains(student)) {
                        gain++;
                    }
                }

                if (gain > bestGain
                        || (gain == bestGain
                        && best != null
                        && issue.getUpvotes() > best.getUpvotes())) {
                    best = issue;
                    bestGain = gain;
                }
            }

            if (best == null) {
                break;
            }

            selected.add(best);
            coveredStudents.addAll(best.getStudents());
            remaining.remove(best);
        }

        return selected;
    }
}
