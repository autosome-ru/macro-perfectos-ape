package ru.autosome.perfectosape.calculations.findThreshold;

import ru.autosome.perfectosape.PvalueBsearchList;
import ru.autosome.perfectosape.motifModels.Named;
import ru.autosome.perfectosape.motifModels.ScoringModel;

import java.io.File;
import java.io.FileNotFoundException;

public class FindThresholdBsearchBuilder<ModelType extends Named & ScoringModel> {
  File pathToThresholds;
  ModelType motif;

  public FindThresholdBsearchBuilder<ModelType> applyMotif(ModelType motif) {
    this.motif = motif;
    return this;
  }

  public CanFindThreshold build() {
    if (motif != null) {
      return thresholdCalculator();
    } else {
      return null;
    }
  }

  public FindThresholdBsearchBuilder(File pathToThresholds) {
    this.pathToThresholds = pathToThresholds;
  }

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
