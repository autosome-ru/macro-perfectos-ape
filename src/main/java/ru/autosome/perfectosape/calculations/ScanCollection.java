package ru.autosome.perfectosape.calculations;

import ru.autosome.perfectosape.*;
import ru.autosome.perfectosape.backgroundModels.BackgroundModel;
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

    public SimilarityInfo(PWM collectionPWM, PWMAligned alignment, double recognizedByBoth, double recognizedByFirst, double recognizedBySecond, boolean precise) {
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
  }

  public final MotifEvaluatorCollection motifEvaluatorCollection;
  public final PWM queryPWM;
  public double pvalue;
  public Double queryPredefinedThreshold;
  public Double roughDiscretization, preciseDiscretization;
  public BackgroundModel queryBackground, collectionBackground;
  public BoundaryType pvalueBoundaryType;
  public Integer maxHashSize, maxPairHashSize;
  Double similarityCutoff;
  Double preciseRecalculationCutoff; // null means that no recalculation will be performed


  public ScanCollection(MotifEvaluatorCollection motifEvaluatorCollection, PWM queryPWM) {
    this.motifEvaluatorCollection = motifEvaluatorCollection;
    this.queryPWM = queryPWM;
  }

  // TODO: fix this trash
  public List<ScanCollection.SimilarityInfo> similarityInfos() throws HashOverflowException {
    List<ScanCollection.SimilarityInfo> result = new ArrayList<ScanCollection.SimilarityInfo>(motifEvaluatorCollection.size());
    for (MotifEvaluatorCollection.MotifEvaluator knownMotifEvaluator: motifEvaluatorCollection) {
      ComparePWM.SimilarityInfo info;
      boolean precise = false;

      ComparePWM roughCalculation = new ComparePWM(queryPWM, knownMotifEvaluator.pwm,
                                                   queryBackground, collectionBackground,
                                                   new FindPvalueAPE(queryPWM, roughDiscretization, queryBackground, maxHashSize),
                                                   knownMotifEvaluator.pvalueCalculator,
                                                   roughDiscretization, maxPairHashSize);
      info = roughCalculation.jaccard(queryThreshold(roughDiscretization),
                                      collectionThreshold(roughDiscretization, knownMotifEvaluator.pwm));

      if (info.similarity() >= preciseRecalculationCutoff) {
        ComparePWM preciseCalculation = new ComparePWM(queryPWM, knownMotifEvaluator.pwm,
                                                     queryBackground, collectionBackground,
                                                     new FindPvalueAPE(queryPWM, preciseDiscretization, queryBackground, maxHashSize),
                                                     knownMotifEvaluator.pvalueCalculator,
                                                     preciseDiscretization, maxPairHashSize);
        info = roughCalculation.jaccard(queryThreshold(preciseDiscretization),
                                        collectionThreshold(preciseDiscretization, knownMotifEvaluator.pwm));
        precise = true;
      }
      if (info.similarity() >= similarityCutoff) {
        result.add(new SimilarityInfo(knownMotifEvaluator.pwm, info, precise));
      }
    }
    return result;
  }


  double queryThreshold(double discretization) throws HashOverflowException {
    if (queryPredefinedThreshold != null) {
      return queryPredefinedThreshold;
    } else {
      CanFindThreshold pvalue_calculator = new FindThresholdAPE(queryPWM,
                                                                queryBackground,
                                                                discretization,
                                                                maxHashSize);
      return pvalue_calculator.thresholdByPvalue(pvalue, pvalueBoundaryType).threshold;
    }
  }

  double collectionThreshold(double discretization, PWM pwm) throws HashOverflowException {
    CanFindThreshold pvalue_calculator = new FindThresholdAPE(pwm,
                                                              collectionBackground,
                                                              discretization,
                                                              maxHashSize);
    return pvalue_calculator.thresholdByPvalue(pvalue, pvalueBoundaryType).threshold;

  }
}
