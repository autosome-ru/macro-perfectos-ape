package ru.autosome.perfectosape.calculations.findPvalue;

import ru.autosome.perfectosape.backgroundModels.DiBackgroundModel;
import ru.autosome.perfectosape.calculations.CountingDiPWM;
import ru.autosome.perfectosape.calculations.ScoringModelDistibutions;
import ru.autosome.perfectosape.motifModels.DiPWM;

public class DiPWMFindPvalueAPE extends FindPvalueByDiscretization<DiPWM, DiBackgroundModel> {
  Integer maxHashSize;

  public DiPWMFindPvalueAPE(DiPWM dipwm, Double discretization, DiBackgroundModel dibackground, Integer maxHashSize) {
    super(dipwm, dibackground, discretization);
    this.maxHashSize = maxHashSize;
  }

  @Override
  ScoringModelDistibutions countingPWM() {
    return new CountingDiPWM(motif.discrete(discretization), background, maxHashSize);
  }
}
