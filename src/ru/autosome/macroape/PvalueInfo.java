package ru.autosome.macroape;

public class PvalueInfo extends ResultInfo {
    private final double threshold;
    public final double pvalue;
    private final int number_of_recognized_words;

    public PvalueInfo(double threshold, double pvalue, int number_of_recognized_words) {
        this.threshold = threshold;
        this.pvalue = pvalue;
        this.number_of_recognized_words = number_of_recognized_words;
    }
}
