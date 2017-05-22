package ru.autosome.macroape.calculation.mono;

import ru.autosome.ape.calculation.findPvalue.CanFindPvalue;
import ru.autosome.commons.backgroundModel.mono.BackgroundModel;
import ru.autosome.commons.model.Discretizer;
import ru.autosome.commons.motifModel.mono.PWM;
import ru.autosome.macroape.model.ThresholdEvaluator;

import java.util.List;

public class ScanCollection extends ru.autosome.macroape.calculation.generalized.ScanCollection<PWM, BackgroundModel> {


  protected CompareModels calculation(PWM firstMotif, PWM secondMotif,  BackgroundModel firstBackground, BackgroundModel secondBackground,
                                      CanFindPvalue firstPvalueCalculator, CanFindPvalue secondPvalueCalculator, Discretizer discretizer) {
    return new CompareModels( firstMotif, secondMotif, firstBackground, secondBackground,
                              firstPvalueCalculator, secondPvalueCalculator, discretizer);
  }

  public ScanCollection(List<ThresholdEvaluator<PWM>> thresholdEvaluators, PWM queryPWM) {
    super(thresholdEvaluators, queryPWM);
  }
}
