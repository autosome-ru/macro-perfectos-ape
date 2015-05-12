package ru.autosome.commons.importer;

import ru.autosome.commons.model.Named;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

public abstract class MotifImporter<ModelType> {
  abstract public ModelType createMotif(double matrix[][]);
  abstract public ParsingResult parse(List<String> strings);

  public ModelType loadMotif(List<String> lines){
    return loadMotifWithName(lines).getObject();
  }
  public ModelType loadMotif(File file) {
    return loadMotifWithName(file).getObject();
  }
  public Named<ModelType> loadMotifWithName(List<String> lines){
    ParsingResult parsingInfo = parse(lines);
    return new Named<ModelType>(createMotif(parsingInfo.getMatrix()),
                       parsingInfo.getName());
  }
  public ModelType loadMotif(String filename) {
    return loadMotifWithName(filename).getObject();
  }


  public Named<ModelType> loadMotifWithName(File file) {
    List<String> lines;
    try {
      lines = InputExtensions.readLinesFromFile(file);
    } catch (FileNotFoundException e) {
      return null;
    }
    ParsingResult parsingInfo = parse(lines);
    String name;
    if (parsingInfo.getName() == null || parsingInfo.getName().isEmpty()) {
      name = file.getName().replaceAll("\\.[^.]+$", "");
    } else {
      name = parsingInfo.getName();
    }
    return new Named<ModelType>(createMotif(parsingInfo.getMatrix()), name);
  }
  public Named<ModelType> loadMotifWithName(String filename) {
    return loadMotifWithName(new File(filename));
  }

  public List<ModelType> loadMotifCollection(File pathToMotifs) {
    List<Named<ModelType>> namedMotifs = loadMotifCollectionWithNames(pathToMotifs);
    List<ModelType> result = new ArrayList<ModelType>(namedMotifs.size());
    for (Named<ModelType> namedModel: namedMotifs) {
      result.add(namedModel.getObject());
    }
    return result;
  }

  public List<Named<ModelType>> loadMotifCollectionWithNames(File pathToMotifs) {
    if (pathToMotifs.isDirectory()) {
      return loadMotifCollectionWithNamesFromFolder(pathToMotifs);
    } else {
      return loadMotifCollectionWithNamesFromFile(pathToMotifs);
    }
  }

  public List<Named<ModelType>> loadMotifCollectionWithNamesFromFolder(File pathToPWMs) {
    List<Named<ModelType>> result = new ArrayList<Named<ModelType>>();
    File[] files = pathToPWMs.listFiles();
    if (files == null) {
      return result;
    }
    for (File file : files) {
      Named<ModelType> motif = loadMotifWithName(file);
      if (motif != null) {
        result.add(motif);
      }
    }
    return result;
  }


  public List<Named<ModelType>> loadMotifCollectionWithNamesFromFile(File pathToPWMs) {
    throw new NotImplementedException();
    // TODO: fix!!!!!
    // TODO: make use of MotifSplitter
    // return new ArrayList<Named<ModelType>>();
  }
}
