package ru.autosome.macroape;

import java.util.ArrayList;
import java.util.Collections;

// Looks for rough pValue of motif under given threshold
// using a sorted list of predefined threshold-pvalues pairs
// by performing binary search
public class FindPvalueBsearch implements CanFindPvalue {
  FindPvalueBsearchParameters parameters;

  public FindPvalueBsearch(FindPvalueBsearchParameters parameters) {
    this.parameters = parameters;
  }

  public ArrayList<PvalueInfo> pvalues_by_thresholds() {
    ArrayList<PvalueInfo> results = new ArrayList<PvalueInfo>();
    for (double threshold : parameters.thresholds) {
      results.add(pvalue_by_threshold(threshold));
    }
    return results;
  }

  double vocabularyVolume() {
    return new CountingPWM(parameters.pwm, parameters.background).vocabularyVolume();
  }

  public PvalueInfo pvalue_by_threshold(double threshold) {
    double pvalue = parameters.bsearchList.pvalue_by_threshold(threshold);
    int count = (int) (pvalue * vocabularyVolume());
    return new PvalueInfo(threshold, pvalue, count);
  }

  // TODO: decide which parameters are relevant
  public OutputInformation report_table_layout() {
    OutputInformation infos = new OutputInformation();
    infos.background_parameter("B", "background", parameters.background);

    infos.add_table_parameter("T", "threshold", "threshold");
    if (parameters.background.is_wordwise()) {
      infos.add_table_parameter("W", "number of recognized words", "number_of_recognized_words");
    }
    infos.add_table_parameter("P", "P-value", "pvalue");

    return infos;
  }
}
