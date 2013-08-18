package ru.autosome.jMacroape;

public class PvalueInfo extends ResultInfo {
  public double threshold;
  public double pvalue;
  public int number_of_recognized_words;
  public PvalueInfo(double threshold, double pvalue, int number_of_recognized_words) {
    this.threshold = threshold;
    this.pvalue = pvalue;
    this.number_of_recognized_words = number_of_recognized_words;
  }
}
