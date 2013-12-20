package ru.autosome.perfectosape.calculations;

import ru.autosome.perfectosape.OutputInformation;
import ru.autosome.perfectosape.ResultInfo;

public interface CanFindPvalue {
  public static class PvalueInfo extends ResultInfo {
    public final double threshold;
    public final double pvalue;
    public final int number_of_recognized_words;

    public PvalueInfo(double threshold, double pvalue, int number_of_recognized_words) {
      this.threshold = threshold;
      this.pvalue = pvalue;
      this.number_of_recognized_words = number_of_recognized_words;
    }
  }


  public OutputInformation report_table_layout();

  public PvalueInfo[] pvalues_by_thresholds(double[] thresholds);

  public PvalueInfo pvalue_by_threshold(double threshold);
}
