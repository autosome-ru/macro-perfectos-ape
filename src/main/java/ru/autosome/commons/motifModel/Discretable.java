package ru.autosome.commons.motifModel;

import ru.autosome.commons.model.Discretizer;

public interface Discretable<ModelType> {
  ModelType discrete(Discretizer discretizer);
}
