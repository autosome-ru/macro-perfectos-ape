package ru.autosome.ape.calculation.findPvalue;

import ru.autosome.commons.cli.ReportListLayout;

import java.util.List;

public interface CanFindPvalue {

  List<FoundedPvalueInfo> pvaluesByThresholds(List<Double> thresholds);

  FoundedPvalueInfo pvalueByThreshold(double threshold);
  ReportListLayout<FoundedPvalueInfo> report_table_layout();
}
