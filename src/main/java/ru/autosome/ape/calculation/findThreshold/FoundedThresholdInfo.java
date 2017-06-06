package ru.autosome.ape.calculation.findThreshold;

import ru.autosome.ape.calculation.findPvalue.FoundedPvalueInfo;
import ru.autosome.commons.model.Discretizer;

public class FoundedThresholdInfo {
  public final double threshold;
  public final double real_pvalue;
  public final double expected_pvalue;

  public FoundedThresholdInfo(double threshold, double real_pvalue, double expected_pvalue) {
    this.threshold = threshold;
    this.real_pvalue = real_pvalue;
    this.expected_pvalue = expected_pvalue;
  }

  public double numberOfRecognizedWords(int backgroundVolume, int length) {
    return real_pvalue * Math.pow(backgroundVolume, length);
  }

  // generate infos for non-discreeted matrix from infos for discreeted matrix
  public FoundedThresholdInfo downscale(Discretizer discretizer) {
    return new FoundedThresholdInfo(discretizer.downscale(threshold), real_pvalue, expected_pvalue);
  }

  public FoundedPvalueInfo toFoundedPvalueInfo() {
    return new FoundedPvalueInfo(threshold, real_pvalue);
  }
}
