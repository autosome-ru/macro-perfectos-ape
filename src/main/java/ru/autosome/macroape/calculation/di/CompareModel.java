package ru.autosome.macroape.calculation.di;

import ru.autosome.ape.calculation.findPvalue.CanFindPvalue;
import ru.autosome.commons.backgroundModel.di.DiBackgroundModel;
import ru.autosome.commons.motifModel.di.DiPWM;
import ru.autosome.macroape.calculation.generalized.ComparableCountsGiven;

public class CompareModel extends ru.autosome.macroape.calculation.generalized.CompareModel<DiPWM, DiBackgroundModel> {

  public CompareModel(DiPWM firstPWM, DiPWM secondPWM,
                      DiBackgroundModel firstBackground,
                      DiBackgroundModel secondBackground,
                      CanFindPvalue firstPvalueCalculator,
                      CanFindPvalue secondPvalueCalculator,
                      Double discretization, Integer maxPairHashSize) {
  super(firstPWM, secondPWM, firstBackground, secondBackground,
        firstPvalueCalculator, secondPvalueCalculator,
        discretization, maxPairHashSize);
  }

  @Override
  protected ComparableCountsGiven calculatorWithCountsGiven() {
    return new CompareDiPWMCountsGiven(firstPWM, secondPWM,
                                     firstBackground, secondBackground,
                                     discretization, maxPairHashSize);
  }


}
