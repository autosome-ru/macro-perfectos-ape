package ru.autosome.macroape.calculation.di;

import ru.autosome.ape.calculation.findPvalue.CanFindPvalue;
import ru.autosome.commons.backgroundModel.di.DiBackgroundModel;
import ru.autosome.commons.model.Discretizer;
import ru.autosome.commons.motifModel.di.DiPWM;

public class CompareModels extends ru.autosome.macroape.calculation.generalized.CompareModels<DiPWM, DiBackgroundModel> {

  public CompareModels(DiPWM firstPWM, DiPWM secondPWM, DiBackgroundModel background,
                       CanFindPvalue firstPvalueCalculator, CanFindPvalue secondPvalueCalculator, Discretizer discretizer) {
    super(firstPWM, secondPWM, background, firstPvalueCalculator, secondPvalueCalculator, discretizer);
  }

  public CompareModels(DiPWM firstPWM, DiPWM secondPWM, DiBackgroundModel background, Discretizer discretizer) {
    super(firstPWM, secondPWM, background, discretizer);
  }

  @Override
  protected ru.autosome.macroape.calculation.di.CompareModelsCountsGiven calculatorWithCountsGiven() {
    return new CompareModelsCountsGiven(firstPWM, secondPWM,
                                     background,
                                     discretizer);
  }
}
