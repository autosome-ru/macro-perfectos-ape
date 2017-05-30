package ru.autosome.macroape.calculation.generalized;

import ru.autosome.commons.backgroundModel.GeneralizedBackgroundModel;
import ru.autosome.commons.model.Orientation;
import ru.autosome.commons.model.Position;
import ru.autosome.commons.motifModel.Alignable;
import ru.autosome.macroape.model.ComparisonSimilarityInfo;
import ru.autosome.macroape.model.PairAligned;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class CompareModelsExact<ModelType extends Alignable<ModelType>,
                                           BackgroundType extends GeneralizedBackgroundModel> {
  public final ModelType firstPWM; // here we store discreted PWMs
  public final ModelType secondPWM;
  public final BackgroundType background;
  public final Function<PairAligned<ModelType>, ? extends AlignedModelIntersection> calculatorOfAligned;

  public CompareModelsExact(ModelType firstPWM, ModelType secondPWM,
                            BackgroundType background,
                            Function<PairAligned<ModelType>, ? extends AlignedModelIntersection> calculatorOfAligned) {
    this.firstPWM = firstPWM;
    this.secondPWM = secondPWM;
    this.background = background;
    this.calculatorOfAligned = calculatorOfAligned;
  }

  private List<Position> relative_alignments() {
    List<Position> result = new ArrayList<>();
    for(int shift = -secondPWM.length(); shift <= firstPWM.length(); ++shift) {
      result.add(new Position(shift, Orientation.direct));
      result.add(new Position(shift, Orientation.revcomp));
    }
    return result;
  }

  protected double firstCountRenormMultiplier(PairAligned alignment) {
    return Math.pow(background.volume(), alignment.length() - firstPWM.length());
  }
  protected double secondCountRenormMultiplier(PairAligned alignment) {
    return Math.pow(background.volume(), alignment.length() - secondPWM.length());
  }

  public ComparisonSimilarityInfo jaccard(double thresholdFirst, double thresholdSecond,
                                          double firstCount, double secondCount) {
    double bestSimilarity = -1;
    ComparisonSimilarityInfo bestSimilarityInfo = null;
    for (Position position: relative_alignments()) {
      ComparisonSimilarityInfo similarityInfo;
      similarityInfo = jaccardAtPosition(thresholdFirst, thresholdSecond, firstCount, secondCount, position);
      double similarity = similarityInfo.similarity();
      if (similarity > bestSimilarity) {
        bestSimilarity = similarity;
        bestSimilarityInfo = similarityInfo;
      }
    }
    return bestSimilarityInfo;
  }

  public ComparisonSimilarityInfo jaccardAtPosition(double thresholdFirst, double thresholdSecond,
                                                    double firstCount, double secondCount,
                                                    Position position) {
    PairAligned<ModelType> alignment = new PairAligned<>(firstPWM, secondPWM, position);
    double intersection = calculatorOfAligned.apply(alignment).count_in_intersection(thresholdFirst, thresholdSecond);

    double firstCountRenormed = firstCount * firstCountRenormMultiplier(alignment);
    double secondCountRenormed = secondCount * secondCountRenormMultiplier(alignment);

    return new ComparisonSimilarityInfo(alignment, intersection, firstCountRenormed, secondCountRenormed);
  }
}
