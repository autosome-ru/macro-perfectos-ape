package ru.autosome.macroape.calculation.di;

import ru.autosome.ape.calculation.findPvalue.CanFindPvalue;
import ru.autosome.commons.backgroundModel.di.DiBackgroundModel;
import ru.autosome.commons.motifModel.di.DiPWM;

public class CompareModels extends ru.autosome.macroape.calculation.generalized.CompareModels<DiPWM, DiBackgroundModel> {

  public CompareModels(DiPWM firstPWM, DiPWM secondPWM, DiBackgroundModel firstBackground, DiBackgroundModel secondBackground,
                       CanFindPvalue firstPvalueCalculator, CanFindPvalue secondPvalueCalculator, Double discretization, Integer maxPairHashSize) {
    super(firstPWM, secondPWM, firstBackground, secondBackground, firstPvalueCalculator, secondPvalueCalculator, discretization, maxPairHashSize);
  }

  public CompareModels(DiPWM firstPWM, DiPWM secondPWM, DiBackgroundModel firstBackground, DiBackgroundModel secondBackground,
                       Double discretization, Integer maxPairHashSize, Integer maxHashSize) {
    super(firstPWM, secondPWM, firstBackground, secondBackground, discretization, maxPairHashSize, maxHashSize);
  }

  @Override
  protected ru.autosome.macroape.calculation.di.CompareModelsCountsGiven calculatorWithCountsGiven() {
    return new CompareModelsCountsGiven(firstPWM, secondPWM,
                                     firstBackground, secondBackground,
                                     discretization, maxPairHashSize);
  }
}
