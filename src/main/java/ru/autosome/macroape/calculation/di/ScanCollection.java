package ru.autosome.macroape.calculation.di;

import ru.autosome.commons.backgroundModel.di.DiBackgroundModel;
import ru.autosome.commons.model.Discretizer;
import ru.autosome.commons.motifModel.di.DiPWM;
import ru.autosome.macroape.model.ThresholdEvaluator;

import java.util.List;

public class ScanCollection extends ru.autosome.macroape.calculation.generalized.ScanCollection<DiPWM, DiBackgroundModel> {

  protected CompareModelsCountsGiven calc_counts_given(DiPWM firstMotif,
                                                       DiPWM secondMotif,
                                                       DiBackgroundModel background,
                                                       Discretizer discretizer) {
  return new CompareModelsCountsGiven(firstMotif, secondMotif, background, discretizer);
}

  public ScanCollection(List<ThresholdEvaluator<DiPWM>> thresholdEvaluators, DiPWM queryPWM) {
    super(thresholdEvaluators, queryPWM);
  }
}

