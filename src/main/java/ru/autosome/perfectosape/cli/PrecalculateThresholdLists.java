package ru.autosome.perfectosape.cli;

import ru.autosome.perfectosape.*;
import ru.autosome.perfectosape.backgroundModels.Background;
import ru.autosome.perfectosape.backgroundModels.BackgroundModel;
import ru.autosome.perfectosape.backgroundModels.WordwiseBackground;
import ru.autosome.perfectosape.calculations.HashOverflowException;
import ru.autosome.perfectosape.calculations.PrecalculateThresholdList;
import ru.autosome.perfectosape.importers.PWMImporter;
import ru.autosome.perfectosape.motifModels.DataModel;
import ru.autosome.perfectosape.motifModels.PWM;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

public class PrecalculateThresholdLists {
  private double discretization;
  private BackgroundModel background;
  private BoundaryType pvalue_boundary;
  private Integer max_hash_size;
  private DataModel data_model;
  private double effective_count; // used for converting PPM --> PWM

  private java.io.File collection_folder;
  private java.io.File results_dir;
  private double[] pvalues;

  private void initialize_defaults() {
    discretization = 1000;
    background = new WordwiseBackground();
    pvalue_boundary = BoundaryType.LOWER;
    max_hash_size = 10000000;
    pvalues = PrecalculateThresholdList.PVALUE_LIST;

    data_model = DataModel.PWM;
    effective_count = 100;
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
      pvalues = PrecalculateThresholdList.Progression.fromString(argv.remove(0)).values();
    } else if (opt.equals("--max-hash-size")) {
      max_hash_size = Integer.valueOf(argv.remove(0));
    } else if (opt.equals("-d")) {
      discretization = Double.valueOf(argv.remove(0));
    } else if (opt.equals("--boundary")) {
      pvalue_boundary = BoundaryType.valueOf(argv.remove(0).toUpperCase());
    } else if (opt.equals("--pcm")) {
      data_model = DataModel.PCM;
    } else if (opt.equals("--ppm") || opt.equals("--pfm")) {
      data_model = DataModel.PPM;
    } else if (opt.equals("--effective-count")) {
      effective_count = Double.valueOf(argv.remove(0));
    } else {
      throw new IllegalArgumentException("Unknown option '" + opt + "'");
    }
  }

  ru.autosome.perfectosape.calculations.PrecalculateThresholdList calculator() {
    return new ru.autosome.perfectosape.calculations.PrecalculateThresholdList(pvalues,
                                                                           discretization,
                                                                           background,
                                                                           pvalue_boundary,
                                                                           max_hash_size);
  }

  void calculate_thresholds_for_collection() throws HashOverflowException, IOException {
    PWMImporter importer = new PWMImporter(background, data_model, effective_count);
    for (File file : collection_folder.listFiles()) {
      System.err.println(file);
      PWM pwm = importer.loadPWMFromFile(file);
      File result_filename = new File(results_dir, pwm.name + ".thr");
      PvalueBsearchList bsearchList = calculator().bsearch_list_for_pwm(pwm);
      bsearchList.save_to_file(result_filename);
    }
  }

  private static final String DOC =
   "Command-line format:\n" +
    "java ru.autosome.perfectosape.cli.PrecalculateThresholdLists <collection folder> <output folder> [options]\n" +
    "\n" +
    "Options:\n" +
    "  [-d <discretization level>]\n" +
    "  [--pcm] - treat the input files as Position Count Matrix. PCM-to-PWM transformation to be done internally.\n" +
    "  [--ppm] or [--pfm] - treat the input file as Position Frequency Matrix. PPM-to-PWM transformation to be done internally.\n" +
    "  [--effective-count <count>] - effective samples set size for PPM-to-PWM conversion (default: 100). \n" +
    "  [--boundary lower|upper] Lower boundary (default) means that the obtained P-value is less than or equal to the requested P-value\n" +
    "  [-b <background probabilities] ACGT - 4 numbers, comma-delimited(spaces not allowed), sum should be equal to 1, like 0.25,0.24,0.26,0.25\n" +
    "  [--pvalues <min pvalue>,<max pvalue>,<step>,<mul|add>] pvalue list parameters: boundaries, step, arithmetic(add)/geometric(mul) progression\n" +
    "\n" +
    "examples:\n" +
    "  java ru.autosome.perfectosape.cli.PrecalculateThresholdLists ./hocomoco/ ./hocomoco_thresholds/\n" +
    "  java ru.autosome.perfectosape.cli.PrecalculateThresholdLists ./hocomoco/ ./hocomoco_thresholds/ -d 100 --pvalues 1e-6,0.1,1.5,mul\n";

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
