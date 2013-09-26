package ru.autosome.macroape;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.StringTokenizer;

public class PrecalculateThresholdList {
  double discretization;
  BackgroundModel background;
  String pvalue_boundary;
  int max_hash_size;
  boolean from_pcm;

  public PrecalculateThresholdList() {
    discretization = 1000;
    background = new WordwiseBackground();
    pvalue_boundary = "lower";
    max_hash_size = 10000000;
    from_pcm = false;
  }

  public HashMap<String, Object> parameters() {
    HashMap<String, Object> result = new HashMap<String,Object>();
    result.put("discretization", discretization);
    result.put("background", background);
    result.put("pvalue_boundary", pvalue_boundary);
    result.put("max_hash_size", max_hash_size);
    return result;
  }

  public void calculate_thresholds_for_collection(
          String collection_folder,
          String results_folder,
          double[] pvalues) {
    java.io.File dir = new File(collection_folder);
    java.io.File results_dir = new File(results_folder);
    if (!results_dir.exists()) {
      results_dir.mkdir();
    }
    for(File filename: dir.listFiles()) {
      System.err.println(filename);
      PWM pwm = PWM.new_from_file(filename.getPath(), background, from_pcm);
      FindThreshold calculation = new FindThreshold();
      calculation.set_parameters(parameters());
      ArrayList<ThresholdInfo> infos = calculation.find_thresholds_by_pvalues(pwm, pvalues);
      File result_filename = new File(results_dir + File.separator + "thresholds_" + filename.getName());
      FindPvalueBsearch.new_from_threshold_infos(pwm, infos).save_to_file(result_filename.getPath());
    }
  }

  static String DOC =
          "Command-line format:\n" +
          "java ru.autosome.macroape.PrecalculateThresholdList <collection folder> <output folder>... [options]\n" +
          "\n" +
          "Options:\n" +
          "  [-d <discretization level>]\n" +
          "  [--pcm] - treat the input files as Position Count Matrix. PCM-to-PWM transformation to be done internally.\n" +
          "  [--boundary lower|upper] Lower boundary (default) means that the obtained P-value is less than or equal to the requested P-value\n" +
          "  [-b <background probabilities] ACGT - 4 numbers, comma-delimited(spaces not allowed), sum should be equal to 1, like 0.25,0.24,0.26,0.25\n" +
          "  [--pvalues <min pvalue>,<max pvalue>,<step>,<mul|add>] pvalue list parameters: boundaries, step, arithmetic(add)/geometric(mul) progression\n" +
          "\n" +
          "Examples:\n" +
          "  java ru.autosome.macroape.PrecalculateThresholdList ./hocomoco/ ./hocomoco_thresholds/\n" +
          "  java ru.autosome.macroape.PrecalculateThresholdList ./hocomoco/ ./hocomoco_thresholds/ -d 100 --pvalues 1e-6,0.1,1.5,mul\n";

  public static void main(String[] args) {
    try {
      ArrayList<String> argv = new ArrayList<String>();
      Collections.addAll(argv, args);

      if (argv.isEmpty()  || ArrayExtensions.contain(argv,"-h") || ArrayExtensions.contain(argv,"--h")
              || ArrayExtensions.contain(argv,"-help") || ArrayExtensions.contain(argv,"--help")) {
        System.err.println(DOC);
        System.exit(1);
      }
      String collection_folder;
      String output_folder;
      try {
        collection_folder = argv.remove(0);
      } catch (IndexOutOfBoundsException e) {
        throw new IllegalArgumentException("Specify PWM-collection folder", e);
      }
      try {
        output_folder = argv.remove(0);
      } catch (IndexOutOfBoundsException e) {
        throw new IllegalArgumentException("Specify output folder", e);
      }
      double[] pvalue_list = Helper.values_in_range_mul(1E-6, 0.3, 1.1);

      PrecalculateThresholdList calculation = new PrecalculateThresholdList();

      while (argv.size() > 0) {
        String opt = argv.remove(0);
        if (opt.equals("-b")) {
          calculation.background = Background.fromString(argv.remove(0));
        } else if (opt.equals("--pvalues")) {
          StringTokenizer parser = new StringTokenizer(argv.remove(0));
          double min_pvalue = Double.valueOf(parser.nextToken(","));
          double max_pvalue = Double.valueOf(parser.nextToken(","));
          double step = Double.valueOf(parser.nextToken(","));
          String progression_method = parser.nextToken();
          if (progression_method.equals("mul")) {
            pvalue_list = Helper.values_in_range_mul(min_pvalue, max_pvalue, step);
          } else if (progression_method.equals("add")) {
            pvalue_list = Helper.values_in_range_add(min_pvalue, max_pvalue, step);
          } else {
            throw new IllegalArgumentException("Progression method for pvalue-list is either add or mul, but you specified " + progression_method);
          }
        }
        else if (opt.equals("--max-hash-size")) {
          calculation.max_hash_size = Integer.valueOf(argv.remove(0));
        } else if (opt.equals("-d")) {
          calculation.discretization = Double.valueOf(argv.remove(0));
        } else if (opt.equals("--boundary")) {
          calculation.pvalue_boundary = argv.remove(0);
          if (! calculation.pvalue_boundary.equalsIgnoreCase("lower") &&
              ! calculation.pvalue_boundary.equalsIgnoreCase("upper")) {
            throw new IllegalArgumentException("boundary should be either lower or upper");
          }
        } else if (opt.equals("--pcm")) {
          calculation.from_pcm = true;
        } else {
          throw new IllegalArgumentException("Unknown option '" + opt + "'");
        }
      }

      calculation.calculate_thresholds_for_collection(collection_folder, output_folder, pvalue_list);

    } catch (Exception err) {
      System.err.println("\n" + err.getMessage() + "\n--------------------------------------\n");
      err.printStackTrace();
      System.err.println("\n--------------------------------------\nUse --help option for help\n\n" + DOC);
      System.exit(1);
    }
  }
}
