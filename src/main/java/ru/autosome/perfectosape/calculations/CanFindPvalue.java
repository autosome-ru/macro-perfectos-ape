package ru.autosome.perfectosape.calculations;

import ru.autosome.perfectosape.OutputInformation;
import ru.autosome.perfectosape.ResultInfo;

public interface CanFindPvalue {
  public static class PvalueInfo extends ResultInfo {
    public final double threshold;
    public final double pvalue;
    public final int numberOfRecognizedWords;

    public PvalueInfo(double threshold, double pvalue, int numberOfRecognizedWords) {
      this.threshold = threshold;
      this.pvalue = pvalue;
      this.numberOfRecognizedWords = numberOfRecognizedWords;
    }
  }

  public OutputInformation report_table_layout();

  public PvalueInfo[] pvalues_by_thresholds(double[] thresholds);

  public PvalueInfo pvalue_by_threshold(double threshold);
}
