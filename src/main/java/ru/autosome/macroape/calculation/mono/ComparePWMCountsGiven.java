package ru.autosome.macroape.calculation.mono;

import ru.autosome.commons.backgroundModel.mono.BackgroundModel;
import ru.autosome.commons.motifModel.mono.PWM;
import ru.autosome.macroape.calculation.generalized.CompareModelsCountsGiven;
import ru.autosome.macroape.model.PairAligned;

public class ComparePWMCountsGiven extends CompareModelsCountsGiven<PWM, BackgroundModel> {

  public ComparePWMCountsGiven(PWM firstPWM, PWM secondPWM,
                               BackgroundModel firstBackground,
                               BackgroundModel secondBackground,
                               Double discretization, Integer maxPairHashSize) {
    super(firstPWM, secondPWM, firstBackground, secondBackground, discretization, maxPairHashSize);
  }

  @Override
  protected AlignedModelIntersection calculator(PairAligned<PWM> alignment) {
    return new AlignedModelIntersection(alignment, firstBackground, secondBackground);
  }
}
