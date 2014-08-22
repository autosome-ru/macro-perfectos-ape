package ru.autosome.commons.importer;

import ru.autosome.commons.backgroundModel.GeneralizedBackgroundModel;
import ru.autosome.commons.model.PseudocountCalculator;
import ru.autosome.commons.motifModel.Named;
import ru.autosome.commons.motifModel.ScoringModel;
import ru.autosome.commons.motifModel.types.DataModel;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

public abstract class MotifImporter<ModelType extends Named & ScoringModel, BackgroundType extends GeneralizedBackgroundModel> {
  final BackgroundType background;
  final DataModel dataModel;
  final Double effectiveCount;
  final PseudocountCalculator pseudocountCalculator;

  public MotifImporter(BackgroundType background, DataModel dataModel, Double effectiveCount, PseudocountCalculator pseudocountCalculator) {
    this.background = background;
    this.dataModel = dataModel;
    this.effectiveCount = effectiveCount;
    this.pseudocountCalculator = pseudocountCalculator;
  }

  abstract public ModelType createMotif(double matrix[][], String name);
  abstract public ParsingResult parse(List<String> strings);

  public ModelType loadMotif(List<String> lines){
    ParsingResult parsingInfo = parse(lines);
    return createMotif(parsingInfo.getMatrix(), parsingInfo.getName());
  }

  public ModelType loadMotif(File file) {
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
    return createMotif(parsingInfo.getMatrix(), name);
  }

  public ModelType loadMotif(String filename) {
    return loadMotif(new File(filename));
  }

  public List<ModelType> loadMotifCollection(File pathToMotifs) {
    if (pathToMotifs.isDirectory()) {
      return loadMotifCollectionFromFolder(pathToMotifs);
    } else {
      return loadMotifCollectionFromFile(pathToMotifs);
    }
  }

  public List<ModelType> loadMotifCollectionFromFolder(File pathToPWMs) {
    List<ModelType> result = new ArrayList<ModelType>();
    File[] files = pathToPWMs.listFiles();
    if (files == null) {
      return result;
    }
    for (File file : files) {
      ModelType motif = loadMotif(file);
      if (motif != null) {
        result.add(motif);
      }
    }
    return result;
  }


  public List<ModelType> loadMotifCollectionFromFile(File pathToPWMs) {
    // TODO: fix!!!!!
    // TODO: make use of MotifSplitter
    return new ArrayList<ModelType>();
  }
}
