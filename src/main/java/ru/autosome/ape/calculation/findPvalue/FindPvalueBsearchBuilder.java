package ru.autosome.ape.calculation.findPvalue;

import ru.autosome.ape.model.PvalueBsearchList;

import java.io.File;
import java.io.FileNotFoundException;

public class FindPvalueBsearchBuilder {
  final File pathToThresholds;

  public FindPvalueBsearchBuilder(File pathToThresholds) {
    this.pathToThresholds = pathToThresholds;
  }

  public CanFindPvalue pvalueCalculator(String motifName) {
    try {
      File thresholds_file = new File(pathToThresholds, motifName + ".thr");
      PvalueBsearchList pvalueBsearchList = PvalueBsearchList.load_from_file(thresholds_file);
      return new FindPvalueBsearch(pvalueBsearchList);
    } catch (FileNotFoundException e) {
      return null;
    }
  }
}
