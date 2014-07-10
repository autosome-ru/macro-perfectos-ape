package ru.autosome.macroape.cli.generalized;

import ru.autosome.ape.calculation.findPvalue.CanFindPvalue;
import ru.autosome.ape.calculation.findPvalue.FindPvalueAPE;
import ru.autosome.ape.calculation.findPvalue.FindPvalueBsearchBuilder;
import ru.autosome.ape.calculation.findThreshold.CanFindThreshold;
import ru.autosome.ape.calculation.findThreshold.FindThresholdAPE;
import ru.autosome.ape.calculation.findThreshold.FindThresholdBsearchBuilder;
import ru.autosome.commons.backgroundModel.GeneralizedBackgroundModel;
import ru.autosome.commons.backgroundModel.mono.WordwiseBackground;
import ru.autosome.commons.cli.OutputInformation;
import ru.autosome.commons.cli.ResultInfo;
import ru.autosome.commons.importer.MotifImporter;
import ru.autosome.commons.model.BoundaryType;
import ru.autosome.commons.model.Discretizer;
import ru.autosome.commons.motifModel.*;
import ru.autosome.commons.motifModel.types.DataModel;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class ScanCollection<ModelType extends Named & ScoringModel & Discretable<ModelType> & ScoreDistribution<BackgroundType> &Alignable<ModelType>,
                                     BackgroundType extends GeneralizedBackgroundModel> {

  public class ThresholdEvaluator {
    public final ModelType pwm;
    public final CanFindThreshold roughThresholdCalculator;
    public final CanFindThreshold preciseThresholdCalculator;

    public final CanFindPvalue roughPvalueCalculator;
    public final CanFindPvalue precisePvalueCalculator;

    public ThresholdEvaluator(ModelType pwm,
                              CanFindThreshold roughThresholdCalculator, CanFindThreshold preciseThresholdCalculator,
                              CanFindPvalue roughPvalueCalculator, CanFindPvalue precisePvalueCalculator) {
      this.pwm = pwm;
      this.roughThresholdCalculator = roughThresholdCalculator;
      this.preciseThresholdCalculator = preciseThresholdCalculator;
      this.roughPvalueCalculator = roughPvalueCalculator;
      this.precisePvalueCalculator = precisePvalueCalculator;
    }
  }
  protected DataModel dataModel;
  protected Double effectiveCount;
  protected BackgroundType queryBackground, collectionBackground;
  protected Discretizer roughDiscretizer, preciseDiscretizer;
  protected Integer maxHashSize;
  protected Integer maxPairHashSize;
  protected double pvalue;
  protected Double queryPredefinedThreshold;
  protected Double similarityCutoff;
  protected Double preciseRecalculationCutoff; // null means that no recalculation will be performed
  protected BoundaryType pvalueBoundaryType;
  protected boolean silenceLog;
  protected String queryPMFilename;
  protected File pathToCollectionOfPWMs;
  protected File thresholds_folder;
  protected ModelType queryPWM;
  protected List<ThresholdEvaluator> pwmCollection;
  protected boolean queryTranspose, collectionTranspose;

  abstract protected String DOC_background_option();
  abstract protected String DOC_run_string();
  protected String documentString() {
    return "Command-line format:\n" +
     DOC_run_string() + " <query PWM file> <folder with PWMs> [options]\n" +
     "\n" +
     "Options:\n" +
     "  [-p <P-value>]\n" +
     "  [-c <similarity cutoff>] minimal similarity to be included in output, '-c 0.05' by default, [--all] to print all results\n" +
     "  [--precise [<level>]] minimal similarity to check on the second pass in precise mode, off by default, '--precise 0.01' if level is not set\n" +
     "  [--rough-discretization <discretization level>] or [-d]\n" +
     "  [--precise-discretization <discretization level>]\n" +
     "  [--silent] - hide current progress information during scan (printed to stderr by default)\n" +
     "  [--pcm] - treat the query input file as Position Count Matrix. PCM-to-PWM transformation to be done internally.\n" +
     "  [--ppm] or [--pfm] - treat the query input file as Position Frequency Matrix. PPM-to-PWM transformation to be done internally.\n" +
     "  [--effective-count <count>] - effective samples set size for PPM-to-PWM conversion (default: 100). \n" +
     "  [--boundary lower|upper] Upper boundary (default) means that the obtained P-value is greater than or equal to the requested P-value\n" +
     "  [-b <background probabilities] " + DOC_background_option() + "\n" +
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

  protected void extract_query_pm_filename(ArrayList<String> argv) {
    if (argv.isEmpty()) {
      throw new IllegalArgumentException("No input. You should specify input file");
    }
    queryPMFilename = argv.remove(0);
  }

  protected void extract_path_to_collection_of_pwms(ArrayList<String> argv) {
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
    maxHashSize = 10000000;
    maxPairHashSize = 10000;
    dataModel = DataModel.PWM;
    effectiveCount = 100.0;
    thresholds_folder = null;
    silenceLog = false;
    pvalueBoundaryType = BoundaryType.UPPER;
    pvalue = 0.0005;
    similarityCutoff = 0.05;
    preciseRecalculationCutoff = null;
    queryTranspose = false;
    collectionTranspose = false;
  }

  protected void extract_option(ArrayList<String> argv) {
    String opt = argv.remove(0);
    if (opt.equals("-b")) {
      BackgroundType background = extractBackground(argv.remove(0));
      queryBackground = background;
      collectionBackground = background;
    } else if (opt.equals("--query-background")) {
      queryBackground = extractBackground(argv.remove(0));
    } else if (opt.equals("--collection-background")) {
      collectionBackground = extractBackground(argv.remove(0));
    } else if (opt.equals("--max-hash-size")) {
      maxHashSize = Integer.valueOf(argv.remove(0));
    } else if (opt.equals("--max-2d-hash-size")) {
      maxPairHashSize = Integer.valueOf(argv.remove(0));
    } else if (opt.equals("--rough-discretization") || opt.equals("-d")) {
      roughDiscretizer = Discretizer.fromString(argv.remove(0));
    } else if (opt.equals("--precise-discretization")) {
      preciseDiscretizer = Discretizer.fromString(argv.remove(0));
    } else if (opt.equals("--boundary")) {
      pvalueBoundaryType = BoundaryType.valueOf(argv.remove(0).toUpperCase());
    } else if (opt.equals("--pcm")) {
      dataModel = DataModel.PCM;
    } else if (opt.equals("--ppm") || opt.equals("--pfm")) {
      dataModel = DataModel.PPM;
    } else if (opt.equals("--effective-count")) {
      effectiveCount = Double.valueOf(argv.remove(0));
    } else if (opt.equals("--precalc")) {
      thresholds_folder = new File(argv.remove(0));
    } else if(opt.equals("-p")) {
      pvalue = Double.valueOf(argv.remove(0));
    } else if(opt.equals("--predefined-threshold")) {
      queryPredefinedThreshold = Double.valueOf(argv.remove(0));
    } else if(opt.equals("-c")) {
      similarityCutoff = Double.valueOf(argv.remove(0));
    } else if(opt.equals("--all")) {
      similarityCutoff = 0.0;
    } else if(opt.equals("--precise")) {
      preciseRecalculationCutoff = Double.valueOf(argv.remove(0));
    } else if(opt.equals("--silent")) {
      silenceLog = true;
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

  public OutputInformation report_table_layout() {
    OutputInformation infos = new OutputInformation();
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
    infos.background_parameter("BQ", "background for query matrix", queryBackground);
    infos.background_parameter("BC", "background for collection", collectionBackground);

    infos.add_table_parameter_without_description("motif", "name");
    infos.add_table_parameter_without_description("similarity", "similarity");
    infos.add_table_parameter_without_description("shift", "shift");
    infos.add_table_parameter_without_description("overlap", "overlap");
    infos.add_table_parameter_without_description("orientation", "orientation");
    if (preciseRecalculationCutoff != null) {
      infos.add_table_parameter_without_description("precise mode", "precision_mode", new OutputInformation.Callback<ru.autosome.macroape.calculation.generalized.ScanCollection.SimilarityInfo>(){
        @Override
        public String run(ru.autosome.macroape.calculation.generalized.ScanCollection.SimilarityInfo cell) {
          return cell.precise ? "*" : ".";
        }
      });
    }
    return infos;
  }

  protected OutputInformation report_table(List<? extends ResultInfo> data) {
    OutputInformation result = report_table_layout();
    result.data = data;
    return result;
  }

  protected <R extends ResultInfo> OutputInformation report_table(R[] data) {
    List<R> data_list = new ArrayList<R>(data.length);
    Collections.addAll(data_list, data);
    return report_table(data_list);
  }

  protected List<? extends ResultInfo> process() throws Exception {
    List<ru.autosome.macroape.calculation.generalized.ScanCollection<ModelType, BackgroundType>.SimilarityInfo> infos;
    infos = calculator().similarityInfos();
    return infos;
  }

  // TODO: Refactor usage of one-stage and two-stage search
  protected List<ThresholdEvaluator> load_collection_of_pwms() {
    MotifImporter<ModelType, BackgroundType> importer = motifImporter(collectionBackground, dataModel, effectiveCount, collectionTranspose);
    List<ModelType> pwmList = importer.loadMotifCollection(pathToCollectionOfPWMs);
    List<ThresholdEvaluator> result;
    result = new ArrayList<ThresholdEvaluator>();
    for (ModelType pwm: pwmList) {
      if (thresholds_folder == null) {
        result.add(new ThresholdEvaluator( pwm,
                                           new FindThresholdAPE<ModelType, BackgroundType>(pwm, collectionBackground, roughDiscretizer, maxHashSize),
                                           new FindThresholdAPE<ModelType, BackgroundType>(pwm, collectionBackground, preciseDiscretizer, maxHashSize),
                                           new FindPvalueAPE<ModelType, BackgroundType>(pwm, collectionBackground, roughDiscretizer, maxHashSize),
                                           new FindPvalueAPE<ModelType, BackgroundType>(pwm, collectionBackground, preciseDiscretizer, maxHashSize)));
      } else {
        result.add(new ThresholdEvaluator( pwm,
                                           new FindThresholdBsearchBuilder(thresholds_folder).thresholdCalculator(pwm),
                                           null,
                                           new FindPvalueBsearchBuilder(thresholds_folder).pvalueCalculator(pwm),
                                           null));
      }
    }
    return result;
  }

  protected void setup_from_arglist(ArrayList<String> argv) {
    extract_query_pm_filename(argv);
    extract_path_to_collection_of_pwms(argv);
    while (argv.size() > 0) {
      extract_option(argv);
    }

    queryPWM = motifImporter(queryBackground, dataModel, effectiveCount, queryTranspose).loadMotif(queryPMFilename);
    pwmCollection = load_collection_of_pwms();
  }


  protected abstract ru.autosome.macroape.calculation.generalized.ScanCollection<ModelType,BackgroundType> calculator();
  protected abstract MotifImporter<ModelType, BackgroundType> motifImporter(BackgroundType background, DataModel dataModel, Double effectiveCount, boolean transpose);

}
