package ru.autosome.commons.converter.generalized;

public interface MotifConverter<ModelTypeFrom, ModelTypeTo> {
  ModelTypeTo convert(ModelTypeFrom motifFrom);
}
