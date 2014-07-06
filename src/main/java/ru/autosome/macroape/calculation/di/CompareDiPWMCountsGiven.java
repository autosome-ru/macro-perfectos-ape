package ru.autosome.macroape.calculation.di;

import ru.autosome.commons.backgroundModel.di.DiBackgroundModel;
import ru.autosome.commons.motifModel.di.DiPWM;
import ru.autosome.macroape.calculation.generalized.CompareModelsCountsGiven;
import ru.autosome.macroape.model.PairAligned;

public class CompareDiPWMCountsGiven extends CompareModelsCountsGiven<DiPWM, DiBackgroundModel> {

  public CompareDiPWMCountsGiven(DiPWM firstPWM, DiPWM secondPWM,
                                 DiBackgroundModel firstBackground,
                                 DiBackgroundModel secondBackground,
                                 Double discretization, Integer maxPairHashSize) {
    super(firstPWM, secondPWM, firstBackground, secondBackground, discretization, maxPairHashSize);
  }

  @Override
  protected AlignedModelIntersection calculator(PairAligned<DiPWM> alignment) {
    return new AlignedModelIntersection(alignment, firstBackground, secondBackground);
  }
}
