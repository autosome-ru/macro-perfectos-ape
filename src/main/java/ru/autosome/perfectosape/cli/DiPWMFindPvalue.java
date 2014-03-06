package ru.autosome.perfectosape.cli;

import ru.autosome.perfectosape.backgroundModels.DiBackground;
import ru.autosome.perfectosape.backgroundModels.DiBackgroundModel;
import ru.autosome.perfectosape.backgroundModels.DiWordwiseBackground;
import ru.autosome.perfectosape.calculations.findPvalue.CanFindPvalue;
import ru.autosome.perfectosape.calculations.findPvalue.DiPWMFindPvalueAPE;
import ru.autosome.perfectosape.calculations.findPvalue.DiPWMFindPvalueBsearch;
import ru.autosome.perfectosape.importers.DiPWMImporter;
import ru.autosome.perfectosape.motifModels.DiPWM;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;

public class DiPWMFindPvalue extends FindPvalueGeneralized<DiPWM> {
  @Override
  protected String DOC_background_option() {
    return "ACGT - 16 numbers, comma-delimited(spaces not allowed), sum should be equal to 1, like 0.2,0.3,0.3,0.2,0.2,0.3,0.3,0.2,0.2,0.3,0.3,0.2,0.2,0.3,0.3,0.2";
  }
  @Override
  protected String DOC_run_string() {
    return "java ru.autosome.perfectosape.cli.DiPWMFindPvalue";
  }

  protected DiBackgroundModel dibackground;
  protected DiPWM dipwm;

  @Override
  protected CanFindPvalue calculator() throws FileNotFoundException {
    if (cache_calculator == null) {
      CanFindPvalue.DiPWMBuilder builder;
      if (thresholds_folder == null) {
        builder = new DiPWMFindPvalueAPE.Builder(discretization, dibackground, max_hash_size);
      } else {
        builder = new DiPWMFindPvalueBsearch.Builder(thresholds_folder);
      }
      cache_calculator = builder.applyMotif(dipwm).build();
    }
    return cache_calculator;
  }

  @Override
  protected void initialize_default_background() {
    dibackground = new DiWordwiseBackground();
  }

  @Override
  protected void extract_background(String str) {
    dibackground = DiBackground.fromString(str);
  }

  @Override
  protected DiPWMImporter motifImporter() {
    return new DiPWMImporter(dibackground, data_model, effective_count);
  }

  @Override
  protected void setScoringModel(DiPWM motif) {
    dipwm = motif;
  }

  protected DiPWMFindPvalue() {
    initialize_defaults();
  }

  protected static DiPWMFindPvalue from_arglist(ArrayList<String> argv) {
    DiPWMFindPvalue result = new DiPWMFindPvalue();
    ru.autosome.perfectosape.cli.Helper.print_help_if_requested(argv, result.documentString());
    result.setup_from_arglist(argv);
    return result;
  }

  protected static DiPWMFindPvalue from_arglist(String[] args) {
    ArrayList<String> argv = new ArrayList<String>();
    Collections.addAll(argv, args);
    return from_arglist(argv);
  }

  public static void main(String[] args) {
    try {
      DiPWMFindPvalue cli = DiPWMFindPvalue.from_arglist(args);
      System.out.println(cli.report_table().report());
    } catch (Exception err) {
      System.err.println("\n" + err.getMessage() + "\n--------------------------------------\n");
      err.printStackTrace();
      System.err.println("\n--------------------------------------\nUse --help option for help\n\n" + new DiPWMFindPvalue().documentString());
      System.exit(1);
    }
  }
}