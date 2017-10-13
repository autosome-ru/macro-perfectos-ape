package ru.autosome.macroape.model;

import ru.autosome.commons.model.Orientation;
import ru.autosome.commons.model.Position;
import ru.autosome.commons.motifModel.Alignable;

import java.util.stream.Stream;

public class AlignmentGenerator<ModelType extends Alignable<ModelType>> {
  private final ModelType firstModel;
  private final ModelType secondModel;
  public AlignmentGenerator(ModelType firstModel, ModelType secondModel) {
    this.firstModel = firstModel;
    this.secondModel = secondModel;
  }

  public Stream<Position> relative_positions() {
    Stream.Builder<Position> builder = Stream.builder();
    for(int shift = -secondModel.length(); shift <= firstModel.length(); ++shift) {
      builder.accept(new Position(shift, Orientation.direct));
      builder.accept(new Position(shift, Orientation.revcomp));
    }
    return builder.build();
  }

  public Stream<Position> relative_positions_fixed_strand(Orientation strand) {
    Stream.Builder<Position> builder = Stream.builder();
    for(int shift = -secondModel.length(); shift <= firstModel.length(); ++shift) {
      builder.accept(new Position(shift, strand));
    }
    return builder.build();
  }

  public PairAligned<ModelType> alignment(Position position) {
    return new PairAligned<>(firstModel, secondModel, position);
  }

  public Stream<PairAligned<ModelType>> alignments() {
    return relative_positions().map(this::alignment);
  }
}
