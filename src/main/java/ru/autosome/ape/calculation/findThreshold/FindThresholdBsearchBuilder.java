package ru.autosome.ape.calculation.findThreshold;

import ru.autosome.ape.model.PvalueBsearchList;
import ru.autosome.commons.motifModel.Named;

import java.io.File;
import java.io.FileNotFoundException;

public class FindThresholdBsearchBuilder {
  final File pathToThresholds;

  public FindThresholdBsearchBuilder(File pathToThresholds) {
    this.pathToThresholds = pathToThresholds;
  }

  public CanFindThreshold thresholdCalculator(Named motif) {
    try {
      File thresholds_file = new File(pathToThresholds, motif.getName() + ".thr");
      PvalueBsearchList pvalueBsearchList = PvalueBsearchList.load_from_file(thresholds_file);
      return new FindThresholdBsearch(pvalueBsearchList);
    } catch (FileNotFoundException e) {
      return null;
    }
  }
}
