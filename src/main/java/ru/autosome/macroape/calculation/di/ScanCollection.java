package ru.autosome.macroape.calculation.di;

import ru.autosome.ape.calculation.findPvalue.CanFindPvalue;
import ru.autosome.commons.backgroundModel.di.DiBackgroundModel;
import ru.autosome.commons.motifModel.di.DiPWM;
import ru.autosome.macroape.calculation.generalized.ThresholdEvaluator;

import java.util.List;

public class ScanCollection extends ru.autosome.macroape.calculation.generalized.ScanCollection<DiPWM, DiBackgroundModel> {


  protected ru.autosome.macroape.calculation.di.CompareModels calculation(DiPWM firstMotif, DiPWM secondMotif,
                                                                            DiBackgroundModel firstBackground, DiBackgroundModel secondBackground,
                                                                            CanFindPvalue firstPvalueCalculator, CanFindPvalue secondPvalueCalculator,
                                                                            Double discretization, Integer max_hash_size) {
    return new ru.autosome.macroape.calculation.di.CompareModels( firstMotif, secondMotif,
                                                                   firstBackground, secondBackground,
                                                                   firstPvalueCalculator, secondPvalueCalculator,
                                                                   discretization, max_hash_size);
  }

  public ScanCollection(List<ThresholdEvaluator<DiPWM>> thresholdEvaluators, DiPWM queryPWM) {
    super(thresholdEvaluators, queryPWM);
  }
}

