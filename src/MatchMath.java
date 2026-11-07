import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

public final class MatchMath {
    public record ScoreProbability(double chance, int homeGoals, int awayGoals) { }
    public record Prediction(double homeWin, double draw, double awayWin, List<ScoreProbability> likelyScores) { }
    private MatchMath() { }

    public static Prediction predict(Team home, Team away) {
        int difference = home.strength() - away.strength();
        double homeMean = clamp(1.63 + difference * 0.035, 0.35, 3.25);
        double awayMean = clamp(1.20 - difference * 0.030, 0.30, 3.00);
        // Cada chamada gera uma nova amostra de jogos, mantendo as forças como base.
        Random random = new Random();
        int simulations = 12000, homeWins = 0, draws = 0, awayWins = 0;
        int[][] scoreCounts = new int[8][8];
        for (int i = 0; i < simulations; i++) {
            int h = sampleGoals(random, homeMean), a = sampleGoals(random, awayMean);
            if (h > a) homeWins++; else if (h == a) draws++; else awayWins++;
            if (h < 8 && a < 8) scoreCounts[h][a]++;
        }
        List<ScoreProbability> scores = new ArrayList<>();
        for (int h=0;h<8;h++) for (int a=0;a<8;a++)
            if(scoreCounts[h][a]>0) scores.add(new ScoreProbability(scoreCounts[h][a]/(double)simulations,h,a));
        scores.sort(Comparator.comparingDouble(ScoreProbability::chance).reversed());
        return new Prediction(homeWins*100.0/simulations, draws*100.0/simulations,
                awayWins*100.0/simulations, scores.subList(0, Math.min(3,scores.size())));
    }
    private static int sampleGoals(Random random, double mean) {
        double limit=Math.exp(-mean), product=1; int count=0;
        do { count++; product*=random.nextDouble(); } while(product>limit);
        return count-1;
    }
    private static double poisson(int k, double mean) {
        double result = Math.exp(-mean);
        for (int i = 1; i <= k; i++) result *= mean / i;
        return result;
    }
    private static double clamp(double n, double low, double high) { return Math.max(low, Math.min(high, n)); }
}
