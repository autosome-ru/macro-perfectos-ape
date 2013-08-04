package ru.autosome.jMacroape;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.*;

public class FindPvalue {
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

  public static void main(String[] args) {
    try{
      ArrayList<String> argv = new ArrayList<String>();
      //for(String arg:args) argv.add(arg);
      Collections.addAll(argv, args);

      if (argv.isEmpty()  || ArrayExtensions.contain(argv,"-h") || ArrayExtensions.contain(argv,"--h")
              || ArrayExtensions.contain(argv,"-help") || ArrayExtensions.contain(argv,"--help")) {
        System.err.println(DOC);
        System.exit(1);
      }

      double discretization = 10000.0;
      double[] background = {1.0, 1.0, 1.0, 1.0};
      ArrayList<Double>thresholds_list = new ArrayList<Double>();
      Integer max_hash_size = 10000000;

      String data_model = "pwm";

      //data_model = argv.delete('--pcm') ? Bioinform::PCM : Bioinform::PWM

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
      pwm = pwm.discrete(discretization);
      pwm.max_hash_size = max_hash_size;

      double[] thresholds = new double[thresholds_list.size()];
      for (int i = 0; i < thresholds_list.size(); ++i) {
        thresholds[i] = thresholds_list.get(i);
      }

      double[] thresholds_discreeted = new double[thresholds.length];
      for (int i = 0; i < thresholds_list.size(); ++i) {
        thresholds_discreeted[i] = thresholds[i] * discretization;
      }

      HashMap<Double,Double> counts = pwm.counts_by_thresholds(thresholds_discreeted);

      ArrayList<HashMap<String, Double>> infos = new ArrayList<HashMap<String, Double>>();
      for (double threshold: thresholds) {
        double count = counts.get(threshold * discretization);
        double pvalue = count / pwm.vocabulary_volume();
        HashMap<String,Double> tmp = new HashMap<String,Double>();
        tmp.put("threshold", threshold);
        tmp.put("pvalue", pvalue);
        tmp.put("number_of_recognized_words", count);
        infos.add(tmp);
      }
      HashMap<String, Object> parameters = new HashMap<String,Object>();
      parameters.put("discretization", discretization);
      parameters.put("background", background);

      System.out.println(Helper.find_pvalue_info_string(infos, parameters));

    } catch (Exception err) {
      System.err.println("\n" + err + "\n");
      err.printStackTrace();
      System.err.println("\n\nUse --help option for help\n\n" + DOC);
    }

  }

}
