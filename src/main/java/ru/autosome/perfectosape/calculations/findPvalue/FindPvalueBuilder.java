package ru.autosome.perfectosape.calculations.findPvalue;

import ru.autosome.perfectosape.motifModels.Named;
import ru.autosome.perfectosape.motifModels.ScoringModel;

public abstract class FindPvalueBuilder<ModelType extends Named & ScoringModel> {
  ModelType motif;
  public abstract CanFindPvalue pvalueCalculator();
  public CanFindPvalue build() {
    if (motif != null) {
      return pvalueCalculator();
    } else {
      return null;
    }
  }
  public FindPvalueBuilder applyMotif(ModelType motif) {
    this.motif = motif;
    return this;
  }
}
