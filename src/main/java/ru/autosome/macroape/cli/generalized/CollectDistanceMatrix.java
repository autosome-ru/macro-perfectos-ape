package ru.autosome.macroape.cli.generalized;

import ru.autosome.ape.calculation.findThreshold.CanFindThreshold;
import ru.autosome.ape.calculation.findThreshold.FindThresholdAPE;
import ru.autosome.ape.calculation.findThreshold.FoundedThresholdInfo;
import ru.autosome.commons.backgroundModel.GeneralizedBackgroundModel;
import ru.autosome.commons.cli.Helper;
import ru.autosome.commons.model.BoundaryType;
import ru.autosome.commons.model.Discretizer;
import ru.autosome.commons.model.Named;
import ru.autosome.commons.model.PseudocountCalculator;
import ru.autosome.commons.motifModel.Alignable;
import ru.autosome.commons.motifModel.Discretable;
import ru.autosome.commons.motifModel.ScoreDistribution;
import ru.autosome.commons.motifModel.types.DataModel;
import ru.autosome.macroape.calculation.generalized.AlignedModelIntersection;
import ru.autosome.macroape.calculation.generalized.CompareModels;
import ru.autosome.macroape.model.ComparisonSimilarityInfo;
import ru.autosome.macroape.model.PairAligned;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;

public abstract class CollectDistanceMatrix<ModelType extends Discretable<ModelType> & ScoreDistribution<BackgroundType> & Alignable<ModelType>,
                                            BackgroundType extends GeneralizedBackgroundModel> {
  class PWMWithThreshold {
    final Named<ModelType> pwm;
    final double roughThreshold;
    final double roughCount;
    final double preciseThreshold;
    final double preciseCount;
    PWMWithThreshold(Named<ModelType> pwm,
                     double roughThreshold, double roughCount,
                     double preciseThreshold, double preciseCount) {
      this.pwm = pwm;
      this.roughThreshold = roughThreshold;
      this.roughCount = roughCount;
      this.preciseThreshold = preciseThreshold;
      this.preciseCount = preciseCount;
    }
  }

  protected CollectDistanceMatrix() {
    initialize_defaults();
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
  protected double effectiveCount;
  protected PseudocountCalculator pseudocount;
  protected BoundaryType pvalueBoundary;
  protected double pvalue;
  protected Double preciseRecalculationCutoff; // null means that no recalculation will be performed
  protected boolean transpose;

  protected int numOfThreads, numThread;
  protected List<Named<ModelType>> pwmCollection;

  protected void setup_from_arglist(String[] args) {
    ArrayList<String> argv = new ArrayList<>();
    Collections.addAll(argv, args);
    setup_from_arglist(argv);
  }

  protected void setup_from_arglist(List<String> argv) {
    Helper.print_help_if_requested(argv, documentString());
    extract_path_to_collection_of_pwms(argv);
    while (argv.size() > 0) {
      extract_option(argv);
    }
    pwmCollection = loadMotifCollection(pathToCollectionOfPWMs);
  }

  protected abstract List<Named<ModelType>> loadMotifCollection(File path_to_collection);

  protected abstract void initialize_default_background();

  protected void initialize_defaults() {
    initialize_default_background();
    roughDiscretizer = new Discretizer(1.0);
    preciseDiscretizer = new Discretizer(10.0);
    dataModel = DataModel.PWM;
    effectiveCount = 100;
    pseudocount = PseudocountCalculator.logPseudocount;
    pvalue = 0.0005;
    pvalueBoundary = BoundaryType.WEAK;
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
      pvalueBoundary = BoundaryType.fromString(argv.remove(0));
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

  protected List<PWMWithThreshold> collectThreshold() {
    List<PWMWithThreshold> result = new ArrayList<>();
    for (Named<ModelType> pwm: pwmCollection) {
      CanFindThreshold roughThresholdCalculator = new FindThresholdAPE<>(pwm.getObject(), background, roughDiscretizer);
      FoundedThresholdInfo roughThresholdInfo = roughThresholdCalculator.thresholdByPvalue(pvalue, pvalueBoundary);
      double roughThreshold = roughThresholdInfo.threshold;
      double roughCount = roughThresholdInfo.numberOfRecognizedWords(background, pwm.getObject().length());

      CanFindThreshold preciseThresholdCalculator = new FindThresholdAPE<>(pwm.getObject(), background, preciseDiscretizer);
      FoundedThresholdInfo preciseThresholdInfo = preciseThresholdCalculator.thresholdByPvalue(pvalue, pvalueBoundary);
      double preciseThreshold = preciseThresholdInfo.threshold;
      double preciseCount = preciseThresholdInfo.numberOfRecognizedWords(background, pwm.getObject().length());

      result.add(new PWMWithThreshold(pwm,
                                      roughThreshold, roughCount,
                                      preciseThreshold, preciseCount));
    }
    return result;
  }

  protected double calculateDistance(PWMWithThreshold first, PWMWithThreshold second) {
    CompareModels calc = new CompareModels<>(first.pwm.getObject(), second.pwm.getObject(), background, roughDiscretizer, calc_alignment());
    ComparisonSimilarityInfo info = calc.jaccard(first.roughThreshold, second.roughThreshold,
                                                 first.roughCount, second.roughCount);

    if (preciseRecalculationCutoff != null && info.similarity() > preciseRecalculationCutoff) {
      calc = new CompareModels<>(first.pwm.getObject(), second.pwm.getObject(), background, preciseDiscretizer, calc_alignment());
      info = calc.jaccard(first.preciseThreshold, second.preciseThreshold,
                          first.preciseCount, second.preciseCount);
    }
    return info.distance();
  }

  public void process() {
    int taskNum = 0;
    List<PWMWithThreshold> thresholds = collectThreshold();
    thresholds.sort(Comparator.comparing(o -> o.pwm.getName()));

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

  abstract protected Function<PairAligned<ModelType>, ? extends AlignedModelIntersection> calc_alignment();
  abstract protected BackgroundType extract_background(String str);
}
