package ru.autosome.macroape.di;

import ru.autosome.commons.backgroundModel.di.DiBackground;
import ru.autosome.commons.backgroundModel.di.DiBackgroundModel;
import ru.autosome.commons.backgroundModel.di.DiWordwiseBackground;
import ru.autosome.commons.importer.DiPWMFromMonoImporter;
import ru.autosome.commons.importer.DiPWMImporter;
import ru.autosome.commons.importer.MotifImporter;
import ru.autosome.commons.model.Named;
import ru.autosome.commons.motifModel.di.DiPWM;
import ru.autosome.macroape.calculation.di.CompareModelsCountsGiven;

import java.io.File;
import java.util.List;

public class CollectDistanceMatrix extends ru.autosome.macroape.cli.generalized.CollectDistanceMatrix<DiPWM, DiBackgroundModel> {
  @Override
  protected String DOC_background_option() {
    return "ACGT - 16 numbers, comma-delimited(spaces not allowed), sum should be equal to 1, like 0.02,0.03,0.03,0.02,0.08,0.12,0.12,0.08,0.08,0.12,0.12,0.08,0.02,0.03,0.03,0.02";
  }
  @Override
  protected String DOC_run_string() {
    return "java ru.autosome.macroape.di.CollectDistanceMatrix";
  }

  @Override
  protected String DOC_additional_options() {
    return "  [--from-mono]  - obtain DiPWMs from mononucleotide PWM/PCM/PPMs.\n";
  }

  protected boolean failed_to_recognize_additional_options(String opt, List<String> argv) {
    if (opt.equals("--from-mono")) {
      fromMononucleotide= true;
      return false;
    } else {
      return true;
    }
  }

  boolean fromMononucleotide;

  @Override
  protected List<Named<DiPWM>> loadMotifCollection(File path_to_collection) {
    MotifImporter<DiPWM> importer;
    if (fromMononucleotide) {
      importer = new DiPWMFromMonoImporter(background, dataModel, effectiveCount, transpose, pseudocount);
    } else {
      importer = new DiPWMImporter(background, dataModel, effectiveCount, transpose, pseudocount);
    }
    return importer.loadMotifCollectionWithNames(path_to_collection);
  }

  @Override
  protected void initialize_defaults() {
    super.initialize_defaults();
    fromMononucleotide = false;
  }

  @Override
  protected void initialize_default_background() {
    background = new DiWordwiseBackground();
  }

  @Override
  protected DiBackgroundModel extract_background(String str) {
    return DiBackground.fromString(str);
  }

  private static CollectDistanceMatrix from_arglist(String[] args) {
    CollectDistanceMatrix result = new CollectDistanceMatrix();
    result.setup_from_arglist(args);
    return result;
  }

  @Override
  protected CompareModelsCountsGiven calculator(DiPWM firstModel, DiPWM secondModel) {
    return new CompareModelsCountsGiven(firstModel, secondModel, background, roughDiscretizer);
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
