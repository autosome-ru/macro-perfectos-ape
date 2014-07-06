package ru.autosome.ape.calculation.findPvalue;

import ru.autosome.ape.model.PvalueBsearchList;
import ru.autosome.commons.cli.OutputInformation;

// Looks for rough pValue of motif under given threshold
// using a sorted list of predefined threshold-pvalues pairs
// by performing binary search

public class FindPvalueBsearch implements CanFindPvalue {

  final PvalueBsearchList bsearchList;

  public FindPvalueBsearch(PvalueBsearchList bsearchList) {
    this.bsearchList = bsearchList;
  }

  @Override
  public PvalueInfo[] pvaluesByThresholds(double[] thresholds) {
    PvalueInfo[] results = new PvalueInfo[thresholds.length];
    for (int i = 0; i < thresholds.length; ++i) {
      results[i] = pvalueByThreshold(thresholds[i]);
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
  public OutputInformation report_table_layout() {
    OutputInformation infos = new OutputInformation();

    infos.add_table_parameter("T", "threshold", "threshold");
    infos.add_table_parameter("P", "P-value", "pvalue");

    return infos;
  }
}
