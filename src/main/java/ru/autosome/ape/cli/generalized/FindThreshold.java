package ru.autosome.ape.cli.generalized;

import ru.autosome.ape.calculation.findPvalue.CanFindPvalue;
import ru.autosome.ape.calculation.findThreshold.CanFindThreshold;
import ru.autosome.commons.backgroundModel.GeneralizedBackgroundModel;
import ru.autosome.commons.cli.Helper;
import ru.autosome.commons.cli.ReportLayout;
import ru.autosome.commons.cli.Reporter;
import ru.autosome.commons.cli.TextReporter;
import ru.autosome.commons.model.BoundaryType;
import ru.autosome.commons.model.Discretizer;
import ru.autosome.commons.model.Named;
import ru.autosome.commons.model.PseudocountCalculator;
import ru.autosome.commons.motifModel.HasLength;
import ru.autosome.commons.motifModel.types.DataModel;
import ru.autosome.commons.support.IOExtensions;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class FindThreshold<ModelType extends HasLength, BackgroundType extends GeneralizedBackgroundModel> {
  protected abstract String DOC_background_option();
  protected abstract String DOC_run_string();
  protected String documentString() {
    return "Command-line format:\n" +
      DOC_run_string() + " <pat-file> [<list of P-values>...] [options]\n" +
      "\n" +
      "Options:\n" +
      "  [--discretization <discretization level>] or [-d]\n" +
      "  [--pcm] - treat the input file as Position Count Matrix. PCM-to-PWM transformation to be done internally.\n" +
      "  [--ppm] or [--pfm] - treat the input file as Position Frequency Matrix. PPM-to-PWM transformation to be done internally.\n" +
      "  [--effective-count <count>] - effective samples set size for PPM-to-PWM conversion (default: 100). \n" +
      "  [--boundary lower|upper] Lower boundary (default) means that the obtained P-value is less than or equal to the requested P-value\n" +
      "  [--background <background probabilities>] or [-b] " + DOC_background_option() + "\n" +
      "  [--precalc <folder>] - specify folder with thresholds for PWM collection (for fast-and-rough calculation).\n" +
      "  [--transpose] - load motif from transposed matrix (nucleotides in lines).\n" +
     DOC_additional_options() +
      "\n" +
      "Examples:\n" +
      "  " + DOC_run_string() + " motifs/diKLF4_f2.pat\n" +
      "  " + DOC_run_string() + "  motifs/diKLF4_f2.pat 0.001 0.0001 0.0005 -d 1000 -b 0.4,0.3,0.2,0.1\n";
  }

  protected String DOC_additional_options() {
    return "";
  }

  protected Discretizer discretizer;

  protected BoundaryType pvalue_boundary;

  protected List<Double> pvalues;
  protected boolean transpose;
  protected boolean should_extract_values_from_stdin;

  protected String pm_filename;
  protected DataModel data_model;
  protected double effective_count;
  protected PseudocountCalculator pseudocount;
  protected BackgroundType background;
  protected Named<ModelType> motif;
  protected File thresholds_folder;
  protected CanFindThreshold cache_calculator;

  protected abstract void initialize_default_background();
  protected abstract void extract_background(String str);
  protected abstract Named<ModelType> loadMotif(String filename);
  protected abstract CanFindThreshold calculator();

  protected void initialize_defaults() {
    initialize_default_background();
    discretizer = new Discretizer(10000.0);
    pvalue_boundary = BoundaryType.LOWER;
    data_model = DataModel.PWM;
    effective_count = 100;
    pseudocount = PseudocountCalculator.logPseudocount;
    thresholds_folder = null;
    transpose = false;
    should_extract_values_from_stdin = false;

    pvalues = new ArrayList<>();
    pvalues.add(0.0005);
  }

  protected void setup_from_arglist(List<String> argv) {
    Helper.print_help_if_requested(argv, documentString());
    extract_pm_filename(argv);
    extract_pvalue_list(argv);
    while (argv.size() > 0) {
      extract_option(argv);
    }
    motif = loadMotif(pm_filename);
  }

  protected void setup_from_arglist(String[] args) {
    ArrayList<String> argv = new ArrayList<>();
    Collections.addAll(argv, args);
    setup_from_arglist(argv);
  }

  protected void extract_option(List<String> argv) {
    String opt = argv.remove(0);
    if (opt.equals("-b") || opt.equals("--background")) {
      extract_background(argv.remove(0));
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
    } else if (opt.equals("--precalc")) {
      thresholds_folder = new File(argv.remove(0));
    } else if (opt.equals("--transpose")) {
      transpose = true;
    } else if (opt.equals("--pvalues-from-stdin")) {
      should_extract_values_from_stdin = true;
    }  else {
      if (failed_to_recognize_additional_options(opt, argv)) {
        throw new IllegalArgumentException("Unknown option '" + opt + "'");
      }
    }
  }

  protected boolean failed_to_recognize_additional_options(String opt, List<String> argv) {
    return true;
  }


  protected void extract_pm_filename(List<String> argv) {
    if (argv.isEmpty()) {
      throw new IllegalArgumentException("No input. You should specify input file");
    }
    pm_filename = argv.remove(0);
  }

  protected void extract_pvalue_list(List<String> argv) {
    ArrayList<Double> pvalues_tmp = new ArrayList<>();

    try {
      while (!argv.isEmpty()) {
        pvalues_tmp.add(Double.valueOf(argv.get(0)));
        argv.remove(0);
      }
    } catch (NumberFormatException e) {
    }

    if (should_extract_values_from_stdin) {
      try {
        IOExtensions.extract_doubles_from_input_stream(System.in, pvalues_tmp);
      } catch (IOException e) { }
    }


    if (pvalues_tmp.size() != 0) {
      this.pvalues = pvalues_tmp;
    }
  }

  ReportLayout<CanFindThreshold.ThresholdInfo> report_table_layout() {
    ReportLayout<CanFindThreshold.ThresholdInfo> infos = new ReportLayout<>();

    infos.add_parameter("V", "discretization value", discretizer);
    infos.add_parameter("PB", "P-value boundary", pvalue_boundary);

    infos.background_parameter("B", "background", background);

    infos.add_table_parameter("P", "requested P-value", (CanFindThreshold.ThresholdInfo cell)-> cell.expected_pvalue);
    infos.add_table_parameter("AP", "actual P-value", (CanFindThreshold.ThresholdInfo cell) -> cell.real_pvalue);

    if (background.is_wordwise()) {
      infos.add_table_parameter("W", "number of recognized words", (CanFindThreshold.ThresholdInfo cell) -> {
          double numberOfRecognizedWords = cell.numberOfRecognizedWords(background, motif.getObject().length());
          return (long)numberOfRecognizedWords;
        });
    }
    infos.add_table_parameter("T", "threshold", (CanFindThreshold.ThresholdInfo cell) -> cell.threshold);

    return infos;
  }

  protected String report() {
    List<CanFindThreshold.ThresholdInfo> results = calculator().thresholdsByPvalues(pvalues, pvalue_boundary);
    ReportLayout<CanFindThreshold.ThresholdInfo> layout = report_table_layout();
    Reporter<CanFindThreshold.ThresholdInfo> reporter = new TextReporter<>();
    return reporter.report(results, layout);
  }

  protected FindThreshold() {
    initialize_defaults();
  }
}
