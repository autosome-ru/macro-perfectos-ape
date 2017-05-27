package ru.autosome.macroape.calculation.mono;

import ru.autosome.ape.calculation.findPvalue.CanFindPvalue;
import ru.autosome.commons.backgroundModel.mono.BackgroundModel;
import ru.autosome.commons.model.Discretizer;
import ru.autosome.commons.motifModel.mono.PWM;

public class CompareModels extends ru.autosome.macroape.calculation.generalized.CompareModels<PWM, BackgroundModel> {

  public CompareModels(PWM firstPWM, PWM secondPWM, BackgroundModel background,
                       CanFindPvalue firstPvalueCalculator, CanFindPvalue secondPvalueCalculator, Discretizer discretizer) {
    super(firstPWM, secondPWM, background, firstPvalueCalculator, secondPvalueCalculator, discretizer);
  }

  public CompareModels(PWM firstPWM, PWM secondPWM, BackgroundModel background, Discretizer discretizer) {
    super(firstPWM, secondPWM, background, discretizer);
  }

  @Override
  protected ru.autosome.macroape.calculation.mono.CompareModelsCountsGiven calculatorWithCountsGiven() {
    return new CompareModelsCountsGiven(firstPWM, secondPWM,
                                     background,
                                     discretizer);
  }
}
