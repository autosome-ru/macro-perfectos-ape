package ru.autosome.ape.cli.generalized;

import ru.autosome.ape.calculation.PrecalculateThresholdList;
import ru.autosome.ape.model.PvalueBsearchList;
import ru.autosome.ape.model.progression.Progression;
import ru.autosome.commons.backgroundModel.GeneralizedBackgroundModel;
import ru.autosome.commons.cli.Helper;
import ru.autosome.commons.model.BoundaryType;
import ru.autosome.commons.model.Discretizer;
import ru.autosome.commons.model.Named;
import ru.autosome.commons.model.PseudocountCalculator;
import ru.autosome.commons.motifModel.Discretable;
import ru.autosome.commons.motifModel.ScoreBoundaries;
import ru.autosome.commons.motifModel.ScoreDistribution;
import ru.autosome.commons.motifModel.types.DataModel;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class PrecalculateThresholds<ModelType extends Discretable<ModelType> & ScoreDistribution<BackgroundType> & ScoreBoundaries, BackgroundType extends GeneralizedBackgroundModel> {
  protected Discretizer discretizer;
  protected BackgroundType background;
  protected BoundaryType pvalue_boundary;
  protected DataModel data_model;
  protected double effective_count; // used for converting PPM --> PWM
  protected PseudocountCalculator pseudocount;
  protected boolean silenceLog;

  protected java.io.File results_dir;
  protected double[] pvalues;
  protected boolean transpose;

  protected List<Named<ModelType>> motifList;

  protected abstract void initialize_default_background();
  protected abstract void extract_background(String s);
  protected abstract PrecalculateThresholdList<ModelType, BackgroundType> calculator();
  protected abstract String DOC_background_option();
  protected abstract String DOC_run_string();

  abstract protected Named<ModelType> loadMotif(File file);

  protected void initialize_defaults() {
    initialize_default_background();
    discretizer = new Discretizer(1000.0);
    pvalue_boundary = BoundaryType.LOWER;
    pvalues = PrecalculateThresholdList.PVALUE_LIST;
    data_model = DataModel.PWM;
    effective_count = 100;
    pseudocount = PseudocountCalculator.logPseudocount;
    silenceLog = false;
    transpose = false;
  }

  protected void setup_from_arglist(String[] args) {
    ArrayList<String> argv = new ArrayList<String>();
    Collections.addAll(argv, args);
    setup_from_arglist(argv);
  }

  protected void setup_from_arglist(List<String> argv) {
    Helper.print_help_if_requested(argv, documentString());
    File[] collection_folder = extract_collection_files(argv);
    extract_output_folder_name(argv);

    while (argv.size() > 0) {
      extract_option(argv);
    }

    motifList = loadMotifs(collection_folder);
    create_results_folder();
  }

  protected File[] extract_collection_files(List<String> argv) {
    try {
      File collection_folder = new File(argv.remove(0));
      File[] files = collection_folder.listFiles();
      if (files == null) {
        System.err.println("Warning! No files in collection folder `" + collection_folder + "`!");
        return new File[0];
      }
      return files;
    } catch (IndexOutOfBoundsException e) {
      throw new IllegalArgumentException("Specify PWM-collection folder", e);
    }
  }

  protected void extract_output_folder_name(List<String> argv) {
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

  protected void extract_option(List<String> argv) {
    String opt = argv.remove(0);
    if (opt.equals("-b") || opt.equals("--background")) {
      extract_background(argv.remove(0));
    } else if (opt.equals("--pvalues")) {
      pvalues = Progression.fromString(argv.remove(0)).values();
    } else if (opt.equals("-d") || opt.equals("--discretization")) {
      discretizer = Discretizer.fromString(argv.remove(0));
    } else if (opt.equals("--boundary")) {
      pvalue_boundary = BoundaryType.valueOf(argv.remove(0).toUpperCase());
    } else if (opt.equals("--pcm")) {
      data_model = DataModel.PCM;
    } else if (opt.equals("--ppm") || opt.equals("--pfm")) {
      data_model = DataModel.PPM;
    } else if (opt.equals("--effective-count")) {
      effective_count = Double.valueOf(argv.remove(0));
    } else if (opt.equals("--pseudocount")) {
      pseudocount = PseudocountCalculator.fromString(argv.remove(0));
    } else if (opt.equals("--silent")) {
      silenceLog = true;
    } else if (opt.equals("--transpose")) {
      transpose = true;
    } else {
      if (failed_to_recognize_additional_options(opt, argv)) {
        throw new IllegalArgumentException("Unknown option '" + opt + "'");
      }
    }
  }

  protected boolean failed_to_recognize_additional_options(String opt, List<String> argv) {
    return true;
  }

  List<Named<ModelType>> loadMotifs(File[] files) {
    List<Named<ModelType>> results = new ArrayList<Named<ModelType>>();

    for (File file : files) {
      results.add(loadMotif(file));
    }
    return results;
  }

  protected void calculate_thresholds_for_collection() throws IOException {
    for (Named<ModelType> motif: motifList) {
      if (!silenceLog) {
        System.err.println(motif.getName());
      }
      File result_filename = new File(results_dir, motif.getName() + ".thr");
      PvalueBsearchList bsearchList = calculator().bsearch_list_for_pwm(motif.getObject());
      bsearchList.save_to_file(result_filename);
    }
  }

  public String documentString() {
    return "Command-line format:\n" +
      DOC_run_string() + " <collection folder> <output folder> [options]\n" +
      "\n" +
      "Options:\n" +
      "  [--discretization <discretization level>] or [-d]\n" +
      "  [--pcm] - treat the input files as Position Count Matrix. PCM-to-PWM transformation to be done internally.\n" +
      "  [--ppm] or [--pfm] - treat the input file as Position Frequency Matrix. PPM-to-PWM transformation to be done internally.\n" +
      "  [--effective-count <count>] - effective samples set size for PPM-to-PWM conversion (default: 100). \n" +
      "  [--boundary lower|upper] Lower boundary (default) means that the obtained P-value is less than or equal to the requested P-value\n" +
      "  [--background <background probabilities>] or [-b] " + DOC_background_option() + "\n" +
      "  [--pvalues <min pvalue>,<max pvalue>,<step>,<mul|add>] pvalue list parameters: boundaries, step, arithmetic(add)/geometric(mul) progression\n" +
      "  [--silent] - suppress logging\n" +
      "  [--transpose] - load motif from transposed matrix (nucleotides in lines).\n" +
     DOC_additional_options() +
      "\n" +
      "Examples:\n" +
      "  " + DOC_run_string() + " ./hocomoco/ ./hocomoco_thresholds/\n" +
      "  " + DOC_run_string() + " ./hocomoco/ ./hocomoco_thresholds/ -d 100 --pvalues 1e-6,0.1,1.5,mul\n";
  }

  protected String DOC_additional_options() {
    return "";
  }

}
