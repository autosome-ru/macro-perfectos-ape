package ru.autosome.ape.cli.generalized;

import ru.autosome.ape.calculation.findPvalue.CanFindPvalue;
import ru.autosome.ape.calculation.findPvalue.FindPvalueBsearch;
import ru.autosome.ape.calculation.findPvalue.FoundedPvalueInfo;
import ru.autosome.commons.cli.Helper;
import ru.autosome.commons.cli.ListReporter;
import ru.autosome.commons.cli.ReportListLayout;
import ru.autosome.commons.cli.TextListReporter;
import ru.autosome.commons.model.Discretizer;
import ru.autosome.commons.model.Named;
import ru.autosome.commons.model.PseudocountCalculator;
import ru.autosome.commons.motifModel.types.DataModel;
import ru.autosome.commons.support.IOExtensions;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class FindPvalue<ModelType, BackgroundType> {

  protected abstract String DOC_background_option();
  protected abstract String DOC_run_string();
  public String documentString() {
    return "Command-line format:\n" +
     DOC_run_string() + " <pat-file> <threshold list>... [options]\n" +
     "\n" +
     "Options:\n" +
     "  [--discretization <discretization level>] or [-d]\n" +
     "  [--pcm] - treat the input file as Position Count Matrix. PCM-to-PWM transformation to be done internally.\n" +
     "  [--ppm] or [--pfm] - treat the input file as Position Frequency Matrix. PPM-to-PWM transformation to be done internally.\n" +
     "  [--effective-count <count>] - effective samples set size for PPM-to-PWM conversion (default: 100). \n" +
     "  [--background <background probabilities>] or [-b]" + DOC_background_option() + "\n" +
     "  [--precalc <folder>] - specify folder with thresholds for PWM collection (for fast-and-rough calculation).\n" +
     "  [--transpose] - load motif from transposed matrix (nucleotides in lines).\n" +
     DOC_additional_options() +
     "\n" +
     "Examples:\n" +
     "  " + DOC_run_string() + " motifs/KLF4_f2.pat 7.32\n" +
     "  " + DOC_run_string() + " motifs/KLF4_f2.pat 7.32 4.31 5.42 -d 1000 -b 0.2,0.3,0.3,0.2\n";
  }

  protected String DOC_additional_options() {
    return "";
  }

  protected String pm_filename; // file with PM (not File instance because it can be .stdin)
  protected Discretizer discretizer;
  protected List<Double> thresholds;
  protected DataModel data_model;
  protected double effective_count;
  protected PseudocountCalculator pseudocount;
  protected boolean transpose;
  protected boolean should_extract_values_from_stdin;

  protected Named<ModelType> motif;
  protected BackgroundType background;

  protected File thresholds_folder;

  abstract protected CanFindPvalue calculator() throws FileNotFoundException;

  protected CanFindPvalue bsearchCalculator() throws FileNotFoundException {
    if (thresholds_folder.isFile()) {
      return new FindPvalueBsearch(thresholds_folder);
    } else {
      File thresholds_file = new File(thresholds_folder, motif.getName() + ".thr");
      return new FindPvalueBsearch(thresholds_file);
    }
  }

  protected abstract void initialize_default_background();
  protected abstract void extract_background(String str);
  abstract protected Named<ModelType> loadMotif(String filename);

  protected void initialize_defaults() {
    initialize_default_background();
    discretizer = new Discretizer(10000.0);
    thresholds = new ArrayList<>();
    data_model = DataModel.PWM;
    thresholds_folder = null;
    effective_count = 100;
    pseudocount = PseudocountCalculator.logPseudocount;
    transpose = false;
    should_extract_values_from_stdin = false;
  }

  protected void extract_pm_filename(List<String> argv) {
    if (argv.isEmpty()) {
      throw new IllegalArgumentException("No input. You should specify input file");
    }
    pm_filename = argv.remove(0);
  }

  protected void extract_threshold_lists(List<String> argv) throws IOException {
    ArrayList<Double> thresholds_list = new ArrayList<>();

    try {
      while (!argv.isEmpty()) {
        thresholds_list.add(Double.valueOf(argv.get(0)));
        argv.remove(0);
      }
    } catch (NumberFormatException e) { }

    if (should_extract_values_from_stdin) {
      IOExtensions.extract_doubles_from_input_stream(System.in, thresholds_list);
    }


    if (thresholds_list.isEmpty()) {
      throw new IllegalArgumentException("You should specify at least one threshold");
    }
    this.thresholds = thresholds_list;
  }


  protected void extract_option(List<String> argv) throws FileNotFoundException {
    String opt = argv.remove(0);
    if (opt.equals("-b") || opt.equals("--background")) {
      extract_background(argv.remove(0));
    } else if (opt.equals("-d") || opt.equals("--discretization")) {
      discretizer = Discretizer.fromString(argv.remove(0));
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
      if (!thresholds_folder.exists()) {
        throw new FileNotFoundException("Specified file/folder with thresholds `" + thresholds_folder + "` not exists");
      }
    } else if (opt.equals("--transpose")) {
      transpose = true;
    } else if (opt.equals("--thresholds-from-stdin")) {
      // Pass; It's already processed, before thresholds extraction
    } else {
      if (failed_to_recognize_additional_options(opt, argv)) {
        throw new IllegalArgumentException("Unknown option '" + opt + "'");
      }
    }
  }

  protected boolean failed_to_recognize_additional_options(String opt, List<String> argv) {
    return true;
  }

  protected void setup_from_arglist(List<String> argv) throws IOException {
    Helper.print_help_if_requested(argv, documentString());
    extract_pm_filename(argv);
    if (argv.contains("--thresholds-from-stdin")) {
      should_extract_values_from_stdin = true;
    }
    extract_threshold_lists(argv);
    while (argv.size() > 0) {
      extract_option(argv);
    }
    motif = loadMotif(pm_filename);
  }

  protected void setup_from_arglist(String[] args) throws IOException {
    ArrayList<String> argv = new ArrayList<>();
    Collections.addAll(argv, args);
    setup_from_arglist(argv);
  }

  protected String report() throws FileNotFoundException {
    CanFindPvalue calc = calculator();
    List<FoundedPvalueInfo> results = calc.pvaluesByThresholds(thresholds);
    ReportListLayout<FoundedPvalueInfo> layout = calc.report_table_layout();
    ListReporter<FoundedPvalueInfo> reporter = new TextListReporter<>();
    return reporter.report(results, layout);
  }

  protected FindPvalue() {
    initialize_defaults();
  }
}
