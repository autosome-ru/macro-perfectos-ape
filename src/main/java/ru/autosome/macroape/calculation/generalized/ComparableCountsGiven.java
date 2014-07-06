package ru.autosome.macroape.calculation.generalized;

import ru.autosome.ape.model.exception.HashOverflowException;
import ru.autosome.commons.model.Position;

public interface ComparableCountsGiven {
  SimilarityInfo jaccard(double thresholdFirst, double thresholdSecond,
                                      double firstCount, double secondCount) throws HashOverflowException;

  SimilarityInfo jaccardAtPosition(double thresholdFirst, double thresholdSecond,
                                                double firstCount, double secondCount,
                                                Position position) throws HashOverflowException;
}
