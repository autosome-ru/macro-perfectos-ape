package ru.autosome.perfectosape.importers;

import ru.autosome.perfectosape.MotifEvaluatorCollection;
import ru.autosome.perfectosape.calculations.findPvalue.CanFindPvalue;
import ru.autosome.perfectosape.calculations.findThreshold.CanFindThreshold;
import ru.autosome.perfectosape.motifModels.PWM;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

public class PWMCollectionImporter {
  PWMImporter importer;
  
  public PWMCollectionImporter(PWMImporter importer) {
    this.importer = importer;
  }

  public List<PWM> loadPWMCollection(File pathToPwms) throws FileNotFoundException {
    // TODO: expand features
    return loadPWMCollectionFromFolder(pathToPwms);
  }

  public List<PWM> loadPWMCollectionFromFolder(File pathToPWMs) throws FileNotFoundException {
    List<PWM> result = new ArrayList<PWM>();
    File[] files = pathToPWMs.listFiles();
    if (files == null) {
      return result;
    }
    for (File file : files) {
      PWM pwm = importer.loadPWMFromFile(file);
      result.add(pwm);
//    result.add(pwm,
//               pvalueCalculatorBuilder.applyMotif(pwm).build(),
//               thresholdCalculatorBuilder.applyMotif(pwm).build());
    }
    return result;
  }

  public List<PWM> loadPWMCollectionFromFile(File pathToPWMs) {
    try {
      List<PWM> result = new ArrayList<PWM>();
      BufferedPushbackReader reader = new BufferedPushbackReader(new FileInputStream(pathToPWMs));
      boolean canExtract = true;
      while (canExtract) {
        PMParser parser = PMParser.loadFromStream(reader);
        canExtract = canExtract && (parser != null);
        if (parser == null) {
          canExtract = false;
        } else {
          PWM pwm = importer.loadPWMFromParser(parser);
          result.add(pwm);
//          result.add(pwm,
//                     pvalueCalculatorBuilder.applyMotif(pwm).build(),
//                     thresholdCalculatorBuilder.applyMotif(pwm).build());
        }
      }
      return result;
    } catch (FileNotFoundException e) {
      return null;
    }
  }

}
