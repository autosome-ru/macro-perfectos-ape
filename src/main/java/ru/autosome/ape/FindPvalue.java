package ru.autosome.ape;

import ru.autosome.ape.calculation.findPvalue.CanFindPvalue;
import ru.autosome.ape.calculation.findPvalue.FindPvalueAPE;
import ru.autosome.ape.calculation.findPvalue.FindPvalueBsearch;
import ru.autosome.commons.backgroundModel.mono.Background;
import ru.autosome.commons.backgroundModel.mono.BackgroundModel;
import ru.autosome.commons.backgroundModel.mono.WordwiseBackground;
import ru.autosome.commons.importer.PWMImporter;
import ru.autosome.commons.model.Named;
import ru.autosome.commons.motifModel.mono.PWM;

import java.io.File;
import java.io.FileNotFoundException;

public class FindPvalue extends ru.autosome.ape.cli.generalized.FindPvalue<PWM, BackgroundModel> {
  @Override
  protected String DOC_background_option() {
    return "ACGT - 4 numbers, comma-delimited(spaces not allowed), sum should be equal to 1, like 0.25,0.24,0.26,0.25";
  }
  @Override
  protected String DOC_run_string() {
    return "java ru.autosome.ape.FindPvalue";
  }

  @Override
  protected CanFindPvalue calculator() {
    if (cache_calculator == null) {
      if (thresholds_folder == null) {
        cache_calculator = new FindPvalueAPE<>(motif.getObject(), background, discretizer);
      } else {
        File thresholds_file = new File(thresholds_folder, motif.getName() + ".thr");
        try {
          cache_calculator = new FindPvalueBsearch(thresholds_file);
        } catch (FileNotFoundException e) {
          System.err.println("Thresholds file `" + thresholds_file + "` not found. Fallback to APE-calculation of P-value");
          cache_calculator = new FindPvalueAPE<>(motif.getObject(), background, discretizer);
        }
      }
    }
    return cache_calculator;
  }

  @Override
  protected void initialize_default_background() {
    background = new WordwiseBackground();
  }

  @Override
  protected void extract_background(String str) {
    background = Background.fromString(str);
  }

  @Override
  protected Named<PWM> loadMotif(String filename) {
    PWMImporter importer = new PWMImporter(background, data_model, effective_count, transpose, pseudocount);
    return importer.loadMotifWithName(filename);
  }

  protected static FindPvalue from_arglist(String[] args) {
    FindPvalue result = new FindPvalue();
    result.setup_from_arglist(args);
    return result;
  }

  public static void main(String[] args) {
    try {
      FindPvalue cli = FindPvalue.from_arglist(args);
      System.out.println(cli.report());
    } catch (Exception err) {
      System.err.println("\n" + err.getMessage() + "\n--------------------------------------\n");
      err.printStackTrace();
      System.err.println("\n--------------------------------------\nUse --help option for help\n\n" + new FindPvalue().documentString());
      System.exit(1);
    }
  }

}
