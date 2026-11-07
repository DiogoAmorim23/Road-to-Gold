package roadtogold;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class MatchMath {
    public record ScoreProbability(double chance, int homeGoals, int awayGoals) { }
    public record Prediction(double homeWin, double draw, double awayWin, List<ScoreProbability> likelyScores) { }
    private MatchMath() { }

    public static Prediction predict(Team home, Team away) {
        int difference = home.strength() - away.strength();
        double homeMean = clamp(1.63 + difference * 0.035, 0.35, 3.25);
        double awayMean = clamp(1.20 - difference * 0.030, 0.30, 3.00);
        double homeWin = 0, draw = 0, awayWin = 0;
        List<ScoreProbability> scores = new ArrayList<>();
        for (int h = 0; h < 8; h++) for (int a = 0; a < 8; a++) {
            double p = poisson(h, homeMean) * poisson(a, awayMean);
            scores.add(new ScoreProbability(p, h, a));
            if (h > a) homeWin += p; else if (h == a) draw += p; else awayWin += p;
        }
        double total = homeWin + draw + awayWin;
        scores.sort(Comparator.comparingDouble(ScoreProbability::chance).reversed());
        return new Prediction(homeWin * 100 / total, draw * 100 / total, awayWin * 100 / total, scores.subList(0, 3));
    }
    private static double poisson(int k, double mean) {
        double result = Math.exp(-mean);
        for (int i = 1; i <= k; i++) result *= mean / i;
        return result;
    }
    private static double clamp(double n, double low, double high) { return Math.max(low, Math.min(high, n)); }
}
