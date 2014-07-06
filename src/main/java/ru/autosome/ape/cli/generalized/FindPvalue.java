package ru.autosome.ape.cli.generalized;

import ru.autosome.commons.support.ArrayExtensions;
import ru.autosome.commons.backgroundModel.GeneralizedBackgroundModel;
import ru.autosome.ape.model.exception.HashOverflowException;
import ru.autosome.ape.calculation.findPvalue.CanFindPvalue;
import ru.autosome.commons.cli.OutputInformation;
import ru.autosome.commons.cli.ResultInfo;
import ru.autosome.commons.importer.MotifImporter;
import ru.autosome.commons.importer.PMParser;
import ru.autosome.commons.motifModel.types.DataModel;
import ru.autosome.commons.motifModel.Named;
import ru.autosome.commons.motifModel.ScoringModel;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;

public abstract class FindPvalue<ModelType extends ScoringModel & Named, BackgroundType extends GeneralizedBackgroundModel> {

  protected abstract String DOC_background_option();
  protected abstract String DOC_run_string();
  public String documentString() {
    return "Command-line format:\n" +
     DOC_run_string() + " <pat-file> <threshold list>... [options]\n" +
     "\n" +
     "Options:\n" +
     "  [-d <discretization level>]\n" +
     "  [--pcm] - treat the input file as Position Count Matrix. PCM-to-PWM transformation to be done internally.\n" +
     "  [--ppm] or [--pfm] - treat the input file as Position Frequency Matrix. PPM-to-PWM transformation to be done internally.\n" +
     "  [--effective-count <count>] - effective samples set size for PPM-to-PWM conversion (default: 100). \n" +
     "  [-b <background probabilities] " + DOC_background_option() + "\n" +
     "  [--precalc <folder>] - specify folder with thresholds for PWM collection (for fast-and-rough calculation).\n" +
     "\n" +
     "Examples:\n" +
     "  " + DOC_run_string() + " motifs/KLF4_f2.pat 7.32\n" +
     "  " + DOC_run_string() + " motifs/KLF4_f2.pat 7.32 4.31 5.42 -d 1000 -b 0.2,0.3,0.3,0.2\n";
  }

  protected String pm_filename; // file with PM (not File instance because it can be .stdin)
  protected Double discretization;
  protected double[] thresholds;
  protected Integer max_hash_size;
  protected DataModel data_model;
  protected double effective_count;

  protected ModelType motif;
  protected BackgroundType background;

  protected File thresholds_folder;
  protected CanFindPvalue cache_calculator;

  abstract protected CanFindPvalue calculator();
  protected abstract void initialize_default_background();
  protected abstract void extract_background(String str);
  protected abstract MotifImporter<ModelType> motifImporter();

  protected void initialize_defaults() {
    initialize_default_background();
    discretization = 10000.0;
    thresholds = new double[0];
    max_hash_size = 10000000;
    data_model = DataModel.PWM;
    thresholds_folder = null;
    effective_count = 100;
  }

  protected void extract_pm_filename(ArrayList<String> argv) {
    if (argv.isEmpty()) {
      throw new IllegalArgumentException("No input. You should specify input file");
    }
    pm_filename = argv.remove(0);
  }

  protected void extract_threshold_lists(ArrayList<String> argv) {
    ArrayList<Double> thresholds_list = new ArrayList<Double>();
    try {
      while (!argv.isEmpty()) {
        thresholds_list.add(Double.valueOf(argv.get(0)));
        argv.remove(0);
      }
    } catch (NumberFormatException e) {
    }
    if (thresholds_list.isEmpty()) {
      throw new IllegalArgumentException("You should specify at least one threshold");
    }
    thresholds = ArrayExtensions.toPrimitiveArray(thresholds_list);
  }

  protected void extract_option(ArrayList<String> argv) {
    String opt = argv.remove(0);
    if (opt.equals("-b")) {
      extract_background(argv.remove(0));
    } else if (opt.equals("--max-hash-size")) {
      max_hash_size = Integer.valueOf(argv.remove(0));
    } else if (opt.equals("-d")) {
      discretization = Double.valueOf(argv.remove(0));
    } else if (opt.equals("--pcm")) {
      data_model = DataModel.PCM;
    } else if (opt.equals("--ppm") || opt.equals("--pfm")) {
      data_model = DataModel.PPM;
    } else if (opt.equals("--effective-count")) {
      effective_count = Double.valueOf(argv.remove(0));
    } else if (opt.equals("--precalc")) {
      thresholds_folder = new File(argv.remove(0));
    } else {
      throw new IllegalArgumentException("Unknown option '" + opt + "'");
    }
  }

  protected void setup_from_arglist(ArrayList<String> argv) {
    extract_pm_filename(argv);
    extract_threshold_lists(argv);
    while (argv.size() > 0) {
      extract_option(argv);
    }

    motif = motifImporter().loadPWMFromParser(PMParser.from_file_or_stdin(pm_filename));
  }

  OutputInformation report_table_layout() {
    return calculator().report_table_layout();
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
    CanFindPvalue.PvalueInfo[] results = calculator().pvaluesByThresholds(thresholds);
    return report_table(results);
  }



}
