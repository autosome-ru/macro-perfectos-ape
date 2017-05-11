package ru.autosome.ape.calculation.findThreshold;

import ru.autosome.ape.model.PvalueBsearchList;

import java.io.File;
import java.io.FileNotFoundException;

public class FindThresholdBsearchBuilder {
  final File thresholds_file;

  public FindThresholdBsearchBuilder(File thresholds_file) {
    this.thresholds_file = thresholds_file;
  }

  public CanFindThreshold thresholdCalculator() {
    try {
      PvalueBsearchList pvalueBsearchList = PvalueBsearchList.load_from_file(thresholds_file);
      return new FindThresholdBsearch(pvalueBsearchList);
    } catch (FileNotFoundException e) {
      return null;
    }
  }
}
