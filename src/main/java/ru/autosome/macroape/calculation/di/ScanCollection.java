package ru.autosome.macroape.calculation.di;

import ru.autosome.ape.calculation.findPvalue.CanFindPvalue;
import ru.autosome.commons.backgroundModel.di.DiBackgroundModel;
import ru.autosome.commons.model.Discretizer;
import ru.autosome.commons.motifModel.di.DiPWM;
import ru.autosome.macroape.model.ThresholdEvaluator;

import java.util.List;

public class ScanCollection extends ru.autosome.macroape.calculation.generalized.ScanCollection<DiPWM, DiBackgroundModel> {


  protected ru.autosome.macroape.calculation.di.CompareModels calculation(DiPWM firstMotif, DiPWM secondMotif,
                                                                            DiBackgroundModel background,
                                                                            CanFindPvalue firstPvalueCalculator, CanFindPvalue secondPvalueCalculator,
                                                                            Discretizer discretizer) {
    return new ru.autosome.macroape.calculation.di.CompareModels( firstMotif, secondMotif,
                                                                   background,
                                                                   firstPvalueCalculator, secondPvalueCalculator,
                                                                   discretizer);
  }

  public ScanCollection(List<ThresholdEvaluator<DiPWM>> thresholdEvaluators, DiPWM queryPWM) {
    super(thresholdEvaluators, queryPWM);
  }
}

