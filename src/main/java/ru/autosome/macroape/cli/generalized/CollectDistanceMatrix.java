package ru.autosome.macroape.cli.generalized;

import ru.autosome.ape.calculation.findThreshold.CanFindThreshold;
import ru.autosome.ape.calculation.findThreshold.FindThresholdAPE;
import ru.autosome.ape.model.exception.HashOverflowException;
import ru.autosome.commons.backgroundModel.GeneralizedBackgroundModel;
import ru.autosome.commons.model.BoundaryType;
import ru.autosome.commons.model.Discretizer;
import ru.autosome.commons.model.PseudocountCalculator;
import ru.autosome.commons.motifModel.*;
import ru.autosome.commons.motifModel.types.DataModel;
import ru.autosome.macroape.calculation.generalized.CompareModelsCountsGiven;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public abstract class CollectDistanceMatrix<ModelType extends Discretable<ModelType> & ScoreDistribution<BackgroundType> & Alignable<ModelType> & Named & ScoringModel, BackgroundType extends GeneralizedBackgroundModel> {
  class PWMWithThreshold {
    final ModelType pwm;
    final double roughThreshold;
    final double roughCount;
    final double preciseThreshold;
    final double preciseCount;
    PWMWithThreshold(ModelType pwm,
                     double roughThreshold, double roughCount,
                     double preciseThreshold, double preciseCount) {
      this.pwm = pwm;
      this.roughThreshold = roughThreshold;
      this.roughCount = roughCount;
      this.preciseThreshold = preciseThreshold;
      this.preciseCount = preciseCount;
    }
  }

  abstract protected String DOC_background_option();
  abstract protected String DOC_run_string();
  protected String documentString() {
    return "Command-line format:\n" +
            DOC_run_string() + " <folder with PWMs> [options]\n" +
            "\n" +
            "Options:\n" +
            "  [--rough-discretization <discretization level>] or [-d]\n" +
            "  [--precise-discretization <discretization level>]\n" +
            "  [--precise [<level>]] minimal similarity to check on the second pass in precise mode, off by default, '--precise 0.01' if level is not set\n" +
            "  [--pvalue <P-value>] or [-p]\n" +
            "  [--boundary lower|upper] Upper boundary (default) means that the obtained P-value is greater than or equal to the requested P-value\n" +
            "  [--pcm] - treat the input file as Position Count Matrix. PCM-to-PWM transformation to be done internally.\n" +
            "  [--ppm] or [--pfm] - treat the input file as Position Frequency Matrix. PPM-to-PWM transformation to be done internally.\n" +
            "  [--effective-count <count>] - effective samples set size for PPM-to-PWM conversion (default: 100). \n" +
            "  [--background <background probabilities>] or [-b] " + DOC_background_option() + "\n" +
            "  [--transpose] - load motif from transposed matrix (nucleotides in lines).\n" +
            "  [--parallelize <num of threads> <thread number>] - run only one task per numOfThreads (those equal to thread number modulo numOfThreads)\n" +
            DOC_additional_options() +
            "\n" +
            "Examples:\n" +
            "  " + DOC_run_string() + " ./motifs/ -d 10\n";
  }

  protected String DOC_additional_options() {
    return "";
  }

  protected Discretizer roughDiscretizer, preciseDiscretizer;
  protected File pathToCollectionOfPWMs;
  protected BackgroundType background;
  protected DataModel dataModel;
  protected Integer maxHashSize, maxPairHashSize;
  protected double effectiveCount;
  protected PseudocountCalculator pseudocount;
  protected BoundaryType pvalueBoundary;
  protected double pvalue;
  protected Double preciseRecalculationCutoff; // null means that no recalculation will be performed
  protected boolean transpose;

  protected int numOfThreads, numThread;
  protected List<ModelType> pwmCollection;

  protected void setup_from_arglist(List<String> argv) {
    extract_path_to_collection_of_pwms(argv);
    while (argv.size() > 0) {
      extract_option(argv);
    }
    pwmCollection = loadMotifCollection(pathToCollectionOfPWMs);
  }

  protected abstract List<ModelType> loadMotifCollection(File path_to_collection);

  protected abstract void initialize_default_background();

  protected void initialize_defaults() {
    initialize_default_background();
    roughDiscretizer = new Discretizer(1.0);
    preciseDiscretizer = new Discretizer(10.0);
    maxHashSize = 10000000;
    maxPairHashSize = 10000;
    dataModel = DataModel.PWM;
    effectiveCount = 100;
    pseudocount = PseudocountCalculator.logPseudocount;
    pvalue = 0.0005;
    pvalueBoundary = BoundaryType.UPPER;
    preciseRecalculationCutoff = null;
    transpose = false;

    numOfThreads = 1;
    numThread = 0;

    pathToCollectionOfPWMs = null;
    pwmCollection = null;
  }

  protected void extract_option(List<String> argv) {
    String opt = argv.remove(0);
    if (opt.equals("-b") || opt.equals("--background")) {
      background = extract_background(argv.remove(0));
    } else if(opt.equals("-p") || opt.equals("--pvalue")) {
      pvalue = Double.valueOf(argv.remove(0));
    } else if (opt.equals("--max-hash-size")) {
      maxHashSize = Integer.valueOf(argv.remove(0));
    } else if (opt.equals("--max-2d-hash-size")) {
      maxPairHashSize = Integer.valueOf(argv.remove(0));
    } else if (opt.equals("--rough-discretization") || opt.equals("-d") || opt.equals("--discretization")) {
      roughDiscretizer = Discretizer.fromString(argv.remove(0));
    } else if (opt.equals("--precise-discretization")) {
      preciseDiscretizer = Discretizer.fromString(argv.remove(0));
    } else if (opt.equals("--pcm")) {
      dataModel = DataModel.PCM;
    } else if (opt.equals("--ppm") || opt.equals("--pfm")) {
      dataModel = DataModel.PPM;
    } else if (opt.equals("--effective-count")) {
      effectiveCount = Double.valueOf(argv.remove(0));
    } else if (opt.equals("--pseudocount")) {
      pseudocount = PseudocountCalculator.fromString(argv.remove(0));
    } else if (opt.equals("--boundary")) {
      pvalueBoundary = BoundaryType.valueOf(argv.remove(0).toUpperCase());
    } else if (opt.equals("--precise")) {
      preciseRecalculationCutoff = Double.valueOf(argv.remove(0));
    } else if (opt.equals("--parallelize")) {
      numOfThreads = Integer.valueOf(argv.remove(0));
      numThread = Integer.valueOf(argv.remove(0));
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

  protected void extract_path_to_collection_of_pwms(List<String> argv) {
    try {
      pathToCollectionOfPWMs = new File(argv.remove(0));
    } catch (IndexOutOfBoundsException e) {
      throw new IllegalArgumentException("Specify PWM-collection folder", e);
    }
  }

  protected List<PWMWithThreshold> collectThreshold() throws HashOverflowException {
    List<PWMWithThreshold> result = new ArrayList<PWMWithThreshold>();
    for (ModelType pwm: pwmCollection) {
      CanFindThreshold roughThresholdCalculator = new FindThresholdAPE<ModelType, BackgroundType>(pwm, background, roughDiscretizer, maxHashSize);
      CanFindThreshold.ThresholdInfo roughThresholdInfo = roughThresholdCalculator.thresholdByPvalue(pvalue, pvalueBoundary);
      double roughThreshold = roughThresholdInfo.threshold;
      double roughCount = roughThresholdInfo.numberOfRecognizedWords(background, pwm.length());

      CanFindThreshold preciseThresholdCalculator = new FindThresholdAPE<ModelType, BackgroundType>(pwm, background, preciseDiscretizer, maxHashSize);
      CanFindThreshold.ThresholdInfo preciseThresholdInfo = preciseThresholdCalculator.thresholdByPvalue(pvalue, pvalueBoundary);
      double preciseThreshold = preciseThresholdInfo.threshold;
      double preciseCount = preciseThresholdInfo.numberOfRecognizedWords(background, pwm.length());

      result.add(new PWMWithThreshold(pwm,
                                      roughThreshold, roughCount,
                                      preciseThreshold, preciseCount));
    }
    return result;
  }

  protected double calculateDistance(PWMWithThreshold first, PWMWithThreshold second) throws HashOverflowException {
    CompareModelsCountsGiven calc;
    CompareModelsCountsGiven.SimilarityInfo info;
    calc = calculator(first.pwm, second.pwm);

    info = calc.jaccard(first.roughThreshold, second.roughThreshold,
                        first.roughCount, second.roughCount);
    if (preciseRecalculationCutoff != null && info.similarity() > preciseRecalculationCutoff) {
      calc = calculator(first.pwm, second.pwm);
      info = calc.jaccard(first.preciseThreshold, second.preciseThreshold,
                          first.preciseCount, second.preciseCount);
    }
    return info.distance();
  }

  public void process() throws HashOverflowException {
    int taskNum = 0;
    List<PWMWithThreshold> thresholds = collectThreshold();
    Collections.sort(thresholds, new Comparator<PWMWithThreshold>() {
      @Override
      public int compare(PWMWithThreshold o1, PWMWithThreshold o2) {
        return o1.pwm.getName().compareTo(o2.pwm.getName());
      }
    });

    System.out.print("Motif name"+ "\t");
    for(PWMWithThreshold second: thresholds) {
      System.out.print(second.pwm.getName() + "\t");
    }
    System.out.println();
    for(PWMWithThreshold first: thresholds) {
      System.out.print(first.pwm.getName() + "\t");
      for(PWMWithThreshold second: thresholds) {

        if (taskNum % numOfThreads == numThread % numOfThreads) {
          // so that numThread in range 0..(n-1) was equal to 1..n
          int cmp = first.pwm.getName().compareTo(second.pwm.getName());
          if (cmp == 0) {
            System.out.print("0.0\t");
          } else if (cmp < 0) {
            System.out.print("x\t");
          } else {
            double distance = calculateDistance(first, second);
            System.out.print(distance + "\t");
          }
        } else {
          System.out.print("x\t");
        }
        taskNum += 1;

      }
      System.out.println();
      System.err.print(".");
    }
  }

  abstract protected CompareModelsCountsGiven<ModelType, BackgroundType> calculator(ModelType firstModel, ModelType secondModel);
  abstract protected BackgroundType extract_background(String str);
}
