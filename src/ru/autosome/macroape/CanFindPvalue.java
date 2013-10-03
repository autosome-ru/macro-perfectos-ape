package ru.autosome.macroape;

import java.util.ArrayList;

public interface CanFindPvalue {
  public OutputInformation report_table_layout();

  public ArrayList<PvalueInfo> pvalues_by_thresholds(double[] thresholds);

  public PvalueInfo pvalue_by_threshold(double threshold);
}
