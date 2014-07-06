package ru.autosome.commons.motifModel;

import ru.autosome.perfectosape.model.Sequence;

public interface ScoringModel {
  public int length();
  public double score(Sequence word);
}
