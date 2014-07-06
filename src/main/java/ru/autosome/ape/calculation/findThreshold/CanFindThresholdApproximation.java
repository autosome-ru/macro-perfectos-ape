package ru.autosome.ape.calculation.findThreshold;

// It's more simple version of CanFindThreshold: it can find an approximation of threshold
// but makes no guaranties about lower or upper boundary
// and don't know real pvalue of predicted threshold
public interface CanFindThresholdApproximation {
  public double thresholdByPvalue(double pvalue);
}
