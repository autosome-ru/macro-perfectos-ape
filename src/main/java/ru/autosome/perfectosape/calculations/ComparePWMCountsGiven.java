package ru.autosome.perfectosape.calculations;

import ru.autosome.perfectosape.MotifsAligned;
import ru.autosome.perfectosape.Position;
import ru.autosome.perfectosape.calculations.ScoringModelDistributions.CountingPWM;

import java.util.ArrayList;
import java.util.List;

public class ComparePWMCountsGiven {
  public final CountingPWM firstPWMCounting;
  public final CountingPWM secondPWMCounting;

  public final Double discretization; // PWMs are already stored discreted, disretization needed in order to upscale thresholds
  public Integer maxPairHashSize;

  public ComparePWMCountsGiven(CountingPWM firstPWMCounting, CountingPWM secondPWMCounting,
                               Double discretization, Integer maxPairHashSize) {
    this.firstPWMCounting = firstPWMCounting;
    this.secondPWMCounting = secondPWMCounting;
    this.discretization = discretization;
    this.maxPairHashSize = maxPairHashSize;
  }

  // TODO: Extract disretizator as a class. Operations of discretization are duplicated many times, and logic isn't trivial (case of null discretization, ceiling mode)
  private double upscaleThreshold(double threshold) {
    if (discretization == null) {
      return threshold;
    } else {
      return threshold * discretization;
    }
  }

  private List<Position> relative_alignments() {
    List<Position> result = new ArrayList<Position>();
    for(int shift = -secondPWMCounting.length(); shift <= firstPWMCounting.length(); ++shift) {
      result.add(new Position(shift, true));
      result.add(new Position(shift, false));
    }
    return result;
  }

  double firstCountRenormMultiplier(MotifsAligned alignment) {
    return Math.pow(firstPWMCounting.background.volume(), alignment.length() - firstPWMCounting.length());
  }
  double secondCountRenormMultiplier(MotifsAligned alignment) {
    return Math.pow(secondPWMCounting.background.volume(), alignment.length() - secondPWMCounting.length());
  }

  public ComparePWM.SimilarityInfo jaccard(double thresholdFirst, double thresholdSecond,
                                double firstCount, double secondCount) throws HashOverflowException {
    double bestSimilarity = -1;
    ComparePWM.SimilarityInfo bestSimilarityInfo = null;
    for (Position position: relative_alignments()) {
      ComparePWM.SimilarityInfo similarityInfo = jaccardAtPosition(thresholdFirst, thresholdSecond, firstCount, secondCount, position);
      double similarity = similarityInfo.similarity();
      if (similarity > bestSimilarity) {
        bestSimilarity = similarity;
        bestSimilarityInfo = similarityInfo;
      }
    }
    return bestSimilarityInfo;
  }

  public ComparePWM.SimilarityInfo jaccardAtPosition(double thresholdFirst, double thresholdSecond,
                                                     double firstCount, double secondCount,
                                                     Position position) throws HashOverflowException {
    MotifsAligned<CountingPWM> alignment = new MotifsAligned<CountingPWM>(firstPWMCounting, secondPWMCounting, position);
    AlignedPWMIntersection calculator = new AlignedPWMIntersection(alignment);
    calculator.maxPairHashSize = maxPairHashSize;
    double intersection = calculator.count_in_intersection(upscaleThreshold(thresholdFirst),
                                                           upscaleThreshold(thresholdSecond));

    double firstCountRenormed = firstCount * firstCountRenormMultiplier(alignment);
    double secondCountRenormed = secondCount * secondCountRenormMultiplier(alignment);

    return new ComparePWM.SimilarityInfo(alignment, intersection, firstCountRenormed, secondCountRenormed);
  }

}
