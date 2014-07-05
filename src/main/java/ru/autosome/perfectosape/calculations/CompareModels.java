package ru.autosome.perfectosape.calculations;

import ru.autosome.perfectosape.PairAligned;
import ru.autosome.perfectosape.backgroundModels.GeneralizedBackgroundModel;

public class CompareModels {
  static public class SimilarityInfo extends AlignedPWMIntersection.SimilarityInfo {
    public final PairAligned alignment;
    public SimilarityInfo(PairAligned alignment, double recognizedByBoth, double recognizedByFirst, double recognizedBySecond) {
      super(recognizedByBoth, recognizedByFirst, recognizedBySecond);
      this.alignment = alignment;
    }
    public SimilarityInfo(PairAligned alignment, AlignedPWMIntersection.SimilarityInfo similarityInfo) {
      super(similarityInfo.recognizedByBoth,
            similarityInfo.recognizedByFirst,
            similarityInfo.recognizedBySecond);
      this.alignment = alignment;
    }
    public Double realPvalueFirst(GeneralizedBackgroundModel background) {
      return realPvalueFirst(background, alignment.length());
    }
    public Double realPvalueSecond(GeneralizedBackgroundModel background) {
      return realPvalueSecond(background, alignment.length());
    }

    public int shift() {
      return alignment.shift();
    }

    public String orientation() {
      return alignment.orientation();
    }

    public int overlap() {
      return alignment.overlapSize();
    }
  }
}
