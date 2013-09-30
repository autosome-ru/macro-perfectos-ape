package ru.autosome.macroape;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;

public class FindPvalue {
  private String pm_filename; // file with PM
  private Double discretization;
  private BackgroundModel background;
  private double[] thresholds;
  private Integer max_hash_size;
  private String data_model;
  private String thresholds_folder;
  private PWM pwm;

  private void initialize_defaults() {
    discretization = 10000.0;
    background = new WordwiseBackground();
    thresholds = new double[0];
    max_hash_size = 10000000;
    data_model = "pwm";
    thresholds_folder = null;
  }

  private void extract_pm_filename(ArrayList<String> argv) {
    if (argv.isEmpty()) {
      throw new IllegalArgumentException("No input. You should specify input file");
    }
    pm_filename = argv.remove(0);
  }

  private void extract_threshold_lists(ArrayList<String> argv) {
    ArrayList<Double> thresholds_list = new ArrayList<Double>();
    try {
      while (!argv.isEmpty()) {
        thresholds_list.add(Double.valueOf(argv.get(0)));
        argv.remove(0);
      }
    } catch (NumberFormatException e) {
    }
    if (thresholds_list.isEmpty()) {
      throw new IllegalArgumentException("You should specify at least one threshold");
    }
    thresholds = ArrayExtensions.toPrimitiveArray(thresholds_list);
  }

  private void extract_option(ArrayList<String> argv) {
    String opt = argv.remove(0);
    if (opt.equals("-b")) {
      background = Background.fromString(argv.remove(0));
    } else if (opt.equals("--max-hash-size")) {
      max_hash_size = Integer.valueOf(argv.remove(0));
    } else if (opt.equals("-d")) {
      discretization = Double.valueOf(argv.remove(0));
    } else if (opt.equals("--pcm")) {
      data_model = "pcm";
    } else if (opt.equals("--precalc")) {
      thresholds_folder = argv.remove(0);
    } else {
      throw new IllegalArgumentException("Unknown option '" + opt + "'");
    }
  }

  private void load_pwm() {
    if (data_model.equals("pcm")) {
      pwm = PCM.new_from_file_or_stdin(pm_filename).to_pwm(background);
    } else {
      pwm = PWM.new_from_file_or_stdin(pm_filename);
    }
  }

  void setup_from_arglist(ArrayList<String> argv) {
    extract_pm_filename(argv);
    extract_threshold_lists(argv);
    while (argv.size() > 0) {
      extract_option(argv);
    }
    load_pwm();
  }

  private FindPvalue() {
    initialize_defaults();
  }

  private static FindPvalue from_arglist(ArrayList<String> argv) {
    FindPvalue result = new FindPvalue();
    Helper.print_help_if_requested(argv, DOC);
    result.setup_from_arglist(argv);
    return result;
  }

  private static FindPvalue from_arglist(String[] args) {
    ArrayList<String> argv = new ArrayList<String>();
    Collections.addAll(argv, args);
    return from_arglist(argv);
  }

  private FindPvalueAPE ape_calculation() {
    FindPvalueAPE calculation = new FindPvalueAPE(pwm);
    calculation.background = background;
    calculation.discretization = discretization;
    calculation.max_hash_size = max_hash_size;
    return calculation;
  }

  private FindPvalueBsearch bsearch_calculation() {
    String thresholds_filename = thresholds_folder + File.separator + "thresholds_" + (new File(pm_filename)).getName();
    FindPvalueBsearch calculation = FindPvalueBsearch.load_from_file(pwm, thresholds_filename);
    calculation.background = background;
    return calculation;
  }

  private CanFindPvalue calculation() {
    CanFindPvalue result;
    if (thresholds_folder != null) {
      result = bsearch_calculation();
    } else {
      result = ape_calculation();
    }
    return result;
  }

  ArrayList<PvalueInfo> pvalues_by_thresholds() {
    return calculation().pvalues_by_thresholds(thresholds);
  }

  OutputInformation report_table_layout() {
    return calculation().report_table_layout();
  }

  private static final String DOC =
          "Command-line format:\n" +
                  "java ru.autosome.macroape.FindPvalue <pat-file> <threshold list>... [options]\n" +
                  "\n" +
                  "Options:\n" +
                  "  [-d <discretization level>]\n" +
                  "  [--pcm] - treat the input file as Position Count Matrix. PCM-to-PWM transformation to be done internally.\n" +
                  "  [-b <background probabilities] ACGT - 4 numbers, comma-delimited(spaces not allowed), sum should be equal to 1, like 0.25,0.24,0.26,0.25\n" +
                  "  [--precalc <folder>] - specify folder with thresholds for PWM collection (for fast-and-rough calculation).\n" +
                  "\n" +
                  "Examples:\n" +
                  "  java ru.autosome.macroape.FindPvalue motifs/KLF4_f2.pat 7.32\n" +
                  "  java ru.autosome.macroape.FindPvalue motifs/KLF4_f2.pat 7.32 4.31 5.42 -d 1000 -b 0.2,0.3,0.3,0.2\n";

  public static void main(String[] args) {
    try {
      FindPvalue calculation = FindPvalue.from_arglist(args);
      ArrayList<PvalueInfo> results = calculation.pvalues_by_thresholds();
      OutputInformation report_table = calculation.report_table_layout();
      report_table.data = results;
      System.out.println(report_table.result());

    } catch (Exception err) {
      System.err.println("\n" + err.getMessage() + "\n--------------------------------------\n");
      err.printStackTrace();
      System.err.println("\n--------------------------------------\nUse --help option for help\n\n" + DOC);
      System.exit(1);
    }

  }

}
