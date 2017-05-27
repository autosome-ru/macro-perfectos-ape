package ru.autosome.macroape.calculation.generalized;

import ru.autosome.commons.backgroundModel.GeneralizedBackgroundModel;
import ru.autosome.commons.motifModel.Alignable;
import ru.autosome.macroape.model.PairAligned;

abstract public class AlignedModelIntersection <ModelType extends Alignable<ModelType>,
                                       BackgroundType extends GeneralizedBackgroundModel> {
  public final BackgroundType background;
  public final PairAligned<ModelType> alignment;

  public AlignedModelIntersection(PairAligned<ModelType> alignment, BackgroundType background) {
    this.background = background;
    this.alignment = alignment;
  }

  abstract protected double count_in_intersection(double threshold_first, double threshold_second);
}
