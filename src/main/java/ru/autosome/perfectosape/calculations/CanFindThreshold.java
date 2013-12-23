package ru.autosome.perfectosape.calculations;

import ru.autosome.perfectosape.ResultInfo;

public interface CanFindThreshold {
  class ThresholdInfo extends ResultInfo {
    public final double threshold;
    public final double real_pvalue;
    public final double expected_pvalue;
    public final double numberOfRecognizedWords;

    public ThresholdInfo(double threshold, double real_pvalue, double expected_pvalue, double numberOfRecognizedWords) {
      this.threshold = threshold;
      this.real_pvalue = real_pvalue;
      this.expected_pvalue = expected_pvalue;
      this.numberOfRecognizedWords = numberOfRecognizedWords;
    }

    // generate infos for non-disreeted matrix from infos for discreeted matrix
    public ThresholdInfo downscale(Double discretization) {
      if (discretization == null) {
        return this;
      } else {
        return new ThresholdInfo(threshold / discretization, real_pvalue, expected_pvalue, numberOfRecognizedWords);
      }
    }
  }

  ThresholdInfo[] find_thresholds_by_pvalues(double[] pvalues);

  ThresholdInfo find_threshold_by_pvalue(double pvalue);
}
