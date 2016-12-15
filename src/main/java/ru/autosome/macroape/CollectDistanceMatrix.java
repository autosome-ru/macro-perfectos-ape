package ru.autosome.macroape;

import ru.autosome.commons.backgroundModel.mono.Background;
import ru.autosome.commons.backgroundModel.mono.BackgroundModel;
import ru.autosome.commons.backgroundModel.mono.WordwiseBackground;
import ru.autosome.commons.importer.PWMImporter;
import ru.autosome.commons.model.Named;
import ru.autosome.commons.motifModel.mono.PWM;
import ru.autosome.macroape.calculation.mono.CompareModelsCountsGiven;

import java.io.File;
import java.util.List;

public class CollectDistanceMatrix extends ru.autosome.macroape.cli.generalized.CollectDistanceMatrix<PWM, BackgroundModel> {
  @Override
  protected String DOC_background_option() {
    return "ACGT - 4 numbers, comma-delimited(spaces not allowed), sum should be equal to 1, like 0.25,0.24,0.26,0.25";
  }
  @Override
  protected String DOC_run_string() {
    return "java ru.autosome.macroape.CollectDistanceMatrix";
  }

  @Override
  protected void initialize_default_background() {
    background = new WordwiseBackground();
  }

  @Override
  protected BackgroundModel extract_background(String str) {
    return Background.fromString(str);
  }

  private CollectDistanceMatrix() {
    initialize_defaults();
  }

  private static CollectDistanceMatrix from_arglist(String[] args) {
    CollectDistanceMatrix result = new CollectDistanceMatrix();
    result.setup_from_arglist(args);
    return result;
  }

  @Override
  protected List<Named<PWM>> loadMotifCollection(File path_to_collection) {
    PWMImporter importer = new PWMImporter(background, dataModel, effectiveCount, transpose, pseudocount);
    return importer.loadMotifCollectionWithNames(path_to_collection);
  }

  @Override
  protected CompareModelsCountsGiven calculator(PWM firstModel, PWM secondModel) {
    return new CompareModelsCountsGiven(firstModel, secondModel,
                                 background, background,
                                 roughDiscretizer);
  }



  public static void main(String[] args) {
    try {
      CollectDistanceMatrix cli = CollectDistanceMatrix.from_arglist(args);
      cli.process();
    } catch (Exception err) {
      System.err.println("\n" + err.getMessage() + "\n--------------------------------------\n");
      err.printStackTrace();
      System.err.println("\n--------------------------------------\nUse --help option for help\n\n" + new CollectDistanceMatrix().documentString());
      System.exit(1);
    }
  }
}
