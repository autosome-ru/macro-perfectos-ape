package ru.autosome.perfectosape.calculations;

import ru.autosome.perfectosape.BoundaryType;
import ru.autosome.perfectosape.MotifsAligned;
import ru.autosome.perfectosape.backgroundModels.BackgroundModel;
import ru.autosome.perfectosape.calculations.ScoringModelDistributions.CountingPWM;
import ru.autosome.perfectosape.calculations.findPvalue.CanFindPvalue;
import ru.autosome.perfectosape.calculations.findPvalue.FindPvalueAPE;
import ru.autosome.perfectosape.calculations.findThreshold.CanFindThreshold;
import ru.autosome.perfectosape.calculations.findThreshold.FindThresholdAPE;
import ru.autosome.perfectosape.motifModels.PWM;

import java.util.ArrayList;
import java.util.List;

public class ScanCollection {

  static public class SimilarityInfo extends ComparePWM.SimilarityInfo {
    public PWM collectionPWM;
    public boolean precise;

    public SimilarityInfo(PWM collectionPWM, MotifsAligned alignment, double recognizedByBoth, double recognizedByFirst, double recognizedBySecond, boolean precise) {
      super(alignment, recognizedByBoth, recognizedByFirst, recognizedBySecond);
      this.collectionPWM = collectionPWM;
      this.precise = precise;
    }
    public SimilarityInfo(PWM collectionPWM, ComparePWM.SimilarityInfo similarityInfo, boolean precise) {
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
    public PWM pwm;
    public CanFindThreshold roughThresholdCalculator;
    public CanFindThreshold preciseThresholdCalculator;

    public CanFindPvalue roughPvalueCalculator;
    public CanFindPvalue precisePvalueCalculator;

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

  List<ThresholdEvaluator> thresholdEvaluators;

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
      ComparePWM.SimilarityInfo info;
      boolean precise = false;
      ComparePWM roughCalculation = new ComparePWM(new CountingPWM(queryPWM, queryBackground, maxHashSize),
                                                   new CountingPWM(knownMotifEvaluator.pwm, collectionBackground, maxHashSize),
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
        ComparePWM preciseCalculation = new ComparePWM(new CountingPWM(queryPWM, queryBackground, maxHashSize),
                                                       new CountingPWM(knownMotifEvaluator.pwm, collectionBackground, maxHashSize),
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
