package ru.autosome.perfectosape.cli;

import ru.autosome.perfectosape.ArrayExtensions;
import ru.autosome.perfectosape.BoundaryType;
import ru.autosome.perfectosape.backgroundModels.*;
import ru.autosome.perfectosape.calculations.HashOverflowException;
import ru.autosome.perfectosape.calculations.findThreshold.CanFindThreshold;
import ru.autosome.perfectosape.calculations.findThreshold.DiPWMFindThresholdAPE;
import ru.autosome.perfectosape.calculations.findThreshold.FindThresholdAPE;
import ru.autosome.perfectosape.formatters.OutputInformation;
import ru.autosome.perfectosape.formatters.ResultInfo;
import ru.autosome.perfectosape.importers.DiPWMImporter;
import ru.autosome.perfectosape.importers.PMParser;
import ru.autosome.perfectosape.importers.PWMImporter;
import ru.autosome.perfectosape.motifModels.DataModel;
import ru.autosome.perfectosape.motifModels.DiPWM;
import ru.autosome.perfectosape.motifModels.PWM;

import java.util.ArrayList;
import java.util.Collections;

public class DiPWMFindThreshold {
  private static final String DOC =
   "Command-line format:\n" +
    "java ru.autosome.perfectosape.cli.DiPWMFindThreshold <pat-file> [<list of P-values>...] [options]\n" +
    "\n" +
    "Options:\n" +
    "  [-d <discretization level>]\n" +
    "  [--pcm] - treat the input file as Position Count Matrix. PCM-to-PWM transformation to be done internally.\n" +
    "  [--ppm] or [--pfm] - treat the input file as Position Frequency Matrix. PPM-to-PWM transformation to be done internally.\n" +
    "  [--effective-count <count>] - effective samples set size for PPM-to-PWM conversion (default: 100). \n" +
    "  [--boundary lower|upper] Lower boundary (default) means that the obtained P-value is less than or equal to the requested P-value\n" +
    "  [-b <background probabilities] ACGT - 16 numbers, comma-delimited(spaces not allowed), sum should be equal to 1, like 0.2,0.3,0.3,0.2,0.2,0.3,0.3,0.2,0.2,0.3,0.3,0.2,0.2,0.3,0.3,0.2\n" +
    "\n" +
    "Examples:\n" +
    "  java ru.autosome.perfectosape.cli.DiPWMFindThreshold motifs/diKLF4_f2.pat\n" +
    "  java ru.autosome.perfectosape.cli.DiPWMFindThreshold  motifs/diKLF4_f2.pat 0.001 0.0001 0.0005 -d 1000 -b 0.4,0.3,0.2,0.1\n";

  DiBackgroundModel dibackground;
  Double discretization;

  BoundaryType pvalue_boundary;
  Integer max_hash_size; // not int because it can be null

  DiPWM dipwm;
  double[] pvalues;

  private String pm_filename;
  private DataModel data_model;
  private double effective_count;

  void initialize_defaults() {
    dibackground = new DiWordwiseBackground();
    discretization = 10000.0;
    pvalue_boundary = BoundaryType.LOWER;
    max_hash_size = 10000000;
    data_model = DataModel.PWM;
    effective_count = 100;

    pvalues = new double[1];
    pvalues[0] = 0.0005;
  }

  public DiPWMFindThreshold() {
    initialize_defaults();
  }

  private static DiPWMFindThreshold from_arglist(ArrayList<String> argv) {
    DiPWMFindThreshold result = new DiPWMFindThreshold();
    Helper.print_help_if_requested(argv, DOC);
    result.setup_from_arglist(argv);
    return result;
  }

  private static DiPWMFindThreshold from_arglist(String[] args) {
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
    dipwm = new DiPWMImporter(dibackground,
                              data_model,
                              effective_count).loadPWMFromParser(PMParser.from_file_or_stdin(pm_filename));
  }

  private void extract_option(ArrayList<String> argv) {
    String opt = argv.remove(0);
    if (opt.equals("-b")) {
      dibackground = DiBackground.fromString(argv.remove(0));
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

  CanFindThreshold calculator() {
    return new DiPWMFindThresholdAPE(dipwm, dibackground, discretization, max_hash_size);
  }

  OutputInformation report_table_layout() {
    OutputInformation infos = new OutputInformation();

    infos.add_parameter("V", "discretization value", discretization);
    infos.add_parameter("PB", "P-value boundary", pvalue_boundary);

    infos.background_parameter("B", "background", dibackground);

    infos.add_table_parameter("P", "requested P-value", "expected_pvalue");
    infos.add_table_parameter("AP", "actual P-value", "real_pvalue");

    if (dibackground.is_wordwise()) {
      infos.add_table_parameter("W", "number of recognized words", "numberOfRecognizedWords", new OutputInformation.Callback<CanFindThreshold.ThresholdInfo>() {
        @Override
        public Object run(CanFindThreshold.ThresholdInfo cell) {
          double numberOfRecognizedWords = cell.numberOfRecognizedWords(dibackground, dipwm.length());
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

  OutputInformation report_table() throws HashOverflowException {
    CanFindThreshold.ThresholdInfo[] results = calculator().thresholdsByPvalues(pvalues, pvalue_boundary);
    return report_table(results);
  }

  public static void main(String args[]) {
    try {
      DiPWMFindThreshold cli = DiPWMFindThreshold.from_arglist(args);
      System.out.println(cli.report_table().report());
    } catch (Exception err) {
      System.err.println("\n" + err.getMessage() + "\n--------------------------------------\n");
      err.printStackTrace();
      System.err.println("\n--------------------------------------\nUse --help option for help\n\n" + DOC);
      System.exit(1);
    }
  }

}
