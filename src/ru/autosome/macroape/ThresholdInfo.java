package ru.autosome.macroape;

public class ThresholdInfo extends ResultInfo {
    public final double threshold;
    public final double real_pvalue;
    private final double expected_pvalue;
    private final int recognized_words;

    public ThresholdInfo(double threshold, double real_pvalue, double expected_pvalue, int recognized_words) {
        this.threshold = threshold;
        this.real_pvalue = real_pvalue;
        this.expected_pvalue = expected_pvalue;
        this.recognized_words = recognized_words;
    }

    // generate infos for non-disreeted matrix from infos for discreeted matrix
    public ThresholdInfo downscale(double discretization) {
        return new ThresholdInfo(threshold / discretization, real_pvalue, expected_pvalue, recognized_words);
    }
}
