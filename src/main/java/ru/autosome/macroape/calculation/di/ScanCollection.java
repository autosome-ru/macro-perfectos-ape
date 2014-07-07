package ru.autosome.macroape.calculation.di;

import ru.autosome.ape.calculation.findPvalue.CanFindPvalue;
import ru.autosome.commons.backgroundModel.di.DiBackgroundModel;
import ru.autosome.commons.model.Discretizer;
import ru.autosome.commons.motifModel.di.DiPWM;

import java.util.List;

public class ScanCollection extends ru.autosome.macroape.calculation.generalized.ScanCollection<DiPWM, DiBackgroundModel> {


  protected ru.autosome.macroape.calculation.di.CompareModels calculation(DiPWM firstMotif, DiPWM secondMotif,
                                                                            DiBackgroundModel firstBackground, DiBackgroundModel secondBackground,
                                                                            CanFindPvalue firstPvalueCalculator, CanFindPvalue secondPvalueCalculator,
                                                                            Discretizer discretizer, Integer max_hash_size) {
    return new ru.autosome.macroape.calculation.di.CompareModels( firstMotif, secondMotif,
                                                                   firstBackground, secondBackground,
                                                                   firstPvalueCalculator, secondPvalueCalculator,
                                                                   discretizer, max_hash_size);
  }

  public ScanCollection(List<ru.autosome.macroape.di.ScanCollection.ThresholdEvaluator> thresholdEvaluators, DiPWM queryPWM) {
    super(thresholdEvaluators, queryPWM);
  }
}

