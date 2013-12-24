package ru.autosome.perfectosape.calculations;

import ru.autosome.perfectosape.HashOverflowException;
import ru.autosome.perfectosape.PWMAligned;
import ru.autosome.perfectosape.Position;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ComparePWM {
  static public class SimilarityInfo extends CompareAligned.SimilarityInfo {
    public final PWMAligned alignment;
    public SimilarityInfo(PWMAligned alignment, double recognizedByBoth, double recognizedByFirst, double recognizedBySecond,
                          Double firstVocabularyVolume, Double secondVocabularyVolume) {
      super(recognizedByBoth, recognizedByFirst, recognizedBySecond, firstVocabularyVolume, secondVocabularyVolume);
      this.alignment = alignment;
    }
    public SimilarityInfo(PWMAligned alignment, CompareAligned.SimilarityInfo similarityInfo) {
      super(similarityInfo.recognizedByBoth,
            similarityInfo.recognizedByFirst,
            similarityInfo.recognizedBySecond,
            similarityInfo.firstVocabularyVolume,
            similarityInfo.secondVocabularyVolume);
      this.alignment = alignment;
    }
  }

  public final CountingPWM firstPWM;
  public final CountingPWM secondPWM;

  public Integer maxPairHashSize;

  public ComparePWM(CountingPWM firstPWM, CountingPWM secondPWM) {
    this.firstPWM = firstPWM;
    this.secondPWM = secondPWM;
  }


  private List<Position> relative_alignments() {
    List<Position> result = new ArrayList<Position>();
    for(int shift = -secondPWM.pwm.length(); shift <= firstPWM.pwm.length(); ++shift) {
      result.add(new Position(shift, true));
      result.add(new Position(shift, false));
    }
    return result;
  }

  private List<SimilarityInfo> all_jaccards(double threshold_first, double threshold_second) throws HashOverflowException {
    List<SimilarityInfo> result = new ArrayList<SimilarityInfo>();
    for (Position relative_position: relative_alignments()) {
      PWMAligned alignment = new PWMAligned(firstPWM.pwm, secondPWM.pwm, relative_position);
      CompareAligned.SimilarityInfo similarityInfo =
       new CompareAligned(firstPWM, secondPWM, relative_position).jaccard(threshold_first, threshold_second);
      result.add(new SimilarityInfo(alignment, similarityInfo));
    }
    return result;
  }

  private Map<Position, Double> all_intersections(double threshold_first, double threshold_second) throws HashOverflowException {
    Map<Position, Double> result = new HashMap<Position, Double>();
    for (Position relative_position: relative_alignments()) {
      PWMAligned alignment = new PWMAligned(firstPWM.pwm, secondPWM.pwm, relative_position);
      double intersection = new CompareAligned(firstPWM,
                                               secondPWM,
                                               relative_position).count_in_intersection(threshold_first, threshold_second);
      result.put(relative_position, intersection);
    }
    return result;
  }

  public SimilarityInfo jaccard(double threshold_first, double threshold_second) throws HashOverflowException {
    double firstCount = firstPWM.count_by_threshold(threshold_first);
    double secondCount = secondPWM.count_by_threshold(threshold_second);

    Map<Position, Double> intersections = all_intersections(threshold_first, threshold_second);
    Position bestPosition = null;
    double bestSimilarity = -1;
    for (Position position: intersections.keySet()) {
      double intersection = intersections.get(position);
      PWMAligned alignment = new PWMAligned(firstPWM.pwm, secondPWM.pwm, position);
      double firstCountRenormed = firstCount * Math.pow(firstPWM.background.volume(), alignment.length() - firstPWM.pwm.length());
      double secondCountRenormed = secondCount * Math.pow(secondPWM.background.volume(), alignment.length() - secondPWM.pwm.length());
      double similarity = CompareAligned.jaccardByCounts(firstCountRenormed, secondCountRenormed, intersection);
      if (similarity > bestSimilarity) {
        bestPosition = position;
        bestSimilarity = similarity;
      }
    }
    PWMAligned bestAlignment = new PWMAligned(firstPWM.pwm, secondPWM.pwm, bestPosition);
    double firstCountRenormedBest = firstCount * Math.pow(firstPWM.background.volume(), bestAlignment.length() - firstPWM.pwm.length());
    double secondCountRenormedBest = secondCount * Math.pow(secondPWM.background.volume(), bestAlignment.length() - secondPWM.pwm.length());
    double firstPWMVocabularyVolume = Math.pow(firstPWM.background.volume(), bestAlignment.length());
    double secondPWMVocabularyVolume = Math.pow(secondPWM.background.volume(), bestAlignment.length());

    return new SimilarityInfo(bestAlignment,
                              intersections.get(bestPosition),
                              firstCountRenormedBest, secondCountRenormedBest,
                              firstPWMVocabularyVolume, secondPWMVocabularyVolume);
  }

  public SimilarityInfo jaccard_by_pvalue(double pvalue) throws HashOverflowException {
    double threshold_first = firstPWM.threshold(pvalue).threshold;
    double threshold_second = secondPWM.threshold(pvalue).threshold;
    return jaccard(threshold_first, threshold_second);
  }

  public SimilarityInfo jaccard_by_weak_pvalue(double pvalue) throws HashOverflowException {
    double threshold_first = firstPWM.weak_threshold(pvalue).threshold;
    double threshold_second = secondPWM.weak_threshold(pvalue).threshold;
    return jaccard(threshold_first, threshold_second);
  }
}
