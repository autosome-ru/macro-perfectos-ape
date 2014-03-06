package ru.autosome.perfectosape.calculations.findThreshold;

import ru.autosome.perfectosape.PvalueBsearchList;

import java.io.File;
import java.io.FileNotFoundException;

public class FindThresholdBsearchBuilder extends FindThresholdBuilder {
  File pathToThresholds;

  public FindThresholdBsearchBuilder(File pathToThresholds) {
    this.pathToThresholds = pathToThresholds;
  }

  @Override
  public CanFindThreshold thresholdCalculator() {
    try {
      File thresholds_file = new File(pathToThresholds, motif.getName() + ".thr");
      PvalueBsearchList pvalueBsearchList = PvalueBsearchList.load_from_file(thresholds_file);
      return new FindThresholdBsearch(pvalueBsearchList);
    } catch (FileNotFoundException e) {
      return null;
    }
  }
}
