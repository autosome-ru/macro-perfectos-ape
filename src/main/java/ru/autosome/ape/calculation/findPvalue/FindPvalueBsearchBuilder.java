package ru.autosome.ape.calculation.findPvalue;

import ru.autosome.ape.model.PvalueBsearchList;

import java.io.File;
import java.io.FileNotFoundException;

public class FindPvalueBsearchBuilder {
  final File thresholds_file;

  public FindPvalueBsearchBuilder(File thresholds_file) {
    this.thresholds_file = thresholds_file;
  }

  public CanFindPvalue pvalueCalculator() {
    try {
      PvalueBsearchList pvalueBsearchList = PvalueBsearchList.load_from_file(thresholds_file);
      return new FindPvalueBsearch(pvalueBsearchList);
    } catch (FileNotFoundException e) {
      return null;
    }
  }
}
