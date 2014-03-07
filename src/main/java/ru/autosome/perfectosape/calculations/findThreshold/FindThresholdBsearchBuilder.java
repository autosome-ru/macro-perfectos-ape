package ru.autosome.perfectosape.calculations.findThreshold;

import ru.autosome.perfectosape.PvalueBsearchList;
import ru.autosome.perfectosape.motifModels.Named;
import ru.autosome.perfectosape.motifModels.ScoringModel;

import java.io.File;
import java.io.FileNotFoundException;

public class FindThresholdBsearchBuilder<ModelType extends Named & ScoringModel> {
  File pathToThresholds;

  public FindThresholdBsearchBuilder(File pathToThresholds) {
    this.pathToThresholds = pathToThresholds;
  }

  public CanFindThreshold thresholdCalculator(ModelType motif) {
    try {
      File thresholds_file = new File(pathToThresholds, motif.getName() + ".thr");
      PvalueBsearchList pvalueBsearchList = PvalueBsearchList.load_from_file(thresholds_file);
      return new FindThresholdBsearch(pvalueBsearchList);
    } catch (FileNotFoundException e) {
      return null;
    }
  }
}
