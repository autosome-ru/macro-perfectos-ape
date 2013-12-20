package ru.autosome.perfectosape;

import ru.autosome.perfectosape.calculations.CanFindPvalue;
import ru.autosome.perfectosape.calculations.FindPvalueAPE;
import ru.autosome.perfectosape.calculations.FindPvalueBsearch;
import ru.autosome.perfectosape.cli.Helper;

import java.io.File;

public class PWMCollectionImporter {
  BackgroundModel background;
  Double discretization;
  Integer maxHashSize;
  DataModel dataModel;
  Double effectiveCount;
  
  public PWMCollectionImporter(BackgroundModel background, Double discretization, Integer maxHashSize, DataModel dataModel, Double effectiveCount) {
    this.background = background;
    this.discretization = discretization;
    this.maxHashSize = maxHashSize;
    this.dataModel = dataModel;
    this.effectiveCount = effectiveCount;
  }
  private CanFindPvalue pvalueCalculation(File pwmFilename, PWM pwm, File pathToThresholds) {
    if (pathToThresholds != null) {
      File thresholds_file = new File(pathToThresholds, "thresholds_" + pwmFilename.getName());
      PvalueBsearchList pvalueBsearchList = PvalueBsearchList.load_from_file(thresholds_file.getAbsolutePath());
      return new FindPvalueBsearch(pwm, background, pvalueBsearchList);
    } else {
      return new FindPvalueAPE(pwm, discretization, background, maxHashSize);
    }
  }

  public PWMCollection loadPWMCollection(File pathToPwms, File pathToThresholds) {
    PWMCollection result = new PWMCollection();
    File[] files = pathToPwms.listFiles();
    if (files == null) {
      return result;
    }
    for (File file : files) {
      PWM pwm = Helper.load_pwm(file, dataModel, background, effectiveCount);
      result.add(pwm, pvalueCalculation(file, pwm, pathToThresholds));
    }
    return result;
  }

}
