package ru.autosome.commons.scoringModel;

import ru.autosome.commons.model.Orientation;
import ru.autosome.commons.motifModel.HasLength;

public interface SequenceScoringModel<SequenceType> extends HasLength {
  double score(SequenceType word);
  double score(SequenceType word, Orientation orientation, int position);
}
