package ru.autosome.macroape;

public class ThresholdInfo extends ResultInfo {
  public final double threshold;
  public final double real_pvalue;
  public final double expected_pvalue;
  public final int recognized_words;

  public ThresholdInfo(double threshold, double real_pvalue, double expected_pvalue, int recognized_words) {
    this.threshold = threshold;
    this.real_pvalue = real_pvalue;
    this.expected_pvalue = expected_pvalue;
    this.recognized_words = recognized_words;
  }

  // generate infos for non-disreeted matrix from infos for discreeted matrix
  public ThresholdInfo downscale(Double discretization) {
    if (discretization == null) {
      return this;
    } else {
      return new ThresholdInfo(threshold / discretization, real_pvalue, expected_pvalue, recognized_words);
    }
  }
}
