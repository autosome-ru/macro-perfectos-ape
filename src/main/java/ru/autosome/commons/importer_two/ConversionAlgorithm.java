package ru.autosome.commons.importer_two;

import ru.autosome.commons.importer_two.matrixLoaders.MatrixLoader;
import ru.autosome.commons.model.Named;

import java.util.ArrayList;

public abstract class ConversionAlgorithm<FromModel, ToModel> {
    public void extractArguments(ArrayList<String> argv) {
      extractRequiredArguments(argv);
      extractOptionalArguments(argv);
    }
    void extractRequiredArguments(ArrayList<String> argv) { }
    void extractOptionalArguments(ArrayList<String> argv) { }
    public abstract ToModel convert(FromModel from);
    public abstract MatrixLoader matrixLoader()

    public Named<ToModel> convertNamed(Named<FromModel> from) {
      return new Named<ToModel>(convert(from.getObject()), from.getName());
    }
}
