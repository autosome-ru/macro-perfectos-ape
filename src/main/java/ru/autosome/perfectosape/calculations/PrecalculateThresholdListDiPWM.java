package ru.autosome.perfectosape.calculations;

import ru.autosome.perfectosape.BoundaryType;
import ru.autosome.perfectosape.backgroundModels.DiBackgroundModel;
import ru.autosome.perfectosape.calculations.findThreshold.CanFindThreshold;
import ru.autosome.perfectosape.calculations.findThreshold.FindThresholdAPE;
import ru.autosome.perfectosape.motifModels.DiPWM;

public class PrecalculateThresholdListDiPWM extends PrecalculateThresholdListGeneralized<DiPWM> {
  double discretization;
  DiBackgroundModel background;
  Integer max_hash_size;

  public PrecalculateThresholdListDiPWM(double[] pvalues, double discretization, DiBackgroundModel background, BoundaryType pvalue_boundary, Integer max_hash_size) {
    this.pvalues = pvalues;
    this.discretization = discretization;
    this.background = background;
    this.pvalue_boundary = pvalue_boundary;
    this.max_hash_size = max_hash_size;
  }

  @Override
  protected CanFindThreshold find_threshold_calculator(DiPWM motif) {
    return new FindThresholdAPE<DiPWM, DiBackgroundModel>(motif, background, discretization, max_hash_size);
  }

}
