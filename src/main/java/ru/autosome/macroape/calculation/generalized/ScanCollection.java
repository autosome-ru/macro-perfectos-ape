package ru.autosome.macroape.calculation.generalized;

import ru.autosome.ape.calculation.findPvalue.CanFindPvalue;
import ru.autosome.ape.calculation.findPvalue.FindPvalueAPE;
import ru.autosome.ape.calculation.findThreshold.CanFindThreshold;
import ru.autosome.ape.calculation.findThreshold.FindThresholdAPE;
import ru.autosome.commons.backgroundModel.GeneralizedBackgroundModel;
import ru.autosome.commons.model.BoundaryType;
import ru.autosome.commons.model.Discretizer;
import ru.autosome.commons.motifModel.Alignable;
import ru.autosome.commons.motifModel.Discretable;
import ru.autosome.commons.motifModel.ScoreDistribution;
import ru.autosome.macroape.model.PairAligned;

import java.util.ArrayList;
import java.util.List;

public abstract class ScanCollection <ModelType extends Alignable<ModelType> & Discretable<ModelType> &ScoreDistribution<BackgroundType>, BackgroundType extends GeneralizedBackgroundModel> {

  protected final List<ru.autosome.macroape.cli.generalized.ScanCollection<ModelType,BackgroundType>.ThresholdEvaluator> thresholdEvaluators;

  public final ModelType queryPWM;
  public double pvalue;
  public Double queryPredefinedThreshold;
  public Discretizer roughDiscretizer, preciseDiscretizer;
  public BackgroundType queryBackground, collectionBackground;
  public BoundaryType pvalueBoundaryType;
  public Double similarityCutoff;
  public Double preciseRecalculationCutoff; // null means that no recalculation will be performed


  public ScanCollection(List<ru.autosome.macroape.cli.generalized.ScanCollection<ModelType,BackgroundType>.ThresholdEvaluator> thresholdEvaluators, ModelType queryPWM) {
    this.thresholdEvaluators = thresholdEvaluators;
    this.queryPWM = queryPWM;
  }

  abstract protected CompareModels<ModelType, BackgroundType> calculation(ModelType firstMotif, ModelType secondMotif,
                                                                          BackgroundType firstBackground, BackgroundType secondBackground,
                                                                          CanFindPvalue firstPvalueCalculator, CanFindPvalue secondPvalueCalculator,
                                                                          Discretizer discretizer);

  public SimilarityInfo similarityInfo(FindPvalueAPE roughQueryPvalueEvaluator,
                                       FindPvalueAPE preciseQueryPvalueEvaluator,
                                       double roughQueryThreshold,
                                       double preciseQueryThreshold,
                                       ru.autosome.macroape.cli.generalized.ScanCollection<ModelType,BackgroundType>.ThresholdEvaluator knownMotifEvaluator) {
    CompareModelsCountsGiven.SimilarityInfo<ModelType> info;
    boolean precise = false;
    CompareModels<ModelType,BackgroundType> roughCalculation = calculation(
        queryPWM, knownMotifEvaluator.pwm,
        queryBackground, collectionBackground,
        roughQueryPvalueEvaluator,
        knownMotifEvaluator.rough.pvalueCalculator,
        roughDiscretizer);

    Double roughCollectionThreshold = knownMotifEvaluator.rough.thresholdCalculator
                                          .thresholdByPvalue(pvalue, pvalueBoundaryType).threshold;

    info = roughCalculation.jaccard(roughQueryThreshold,
        roughCollectionThreshold);

    if (preciseRecalculationCutoff != null &&
            info.similarity() >= preciseRecalculationCutoff &&
            knownMotifEvaluator.precise.thresholdCalculator != null) {
      CompareModels<ModelType,BackgroundType> preciseCalculation = calculation(
          queryPWM, knownMotifEvaluator.pwm,
          queryBackground, collectionBackground,
          preciseQueryPvalueEvaluator,
          knownMotifEvaluator.precise.pvalueCalculator,
          preciseDiscretizer);

      Double preciseCollectionThreshold = knownMotifEvaluator.precise.thresholdCalculator
                                              .thresholdByPvalue(pvalue, pvalueBoundaryType).threshold;

      info = preciseCalculation.jaccard(preciseQueryThreshold,
          preciseCollectionThreshold);
      precise = true;
    }
    if (similarityCutoff == null || info.similarity() >= similarityCutoff) {
      return new SimilarityInfo(knownMotifEvaluator.pwm, knownMotifEvaluator.name, info, precise);
    } else {
      return null;
    }
  }

  public List<SimilarityInfo> similarityInfos() {
    List<SimilarityInfo> result;
    result = new ArrayList<SimilarityInfo>(thresholdEvaluators.size());

    FindPvalueAPE roughQueryPvalueEvaluator = new FindPvalueAPE<ModelType, BackgroundType>(queryPWM, queryBackground, roughDiscretizer);
    FindPvalueAPE preciseQueryPvalueEvaluator = new FindPvalueAPE<ModelType, BackgroundType>(queryPWM, queryBackground, preciseDiscretizer);

    double roughQueryThreshold = queryThreshold(roughDiscretizer);
    double preciseQueryThreshold = queryThreshold(preciseDiscretizer);

    for (ru.autosome.macroape.cli.generalized.ScanCollection<ModelType,BackgroundType>.ThresholdEvaluator knownMotifEvaluator: thresholdEvaluators) {
      SimilarityInfo info = similarityInfo(roughQueryPvalueEvaluator, preciseQueryPvalueEvaluator,
          roughQueryThreshold, preciseQueryThreshold,
          knownMotifEvaluator);
      if (info != null) {
        result.add(info);
      }
    }
    return result;
  }


  double queryThreshold(Discretizer discretizer) {
    if (queryPredefinedThreshold != null) {
      return queryPredefinedThreshold;
    } else {
      CanFindThreshold pvalue_calculator = new FindThresholdAPE<ModelType, BackgroundType>(queryPWM, queryBackground, discretizer);
      return pvalue_calculator.thresholdByPvalue(pvalue, pvalueBoundaryType).threshold;
    }
  }

  public class SimilarityInfo extends CompareModelsCountsGiven.SimilarityInfo<ModelType> {
    public final ModelType collectionPWM;
    public final String name;
    public final boolean precise;

    public SimilarityInfo(ModelType collectionPWM, String name, PairAligned<ModelType> alignment,
                          double recognizedByBoth, double recognizedByFirst, double recognizedBySecond,
                          boolean precise) {
      super(alignment, recognizedByBoth, recognizedByFirst, recognizedBySecond);
      this.collectionPWM = collectionPWM;
      this.name = name;
      this.precise = precise;

    }
    public SimilarityInfo(ModelType collectionPWM, String name, CompareModelsCountsGiven.SimilarityInfo<ModelType> similarityInfo, boolean precise) {
      super(similarityInfo.alignment,
            similarityInfo.recognizedByBoth,
            similarityInfo.recognizedByFirst,
            similarityInfo.recognizedBySecond);
      this.collectionPWM = collectionPWM;
      this.name = name;
      this.precise = precise;
    }
  }
}
