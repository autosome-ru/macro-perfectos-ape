package ru.autosome.perfectosape.importers;

import ru.autosome.perfectosape.backgroundModels.BackgroundModel;
import ru.autosome.perfectosape.motifModels.DataModel;
import ru.autosome.perfectosape.motifModels.PCM;
import ru.autosome.perfectosape.motifModels.PPM;
import ru.autosome.perfectosape.motifModels.PWM;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

public class PWMImporter {
  BackgroundModel background;
  DataModel dataModel;
  Double effectiveCount;

  public PWMImporter(BackgroundModel background, DataModel dataModel, Double effectiveCount) {
    this.background = background;
    this.dataModel = dataModel;
    this.effectiveCount = effectiveCount;
  }
  
  // constructs PWM from any source: pwm/pcm/ppm matrix
  public PWM transformToPWM(double matrix[][], String name) {
    PWM pwm;
    switch (dataModel) {
      case PCM:
        pwm = new PCM(matrix, name).to_pwm(background);
        break;
      case PPM:
        pwm = new PPM(matrix, name).to_pwm(background, effectiveCount);
        break;
      case PWM:
        pwm = new PWM(matrix, name);
        break;
      default:
        throw new Error("This code never reached");
    }
    return pwm;
  }

  public List<PWM> loadPWMsFromFile(File pathToPWMs) throws FileNotFoundException {
    List pwms = new ArrayList();
    BufferedPushbackReader reader = new BufferedPushbackReader(new FileInputStream(pathToPWMs));
    boolean canExtract = true;
    while (canExtract) {
      PMParser parser = PMParser.loadFromStream(reader);
      canExtract = canExtract && (parser != null);
      if (parser == null) {
        canExtract = false;
      } else {
        PWM pwm = transformToPWM(parser.matrix(), parser.name());
        pwms.add(pwm);
      }
    }
    return pwms;
  }

  public PWM loadPWMFromFile(File file) {
    PMParser parser = PMParser.from_file(file);
    PWM pwm = transformToPWM(parser.matrix(), parser.name());
    if (pwm.name == null || pwm.name.isEmpty()) {
      pwm.name = file.getName().replaceAll("\\.[^.]+$", "");
    }
    return pwm;
  }

  public PWM loadPWMFromParser(PMParser parser) {
    PWM pwm = transformToPWM(parser.matrix(), parser.name());
    return pwm;
  }

  public List<PWM> loadPWMsFromFolder(File pathToPWMs) {
    List<PWM> result = new ArrayList<PWM>();
    File[] files = pathToPWMs.listFiles();
    if (files == null) {
      return result;
    }
    for (File file : files) {
      result.add(loadPWMFromFile(file));
    }
    return result;
  }
}
