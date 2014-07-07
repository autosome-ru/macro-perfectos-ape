package ru.autosome.macroape.calculation.di;

import ru.autosome.ape.calculation.findPvalue.CanFindPvalue;
import ru.autosome.commons.backgroundModel.di.DiBackgroundModel;
import ru.autosome.commons.model.Discretizer;
import ru.autosome.commons.motifModel.di.DiPWM;

public class CompareModels extends ru.autosome.macroape.calculation.generalized.CompareModels<DiPWM, DiBackgroundModel> {

  public CompareModels(DiPWM firstPWM, DiPWM secondPWM, DiBackgroundModel firstBackground, DiBackgroundModel secondBackground,
                       CanFindPvalue firstPvalueCalculator, CanFindPvalue secondPvalueCalculator, Discretizer discretizer, Integer maxPairHashSize) {
    super(firstPWM, secondPWM, firstBackground, secondBackground, firstPvalueCalculator, secondPvalueCalculator, discretizer, maxPairHashSize);
  }

  public CompareModels(DiPWM firstPWM, DiPWM secondPWM, DiBackgroundModel firstBackground, DiBackgroundModel secondBackground,
                       Discretizer discretizer, Integer maxPairHashSize, Integer maxHashSize) {
    super(firstPWM, secondPWM, firstBackground, secondBackground, discretizer, maxPairHashSize, maxHashSize);
  }

  @Override
  protected ru.autosome.macroape.calculation.di.CompareModelsCountsGiven calculatorWithCountsGiven() {
    return new CompareModelsCountsGiven(firstPWM, secondPWM,
                                     firstBackground, secondBackground,
                                     discretizer, maxPairHashSize);
  }
}
