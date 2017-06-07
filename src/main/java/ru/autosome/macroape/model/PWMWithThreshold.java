package ru.autosome.macroape.model;

import ru.autosome.ape.calculation.findPvalue.FoundedPvalueInfo;
import ru.autosome.ape.calculation.findThreshold.FoundedThresholdInfo;
import ru.autosome.commons.model.Named;

public class PWMWithThreshold<ModelType> {
  public final String name;
  public final ModelType pwm;
  public final FoundedPvalueInfo roughInfos;
  public final FoundedPvalueInfo preciseInfos;

  public PWMWithThreshold(Named<ModelType> pwm, FoundedThresholdInfo rough, FoundedThresholdInfo precise) {
    this.name = pwm.getName();
    this.pwm = pwm.getObject();
    this.roughInfos = rough.toFoundedPvalueInfo();
    this.preciseInfos = precise.toFoundedPvalueInfo();
  }
}
