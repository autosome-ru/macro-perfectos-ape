package ru.autosome.macroape.CLI;

import ru.autosome.macroape.*;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;

public class PrecalculateThresholdList {
  private double discretization;
  private BackgroundModel background;
  private String pvalue_boundary;
  private int max_hash_size;
  private String data_model;

  private java.io.File collection_folder;
  private java.io.File results_dir;
  private double[] pvalues;

  private void initialize_defaults() {
    discretization = 1000;
    background = new WordwiseBackground();
    pvalue_boundary = "lower";
    max_hash_size = 10000000;
    pvalues = new GeometricProgression(1E-6, 0.3, 1.1).values();

    data_model = "pwm";
  }

  private PrecalculateThresholdList() {
    initialize_defaults();
  }

  private static PrecalculateThresholdList from_arglist(ArrayList<String> argv) {
    PrecalculateThresholdList result = new PrecalculateThresholdList();
    Helper.print_help_if_requested(argv, DOC);
    result.setup_from_arglist(argv);
    return result;
  }

  private static PrecalculateThresholdList from_arglist(String[] args) {
    ArrayList<String> argv = new ArrayList<String>();
    Collections.addAll(argv, args);
    return from_arglist(argv);
  }

  void setup_from_arglist(ArrayList<String> argv) {
    extract_collection_folder_name(argv);
    extract_output_folder_name(argv);

    while (argv.size() > 0) {
      extract_option(argv);
    }
    create_results_folder();
  }

  private FindThresholdAPEParameters find_threshold_parameters(PWM pwm) {
    return new FindThresholdAPEParameters(pwm, pvalues, background, discretization, pvalue_boundary, max_hash_size);
  }

  private FindThresholdAPE find_threshold_calculator(PWM pwm) {
    return new FindThresholdAPE(find_threshold_parameters(pwm));
  }

  private void extract_collection_folder_name(ArrayList<String> argv) {
    try {
      collection_folder = new File(argv.remove(0));
    } catch (IndexOutOfBoundsException e) {
      throw new IllegalArgumentException("Specify PWM-collection folder", e);
    }
  }

  private void extract_output_folder_name(ArrayList<String> argv) {
    try {
      results_dir = new File(argv.remove(0));
    } catch (IndexOutOfBoundsException e) {
      throw new IllegalArgumentException("Specify output folder", e);
    }
  }

  private void create_results_folder(){
    if (!results_dir.exists()) {
      results_dir.mkdir();
    }
  }

  private void extract_option(ArrayList<String> argv) {
    String opt = argv.remove(0);
    if (opt.equals("-b")) {
      background = Background.fromString(argv.remove(0));
    } else if (opt.equals("--pvalues")) {
      pvalues = Progression.fromString(argv.remove(0)).values();
    } else if (opt.equals("--max-hash-size")) {
      max_hash_size = Integer.valueOf(argv.remove(0));
    } else if (opt.equals("-d")) {
      discretization = Double.valueOf(argv.remove(0));
    } else if (opt.equals("--boundary")) {
      pvalue_boundary = argv.remove(0);
      if (!pvalue_boundary.equalsIgnoreCase("lower") &&
              !pvalue_boundary.equalsIgnoreCase("upper")) {
        throw new IllegalArgumentException("boundary should be either lower or upper");
      }
    } else if (opt.equals("--pcm")) {
      data_model = "pcm";
    } else {
      throw new IllegalArgumentException("Unknown option '" + opt + "'");
    }
  }

  PWM load_pwm(File filename) {
    if (data_model.equals("pcm")) {
      return PCM.new_from_file(filename.getPath()).to_pwm(background);
    } else {
      return PWM.new_from_file(filename.getPath());
    }
  }

  void calculate_thresholds_for_file(File filename) {
    ArrayList<ThresholdPvaluePair> pairs = new ArrayList<ThresholdPvaluePair>();
    for (ThresholdInfo info: find_threshold_calculator(load_pwm(filename)).find_thresholds_by_pvalues()) {
      pairs.add(new ThresholdPvaluePair(info));
    }

    File result_filename = new File(results_dir + File.separator + "thresholds_" + filename.getName());
    new PvalueBsearchList(pairs).save_to_file(result_filename.getPath());
  }

  void calculate_thresholds_for_collection() {
    for (File filename : collection_folder.listFiles()) {
      System.err.println(filename);
      calculate_thresholds_for_file(filename);
    }
  }

  private static final String DOC =
          "Command-line format:\n" +
                  "java ru.autosome.macroape.CLI.PrecalculateThresholdList <collection folder> <output folder>... [options]\n" +
                  "\n" +
                  "Options:\n" +
                  "  [-d <discretization level>]\n" +
                  "  [--pcm] - treat the input files as Position Count Matrix. PCM-to-PWM transformation to be done internally.\n" +
                  "  [--boundary lower|upper] Lower boundary (default) means that the obtained P-value is less than or equal to the requested P-value\n" +
                  "  [-b <background probabilities] ACGT - 4 numbers, comma-delimited(spaces not allowed), sum should be equal to 1, like 0.25,0.24,0.26,0.25\n" +
                  "  [--pvalues <min pvalue>,<max pvalue>,<step>,<mul|add>] pvalue list parameters: boundaries, step, arithmetic(add)/geometric(mul) progression\n" +
                  "\n" +
                  "Examples:\n" +
                  "  java ru.autosome.macroape.CLI.PrecalculateThresholdList ./hocomoco/ ./hocomoco_thresholds/\n" +
                  "  java ru.autosome.macroape.CLI.PrecalculateThresholdList ./hocomoco/ ./hocomoco_thresholds/ -d 100 --pvalues 1e-6,0.1,1.5,mul\n";

  public static void main(String[] args) {
    try {
      PrecalculateThresholdList calculation = PrecalculateThresholdList.from_arglist(args);
      calculation.calculate_thresholds_for_collection();
    } catch (Exception err) {
      System.err.println("\n" + err.getMessage() + "\n--------------------------------------\n");
      err.printStackTrace();
      System.err.println("\n--------------------------------------\nUse --help option for help\n\n" + DOC);
      System.exit(1);
    }
  }
}
