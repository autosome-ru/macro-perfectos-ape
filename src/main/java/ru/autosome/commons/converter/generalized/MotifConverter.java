package ru.autosome.commons.converter.generalized;

import ru.autosome.commons.motifModel.MatrixModel;
import ru.autosome.commons.motifModel.Named;

public interface MotifConverter<ModelTypeFrom extends MatrixModel & Named, ModelTypeTo extends MatrixModel & Named> {
  ModelTypeTo convert(ModelTypeFrom motifFrom);
}
