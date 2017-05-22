package ru.autosome.ape.calculation.findPvalue;

import ru.autosome.ape.model.PvalueBsearchList;
import ru.autosome.commons.cli.ReportLayout;

import java.util.ArrayList;
import java.util.List;

// Looks for rough pValue of motif under given threshold
// using a sorted list of predefined threshold-pvalues pairs
// by performing binary search

public class FindPvalueBsearch implements CanFindPvalue {

  final PvalueBsearchList bsearchList;

  public FindPvalueBsearch(PvalueBsearchList bsearchList) {
    this.bsearchList = bsearchList;
  }

  @Override
  public List<PvalueInfo> pvaluesByThresholds(List<Double> thresholds) {
    List<PvalueInfo> results = new ArrayList<>();
    for (double threshold: thresholds) {
      results.add(pvalueByThreshold(threshold));
    }
    return results;
  }

  @Override
  public PvalueInfo pvalueByThreshold(double threshold) {
    double pvalue = bsearchList.pvalue_by_threshold(threshold);
    return new PvalueInfo(threshold, pvalue);
  }

  // TODO: decide which parameters are relevant
  @Override
  public ReportLayout<PvalueInfo> report_table_layout() {
    ReportLayout<PvalueInfo> infos = new ReportLayout<>();

    infos.add_table_parameter("T", "threshold", (PvalueInfo cell) -> cell.threshold);
    infos.add_table_parameter("P", "P-value", (PvalueInfo cell) -> cell.pvalue);

    return infos;
  }
}
