package ru.autosome.commons.motifModel;

import ru.autosome.commons.backgroundModel.AbstractBackgroundFactory;
import ru.autosome.commons.backgroundModel.GeneralizedBackgroundModel;

public interface BackgroundCompatible<BackgroundType extends GeneralizedBackgroundModel> {
  public AbstractBackgroundFactory<BackgroundType> compatibleBackground();
}
