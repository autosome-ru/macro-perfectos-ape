package ru.autosome.ape.calculation.findPvalue;

import ru.autosome.commons.backgroundModel.GeneralizedBackgroundModel;
import ru.autosome.commons.cli.ReportLayout;

import java.util.List;

public interface CanFindPvalue {
  class PvalueInfo {
    public final double threshold;
    public final double pvalue;

    public PvalueInfo(double threshold, double pvalue) {
      this.threshold = threshold;
      this.pvalue = pvalue;
    }

    public double numberOfRecognizedWords(GeneralizedBackgroundModel background, int length) {
      return pvalue * Math.pow(background.volume(), length);
    }
  }

  List<PvalueInfo> pvaluesByThresholds(List<Double> thresholds);

  PvalueInfo pvalueByThreshold(double threshold);
  ReportLayout<PvalueInfo> report_table_layout();
}
