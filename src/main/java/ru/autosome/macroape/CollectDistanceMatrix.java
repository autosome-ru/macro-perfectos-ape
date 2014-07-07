package ru.autosome.macroape;

import ru.autosome.commons.backgroundModel.mono.Background;
import ru.autosome.commons.backgroundModel.mono.BackgroundModel;
import ru.autosome.commons.backgroundModel.mono.WordwiseBackground;
import ru.autosome.commons.cli.Helper;
import ru.autosome.commons.importer.PWMImporter;
import ru.autosome.commons.model.BoundaryType;
import ru.autosome.commons.model.Discretizer;
import ru.autosome.commons.motifModel.mono.PWM;
import ru.autosome.commons.motifModel.types.DataModel;
import ru.autosome.macroape.calculation.mono.CompareModelsCountsGiven;

import java.util.ArrayList;
import java.util.Collections;
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

  private void initialize_defaults() {
    roughDiscretizer = new Discretizer(1.0);
    preciseDiscretizer = new Discretizer(10.0);

    background = new WordwiseBackground();
    maxHashSize = 10000000;
    maxPairHashSize = 10000;
    dataModel = DataModel.PWM;
    effectiveCount = 100;
    pvalue = 0.0005;
    pvalueBoundary = BoundaryType.UPPER;
    preciseRecalculationCutoff = null;

    numOfThreads = 1;
    numThread = 0;

    pathToCollectionOfPWMs = null;
    pwmCollection = null;
  }


  @Override
  protected BackgroundModel extract_background(String str) {
    return Background.fromString(str);
  }

  private CollectDistanceMatrix() {
    initialize_defaults();
  }

  private static CollectDistanceMatrix from_arglist(List<String> argv) {
    CollectDistanceMatrix result = new CollectDistanceMatrix();
    Helper.print_help_if_requested(argv, new CollectDistanceMatrix().documentString());
    result.setup_from_arglist(argv);
    return result;
  }

  private static CollectDistanceMatrix from_arglist(String[] args) {
    ArrayList<String> argv = new ArrayList<String>();
    Collections.addAll(argv, args);
    return from_arglist(argv);
  }


  @Override
  protected PWMImporter motifImporter() {
    return new PWMImporter(background, dataModel, effectiveCount);
  }

  @Override
  protected CompareModelsCountsGiven calculator(PWM firstModel, PWM secondModel) {
    return new CompareModelsCountsGiven(firstModel, secondModel,
                                 background, background,
                                 roughDiscretizer, maxPairHashSize);
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
