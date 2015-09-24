package ru.autosome.macroape.calculation.mono;

import ru.autosome.commons.backgroundModel.mono.BackgroundModel;
import ru.autosome.commons.model.Discretizer;
import ru.autosome.commons.motifModel.mono.PWM;
import ru.autosome.macroape.model.PairAligned;

public class CompareModelsCountsGiven extends ru.autosome.macroape.calculation.generalized.CompareModelsCountsGiven<PWM, BackgroundModel> {

  public CompareModelsCountsGiven(PWM firstPWM, PWM secondPWM,
                                  BackgroundModel firstBackground,
                                  BackgroundModel secondBackground,
                                  Discretizer discretizer) {
    super(firstPWM, secondPWM, firstBackground, secondBackground, discretizer);
  }

  @Override
  protected AlignedModelIntersection calculator(PairAligned<PWM> alignment) {
    return new AlignedModelIntersection(alignment, firstBackground, secondBackground);
  }
}
