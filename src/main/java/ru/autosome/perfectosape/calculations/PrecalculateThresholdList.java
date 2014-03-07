package ru.autosome.perfectosape.calculations;

import ru.autosome.perfectosape.BoundaryType;
import ru.autosome.perfectosape.backgroundModels.BackgroundModel;
import ru.autosome.perfectosape.calculations.findThreshold.CanFindThreshold;
import ru.autosome.perfectosape.calculations.findThreshold.FindThresholdAPE;
import ru.autosome.perfectosape.motifModels.PWM;

public class PrecalculateThresholdList extends PrecalculateThresholdListGeneralized<PWM> {
  double discretization;
  BackgroundModel background;
  Integer max_hash_size;

  public PrecalculateThresholdList(double[] pvalues, double discretization, BackgroundModel background, BoundaryType pvalue_boundary, Integer max_hash_size) {
    this.pvalues = pvalues;
    this.discretization = discretization;
    this.background = background;
    this.pvalue_boundary = pvalue_boundary;
    this.max_hash_size = max_hash_size;
  }

  @Override
  protected CanFindThreshold find_threshold_calculator(PWM motif) {
    return new FindThresholdAPE<PWM, BackgroundModel>(motif, background, discretization, max_hash_size);
  }
}
