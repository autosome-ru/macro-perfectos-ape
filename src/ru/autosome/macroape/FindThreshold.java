package ru.autosome.macroape;

import java.util.*;

public class FindThreshold {
  double[] background;
  Double discretization;
  Integer max_hash_size;
  String pvalue_boundary;
  public FindThreshold() {
    background = Helper.wordwise_background();
    discretization = 10000.0;
    max_hash_size = 10000000; // not int because it can be null
    pvalue_boundary = "lower";
  }
  public HashMap<String, Object> parameters() {
    HashMap<String, Object> result = new HashMap<String,Object>();
    result.put("discretization", discretization);
    result.put("background", background);
    result.put("pvalue_boundary", pvalue_boundary);
    result.put("max_hash_size", max_hash_size);
    return result;
  }
  public void set_parameters(HashMap<String, Object> params) {
    discretization = (Double)params.get("discretization");
    background = (double[])params.get("background");
    pvalue_boundary = (String)params.get("pvalue_boundary");
    max_hash_size = (Integer)params.get("max_hash_size");
  }
  public ArrayList<ThresholdInfo> find_thresholds_by_pvalues(PWM pwm, double[] pvalues) {
    if (discretization != null) {
      pwm = pwm.discrete(discretization);
    }
    pwm.max_hash_size = max_hash_size;
    pwm.background = background;

    ArrayList<ThresholdInfo> threshold_infos;
    if (pvalue_boundary.equals("lower")) {
      threshold_infos = pwm.thresholds(pvalues);
    } else {
      threshold_infos = pwm.weak_thresholds(pvalues);
    }

    if (discretization == null) {
      return threshold_infos;
    } else {
      ArrayList<ThresholdInfo> infos = new ArrayList<ThresholdInfo>();
      for (ThresholdInfo info: threshold_infos) {
        ThresholdInfo nondiscreet_info;
        nondiscreet_info = new ThresholdInfo(info.threshold / discretization,
                info.real_pvalue,
                info.expected_pvalue,
                info.recognized_words);
        infos.add(nondiscreet_info);
      }
      return infos;
    }

  }

  static String DOC =
          "Command-line format:\n" +
          "java ru.autosome.macroape.FindThreshold <pat-file> [<list of P-values>...] [options]\n" +
          "\n" +
          "Options:\n" +
          "  [-d <discretization level>]\n" +
          "  [--pcm] - treat the input file as Position Count Matrix. PCM-to-PWM transformation to be done internally.\n" +
          "  [--boundary lower|upper] Lower boundary (default) means that the obtained P-value is less than or equal to the requested P-value\n" +
          "  [-b <background probabilities] ACGT - 4 numbers, comma-delimited(spaces not allowed), sum should be equal to 1, like 0.25,0.24,0.26,0.25\n" +
          "\n" +
          "Example:\n" +
          "  java ru.autosome.macroape.FindThreshold motifs/KLF4_f2.pat\n" +
          "  java ru.autosome.macroape.FindThreshold  motifs/KLF4_f2.pat 0.001 0.0001 0.0005 -d 1000 -b 0.4,0.3,0.2,0.1\n";

  public static void main(String args[]){
    try {
      ArrayList<String> argv = new ArrayList<String>();
      Collections.addAll(argv, args);

      if (argv.isEmpty()  || ArrayExtensions.contain(argv,"-h") || ArrayExtensions.contain(argv,"--h")
                          || ArrayExtensions.contain(argv,"-help") || ArrayExtensions.contain(argv,"--help")) {
        System.err.println(DOC);
        System.exit(1);
      }

      double[] background = {1.0, 1.0, 1.0, 1.0};
      ArrayList<Double> default_pvalues = new ArrayList<Double>();
      default_pvalues.add(0.0005);
      String data_model = "pwm";

      if (argv.isEmpty()) {
        throw new IllegalArgumentException("No input. You should specify input file");
      }
      String filename = argv.remove(0);

      ArrayList<Double> pvalues = new ArrayList<Double>();

      try{
        while(! argv.isEmpty()) {
          pvalues.add(Double.valueOf(argv.get(0)));
          argv.remove(0);
        }
      } catch(NumberFormatException e) { }


      FindThreshold calculation = new FindThreshold();

      if (pvalues.size() == 0) { pvalues = default_pvalues; }

      while (argv.size() > 0) {
        String opt = argv.remove(0);
        if (opt.equals("-b")) {
          StringTokenizer parser = new StringTokenizer(argv.remove(0));
          for (int i = 0; i < 4; ++i) {
            background[i] = Double.valueOf(parser.nextToken(","));
          }
        } else if (opt.equals("--max-hash-size")) {
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
          data_model = "pcm";
        } else {
          throw new IllegalArgumentException("Unknown option '" + opt + "'");
        }
      }

      PWM pwm = PWM.new_from_file_or_stdin(filename, background, data_model.equals("pcm"));

      ArrayList<ThresholdInfo> infos = calculation.find_thresholds_by_pvalues(pwm, ArrayExtensions.toPrimitiveArray(pvalues));

      System.out.println(Helper.threshold_infos_string(infos, calculation.parameters()));
    } catch(Exception err) {
      System.err.println("\n" + err.getMessage() + "\n--------------------------------------\n");
      err.printStackTrace();
      System.err.println("\n--------------------------------------\nUse --help option for help\n\n" + DOC);
      System.exit(1);
    }
  }
}
