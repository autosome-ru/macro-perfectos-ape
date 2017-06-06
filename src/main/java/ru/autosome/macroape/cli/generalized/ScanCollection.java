package ru.autosome.macroape.cli.generalized;

import ru.autosome.ape.calculation.findThreshold.FindThresholdAPE;
import ru.autosome.ape.calculation.findThreshold.FindThresholdBsearch;
import ru.autosome.commons.backgroundModel.GeneralizedBackgroundModel;
import ru.autosome.commons.cli.Helper;
import ru.autosome.commons.cli.ListReporter;
import ru.autosome.commons.cli.ReportListLayout;
import ru.autosome.commons.cli.TextListReporter;
import ru.autosome.commons.importer.InputExtensions;
import ru.autosome.commons.model.BoundaryType;
import ru.autosome.commons.model.Discretizer;
import ru.autosome.commons.model.Named;
import ru.autosome.commons.model.PseudocountCalculator;
import ru.autosome.commons.motifModel.Alignable;
import ru.autosome.commons.motifModel.Discretable;
import ru.autosome.commons.motifModel.ScoreDistribution;
import ru.autosome.commons.motifModel.types.DataModel;
import ru.autosome.macroape.calculation.generalized.AlignedModelIntersection;
import ru.autosome.macroape.calculation.generalized.ScanningCollection;
import ru.autosome.macroape.model.PairAligned;
import ru.autosome.macroape.model.ScanningSimilarityInfo;
import ru.autosome.macroape.model.SingleThresholdEvaluator;
import ru.autosome.macroape.model.ThresholdEvaluator;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public abstract class ScanCollection<ModelType extends Discretable<ModelType> & ScoreDistribution<BackgroundType> & Alignable<ModelType>,
                                     BackgroundType extends GeneralizedBackgroundModel> {

  protected DataModel collectionDataModel;
  protected Double collectionEffectiveCount;
  protected PseudocountCalculator collectionPseudocount;
  protected DataModel queryDataModel;
  protected Double queryEffectiveCount;
  protected PseudocountCalculator queryPseudocount;

  protected BackgroundType background;
  protected Discretizer roughDiscretizer, preciseDiscretizer;
  protected double pvalue;
  protected Double queryPredefinedThreshold;
  protected Double similarityCutoff;
  protected Double preciseRecalculationCutoff; // null means that no recalculation will be performed
  protected BoundaryType pvalueBoundaryType;
  protected String queryPMFilename;
  protected File pathToCollectionOfPWMs;
  protected File thresholds_folder;
  protected ModelType queryPWM;
  protected List<ThresholdEvaluator<ModelType>> pwmCollection;
  protected boolean queryTranspose, collectionTranspose;

  abstract protected String DOC_background_option();
  abstract protected String DOC_run_string();
  protected String documentString() {
    return "Command-line format:\n" +
     DOC_run_string() + " <query PWM file> <folder with PWMs> [options]\n" +
     "\n" +
     "Options:\n" +
     "  [--pvalue <P-value>] or [-p]\n" +
     "  [--similarity-cutoff <similarity cutoff>] or [-c] minimal similarity to be included in output, '--similarity-cutoff 0.05' by default, [--all] to print all results\n" +
     "  [--precise [<level>]] minimal similarity to check on the second pass in precise mode, off by default, '--precise 0.01' if level is not set\n" +
     "  [--rough-discretization <discretization level>] or [-d]\n" +
     "  [--precise-discretization <discretization level>]\n" +
     "  [--[query-|collection-]pcm] - treat the query input file as Position Count Matrix. PCM-to-PWM transformation to be done internally.\n" +
     "  [--[query-|collection-]ppm] or [--pfm] - treat the query input file as Position Frequency Matrix. PPM-to-PWM transformation to be done internally.\n" +
     "  [--[query-|collection-]effective-count <count>] - effective samples set size for PPM-to-PWM conversion (default: 100). \n" +
     "  [--boundary lower|upper] Upper boundary (default) means that the obtained P-value is greater than or equal to the requested P-value\n" +
     "  [--background <background probabilities>] or [-b] " + DOC_background_option() + "\n" +
     "  [--precalc <folder>] - specify folder with thresholds for PWM collection (for fast-and-rough calculation).\n" +
     "                         Attention! Don't use threshold lists calculated for a different discretization (or background)!\n" +
     "  [--[query-|collection-]transpose] - load motif from transposed matrix (nucleotides in lines).\n" +
     DOC_additional_options() +
     "\n" +
     "Output format:\n" +
     "           <name> <jaccard index> <shift> <overlap> <orientation> ['*' in case that result was calculated on the second pass (in precise mode), '.' otherwise]\n" +
     "              Attention! Name can contain whitespace characters.\n" +
     "              Attention! The shift and orientation are reported for the collection matrix relative to the query matrix.\n" +
     "\n" +
     "Examples:\n" +
     "  " + DOC_run_string() + " ./query_motif.pwm ./hocomoco/ --precalc ./hocomoco_thresholds\n" +
     "  " + DOC_run_string() + " ./query_motif.pcm ./hocomoco/ --pcm -p 0.0005 --precise 0.03\n";
  }

  protected String DOC_additional_options() {
    return "";
  }

  protected ScanCollection() {
    initialize_defaults();
  }

  protected void extract_query_pm_filename(List<String> argv) {
    if (argv.isEmpty()) {
      throw new IllegalArgumentException("No input. You should specify input file");
    }
    queryPMFilename = argv.remove(0);
  }

  protected void extract_path_to_collection_of_pwms(List<String> argv) {
    try {
      pathToCollectionOfPWMs = new File(argv.remove(0));
    } catch (IndexOutOfBoundsException e) {
      throw new IllegalArgumentException("Specify PWM-collection folder", e);
    }
  }

  protected abstract BackgroundType extractBackground(String str);
  protected abstract void initialize_default_background();
  protected void initialize_defaults() {
    initialize_default_background();
    roughDiscretizer = new Discretizer(1.0);
    preciseDiscretizer = new Discretizer(10.0);
    queryDataModel = DataModel.PWM;
    collectionDataModel = DataModel.PWM;
    queryEffectiveCount = 100.0;
    queryPseudocount = PseudocountCalculator.logPseudocount;
    collectionEffectiveCount = 100.0;
    collectionPseudocount = PseudocountCalculator.logPseudocount;
    thresholds_folder = null;
    pvalueBoundaryType = BoundaryType.WEAK;
    pvalue = 0.0005;
    similarityCutoff = 0.05;
    preciseRecalculationCutoff = null;
    queryTranspose = false;
    collectionTranspose = false;
  }

  protected void extract_option(List<String> argv) {
    String opt = argv.remove(0);
    if (opt.equals("-b") || opt.equals("--background")) {
      background = extractBackground(argv.remove(0));
    } else if (opt.equals("--rough-discretization") || opt.equals("-d") || opt.equals("--discretization")) {
      roughDiscretizer = Discretizer.fromString(argv.remove(0));
    } else if (opt.equals("--precise-discretization")) {
      preciseDiscretizer = Discretizer.fromString(argv.remove(0));
    } else if (opt.equals("--boundary")) {
      pvalueBoundaryType = BoundaryType.fromString(argv.remove(0));
    } else if (opt.equals("--pcm")) {
      queryDataModel = DataModel.PCM;
      collectionDataModel = DataModel.PCM;
    } else if (opt.equals("--query-pcm")) {
      queryDataModel = DataModel.PCM;
    } else if (opt.equals("--collection-pcm")) {
      collectionDataModel = DataModel.PCM;
    } else if (opt.equals("--ppm") || opt.equals("--pfm")) {
      queryDataModel = DataModel.PPM;
      collectionDataModel = DataModel.PPM;
    } else if (opt.equals("--query-ppm") || opt.equals("--query-pfm")) {
      queryDataModel = DataModel.PPM;
    } else if (opt.equals("--collection-ppm") || opt.equals("--collection-pfm")) {
      collectionDataModel = DataModel.PPM;
    } else if (opt.equals("--effective-count")) {
      Double effectiveCount = Double.valueOf(argv.remove(0));
      queryEffectiveCount = effectiveCount;
      collectionEffectiveCount = effectiveCount;
    } else if (opt.equals("--query-effective-count")) {
      queryEffectiveCount = Double.valueOf(argv.remove(0));
    } else if (opt.equals("--collection-effective-count")) {
      collectionEffectiveCount = Double.valueOf(argv.remove(0));
    } else if (opt.equals("--pseudocount")) {
      PseudocountCalculator pseudocount = PseudocountCalculator.fromString(argv.remove(0));
      queryPseudocount = pseudocount;
      collectionPseudocount = pseudocount;
    } else if (opt.equals("--query-pseudocount")) {
      queryPseudocount = PseudocountCalculator.fromString(argv.remove(0));
    } else if (opt.equals("--collection-pseudocount")) {
      collectionPseudocount = PseudocountCalculator.fromString(argv.remove(0));
    } else if (opt.equals("--precalc")) {
      thresholds_folder = new File(argv.remove(0));
    } else if(opt.equals("-p") || opt.equals("--pvalue")) {
      pvalue = Double.valueOf(argv.remove(0));
    } else if(opt.equals("--predefined-threshold")) {
      queryPredefinedThreshold = Double.valueOf(argv.remove(0));
    } else if(opt.equals("-c") || opt.equals("--similarity-cutoff")) {
      similarityCutoff = Double.valueOf(argv.remove(0));
    } else if(opt.equals("--all")) {
      similarityCutoff = 0.0;
    } else if(opt.equals("--precise")) {
      if (!argv.isEmpty() && InputExtensions.isDouble(argv.get(0))) {
        preciseRecalculationCutoff = Double.valueOf(argv.remove(0));
      } else {
        preciseRecalculationCutoff = 0.01;
      }
    } else if(opt.equals("--transpose")) {
      queryTranspose = true;
      collectionTranspose = true;
    } else if(opt.equals("--query-transpose")) {
      queryTranspose = true;
    } else if(opt.equals("--collection-transpose")) {
      collectionTranspose = true;
    } else {
      if (failed_to_recognize_additional_options(opt, argv)) {
        throw new IllegalArgumentException("Unknown option '" + opt + "'");
      }
    }
  }

  protected boolean failed_to_recognize_additional_options(String opt, List<String> argv) {
    return true;
  }

  public ReportListLayout<ScanningSimilarityInfo> report_table_layout() {
    ReportListLayout<ScanningSimilarityInfo> infos = new ReportListLayout<>();
    infos.add_parameter("MS", "minimal similarity to output", similarityCutoff);
    infos.add_parameter("P", "P-value", pvalue);
    infos.add_parameter("PB", "P-value boundary", pvalueBoundaryType);
    if (preciseRecalculationCutoff != null) {
      infos.add_parameter("VR", "discretization value, rough", roughDiscretizer);
      infos.add_parameter("VP", "discretization value, precise", preciseDiscretizer);
      infos.add_parameter("MP", "minimal similarity for the 2nd pass in \'precise\' mode", preciseRecalculationCutoff);
    } else {
      infos.add_parameter("V", "discretization value", roughDiscretizer);
    }
    infos.background_parameter("B", "background", background);

    infos.add_table_parameter("motif", (ScanningSimilarityInfo cell) -> cell.name);
    infos.add_table_parameter("similarity", ScanningSimilarityInfo::similarity);
    infos.add_table_parameter("shift", ScanningSimilarityInfo::shift);
    infos.add_table_parameter("overlap", ScanningSimilarityInfo::overlap);
    infos.add_table_parameter("orientation", ScanningSimilarityInfo::orientation);
    if (preciseRecalculationCutoff != null) {
      infos.add_table_parameter("precise mode", (ScanningSimilarityInfo cell) -> cell.precise ? "*" : ".");
    }
    return infos;
  }

  protected String report(List<ScanningSimilarityInfo> data) {
    data.sort(Comparator.comparing(ScanningSimilarityInfo::similarity));
    ReportListLayout<ScanningSimilarityInfo> layout = report_table_layout();
    ListReporter<ScanningSimilarityInfo> reporter = new TextListReporter<>();
    return reporter.report(data, layout);
  }

  protected List<ScanningSimilarityInfo> process() {
    return calculator().similarityInfos().collect(Collectors.toList());
  }

  // TODO: Refactor usage of one-stage and two-stage search
  protected List<ThresholdEvaluator<ModelType>> load_collection_of_pwms() {
    List<Named<ModelType>> pwmList = loadMotifCollection();
    List<ThresholdEvaluator<ModelType>> result;
    result = new ArrayList<>();
    for (Named<ModelType> namedModel: pwmList) {
      boolean bsearch_evaluator_succeed = false;
      ModelType pwm = namedModel.getObject();
      if (thresholds_folder != null) {
        File thresholds_file = new File(thresholds_folder, namedModel.getName() + ".thr");
        try {
          SingleThresholdEvaluator<ModelType> evaluator = new SingleThresholdEvaluator<>(pwm, new FindThresholdBsearch(thresholds_file));
          result.add(new ThresholdEvaluator<>(namedModel.getName(), evaluator, null));
          bsearch_evaluator_succeed = true;
        } catch (FileNotFoundException e) {
          System.err.println("Thresholds file `" + thresholds_file + "` not found. Fallback to APE-calculations");
        }
      }

      if (!bsearch_evaluator_succeed){
        SingleThresholdEvaluator<ModelType> roughEvaluator =
            new SingleThresholdEvaluator<>(pwm, new FindThresholdAPE<>(pwm, background, roughDiscretizer));
        SingleThresholdEvaluator<ModelType> preciseEvaluator =
            new SingleThresholdEvaluator<>(pwm, new FindThresholdAPE<>(pwm, background, preciseDiscretizer));

        result.add(new ThresholdEvaluator<>(namedModel.getName(), roughEvaluator, preciseEvaluator));
      }
    }
    return result;
  }

  protected void setup_from_arglist(String[] args) {
    ArrayList<String> argv = new ArrayList<>();
    Collections.addAll(argv, args);
    setup_from_arglist(argv);
  }

  protected void setup_from_arglist(List<String> argv) {
    Helper.print_help_if_requested(argv, documentString());
    extract_query_pm_filename(argv);
    extract_path_to_collection_of_pwms(argv);
    while (argv.size() > 0) {
      extract_option(argv);
    }

    queryPWM = loadQueryMotif();
    pwmCollection = load_collection_of_pwms();
  }

  protected ScanningCollection<ModelType, BackgroundType> calculator() {
    ScanningCollection<ModelType, BackgroundType> calculator;
    calculator = new ScanningCollection<>(pwmCollection, queryPWM, calc_alignment());
    calculator.pvalue = pvalue;
    calculator.queryPredefinedThreshold = queryPredefinedThreshold;
    calculator.roughDiscretizer = roughDiscretizer;
    calculator.preciseDiscretizer = preciseDiscretizer;
    calculator.background = background;
    calculator.pvalueBoundaryType = pvalueBoundaryType;
    calculator.similarityCutoff = similarityCutoff;
    calculator.preciseRecalculationCutoff = preciseRecalculationCutoff;
    return calculator;
  }

  protected abstract Function<PairAligned<ModelType>, ? extends AlignedModelIntersection> calc_alignment();
  protected abstract List<Named<ModelType>> loadMotifCollection();
  protected abstract ModelType loadQueryMotif();
}
