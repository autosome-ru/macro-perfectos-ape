package ru.autosome.macroape.calculation.di;

import ru.autosome.ape.calculation.findPvalue.CanFindPvalue;
import ru.autosome.commons.backgroundModel.di.DiBackgroundModel;
import ru.autosome.commons.model.Discretizer;
import ru.autosome.commons.motifModel.di.DiPWM;
import ru.autosome.macroape.calculation.generalized.CompareModels;
import ru.autosome.macroape.model.ThresholdEvaluator;

import java.util.List;

public class ScanCollection extends ru.autosome.macroape.calculation.generalized.ScanCollection<DiPWM, DiBackgroundModel> {


  protected CompareModels<DiPWM, DiBackgroundModel> calculation(
      DiPWM firstMotif, DiPWM secondMotif,
      DiBackgroundModel background,
      CanFindPvalue firstPvalueCalculator, CanFindPvalue secondPvalueCalculator,
      Discretizer discretizer
  ) {
    return new CompareModels<>(firstMotif, secondMotif,
                               background,
                               firstPvalueCalculator, secondPvalueCalculator,
                               discretizer,
                               new CompareModelsCountsGiven(firstMotif, secondMotif, background, discretizer));
  }

  public ScanCollection(List<ThresholdEvaluator<DiPWM>> thresholdEvaluators, DiPWM queryPWM) {
    super(thresholdEvaluators, queryPWM);
  }
}

