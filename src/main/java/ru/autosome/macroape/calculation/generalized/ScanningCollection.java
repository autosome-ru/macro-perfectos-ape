package ru.autosome.macroape.calculation.generalized;

import ru.autosome.ape.calculation.findPvalue.CanFindPvalue;
import ru.autosome.ape.calculation.findPvalue.FindPvalueAPE;
import ru.autosome.ape.calculation.findPvalue.FoundedPvalueInfo;
import ru.autosome.ape.calculation.findThreshold.CanFindThreshold;
import ru.autosome.ape.calculation.findThreshold.FindThresholdAPE;
import ru.autosome.ape.calculation.findThreshold.FoundedThresholdInfo;
import ru.autosome.commons.backgroundModel.GeneralizedBackgroundModel;
import ru.autosome.commons.model.BoundaryType;
import ru.autosome.commons.model.Discretizer;
import ru.autosome.commons.motifModel.Alignable;
import ru.autosome.commons.motifModel.Discretable;
import ru.autosome.commons.motifModel.ScoreDistribution;
import ru.autosome.macroape.model.ComparisonSimilarityInfo;
import ru.autosome.macroape.model.PairAligned;
import ru.autosome.macroape.model.ScanningSimilarityInfo;
import ru.autosome.macroape.model.ThresholdEvaluator;

import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Stream;

public class ScanningCollection<ModelType extends Alignable<ModelType> & Discretable<ModelType> & ScoreDistribution<BackgroundType>,
                                BackgroundType extends GeneralizedBackgroundModel> {

  protected final List<ThresholdEvaluator<ModelType>> thresholdEvaluators;

  public final ModelType queryPWM;
  public double pvalue;
  public Double queryPredefinedThreshold;
  public Discretizer roughDiscretizer, preciseDiscretizer;
  public BackgroundType background;
  public BoundaryType pvalueBoundaryType;
  public Double similarityCutoff;
  public Double preciseRecalculationCutoff; // null means that no recalculation will be performed
  public final Function<PairAligned<ModelType>, ? extends AlignedModelIntersection> calculatorOfAligned;

  public ScanningCollection(List<ThresholdEvaluator<ModelType>> thresholdEvaluators, ModelType queryPWM, Function<PairAligned<ModelType>, ? extends AlignedModelIntersection> calculatorOfAligned) {
    this.thresholdEvaluators = thresholdEvaluators;
    this.queryPWM = queryPWM;
    this.calculatorOfAligned = calculatorOfAligned;
  }

  public ComparisonSimilarityInfo comparisonInfo(FoundedPvalueInfo countByThresholdQuery,
                                                 ModelType pwmKnown,
                                                 CanFindThreshold thresholdCalculatorKnown,
                                                 Discretizer discretizer) {
    CompareModels<ModelType> calc;
    calc = new CompareModels<>(queryPWM, pwmKnown, background.volume(), discretizer, calculatorOfAligned);

    FoundedThresholdInfo knownInfo = thresholdCalculatorKnown.thresholdByPvalue(pvalue, pvalueBoundaryType);
    FoundedPvalueInfo countByThresholdKnown = knownInfo.toFoundedPvalueInfo();
    return calc.jaccard(countByThresholdQuery, countByThresholdKnown);
  }

  public ScanningSimilarityInfo similarityInfo(FoundedPvalueInfo roughCountByThresholdQuery,
                                               FoundedPvalueInfo preciseCountByThresholdQuery,
                                               ThresholdEvaluator<ModelType> knownMotifEvaluator) {
    ComparisonSimilarityInfo info;
    boolean precise = false;

    info = comparisonInfo(roughCountByThresholdQuery, knownMotifEvaluator.pwm, knownMotifEvaluator.rough, roughDiscretizer);

    if (preciseRecalculationCutoff != null &&
            info.similarity() >= preciseRecalculationCutoff &&
            knownMotifEvaluator.precise != null) {
      info = comparisonInfo(preciseCountByThresholdQuery, knownMotifEvaluator.pwm, knownMotifEvaluator.precise, preciseDiscretizer);
      precise = true;
    }
    if (similarityCutoff == null || info.similarity() >= similarityCutoff) {
      return new ScanningSimilarityInfo(knownMotifEvaluator.name, info, precise);
    } else {
      return null;
    }
  }

  public Stream<ScanningSimilarityInfo> similarityInfos() {
    CanFindPvalue roughQueryPvalueEvaluator = new FindPvalueAPE<>(queryPWM, background, roughDiscretizer);
    CanFindPvalue preciseQueryPvalueEvaluator = new FindPvalueAPE<>(queryPWM, background, preciseDiscretizer);

    double roughQueryThreshold = queryThreshold(roughDiscretizer);
    double preciseQueryThreshold = queryThreshold(preciseDiscretizer);

    FoundedPvalueInfo roughCountByThresholdQuery = roughQueryPvalueEvaluator.pvalueByThreshold(roughQueryThreshold);
    FoundedPvalueInfo preciseCountByThresholdQuery = preciseQueryPvalueEvaluator.pvalueByThreshold(preciseQueryThreshold);

    return thresholdEvaluators.stream()
        .map((ThresholdEvaluator<ModelType> knownMotifEvaluator)->
            similarityInfo(roughCountByThresholdQuery, preciseCountByThresholdQuery, knownMotifEvaluator))
        .filter(Objects::nonNull);
  }


  double queryThreshold(Discretizer discretizer) {
    if (queryPredefinedThreshold != null) {
      return queryPredefinedThreshold;
    } else {
      CanFindThreshold pvalue_calculator = new FindThresholdAPE<>(queryPWM, background, discretizer);
      return pvalue_calculator.thresholdByPvalue(pvalue, pvalueBoundaryType).threshold;
    }
  }

}
