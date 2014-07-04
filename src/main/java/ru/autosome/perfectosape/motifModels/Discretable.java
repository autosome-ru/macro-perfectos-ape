package ru.autosome.perfectosape.motifModels;

import ru.autosome.perfectosape.Discretizer;

public interface Discretable<ModelType> {
  ModelType discrete(Discretizer discretizer);
}
