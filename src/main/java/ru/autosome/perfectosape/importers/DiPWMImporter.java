package ru.autosome.perfectosape.importers;

import ru.autosome.perfectosape.backgroundModels.BackgroundModel;
import ru.autosome.perfectosape.backgroundModels.DiBackgroundModel;
import ru.autosome.perfectosape.motifModels.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

public class DiPWMImporter {
  DiBackgroundModel dibackground;
  DataModel dataModel;
  Double effectiveCount;

  public DiPWMImporter(DiBackgroundModel dibackground, DataModel dataModel, Double effectiveCount) {
    this.dibackground = dibackground;
    this.dataModel = dataModel;
    this.effectiveCount = effectiveCount;
  }

  // constructs DiPWM from any source: pwm/pcm/ppm matrix
  public DiPWM transformToPWM(double matrix[][], String name) {
    DiPWM dipwm;
    switch (dataModel) {
      case PCM:
        throw new Error("PCM dinucleotide mode not yet implemented");
      case PPM:
        throw new Error("PPM dinucleotide mode not yet implemented");
      case PWM:
        dipwm = new DiPWM(matrix, name);
        break;
      default:
        throw new Error("This code never reached");
    }
    return dipwm;
  }

  public List<DiPWM> loadPWMsFromFile(File pathToPWMs) throws FileNotFoundException {
    List<DiPWM> dipwms = new ArrayList<DiPWM>();
    BufferedPushbackReader reader = new BufferedPushbackReader(new FileInputStream(pathToPWMs));
    boolean canExtract = true;
    while (canExtract) {
      PMParser parser = PMParser.loadFromStream(reader);
      canExtract = canExtract && (parser != null);
      if (parser == null) {
        canExtract = false;
      } else {
        DiPWM dipwm = transformToPWM(parser.matrix(), parser.name());
        dipwms.add(dipwm);
      }
    }
    return dipwms;
  }

  public DiPWM loadPWMFromFile(File file) {
    PMParser parser = PMParser.from_file(file);
    DiPWM dipwm = transformToPWM(parser.matrix(), parser.name());
    if (dipwm.name == null || dipwm.name.isEmpty()) {
      dipwm.name = file.getName().replaceAll("\\.[^.]+$", "");
    }
    return dipwm;
  }

  public DiPWM loadPWMFromParser(PMParser parser) {
    DiPWM dipwm = transformToPWM(parser.matrix(), parser.name());
    return dipwm;
  }

  public List<DiPWM> loadPWMsFromFolder(File pathToPWMs) {
    List<DiPWM> result = new ArrayList<DiPWM>();
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
