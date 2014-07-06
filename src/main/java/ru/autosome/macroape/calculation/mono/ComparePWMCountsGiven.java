package ru.autosome.macroape.calculation.mono;

import ru.autosome.ape.model.exception.HashOverflowException;
import ru.autosome.commons.backgroundModel.mono.BackgroundModel;
import ru.autosome.commons.model.Position;
import ru.autosome.commons.motifModel.mono.PWM;
import ru.autosome.macroape.calculation.generalized.CompareModelsCountsGiven;
import ru.autosome.macroape.calculation.generalized.SimilarityInfo;
import ru.autosome.macroape.model.PairAligned;

public class ComparePWMCountsGiven extends CompareModelsCountsGiven<PWM, BackgroundModel> {

  public ComparePWMCountsGiven(PWM firstPWM, PWM secondPWM,
                               BackgroundModel firstBackground,
                               BackgroundModel secondBackground,
                               Double discretization, Integer maxPairHashSize) {
    super(firstPWM, secondPWM, firstBackground, secondBackground, discretization, maxPairHashSize);
  }

  @Override
  public SimilarityInfo jaccardAtPosition(double thresholdFirst, double thresholdSecond,
                                          double firstCount, double secondCount,
                                          Position position) throws HashOverflowException {

    PairAligned<PWM> alignment = new PairAligned<PWM>(firstPWM, secondPWM, position);
    AlignedModelIntersection calculator = new AlignedModelIntersection(alignment, firstBackground, secondBackground);
    double intersection = calculator.count_in_intersection(upscaleThreshold(thresholdFirst), upscaleThreshold(thresholdSecond));

    double firstCountRenormed = firstCount * firstCountRenormMultiplier(alignment);
    double secondCountRenormed = secondCount * secondCountRenormMultiplier(alignment);

    return new SimilarityInfo(alignment, intersection, firstCountRenormed, secondCountRenormed);
  }

}
