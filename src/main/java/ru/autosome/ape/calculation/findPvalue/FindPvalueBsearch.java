package ru.autosome.ape.calculation.findPvalue;

import ru.autosome.ape.model.PvalueBsearchList;
import ru.autosome.commons.cli.ReportListLayout;

import java.io.File;
import java.io.FileNotFoundException;
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

  public FindPvalueBsearch(File thresholds_file) throws FileNotFoundException {
      this.bsearchList = PvalueBsearchList.load_from_file(thresholds_file);
  }

  @Override
  public List<FoundedPvalueInfo> pvaluesByThresholds(List<Double> thresholds) {
    List<FoundedPvalueInfo> results = new ArrayList<>();
    for (double threshold: thresholds) {
      results.add(pvalueByThreshold(threshold));
    }
    return results;
  }

  @Override
  public FoundedPvalueInfo pvalueByThreshold(double threshold) {
    double pvalue = bsearchList.pvalue_by_threshold(threshold);
    return new FoundedPvalueInfo(threshold, pvalue);
  }

  // TODO: decide which parameters are relevant
  @Override
  public ReportListLayout<FoundedPvalueInfo> report_table_layout() {
    ReportListLayout<FoundedPvalueInfo> infos = new ReportListLayout<>();

    infos.add_table_parameter("T", "threshold", (FoundedPvalueInfo cell) -> cell.threshold);
    infos.add_table_parameter("P", "P-value", (FoundedPvalueInfo cell) -> cell.pvalue);

    return infos;
  }
}
