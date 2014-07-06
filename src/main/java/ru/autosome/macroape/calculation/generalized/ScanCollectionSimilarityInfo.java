package ru.autosome.macroape.calculation.generalized;

import ru.autosome.commons.motifModel.Alignable;
import ru.autosome.commons.motifModel.Named;
import ru.autosome.macroape.model.PairAligned;

public class ScanCollectionSimilarityInfo<ModelType extends Named & Alignable<ModelType>> extends SimilarityInfo {
  public final ModelType collectionPWM;
  public final boolean precise;

  public ScanCollectionSimilarityInfo(ModelType collectionPWM, PairAligned<ModelType> alignment,
                                      double recognizedByBoth, double recognizedByFirst, double recognizedBySecond,
                                      boolean precise) {
    super(alignment, recognizedByBoth, recognizedByFirst, recognizedBySecond);
    this.collectionPWM = collectionPWM;
    this.precise = precise;
  }
  public ScanCollectionSimilarityInfo(ModelType collectionPWM, SimilarityInfo similarityInfo, boolean precise) {
    super(similarityInfo.alignment,
          similarityInfo.recognizedByBoth,
          similarityInfo.recognizedByFirst,
          similarityInfo.recognizedBySecond);
    this.collectionPWM = collectionPWM;
    this.precise = precise;
  }
  public String name() {
    return collectionPWM.getName();
  }
}
