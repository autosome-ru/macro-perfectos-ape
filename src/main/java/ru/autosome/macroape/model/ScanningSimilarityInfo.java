package ru.autosome.macroape.model;

import ru.autosome.commons.motifModel.Alignable;
import ru.autosome.macroape.calculation.generalized.CompareModelsCountsGiven;

public class ScanningSimilarityInfo<ModelType extends Alignable<ModelType>> extends CompareModelsCountsGiven.SimilarityInfo<ModelType> {
  public final ModelType collectionPWM;
  public final String name;
  public final boolean precise;

  public ScanningSimilarityInfo(ModelType collectionPWM, String name, PairAligned<ModelType> alignment,
                                double recognizedByBoth, double recognizedByFirst, double recognizedBySecond,
                                boolean precise) {
    super(alignment, recognizedByBoth, recognizedByFirst, recognizedBySecond);
    this.collectionPWM = collectionPWM;
    this.name = name;
    this.precise = precise;

  }
  public ScanningSimilarityInfo(ModelType collectionPWM, String name, CompareModelsCountsGiven.SimilarityInfo<ModelType> similarityInfo, boolean precise) {
    super(similarityInfo.alignment,
          similarityInfo.recognizedByBoth,
          similarityInfo.recognizedByFirst,
          similarityInfo.recognizedBySecond);
    this.collectionPWM = collectionPWM;
    this.name = name;
    this.precise = precise;
  }
}
