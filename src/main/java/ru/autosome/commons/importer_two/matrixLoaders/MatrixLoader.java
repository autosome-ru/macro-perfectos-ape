package ru.autosome.commons.importer_two.matrixLoaders;

import ru.autosome.commons.importer.InputExtensions;
import ru.autosome.commons.model.Named;
import ru.autosome.commons.motifModel.mono.PCM;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;

// It should be an interface, but default interface method needs Java 8
public abstract class MatrixLoader {
  public abstract Named<double[][]> loadMatrix(List<String> strings);

  public Named<double[][]> loadMatrix(File file) {
    List<String> lines;
    try {
      lines = InputExtensions.readLinesFromFile(file);
    } catch (FileNotFoundException e) {
      return null;
    }
    Named<double[][]> result = loadMatrix(lines);
    if (result.getName() == null || result.getName().isEmpty()) {
      String name = file.getName().replaceAll("\\.[^.]+$", "");
      result.setName(name);
    }
    return result;
  }
}
