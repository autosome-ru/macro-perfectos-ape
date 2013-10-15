package ru.autosome.macroape.CLI;

import ru.autosome.macroape.*;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.StringTokenizer;

public class PrecalculateThresholdLists {


  abstract static public class Progression {
    public abstract double[] values();

    public static Progression fromString(String s) {
      StringTokenizer parser = new StringTokenizer(s);
      double min = Double.valueOf(parser.nextToken(","));
      double max = Double.valueOf(parser.nextToken(","));
      double step = Double.valueOf(parser.nextToken(","));
      String progression_method = parser.nextToken();

      if (progression_method.equals("mul")) {
        return new GeometricProgression(min, max, step);
      } else if (progression_method.equals("add")) {
        return new ArithmeticProgression(min, max, step);
      } else {
        throw new IllegalArgumentException("Progression method for pvalue-list is either add or mul, but you specified " + progression_method);
      }
    }
  }

  public static class GeometricProgression extends Progression {
    double from;
    double to;
    double step;

    public double[] values() {
      ArrayList<Double> results = new ArrayList<Double>();
      for (double x = from; x <= to; x *= step) {
        results.add(x);
      }
      return ArrayExtensions.toPrimitiveArray(results);
    }

    public GeometricProgression(double min, double to, double step) {
      this.from = min;
      this.to = to;
      this.step = step;
    }
  }

  public static class ArithmeticProgression extends Progression {
    double from;
    double to;
    double step;

    public double[] values() {
      ArrayList<Double> results = new ArrayList<Double>();
      for (double x = from; x <= to; x += step) {
        results.add(x);
      }
      return ArrayExtensions.toPrimitiveArray(results);
    }

    ArithmeticProgression(double from, double to, double step) {
      this.from = from;
      this.to = to;
      this.step = step;
    }
  }

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

  private PrecalculateThresholdLists() {
    initialize_defaults();
  }

  private static PrecalculateThresholdLists from_arglist(ArrayList<String> argv) {
    PrecalculateThresholdLists result = new PrecalculateThresholdLists();
    Helper.print_help_if_requested(argv, DOC);
    result.setup_from_arglist(argv);
    return result;
  }

  private static PrecalculateThresholdLists from_arglist(String[] args) {
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

  private void create_results_folder() {
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


  ru.autosome.macroape.Calculations.PrecalculateThresholdList calculator() {
    return new ru.autosome.macroape.Calculations.PrecalculateThresholdList(calculator_parameters());
  }

  ru.autosome.macroape.Calculations.PrecalculateThresholdList.Parameters calculator_parameters() {
    return new ru.autosome.macroape.Calculations.PrecalculateThresholdList.Parameters(pvalues,
                                                                                      discretization,
                                                                                      background,
                                                                                      pvalue_boundary,
                                                                                      max_hash_size);
  }

  void calculate_thresholds_for_collection() {
    for (File file : collection_folder.listFiles()) {
      System.err.println(file);
      File result_filename = new File(results_dir, "thresholds_" + file.getName());
      calculator().bsearch_list_for_pwm(load_pwm(file)).save_to_file(result_filename.getPath());
    }
  }

  private static final String DOC =
   "Command-line format:\n" +
    "java ru.autosome.macroape.CLI.PrecalculateThresholdLists <collection folder> <output folder>... [options]\n" +
    "\n" +
    "Options:\n" +
    "  [-d <discretization level>]\n" +
    "  [--pcm] - treat the input files as Position Count Matrix. PCM-to-PWM transformation to be done internally.\n" +
    "  [--boundary lower|upper] Lower boundary (default) means that the obtained P-value is less than or equal to the requested P-value\n" +
    "  [-b <background probabilities] ACGT - 4 numbers, comma-delimited(spaces not allowed), sum should be equal to 1, like 0.25,0.24,0.26,0.25\n" +
    "  [--pvalues <min pvalue>,<max pvalue>,<step>,<mul|add>] pvalue list parameters: boundaries, step, arithmetic(add)/geometric(mul) progression\n" +
    "\n" +
    "Examples:\n" +
    "  java ru.autosome.macroape.CLI.PrecalculateThresholdLists ./hocomoco/ ./hocomoco_thresholds/\n" +
    "  java ru.autosome.macroape.CLI.PrecalculateThresholdLists ./hocomoco/ ./hocomoco_thresholds/ -d 100 --pvalues 1e-6,0.1,1.5,mul\n";

  public static void main(String[] args) {
    try {
      PrecalculateThresholdLists calculation = PrecalculateThresholdLists.from_arglist(args);
      calculation.calculate_thresholds_for_collection();
    } catch (Exception err) {
      System.err.println("\n" + err.getMessage() + "\n--------------------------------------\n");
      err.printStackTrace();
      System.err.println("\n--------------------------------------\nUse --help option for help\n\n" + DOC);
      System.exit(1);
    }
  }
}
