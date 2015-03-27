package ru.autosome.ape.di;

import ru.autosome.ape.calculation.PrecalculateThresholdList;
import ru.autosome.commons.backgroundModel.di.DiBackground;
import ru.autosome.commons.backgroundModel.di.DiBackgroundModel;
import ru.autosome.commons.backgroundModel.di.DiWordwiseBackground;
import ru.autosome.commons.backgroundModel.mono.Background;
import ru.autosome.commons.backgroundModel.mono.BackgroundModel;
import ru.autosome.commons.cli.Helper;
import ru.autosome.commons.importer.DiPWMImporter;
import ru.autosome.commons.importer.PWMImporter;
import ru.autosome.commons.model.Named;
import ru.autosome.commons.motifModel.di.DiPWM;
import ru.autosome.commons.motifModel.mono.PWM;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PrecalculateThresholds extends ru.autosome.ape.cli.generalized.PrecalculateThresholds<DiPWM,DiBackgroundModel> {

  boolean fromMononucleotide;

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
  protected void extract_background(String str) {
    background = DiBackground.fromString(str);
  }

  @Override
  protected PrecalculateThresholdList<DiPWM, DiBackgroundModel> calculator() {
    return new PrecalculateThresholdList<>(pvalues, discretizer, background, pvalue_boundary, max_hash_size);
  }

  @Override
  protected Named<DiPWM> loadMotif(File file){
    if (fromMononucleotide) {
      BackgroundModel backgroundMononucleotide = Background.fromDiBackground(background);
      PWMImporter importer = new PWMImporter(backgroundMononucleotide, data_model, effective_count, transpose, pseudocount);
      Named<PWM> namedMonoPWM = importer.loadMotifWithName(file);
      return new Named<>(DiPWM.fromPWM(namedMonoPWM.getObject()),
                         namedMonoPWM.getName());
    } else {
      DiPWMImporter importer = new DiPWMImporter(background, data_model, effective_count, transpose, pseudocount);
      return importer.loadMotifWithName(file);
    }
  }

  @Override
  protected String DOC_background_option() {
    return "ACGT - 16 numbers, comma-delimited(spaces not allowed), sum should be equal to 1, like 0.02,0.03,0.03,0.02,0.08,0.12,0.12,0.08,0.08,0.12,0.12,0.08,0.02,0.03,0.03,0.02";
  }

  @Override
  protected String DOC_run_string() {
    return "java ru.autosome.ape.di.PrecalculateThresholds";
  }

  @Override
  protected String DOC_additional_options() {
    return "  [--from-mono]  - obtain DiPWMs from mono PWM/PCM/PPMs.\n";
  }

  protected PrecalculateThresholds() {
    initialize_defaults();
  }

  @Override
  protected boolean failed_to_recognize_additional_options(String opt, List<String> argv) {
    if (opt.equals("--from-mono")) {
      fromMononucleotide = true;
      return false;
    } else {
      return true;
    }
  }

  protected static ru.autosome.ape.di.PrecalculateThresholds from_arglist(ArrayList<String> argv) {
    ru.autosome.ape.di.PrecalculateThresholds result = new ru.autosome.ape.di.PrecalculateThresholds();
    Helper.print_help_if_requested(argv, result.documentString());
    result.setup_from_arglist(argv);
    return result;
  }

  protected static ru.autosome.ape.di.PrecalculateThresholds from_arglist(String[] args) {
    ArrayList<String> argv = new ArrayList<String>();
    Collections.addAll(argv, args);
    return from_arglist(argv);
  }

  public static void main(String[] args) {
    try {
      ru.autosome.ape.di.PrecalculateThresholds calculation = ru.autosome.ape.di.PrecalculateThresholds.from_arglist(args);
      calculation.calculate_thresholds_for_collection();
    } catch (Exception err) {
      System.err.println("\n" + err.getMessage() + "\n--------------------------------------\n");
      err.printStackTrace();
      System.err.println("\n--------------------------------------\nUse --help option for help\n\n" + new ru.autosome.ape.PrecalculateThresholds().documentString());
      System.exit(1);
    }
  }
}
