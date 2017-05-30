package ru.autosome.macroape.calculation.generalized;

import ru.autosome.commons.backgroundModel.GeneralizedBackgroundModel;
import ru.autosome.commons.model.Orientation;
import ru.autosome.commons.model.Position;
import ru.autosome.commons.motifModel.Alignable;
import ru.autosome.macroape.model.ComparisonSimilarityInfo;
import ru.autosome.macroape.model.PairAligned;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;

public class CompareModelsExact<ModelType extends Alignable<ModelType>,
                                           BackgroundType extends GeneralizedBackgroundModel> {
  private final ModelType firstPWM; // here we store discreted PWMs
  private final ModelType secondPWM;
  private final BackgroundType background;
  private final Function<PairAligned<ModelType>, ? extends AlignedModelIntersection> calculatorOfAligned;

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

  private double firstCountRenormMultiplier(PairAligned alignment) {
    return Math.pow(background.volume(), alignment.length() - firstPWM.length());
  }
  private double secondCountRenormMultiplier(PairAligned alignment) {
    return Math.pow(background.volume(), alignment.length() - secondPWM.length());
  }

  public ComparisonSimilarityInfo jaccard(double thresholdFirst, double thresholdSecond,
                                          double firstCount, double secondCount) {
    return relative_alignments().stream().map(
        (Position position) -> jaccardAtPosition(thresholdFirst, thresholdSecond, firstCount, secondCount, position)
    ).max(Comparator.comparingDouble(ComparisonSimilarityInfo::similarity)).get();
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
