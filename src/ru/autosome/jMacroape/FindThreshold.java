package ru.autosome.jMacroape;

import com.sun.deploy.util.StringUtils;

import java.io.*;
import java.nio.charset.Charset;
import java.util.*;

public class FindThreshold {

  public static ArrayList<HashMap<String, Double>> find_thresholds_by_pvalues(PWM pwm, double[] pvalues, Map<String,Object> parameters){
    Double discretization = (Double)parameters.get("discretization");
    if (discretization != null) {
      pwm = pwm.discrete(discretization);
    }
    pwm.max_hash_size = (Integer)parameters.get("max_hash_size"); // not int because it can be null
    pwm.background = (double[])parameters.get("background");
    String pvalue_boundary = (String)parameters.get("pvalue_boundary");

    HashMap<Double, double[]> threshold_infos;
    if (pvalue_boundary.equals("lower")) {
      threshold_infos = pwm.thresholds(pvalues);
    } else {
      threshold_infos = pwm.weak_thresholds(pvalues);
    }

    ArrayList<HashMap<String, Double>> infos = new ArrayList<HashMap<String, Double>>();
    for (double pvalue: threshold_infos.keySet()) {
      double threshold = threshold_infos.get(pvalue)[0];
      double real_pvalue = threshold_infos.get(pvalue)[1];
      HashMap<String,Double> tmp = new HashMap<String,Double>();
      tmp.put("expected_pvalue", pvalue);
      tmp.put("threshold", threshold / discretization);
      tmp.put("real_pvalue", real_pvalue);
      tmp.put("recognized_words", pwm.vocabulary_volume() * real_pvalue);
      infos.add(tmp);
    }
    return infos;
  }

  static String DOC =
          "Command-line format:\n" +
          "java ru.autosome.jMacroape.FindThreshold <pat-file> [<list of P-values>...] [options]\n" +
          "\n" +
          "Options:\n" +
          "  [-d <discretization level>]\n" +
          "  [--pcm] - treat the input file as Position Count Matrix. PCM-to-PWM transformation to be done internally.\n" +
          "  [--boundary lower|upper] Lower boundary (default) means that the obtained P-value is less than or equal to the requested P-value\n" +
          "  [-b <background probabilities] ACGT - 4 numbers, comma-delimited(spaces not allowed), sum should be equal to 1, like 0.25,0.24,0.26,0.25\n" +
          "\n" +
          "Example:\n" +
          "  java ru.autosome.jMacroape.FindThreshold motifs/KLF4_f2.pat\n" +
          "  java ru.autosome.jMacroape.FindThreshold  motifs/KLF4_f2.pat 0.001 0.0001 0.0005 -d 1000 -b 0.4,0.3,0.2,0.1\n";

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
      double discretization = 10000.0;
      Integer max_hash_size = 10000000;
      String pvalue_boundary = "lower";

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

      if (pvalues.size() == 0) { pvalues = default_pvalues; }

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
        } else if (opt.equals("--boundary")) {
          pvalue_boundary = argv.remove(0);
          if (! pvalue_boundary.equalsIgnoreCase("lower") && ! pvalue_boundary.equalsIgnoreCase("upper")) {
            throw new IllegalArgumentException("boundary should be either lower or upper");
          }
        } else if (opt.equals("--pcm")) {
          data_model = "pcm";
        } else {
          throw new IllegalArgumentException("Unknown option '" + opt + "'");
        }
      }

      InputStream reader;
      if (filename.equals(".stdin")) {
        reader = System.in;
      } else {
        if(!(new File(filename).exists())) {
          throw new RuntimeException("Error! File #{filename} doesn't exist");
        }
        reader = new FileInputStream(filename);
      }

      PMParser matrix_parser = new PMParser(InputExtensions.readLinesFromInputStream(reader));

      double[][] matrix = matrix_parser.matrix();
      String name = matrix_parser.name();

      PWM pwm;
      if (data_model.equals("pwm")) {
        pwm = new PWM(matrix, background, name);
      } else {
        PCM pcm = new PCM(matrix, background, name);
        pwm = pcm.to_pwm();
      }

      HashMap<String, Object> parameters = new HashMap<String,Object>();
      parameters.put("discretization", discretization);
      parameters.put("background", background);
      parameters.put("pvalue_boundary", pvalue_boundary);
      parameters.put("max_hash_size", max_hash_size);
      ArrayList<HashMap<String, Double>> infos = find_thresholds_by_pvalues(pwm, ArrayExtensions.toPrimitiveArray(pvalues), parameters);

      System.out.println(Helper.threshold_infos_string(infos, parameters));
    } catch(Exception err) {
      System.err.println("\n" + err + "\n");
      err.printStackTrace();
      System.err.println("\n\nUse --help option for help\n\n" + DOC);
    }
  }
}
