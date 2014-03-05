package ru.autosome.perfectosape.motifModels;

import ru.autosome.perfectosape.Sequence;

public interface ScoringModel {
  public int length();
  public double score(Sequence word);
}
