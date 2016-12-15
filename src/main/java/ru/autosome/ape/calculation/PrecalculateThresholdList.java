package ru.autosome.ape.calculation;

import ru.autosome.ape.calculation.findThreshold.CanFindThreshold;
import ru.autosome.ape.calculation.findThreshold.FindThresholdAPE;
import ru.autosome.ape.model.PvalueBsearchList;
import ru.autosome.ape.model.progression.GeometricProgression;
import ru.autosome.commons.model.BoundaryType;
import ru.autosome.commons.model.Discretizer;
import ru.autosome.commons.motifModel.Discretable;
import ru.autosome.commons.motifModel.ScoreBoundaries;
import ru.autosome.commons.motifModel.ScoreDistribution;

import java.util.ArrayList;
import java.util.List;

public class PrecalculateThresholdList<ModelType extends  Discretable<ModelType> & ScoreDistribution<BackgroundType> & ScoreBoundaries,
                                       BackgroundType> {
  // We expect not to have P-values less than 1e-15 in common case.
  // It's possible only for motifs of length 25 or more.
  // For SNPScan differences in such low P-values actually doesn't matter
  public static final List<Double> PVALUE_LIST = new GeometricProgression(1.0, 1E-15, 1.05).values();

  final List<Double> pvalues;
  final BoundaryType pvalue_boundary;

  final Discretizer discretizer;
  final BackgroundType background;

  public PrecalculateThresholdList(List<Double> pvalues, Discretizer discretizer, BackgroundType background, BoundaryType pvalue_boundary) {
    this.pvalues = pvalues;
    this.discretizer = discretizer;
    this.background = background;
    this.pvalue_boundary = pvalue_boundary;
  }

  protected CanFindThreshold find_threshold_calculator(ModelType motif) {
    return new FindThresholdAPE<ModelType, BackgroundType>(motif, background, discretizer);
  }

  public PvalueBsearchList bsearch_list_for_pwm(ModelType motif) {
    List<CanFindThreshold.ThresholdInfo> infos = find_threshold_calculator(motif).thresholdsByPvalues(pvalues, pvalue_boundary);

    List<PvalueBsearchList.ThresholdPvaluePair> pairs = new ArrayList<PvalueBsearchList.ThresholdPvaluePair>(infos.size() + 2);
    for (CanFindThreshold.ThresholdInfo info: infos) {
      pairs.add(new PvalueBsearchList.ThresholdPvaluePair(info));
    }

    double worstScore = motif.worst_score();
    double bestScore = motif.best_score();
    double eps = 1;
    if ((bestScore - worstScore > 0) && (bestScore - worstScore < eps)) {
      eps = bestScore - worstScore;
    }
    eps *= 0.1;
    pairs.add(new PvalueBsearchList.ThresholdPvaluePair(worstScore, 1.0)); // every score is >= than worst score
    pairs.add(new PvalueBsearchList.ThresholdPvaluePair(bestScore + eps, 0.0));  // no score is >= than best score + eps

    return new PvalueBsearchList(pairs);
  }
}
