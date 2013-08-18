package ru.autosome.jMacroape;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.*;

public class FindPvalue {

  // In some cases (when discretization is null) pwm can be altered by background and max_hash_size
  public ArrayList<PvalueInfo> pvalues_by_thresholds(double[] thresholds) {
    if (discretization != null) {
      pwm = pwm.discrete(discretization);
    }
    pwm.max_hash_size = max_hash_size; // not int because it can be null
    pwm.background = background;

    double[] thresholds_discreeted = new double[thresholds.length];
    for (int i = 0; i < thresholds.length; ++i) {
      thresholds_discreeted[i] = thresholds[i] * discretization;
    }

    HashMap<Double,Double> counts = pwm.counts_by_thresholds(thresholds_discreeted);
    ArrayList<PvalueInfo> infos = new ArrayList<PvalueInfo>();
    for (double threshold: thresholds) {
      double count = counts.get(threshold * discretization);
      double pvalue = count / pwm.vocabulary_volume();

      infos.add(new PvalueInfo(threshold, pvalue, (int)count));
    }
    return infos;
  }

  public PvalueInfo pvalue_by_threshold(double threshold) {
    double[] thresholds = {threshold};
    return pvalues_by_thresholds(thresholds).get(0);
  }

  static String DOC =
        "Command-line format:\n" +
        "java ru.autosome.jMacroape.FindPvalue <pat-file> <threshold list>... [options]\n" +
        "\n" +
        "Options:\n" +
        "  [-d <discretization level>]\n" +
        "  [--pcm] - treat the input file as Position Count Matrix. PCM-to-PWM transformation to be done internally.\n" +
        "  [-b <background probabilities] ACGT - 4 numbers, comma-delimited(spaces not allowed), sum should be equal to 1, like 0.25,0.24,0.26,0.25\n" +
        "\n" +
        "Examples:\n" +
        "  java ru.autosome.jMacroape.FindPvalue motifs/KLF4_f2.pat 7.32\n" +
        "  java ru.autosome.jMacroape.FindPvalue motifs/KLF4_f2.pat 7.32 4.31 5.42 -d 1000 -b 0.2,0.3,0.3,0.2\n";

  PWM pwm;
  Double discretization;
  double[] background;
  Integer max_hash_size;

  public FindPvalue(PWM pwm) {
    this.pwm = pwm;
    this.discretization = 10000.0;
    this.background = Helper.wordwise_background();
    this.max_hash_size = 10000000;
  }
  public HashMap<String, Object> parameters() {
    HashMap<String, Object> parameters = new HashMap<String,Object>();
    parameters.put("discretization", discretization);
    parameters.put("background", background);
    parameters.put("max_hash_size", max_hash_size);
    return parameters;
  }
  public void set_parameters(HashMap<String, Object> parameters) {
    if(parameters.containsKey("discretization")) {
      discretization = (Double)parameters.get("discretization");
    }
    if(parameters.containsKey("background")) {
      background = (double[])parameters.get("background");
    }
    if(parameters.containsKey("max_hash_size")) {
      max_hash_size = (Integer)parameters.get("max_hash_size");
    }
  }


  public static void main(String[] args) {
    try{
      ArrayList<String> argv = new ArrayList<String>();
      Collections.addAll(argv, args);

      if (argv.isEmpty()  || ArrayExtensions.contain(argv,"-h") || ArrayExtensions.contain(argv,"--h")
              || ArrayExtensions.contain(argv,"-help") || ArrayExtensions.contain(argv,"--help")) {
        System.err.println(DOC);
        System.exit(1);
      }


      double discretization = 10000.0;
      double[] background = Helper.wordwise_background();
      ArrayList<Double>thresholds_list = new ArrayList<Double>();
      Integer max_hash_size = 10000000;
      String data_model = "pwm";
  //    boolean fast_mode = false;

      if (argv.isEmpty()) {
        throw new IllegalArgumentException("No input. You should specify input file");
      }
      String filename = argv.remove(0);

      try{
        while(! argv.isEmpty()) {
          thresholds_list.add(Double.valueOf(argv.get(0)));
          argv.remove(0);
        }
      } catch(NumberFormatException e) { }
      if (thresholds_list.isEmpty()) {
        throw new IllegalArgumentException("You should specify at least one threshold");
      }
      double[] thresholds = ArrayExtensions.toPrimitiveArray(thresholds_list);

      while (argv.size() > 0) {
        String opt = argv.remove(0);
        if (opt.equals("-b")) {
          StringTokenizer parser = new StringTokenizer(argv.remove(0));
          for (int i = 0; i < 4; ++i) {
            background[i] = Double.valueOf(parser.nextToken(","));
          }
        } else if (opt.equals("--max-hash-size")) {
          max_hash_size = Integer.valueOf(argv.remove(0));
        } else if (opt.equals("-d")) {
          discretization = Double.valueOf(argv.remove(0));
        } else if (opt.equals("--pcm")) {
          data_model = "pcm";
        } else {
          throw new IllegalArgumentException("Unknown option '" + opt + "'");
        }
      }

      PWM pwm = PWM.new_from_file_or_stdin(filename, background, data_model.equals("pcm"));
      FindPvalue calculation = new FindPvalue(pwm);
      calculation.discretization = discretization;
      calculation.background = background;
      calculation.max_hash_size = max_hash_size;

      HashMap<String, Object> parameters = calculation.parameters();

      ArrayList<PvalueInfo> infos = calculation.pvalues_by_thresholds(thresholds);

      System.out.println(Helper.find_pvalue_info_string(infos, parameters));

    } catch (Exception err) {
      System.err.println("\n" + err + "\n");
      err.printStackTrace();
      System.err.println("\n\nUse --help option for help\n\n" + DOC);
    }

  }

}
