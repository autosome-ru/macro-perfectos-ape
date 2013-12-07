package ru.autosome.perfectosape.calculations;

import ru.autosome.perfectosape.Position;

import java.util.ArrayList;
import java.util.List;

public class ComparePWM {
  static public class SimilarityInfo extends CompareAligned.SimilarityInfo {
    public final Position alignment;
    public SimilarityInfo(Position alignment, double recognizedByBoth, double recognizedByFirst, double recognizedBySecond,
                          Double firstVocabularyVolume, Double secondVocabularyVolume) {
      super(recognizedByBoth, recognizedByFirst, recognizedBySecond, firstVocabularyVolume, secondVocabularyVolume);
      this.alignment = alignment;
    }
    public SimilarityInfo(Position alignment, CompareAligned.SimilarityInfo similarityInfo) {
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

  public Double max_pair_hash_size;

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

  private List<SimilarityInfo> all_jaccards(double threshold_first, double threshold_second) throws Exception {
    List<SimilarityInfo> result = new ArrayList<SimilarityInfo>();
    for (Position alignment: relative_alignments()) {
      CompareAligned.SimilarityInfo similarityInfo =
       new CompareAligned(firstPWM, secondPWM, alignment).jaccard(threshold_first, threshold_second);

      result.add(new SimilarityInfo(alignment, similarityInfo));
    }
    return result;
  }

  public SimilarityInfo jaccard(double threshold_first, double threshold_second) throws Exception {
    SimilarityInfo bestSimilarityInfo = null;
    for(SimilarityInfo similarityInfo: all_jaccards(threshold_first, threshold_second)) {
      if (bestSimilarityInfo == null || similarityInfo.similarity() > bestSimilarityInfo.similarity()) {
        bestSimilarityInfo = similarityInfo;
      }
    }
    return bestSimilarityInfo;
  }

  public SimilarityInfo jaccard_by_pvalue(double pvalue) throws Exception {
    double threshold_first = firstPWM.threshold(pvalue).threshold;
    double threshold_second = secondPWM.threshold(pvalue).threshold;
    return jaccard(threshold_first, threshold_second);
  }

  public SimilarityInfo jaccard_by_weak_pvalue(double pvalue) throws Exception {
    double threshold_first = firstPWM.weak_threshold(pvalue).threshold;
    double threshold_second = secondPWM.weak_threshold(pvalue).threshold;
    return jaccard(threshold_first, threshold_second);
  }
}
