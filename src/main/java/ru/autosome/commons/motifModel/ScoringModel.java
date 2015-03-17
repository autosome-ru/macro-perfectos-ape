package ru.autosome.commons.motifModel;

import ru.autosome.commons.model.Orientation;

public interface ScoringModel<SequenceType> extends HasLength, ScoreStatistics {
  double score(SequenceType word);
  double score(SequenceType word, Orientation orientation, int position);
}
