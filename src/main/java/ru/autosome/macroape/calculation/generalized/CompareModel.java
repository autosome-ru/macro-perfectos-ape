package ru.autosome.macroape.calculation.generalized;

import ru.autosome.macroape.model.PairAligned;
import ru.autosome.commons.backgroundModel.GeneralizedBackgroundModel;
import ru.autosome.macroape.calculation.mono.AlignedModelIntersection;

public class CompareModel {
  static public class SimilarityInfo extends AlignedModelIntersection.SimilarityInfo {
    public final PairAligned alignment;
    public SimilarityInfo(PairAligned alignment, double recognizedByBoth, double recognizedByFirst, double recognizedBySecond) {
      super(recognizedByBoth, recognizedByFirst, recognizedBySecond);
      this.alignment = alignment;
    }
    public SimilarityInfo(PairAligned alignment, AlignedModelIntersection.SimilarityInfo similarityInfo) {
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
