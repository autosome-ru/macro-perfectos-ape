package ru.autosome.perfectosape.cli;

import ru.autosome.perfectosape.BoundaryType;
import ru.autosome.perfectosape.PvalueBsearchList;
import ru.autosome.perfectosape.backgroundModels.GeneralizedBackgroundModel;
import ru.autosome.perfectosape.calculations.HashOverflowException;
import ru.autosome.perfectosape.calculations.PrecalculateThresholdList;
import ru.autosome.perfectosape.importers.MotifImporter;
import ru.autosome.perfectosape.motifModels.DataModel;
import ru.autosome.perfectosape.motifModels.Named;
import ru.autosome.perfectosape.motifModels.ScoringModel;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public abstract class PrecalculateThresholdListsGeneralized<ModelType extends Named & ScoringModel, BackgroundType extends GeneralizedBackgroundModel> {
  protected double discretization;
  protected BackgroundType background;
  protected BoundaryType pvalue_boundary;
  protected Integer max_hash_size;
  protected DataModel data_model;
  protected double effective_count; // used for converting PPM --> PWM
  boolean silence;

  protected java.io.File collection_folder;
  protected java.io.File results_dir;
  protected double[] pvalues;

  abstract protected void initialize_default_background();
  abstract void extract_background(String s);
  abstract PrecalculateThresholdList calculator();
  abstract MotifImporter<ModelType> motifImporter();
  protected abstract String DOC_background_option();
  protected abstract String DOC_run_string();

  protected void initialize_defaults() {
    initialize_default_background();
    discretization = 1000;
    pvalue_boundary = BoundaryType.LOWER;
    max_hash_size = 10000000;
    pvalues = PrecalculateThresholdList.PVALUE_LIST;
    data_model = DataModel.PWM;
    effective_count = 100;
    silence = false;
  }

  void setup_from_arglist(ArrayList<String> argv) {
    extract_collection_folder_name(argv);
    extract_output_folder_name(argv);

    while (argv.size() > 0) {
      extract_option(argv);
    }
    create_results_folder();
  }

  protected void extract_collection_folder_name(ArrayList<String> argv) {
    try {
      collection_folder = new File(argv.remove(0));
    } catch (IndexOutOfBoundsException e) {
      throw new IllegalArgumentException("Specify PWM-collection folder", e);
    }
  }

  protected void extract_output_folder_name(ArrayList<String> argv) {
    try {
      results_dir = new File(argv.remove(0));
    } catch (IndexOutOfBoundsException e) {
      throw new IllegalArgumentException("Specify output folder", e);
    }
  }

  protected void create_results_folder() {
    if (!results_dir.exists()) {
      results_dir.mkdir();
    }
  }

  protected void extract_option(ArrayList<String> argv) {
    String opt = argv.remove(0);
    if (opt.equals("-b")) {
      extract_background(argv.remove(0));
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
    } else if (opt.equals("--silence")) {
      silence = true;
    } else {
      throw new IllegalArgumentException("Unknown option '" + opt + "'");
    }
  }

  void calculate_thresholds_for_collection() throws HashOverflowException, IOException {
    MotifImporter<ModelType> importer = motifImporter();
    for (File file : collection_folder.listFiles()) {
      if (!silence) {
        System.err.println(file);
      }
      ModelType motif = importer.loadPWMFromFile(file);
      File result_filename = new File(results_dir, motif.getName() + ".thr");
      PvalueBsearchList bsearchList = calculator().bsearch_list_for_pwm(motif);
      bsearchList.save_to_file(result_filename);
    }
  }

  String documentString() {
    return "Command-line format:\n" +
      DOC_run_string() + " <collection folder> <output folder> [options]\n" +
      "\n" +
      "Options:\n" +
      "  [-d <discretization level>]\n" +
      "  [--pcm] - treat the input files as Position Count Matrix. PCM-to-PWM transformation to be done internally.\n" +
      "  [--ppm] or [--pfm] - treat the input file as Position Frequency Matrix. PPM-to-PWM transformation to be done internally.\n" +
      "  [--effective-count <count>] - effective samples set size for PPM-to-PWM conversion (default: 100). \n" +
      "  [--boundary lower|upper] Lower boundary (default) means that the obtained P-value is less than or equal to the requested P-value\n" +
      "  [-b <background probabilities] " + DOC_background_option() + "\n" +
      "  [--pvalues <min pvalue>,<max pvalue>,<step>,<mul|add>] pvalue list parameters: boundaries, step, arithmetic(add)/geometric(mul) progression\n" +
      "  [--silence] - suppress logging\n" +
      "\n" +
      "Examples:\n" +
      "  " + DOC_run_string() + " ./hocomoco/ ./hocomoco_thresholds/\n" +
      "  " + DOC_run_string() + " ./hocomoco/ ./hocomoco_thresholds/ -d 100 --pvalues 1e-6,0.1,1.5,mul\n";
  }

}
