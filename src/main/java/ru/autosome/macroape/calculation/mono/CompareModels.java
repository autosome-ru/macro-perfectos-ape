package ru.autosome.macroape.calculation.mono;

import ru.autosome.ape.calculation.findPvalue.CanFindPvalue;
import ru.autosome.commons.backgroundModel.mono.BackgroundModel;
import ru.autosome.commons.model.Discretizer;
import ru.autosome.commons.motifModel.mono.PWM;

public class CompareModels extends ru.autosome.macroape.calculation.generalized.CompareModels<PWM, BackgroundModel> {

  public CompareModels(PWM firstPWM, PWM secondPWM, BackgroundModel firstBackground, BackgroundModel secondBackground,
                       CanFindPvalue firstPvalueCalculator, CanFindPvalue secondPvalueCalculator, Discretizer discretizer, Integer maxPairHashSize) {
    super(firstPWM, secondPWM, firstBackground, secondBackground, firstPvalueCalculator, secondPvalueCalculator, discretizer, maxPairHashSize);
  }

  public CompareModels(PWM firstPWM, PWM secondPWM, BackgroundModel firstBackground, BackgroundModel secondBackground,
                       Discretizer discretizer, Integer maxPairHashSize, Integer maxHashSize) {
    super(firstPWM, secondPWM, firstBackground, secondBackground, discretizer, maxPairHashSize, maxHashSize);
  }

  @Override
  protected ru.autosome.macroape.calculation.mono.CompareModelsCountsGiven calculatorWithCountsGiven() {
    return new CompareModelsCountsGiven(firstPWM, secondPWM,
                                     firstBackground, secondBackground,
                                     discretizer, maxPairHashSize);
  }
}
