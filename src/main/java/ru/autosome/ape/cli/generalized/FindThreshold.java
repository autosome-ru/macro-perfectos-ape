package ru.autosome.ape.cli.generalized;

import ru.autosome.ape.calculation.findThreshold.CanFindThreshold;
import ru.autosome.ape.model.exception.HashOverflowException;
import ru.autosome.commons.backgroundModel.GeneralizedBackgroundModel;
import ru.autosome.commons.cli.OutputInformation;
import ru.autosome.commons.cli.ResultInfo;
import ru.autosome.commons.model.BoundaryType;
import ru.autosome.commons.model.Discretizer;
import ru.autosome.commons.model.PseudocountCalculator;
import ru.autosome.commons.motifModel.HasLength;
import ru.autosome.commons.motifModel.types.DataModel;
import ru.autosome.commons.support.ArrayExtensions;

import java.io.File;
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
  protected Integer max_hash_size; // not int because it can be null

  protected double[] pvalues;
  protected boolean transpose;

  protected String pm_filename;
  protected DataModel data_model;
  protected double effective_count;
  protected PseudocountCalculator pseudocount;
  protected BackgroundType background;
  protected ModelType motif;
  protected File thresholds_folder;
  protected CanFindThreshold cache_calculator;

  protected abstract void initialize_default_background();
  protected abstract void extract_background(String str);
  protected abstract ModelType loadMotif(String filename);
  protected abstract CanFindThreshold calculator();

  protected void initialize_defaults() {
    initialize_default_background();
    discretizer = new Discretizer(10000.0);
    pvalue_boundary = BoundaryType.LOWER;
    max_hash_size = 10000000;
    data_model = DataModel.PWM;
    effective_count = 100;
    pseudocount = PseudocountCalculator.logPseudocount;
    thresholds_folder = null;
    transpose = false;

    pvalues = new double[1];
    pvalues[0] = 0.0005;
  }

  protected void setup_from_arglist(ArrayList<String> argv) {
    extract_pm_filename(argv);
    extract_pvalue_list(argv);
    while (argv.size() > 0) {
      extract_option(argv);
    }
    motif = loadMotif(pm_filename);
  }

  protected void extract_option(ArrayList<String> argv) {
    String opt = argv.remove(0);
    if (opt.equals("-b") || opt.equals("--background")) {
      extract_background(argv.remove(0));
    } else if (opt.equals("--max-hash-size")) {
      max_hash_size = Integer.valueOf(argv.remove(0));
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
    } else {
      if (failed_to_recognize_additional_options(opt, argv)) {
        throw new IllegalArgumentException("Unknown option '" + opt + "'");
      }
    }
  }

  protected boolean failed_to_recognize_additional_options(String opt, List<String> argv) {
    return true;
  }


  protected void extract_pm_filename(ArrayList<String> argv) {
    if (argv.isEmpty()) {
      throw new IllegalArgumentException("No input. You should specify input file");
    }
    pm_filename = argv.remove(0);
  }

  protected void extract_pvalue_list(ArrayList<String> argv) {
    ArrayList<Double> pvalues_tmp = new ArrayList<Double>();

    try {
      while (!argv.isEmpty()) {
        pvalues_tmp.add(Double.valueOf(argv.get(0)));
        argv.remove(0);
      }
    } catch (NumberFormatException e) {
    }
    if (pvalues_tmp.size() != 0) {
      pvalues = ArrayExtensions.toPrimitiveArray(pvalues_tmp);
    }
  }

  OutputInformation report_table_layout() {
    OutputInformation infos = new OutputInformation();

    infos.add_parameter("V", "discretization value", discretizer);
    infos.add_parameter("PB", "P-value boundary", pvalue_boundary);

    infos.background_parameter("B", "background", background);

    infos.add_table_parameter("P", "requested P-value", "expected_pvalue");
    infos.add_table_parameter("AP", "actual P-value", "real_pvalue");

    if (background.is_wordwise()) {
      infos.add_table_parameter("W", "number of recognized words", "numberOfRecognizedWords", new OutputInformation.Callback<CanFindThreshold.ThresholdInfo>() {
        @Override
        public Object run(CanFindThreshold.ThresholdInfo cell) {
          double numberOfRecognizedWords = cell.numberOfRecognizedWords(background, motif.length());
          return (long)numberOfRecognizedWords;
        }
      });
    }
    infos.add_table_parameter("T", "threshold", "threshold");

    return infos;
  }

  OutputInformation report_table(ArrayList<? extends ResultInfo> data) {
    OutputInformation result = report_table_layout();
    result.data = data;
    return result;
  }

  <R extends ResultInfo> OutputInformation report_table(R[] data) {
    ArrayList<R> data_list = new ArrayList<R>(data.length);
    Collections.addAll(data_list, data);
    return report_table(data_list);
  }

  protected OutputInformation report_table() throws HashOverflowException {
    CanFindThreshold.ThresholdInfo[] results = calculator().thresholdsByPvalues(pvalues, pvalue_boundary);
    return report_table(results);
  }
}
