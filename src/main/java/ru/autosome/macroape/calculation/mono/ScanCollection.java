package ru.autosome.macroape.calculation.mono;

import ru.autosome.commons.model.BoundaryType;
import ru.autosome.macroape.model.PairAligned;
import ru.autosome.commons.backgroundModel.mono.BackgroundModel;
import ru.autosome.ape.model.exception.HashOverflowException;
import ru.autosome.ape.calculation.findPvalue.CanFindPvalue;
import ru.autosome.ape.calculation.findPvalue.FindPvalueAPE;
import ru.autosome.ape.calculation.findThreshold.CanFindThreshold;
import ru.autosome.ape.calculation.findThreshold.FindThresholdAPE;
import ru.autosome.commons.motifModel.mono.PWM;

import java.util.ArrayList;
import java.util.List;

public class ScanCollection {

  static public class SimilarityInfo extends ru.autosome.macroape.calculation.generalized.SimilarityInfo {
    public final PWM collectionPWM;
    public final boolean precise;

    public SimilarityInfo(PWM collectionPWM, PairAligned alignment, double recognizedByBoth, double recognizedByFirst, double recognizedBySecond, boolean precise) {
      super(alignment, recognizedByBoth, recognizedByFirst, recognizedBySecond);
      this.collectionPWM = collectionPWM;
      this.precise = precise;
    }
    public SimilarityInfo(PWM collectionPWM, ru.autosome.macroape.calculation.generalized.SimilarityInfo similarityInfo, boolean precise) {
      super(similarityInfo.alignment,
            similarityInfo.recognizedByBoth,
            similarityInfo.recognizedByFirst,
            similarityInfo.recognizedBySecond);
      this.collectionPWM = collectionPWM;
      this.precise = precise;
    }
    public String name() {
      return collectionPWM.name;
    }
  }

  public static class ThresholdEvaluator {
    public final PWM pwm;
    public final CanFindThreshold roughThresholdCalculator;
    public final CanFindThreshold preciseThresholdCalculator;

    public final CanFindPvalue roughPvalueCalculator;
    public final CanFindPvalue precisePvalueCalculator;

    public ThresholdEvaluator(PWM pwm,
                              CanFindThreshold roughThresholdCalculator, CanFindThreshold preciseThresholdCalculator,
                              CanFindPvalue roughPvalueCalculator, CanFindPvalue precisePvalueCalculator) {
      this.pwm = pwm;
      this.roughThresholdCalculator = roughThresholdCalculator;
      this.preciseThresholdCalculator = preciseThresholdCalculator;
      this.roughPvalueCalculator = roughPvalueCalculator;
      this.precisePvalueCalculator = precisePvalueCalculator;
    }
  }

  final List<ThresholdEvaluator> thresholdEvaluators;

  public final PWM queryPWM;
  public double pvalue;
  public Double queryPredefinedThreshold;
  public Double roughDiscretization, preciseDiscretization;
  public BackgroundModel queryBackground, collectionBackground;
  public BoundaryType pvalueBoundaryType;
  public Integer maxHashSize, maxPairHashSize;
  public Double similarityCutoff;
  public Double preciseRecalculationCutoff; // null means that no recalculation will be performed


  public ScanCollection(List<ThresholdEvaluator> thresholdEvaluators, PWM queryPWM) {
    this.thresholdEvaluators = thresholdEvaluators;
    this.queryPWM = queryPWM;
  }

  public List<ScanCollection.SimilarityInfo> similarityInfos() throws HashOverflowException {
    List<ScanCollection.SimilarityInfo> result;
    result = new ArrayList<SimilarityInfo>(thresholdEvaluators.size());

    FindPvalueAPE roughQueryPvalueEvaluator = new FindPvalueAPE<PWM, BackgroundModel>(queryPWM, queryBackground, roughDiscretization, maxHashSize);
    FindPvalueAPE preciseQueryPvalueEvaluator = new FindPvalueAPE<PWM, BackgroundModel>(queryPWM, queryBackground, preciseDiscretization, maxHashSize);

    double roughQueryThreshold = queryThreshold(roughDiscretization);
    double preciseQueryThreshold = queryThreshold(preciseDiscretization);


    for (ThresholdEvaluator knownMotifEvaluator: thresholdEvaluators) {
      ru.autosome.macroape.calculation.generalized.SimilarityInfo info;
      boolean precise = false;
      ru.autosome.macroape.calculation.mono.CompareModel roughCalculation = new ru.autosome.macroape.calculation.mono.CompareModel(queryPWM, knownMotifEvaluator.pwm,
                                                   queryBackground, collectionBackground,
                                                   roughQueryPvalueEvaluator,
                                                   knownMotifEvaluator.roughPvalueCalculator,
                                                   roughDiscretization, maxPairHashSize);

      Double roughCollectionThreshold = knownMotifEvaluator.roughThresholdCalculator
                                         .thresholdByPvalue(pvalue, pvalueBoundaryType).threshold;

      info = roughCalculation.jaccard(roughQueryThreshold,
                                      roughCollectionThreshold);

      if (preciseRecalculationCutoff != null &&
         info.similarity() >= preciseRecalculationCutoff &&
         knownMotifEvaluator.preciseThresholdCalculator != null) {
        ru.autosome.macroape.calculation.mono.CompareModel preciseCalculation = new ru.autosome.macroape.calculation.mono.CompareModel(queryPWM, knownMotifEvaluator.pwm,
                                                     queryBackground, collectionBackground,
                                                     preciseQueryPvalueEvaluator,
                                                     knownMotifEvaluator.precisePvalueCalculator,
                                                     preciseDiscretization, maxPairHashSize);

        Double preciseCollectionThreshold = knownMotifEvaluator.preciseThresholdCalculator
                                             .thresholdByPvalue(pvalue, pvalueBoundaryType).threshold;

        info = preciseCalculation.jaccard(preciseQueryThreshold,
                                        preciseCollectionThreshold);
        precise = true;
      }
      if (similarityCutoff == null || info.similarity() >= similarityCutoff) {
        result.add(new SimilarityInfo(knownMotifEvaluator.pwm, info, precise));
      }
    }
    return result;
  }


  double queryThreshold(Double discretization) throws HashOverflowException {
    if (queryPredefinedThreshold != null) {
      return queryPredefinedThreshold;
    } else {
      CanFindThreshold pvalue_calculator = new FindThresholdAPE<PWM, BackgroundModel>(queryPWM, queryBackground, discretization, maxHashSize);
      return pvalue_calculator.thresholdByPvalue(pvalue, pvalueBoundaryType).threshold;
    }
  }

}
