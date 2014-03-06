package ru.autosome.perfectosape.calculations.findPvalue;

import ru.autosome.perfectosape.backgroundModels.GeneralizedBackgroundModel;
import ru.autosome.perfectosape.calculations.HashOverflowException;
import ru.autosome.perfectosape.formatters.OutputInformation;
import ru.autosome.perfectosape.formatters.ResultInfo;

public interface CanFindPvalue {
  public static class PvalueInfo extends ResultInfo {
    public final double threshold;
    public final double pvalue;

    public PvalueInfo(double threshold, double pvalue) {
      this.threshold = threshold;
      this.pvalue = pvalue;
    }

    public double numberOfRecognizedWords(GeneralizedBackgroundModel background, int length) {
      return pvalue * Math.pow(background.volume(), length);
    };
  }

  public OutputInformation report_table_layout();

  public PvalueInfo[] pvaluesByThresholds(double[] thresholds) throws HashOverflowException;

  public PvalueInfo pvalueByThreshold(double threshold) throws HashOverflowException;
}
