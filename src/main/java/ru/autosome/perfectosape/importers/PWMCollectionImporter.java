package ru.autosome.perfectosape.importers;

import ru.autosome.perfectosape.MotifEvaluatorCollection;
import ru.autosome.perfectosape.PvalueBsearchList;
import ru.autosome.perfectosape.backgroundModels.BackgroundModel;
import ru.autosome.perfectosape.calculations.findPvalue.CanFindPvalue;
import ru.autosome.perfectosape.calculations.findPvalue.FindPvalueAPE;
import ru.autosome.perfectosape.calculations.findPvalue.FindPvalueBsearch;
import ru.autosome.perfectosape.motifModels.PWM;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

public class PWMCollectionImporter {
  // TODO: to be removed
  CanFindPvalue.Builder builder;
  PWMImporter importer;
  
  public PWMCollectionImporter(PWMImporter importer, CanFindPvalue.Builder builder) {
    this.importer = importer;
    this.builder = builder;
  }

  public MotifEvaluatorCollection loadPWMCollection(File pathToPwms) throws FileNotFoundException {
    return loadPWMCollectionFromFolder(pathToPwms);
  }

  public MotifEvaluatorCollection loadPWMCollectionFromFolder(File pathToPWMs) throws FileNotFoundException {
    MotifEvaluatorCollection result = new MotifEvaluatorCollection();
    File[] files = pathToPWMs.listFiles();
    if (files == null) {
      return result;
    }
    for (File file : files) {
      PWM pwm = importer.loadPWMFromFile(file);
      result.add(pwm, builder.applyMotif(pwm).build());
    }
    return result;
  }

  public MotifEvaluatorCollection loadPWMCollectionFromFile(File pathToPWMs) {
    try {
      MotifEvaluatorCollection result = new MotifEvaluatorCollection();
      BufferedPushbackReader reader = new BufferedPushbackReader(new FileInputStream(pathToPWMs));
      boolean canExtract = true;
      while (canExtract) {
        PMParser parser = PMParser.loadFromStream(reader);
        canExtract = canExtract && (parser != null);
        if (parser == null) {
          canExtract = false;
        } else {
          PWM pwm = importer.loadPWMFromParser(parser);
          result.add(pwm, builder.applyMotif(pwm).build());
        }
      }
      return result;
    } catch (FileNotFoundException e) {
      return new MotifEvaluatorCollection();
    }
  }

}
