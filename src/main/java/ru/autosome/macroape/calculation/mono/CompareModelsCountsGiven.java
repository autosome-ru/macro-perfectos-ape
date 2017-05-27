package ru.autosome.macroape.calculation.mono;

import ru.autosome.commons.backgroundModel.mono.BackgroundModel;
import ru.autosome.commons.model.Discretizer;
import ru.autosome.commons.motifModel.mono.PWM;
import ru.autosome.macroape.model.PairAligned;

public class CompareModelsCountsGiven extends ru.autosome.macroape.calculation.generalized.CompareModelsCountsGiven<PWM, BackgroundModel> {

  public CompareModelsCountsGiven(PWM firstPWM, PWM secondPWM,
                                  BackgroundModel background,
                                  Discretizer discretizer) {
    super(firstPWM, secondPWM, background, discretizer);
  }

  @Override
  protected AlignedModelIntersection calculator(PairAligned<PWM> alignment) {
    return new AlignedModelIntersection(alignment, background);
  }
}
