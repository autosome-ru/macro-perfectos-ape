package ru.autosome.perfectosape.calculations.findPvalue;

import ru.autosome.perfectosape.PvalueBsearchList;
import ru.autosome.perfectosape.motifModels.Named;
import ru.autosome.perfectosape.motifModels.ScoringModel;

import java.io.File;
import java.io.FileNotFoundException;

public class FindPvalueBsearchBuilder<ModelType extends Named & ScoringModel> extends FindPvalueBuilder<ModelType> {
  File pathToThresholds;

  public FindPvalueBsearchBuilder(File pathToThresholds) {
    this.pathToThresholds = pathToThresholds;
  }

  @Override
  public CanFindPvalue pvalueCalculator() {
    try {
      File thresholds_file = new File(pathToThresholds, motif.getName() + ".thr");
      PvalueBsearchList pvalueBsearchList = PvalueBsearchList.load_from_file(thresholds_file);
      return new FindPvalueBsearch(pvalueBsearchList);
    } catch (FileNotFoundException e) {
      return null;
    }
  }
}
