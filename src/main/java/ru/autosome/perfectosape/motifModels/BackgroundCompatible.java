package ru.autosome.perfectosape.motifModels;

import ru.autosome.perfectosape.backgroundModels.AbstractBackgroundFactory;
import ru.autosome.perfectosape.backgroundModels.GeneralizedBackgroundModel;

public interface BackgroundCompatible<BackgroundType extends GeneralizedBackgroundModel> {
  public AbstractBackgroundFactory<BackgroundType> compatibleBackground();
}
