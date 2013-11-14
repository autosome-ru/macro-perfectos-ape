package ru.autosome.perfectosape.cli;

import ru.autosome.perfectosape.*;
import ru.autosome.perfectosape.calculations.CountingPWM;

import java.util.ArrayList;
import java.util.Collections;

public class FindThreshold {
  private static final String DOC =
   "Command-line format:\n" +
    "java ru.autosome.perfectosape.cli.FindThreshold <pat-file> [<list of P-values>...] [options]\n" +
    "\n" +
    "Options:\n" +
    "  [-d <discretization level>]\n" +
    "  [--pcm] - treat the input file as Position Count Matrix. PCM-to-PWM transformation to be done internally.\n" +
    "  [--ppm] or [--pfm] - treat the input file as Position Frequency Matrix. PPM-to-PWM transformation to be done internally.\n" +
    "  [--effective-count] - effective samples set size for PPM-to-PWM conversion (default: 100). \n" +
    "  [--boundary lower|upper] Lower boundary (default) means that the obtained P-value is less than or equal to the requested P-value\n" +
    "  [-b <background probabilities] ACGT - 4 numbers, comma-delimited(spaces not allowed), sum should be equal to 1, like 0.25,0.24,0.26,0.25\n" +
    "\n" +
    "Example:\n" +
    "  java ru.autosome.perfectosape.cli.FindThreshold motifs/KLF4_f2.pat\n" +
    "  java ru.autosome.perfectosape.cli.FindThreshold  motifs/KLF4_f2.pat 0.001 0.0001 0.0005 -d 1000 -b 0.4,0.3,0.2,0.1\n";

  BackgroundModel background;
  Double discretization;

  BoundaryType pvalue_boundary;
  Integer max_hash_size; // not int because it can be null

  PWM pwm;
  double[] pvalues;

  private String pm_filename;
  private DataModel data_model;
  private double effective_count;

  void initialize_defaults() {
    background = new WordwiseBackground();
    discretization = 10000.0;
    pvalue_boundary = BoundaryType.LOWER;
    max_hash_size = 10000000;
    data_model = DataModel.PWM;
    effective_count = 100;

    pvalues = new double[1];
    pvalues[0] = 0.0005;
  }

  public FindThreshold() {
    initialize_defaults();
  }

  private static FindThreshold from_arglist(ArrayList<String> argv) {
    FindThreshold result = new FindThreshold();
    Helper.print_help_if_requested(argv, DOC);
    result.setup_from_arglist(argv);
    return result;
  }

  private static FindThreshold from_arglist(String[] args) {
    ArrayList<String> argv = new ArrayList<String>();
    Collections.addAll(argv, args);
    return from_arglist(argv);
  }

  void setup_from_arglist(ArrayList<String> argv) {
    extract_pm_filename(argv);
    extract_pvalue_list(argv);
    while (argv.size() > 0) {
      extract_option(argv);
    }
    pwm = Helper.load_pwm(PMParser.from_file_or_stdin(pm_filename), data_model, background, effective_count);
  }

  private void extract_option(ArrayList<String> argv) {
    String opt = argv.remove(0);
    if (opt.equals("-b")) {
      background = Background.fromString(argv.remove(0));
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

  private void extract_pm_filename(ArrayList<String> argv) {
    if (argv.isEmpty()) {
      throw new IllegalArgumentException("No input. You should specify input file");
    }
    pm_filename = argv.remove(0);
  }

  private void extract_pvalue_list(ArrayList<String> argv) {
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

  ru.autosome.perfectosape.calculations.FindThresholdAPE calculator() {
    return new ru.autosome.perfectosape.calculations.FindThresholdAPE(pwm, background, discretization, pvalue_boundary, max_hash_size);
  }

  OutputInformation report_table_layout() {
    OutputInformation infos = new OutputInformation();

    infos.add_parameter("V", "discretization value", discretization);
    infos.add_parameter("PB", "P-value boundary", pvalue_boundary);

    infos.background_parameter("B", "background", background);

    infos.add_table_parameter("P", "requested P-value", "expected_pvalue");
    infos.add_table_parameter("AP", "actual P-value", "real_pvalue");

    if (background.is_wordwise()) {
      infos.add_table_parameter("W", "number of recognized words", "recognized_words");
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

  OutputInformation report_table() {
    CountingPWM.ThresholdInfo[] results = calculator().find_thresholds_by_pvalues(pvalues);
    return report_table(results);
  }

  public static void main(String args[]) {
    try {
      FindThreshold cli = FindThreshold.from_arglist(args);
      System.out.println(cli.report_table().report());
    } catch (Exception err) {
      System.err.println("\n" + err.getMessage() + "\n--------------------------------------\n");
      err.printStackTrace();
      System.err.println("\n--------------------------------------\nUse --help option for help\n\n" + DOC);
      System.exit(1);
    }
  }

}
