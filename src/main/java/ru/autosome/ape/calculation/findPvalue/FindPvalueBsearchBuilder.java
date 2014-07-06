package ru.autosome.ape.calculation.findPvalue;

import ru.autosome.ape.model.PvalueBsearchList;
import ru.autosome.commons.motifModel.Named;

import java.io.File;
import java.io.FileNotFoundException;

public class FindPvalueBsearchBuilder {
  final File pathToThresholds;

  public FindPvalueBsearchBuilder(File pathToThresholds) {
    this.pathToThresholds = pathToThresholds;
  }

  public CanFindPvalue pvalueCalculator(Named motif) {
    try {
      File thresholds_file = new File(pathToThresholds, motif.getName() + ".thr");
      PvalueBsearchList pvalueBsearchList = PvalueBsearchList.load_from_file(thresholds_file);
      return new FindPvalueBsearch(pvalueBsearchList);
    } catch (FileNotFoundException e) {
      return null;
    }
  }
}
