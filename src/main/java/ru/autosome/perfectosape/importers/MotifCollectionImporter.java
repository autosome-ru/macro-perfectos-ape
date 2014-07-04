package ru.autosome.perfectosape.importers;

import ru.autosome.perfectosape.motifModels.Named;
import ru.autosome.perfectosape.motifModels.ScoringModel;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

public class MotifCollectionImporter<ModelType extends Named & ScoringModel> {
  private final MotifImporter<ModelType> importer;
  
  public MotifCollectionImporter(MotifImporter<ModelType> importer) {
    this.importer = importer;
  }

  public List<ModelType> loadPWMCollection(File pathToPwms) {
    if (pathToPwms.isDirectory()) {
      return loadPWMCollectionFromFolder(pathToPwms);
    } else {
      return loadPWMCollectionFromFile(pathToPwms);
    }
  }

  private List<ModelType> loadPWMCollectionFromFolder(File pathToPWMs) {
    List<ModelType> result = new ArrayList<ModelType>();
    File[] files = pathToPWMs.listFiles();
    if (files == null) {
      return result;
    }
    for (File file : files) {
      ModelType pwm = importer.loadPWMFromFile(file);
      result.add(pwm);
    }
    return result;
  }

  private List<ModelType> loadPWMCollectionFromFile(File pathToPWMs) {
    try {
      List<ModelType> result = new ArrayList<ModelType>();
      BufferedPushbackReader reader = new BufferedPushbackReader(new FileInputStream(pathToPWMs));
      boolean canExtract = true;
      while (canExtract) {
        PMParser parser = PMParser.loadFromStream(reader);
        canExtract = canExtract && (parser != null);
        if (parser == null) {
          canExtract = false;
        } else {
          ModelType pwm = importer.loadPWMFromParser(parser);
          result.add(pwm);
        }
      }
      return result;
    } catch (FileNotFoundException e) {
      return null;
    }
  }

}
