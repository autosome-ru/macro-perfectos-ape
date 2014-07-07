package ru.autosome.macroape.calculation.mono;

import ru.autosome.ape.calculation.findPvalue.CanFindPvalue;
import ru.autosome.commons.backgroundModel.mono.BackgroundModel;
import ru.autosome.commons.motifModel.mono.PWM;

import java.util.List;

public class ScanCollection extends ru.autosome.macroape.calculation.generalized.ScanCollection<PWM, BackgroundModel> {


  protected CompareModels calculation(PWM firstMotif, PWM secondMotif,
                                      BackgroundModel firstBackground, BackgroundModel secondBackground,
                                      CanFindPvalue firstPvalueCalculator, CanFindPvalue secondPvalueCalculator,
                                      Double discretization, Integer max_hash_size) {
    return new CompareModels( firstMotif, secondMotif,
                              firstBackground, secondBackground,
                              firstPvalueCalculator, secondPvalueCalculator,
                              discretization, max_hash_size);
  }

  public ScanCollection(List<ru.autosome.macroape.ScanCollection.ThresholdEvaluator> thresholdEvaluators, PWM queryPWM) {
    super(thresholdEvaluators, queryPWM);
  }
}
