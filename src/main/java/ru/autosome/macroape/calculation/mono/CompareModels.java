package ru.autosome.macroape.calculation.mono;

import ru.autosome.ape.calculation.findPvalue.CanFindPvalue;
import ru.autosome.commons.backgroundModel.mono.BackgroundModel;
import ru.autosome.commons.motifModel.mono.PWM;

public class CompareModels extends ru.autosome.macroape.calculation.generalized.CompareModels<PWM, BackgroundModel> {

  public CompareModels(PWM firstPWM, PWM secondPWM, BackgroundModel firstBackground, BackgroundModel secondBackground,
                       CanFindPvalue firstPvalueCalculator, CanFindPvalue secondPvalueCalculator, Double discretization, Integer maxPairHashSize) {
    super(firstPWM, secondPWM, firstBackground, secondBackground, firstPvalueCalculator, secondPvalueCalculator, discretization, maxPairHashSize);
  }

  public CompareModels(PWM firstPWM, PWM secondPWM, BackgroundModel firstBackground, BackgroundModel secondBackground,
                       Double discretization, Integer maxPairHashSize, Integer maxHashSize) {
    super(firstPWM, secondPWM, firstBackground, secondBackground, discretization, maxPairHashSize, maxHashSize);
  }

  @Override
  protected ru.autosome.macroape.calculation.mono.CompareModelsCountsGiven calculatorWithCountsGiven() {
    return new CompareModelsCountsGiven(firstPWM, secondPWM,
                                     firstBackground, secondBackground,
                                     discretization, maxPairHashSize);
  }
}
