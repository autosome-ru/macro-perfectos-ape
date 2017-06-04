package ru.autosome.macroape.calculation.generalized;

import ru.autosome.commons.model.Position;
import ru.autosome.commons.motifModel.Alignable;
import ru.autosome.macroape.model.AlignmentGenerator;
import ru.autosome.macroape.model.ComparisonSimilarityInfo;
import ru.autosome.macroape.model.PairAligned;

import java.util.Comparator;
import java.util.function.Function;

public class CompareModelsExact<ModelType extends Alignable<ModelType>> {
  private final AlignmentGenerator<ModelType> alignmentGenerator;
  private final int backgroundVolume;
  private final Function<PairAligned<ModelType>, ? extends AlignedModelIntersection> calculatorOfAligned;

  public CompareModelsExact(ModelType firstPWM, ModelType secondPWM,
                            int backgroundVolume,
                            Function<PairAligned<ModelType>, ? extends AlignedModelIntersection> calculatorOfAligned) {
    this.backgroundVolume = backgroundVolume;
    this.calculatorOfAligned = calculatorOfAligned;
    this.alignmentGenerator = new AlignmentGenerator<>(firstPWM, secondPWM);
  }

  private double firstCountRenormMultiplier(PairAligned alignment) {
    return Math.pow(backgroundVolume, alignment.firstComplementLength());
  }
  private double secondCountRenormMultiplier(PairAligned alignment) {
    return Math.pow(backgroundVolume, alignment.secondComplementLength());
  }

  public ComparisonSimilarityInfo jaccard(double thresholdFirst, double thresholdSecond,
                                          double firstCount, double secondCount) {
    return alignmentGenerator.relative_positions().map(
        (Position position) -> jaccardAtPosition(thresholdFirst, thresholdSecond, firstCount, secondCount, position)
    ).max(Comparator.comparingDouble(ComparisonSimilarityInfo::similarity)).get();
  }

  public ComparisonSimilarityInfo jaccardAtPosition(double thresholdFirst, double thresholdSecond,
                                                    double firstCount, double secondCount,
                                                    Position position) {
    PairAligned<ModelType> alignment = alignmentGenerator.alignment(position);
    double intersection = calculatorOfAligned.apply(alignment).count_in_intersection(thresholdFirst, thresholdSecond);

    double firstCountRenormed = firstCount * firstCountRenormMultiplier(alignment);
    double secondCountRenormed = secondCount * secondCountRenormMultiplier(alignment);

    return new ComparisonSimilarityInfo(alignment, intersection, firstCountRenormed, secondCountRenormed);
  }
}
