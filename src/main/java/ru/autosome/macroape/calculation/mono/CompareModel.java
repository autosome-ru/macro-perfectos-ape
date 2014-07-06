package ru.autosome.macroape.calculation.mono;

import ru.autosome.ape.calculation.findPvalue.CanFindPvalue;
import ru.autosome.ape.model.exception.HashOverflowException;
import ru.autosome.commons.backgroundModel.mono.BackgroundModel;
import ru.autosome.commons.motifModel.mono.PWM;
import ru.autosome.macroape.calculation.generalized.ComparableCountsGiven;

public class CompareModel extends ru.autosome.macroape.calculation.generalized.CompareModel<PWM, BackgroundModel> {

  public CompareModel(PWM firstPWM, PWM secondPWM,
                      BackgroundModel firstBackground,
                      BackgroundModel secondBackground,
                      CanFindPvalue firstPvalueCalculator,
                      CanFindPvalue secondPvalueCalculator,
                      Double discretization, Integer maxPairHashSize) {
    super(firstPWM, secondPWM, firstBackground, secondBackground,
          firstPvalueCalculator, secondPvalueCalculator,
          discretization, maxPairHashSize);
  }

  @Override
  protected ComparableCountsGiven calculatorWithCountsGiven() {
    return new ComparePWMCountsGiven(firstPWM, secondPWM,
                                     firstBackground, secondBackground,
                                     discretization, maxPairHashSize);
  }

  double firstCount(double threshold_first) throws HashOverflowException {
    return firstPvalueCalculator
           .pvalueByThreshold(threshold_first)
           .numberOfRecognizedWords(firstBackground, firstPWM.length());
  }

  double secondCount(double threshold_second) throws HashOverflowException {
    return secondPvalueCalculator
            .pvalueByThreshold(threshold_second)
            .numberOfRecognizedWords(secondBackground, secondPWM.length());
  }

}
