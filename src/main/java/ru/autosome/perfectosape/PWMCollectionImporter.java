package ru.autosome.perfectosape;

import ru.autosome.perfectosape.calculations.CanFindPvalue;
import ru.autosome.perfectosape.calculations.FindPvalueAPE;
import ru.autosome.perfectosape.calculations.FindPvalueBsearch;
import ru.autosome.perfectosape.cli.Helper;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

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
    if (pathToPwms.isDirectory() && (pathToThresholds == null || pathToThresholds.isDirectory())) {
      return loadPWMCollectionFromFolder(pathToPwms, pathToThresholds);
    }
    PWMCollection result = new PWMCollection();
    return result;
  }

  public PWMCollection loadPWMCollectionFromFolder(File pathToPWMs, File pathToThresholds) {
    PWMCollection result = new PWMCollection();
    File[] files = pathToPWMs.listFiles();
    if (files == null) {
      return result;
    }
    for (File file : files) {
      PWM pwm = Helper.load_pwm(file, dataModel, background, effectiveCount);
      result.add(pwm, pvalueCalculation(file, pwm, pathToThresholds));
    }
    return result;
  }

  public PWMCollection loadPWMCollectionFromFile(File pathToPWMs) {
    try {
      PWMCollection result = new PWMCollection();
      BufferedPushbackReader reader = new BufferedPushbackReader(new FileInputStream(pathToPWMs));
      boolean canExtract = true;
      while (canExtract) {
        PMParser parser = PMParser.loadFromStream(reader);
        canExtract = canExtract && (parser != null);
        if (parser == null) {
          canExtract = false;
        } else {
          PWM pwm = PWM.fromParser(parser);
          result.add(pwm, new FindPvalueAPE(pwm, discretization, background, maxHashSize));
        }
      }
      return result;
    } catch (FileNotFoundException e) {
      return new PWMCollection();
    }
  }
}
