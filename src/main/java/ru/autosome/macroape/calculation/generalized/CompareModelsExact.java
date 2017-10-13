package ru.autosome.macroape.calculation.generalized;

import ru.autosome.ape.calculation.findPvalue.FoundedPvalueInfo;
import ru.autosome.commons.model.Orientation;
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


  public ComparisonSimilarityInfo jaccard(FoundedPvalueInfo first, FoundedPvalueInfo second) {
    return alignmentGenerator.relative_positions().map(
        (Position position) -> jaccardAtPosition(first, second, position)
    ).max(Comparator.comparingDouble(ComparisonSimilarityInfo::similarity)).get();
  }

  public ComparisonSimilarityInfo jaccardFixedStrand(FoundedPvalueInfo first, FoundedPvalueInfo second, Orientation strand) {
    return alignmentGenerator.relative_positions_fixed_strand(strand).map(
        (Position position) -> jaccardAtPosition(first, second, position)
    ).max(Comparator.comparingDouble(ComparisonSimilarityInfo::similarity)).get();
  }

  public ComparisonSimilarityInfo jaccardAtPosition(FoundedPvalueInfo first, FoundedPvalueInfo second, Position position) {
    PairAligned<ModelType> alignment = alignmentGenerator.alignment(position);
    double intersection = calculatorOfAligned.apply(alignment).count_in_intersection(first.threshold, second.threshold);
    double vocabularySize = Math.pow(backgroundVolume, alignment.length());

    return new ComparisonSimilarityInfo(alignment, intersection, first.pvalue * vocabularySize, second.pvalue * vocabularySize);
  }
}
