package ru.autosome.jMacroape;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.*;

public class FindThreshold {
  static String DOC = "EOS.strip_doc\n" +
          "Command-line format:\n" +
          "#{run_tool_cmd} <pat-file> [<list of P-values>...] [options]\n" +
          "\n" +
          "Options:\n" +
          "[-d <discretization level>]\n" +
          "[--pcm] - treat the input file as Position Count Matrix. PCM-to-PWM transformation to be done internally.\n" +
          "[--boundary lower|upper] Lower boundary (default) means that the obtained P-value is less than or equal to the requested P-value\n" +
          "[-b <background probabilities] ACGT - 4 numbers, comma-delimited(spaces not allowed), sum should be equal to 1, like 0.25,0.24,0.26,0.25\n" +
          "\n" +
          "Example:\n" +
          "#{run_tool_cmd} motifs/KLF4_f2.pat\n" +
          "#{run_tool_cmd} motifs/KLF4_f2.pat 0.001 0.0001 0.0005 -d 1000 -b 0.4,0.3,0.2,0.1\n";

  public static void main(String argv[]){
    try {
      HashSet<String> argv_set = new HashSet<String>();
      for (String arg: argv) {
        argv_set.add(arg);
      }
      if (argv.length == 0 || argv_set.contains("-h") || argv_set.contains("--h") || argv_set.contains("-help") || argv_set.contains("--help") ) {
        System.err.println(DOC);
        System.exit(1);
      }

      Double[] background = {1.0,1.0,1.0,1.0};
      ArrayList<Double> default_pvalues = new ArrayList<Double>();
      default_pvalues.add(0.0005);
      Double discretization = 10000.0;
      Integer max_hash_size = 10000000;
      String pvalue_boundary = "lower";

      String data_model = "pwm";
      if (argv_set.contains("--pcm")) {
        data_model = "pcm";
      }

      if (argv.length == 0) {
        throw new IllegalArgumentException("No input. You should specify input file");
      }
      String filename = argv[0];
      argv = Arrays.copyOfRange(argv, 1, argv.length);

      ArrayList<Double> pvalues = new ArrayList<Double>();
      try{
        while(true) {
          pvalues.add(Double.valueOf(argv[0]));
          argv = Arrays.copyOfRange(argv, 1, argv.length);
        }
      } catch(Exception e) {
      }

      if (pvalues.size() == 0) {
        pvalues = default_pvalues;
      }

      while (argv.length > 0) {
        String opt = argv[0];
        argv = Arrays.copyOfRange(argv, 1, argv.length);
        String arg;
        if (opt.equals("-b")) {
          arg = argv[0];
          argv = Arrays.copyOfRange(argv, 1, argv.length);

          StringTokenizer parser = new StringTokenizer(arg);
          for (int i = 0; i < 4; ++i) {
            background[i] = Double.valueOf(parser.nextToken(","));
          }
        } else if (opt.equals("--max-hash-size")) {
          max_hash_size = Integer.valueOf(argv[0]);
          argv = Arrays.copyOfRange(argv, 1, argv.length);
        } else if (opt.equals("-d")) {
          arg = argv[0];
          argv = Arrays.copyOfRange(argv, 1, argv.length);
          discretization = Double.valueOf(arg);
        } else if (opt.equals("--boundary")) {
          pvalue_boundary = argv[0];
          argv = Arrays.copyOfRange(argv, 1, argv.length);
          if (! pvalue_boundary.equalsIgnoreCase("lower") && ! pvalue_boundary.equalsIgnoreCase("upper")) {
            throw new IllegalArgumentException("boundary should be either lower or upper");
          }
        } else if (opt.equals("--pcm")) {
          // skip
        } else {
          throw new IllegalArgumentException("Unknown option '" + opt + "'");
        }
      }

      String input = "";
      ArrayList<String> inp_strings = new ArrayList<String>();
      if (filename.equals(".stdin")) {
        inp_strings = InputExtensions.readLinesFromInputStream(System.in, Charset.forName("CP1251"));
      } else {
        if(!(new File(filename).exists())) {
          throw new RuntimeException("Error! File #{filename} doesn't exist");
        }
        inp_strings = InputExtensions.readLinesFromInputStream(new FileInputStream(filename), Charset.forName("CP1251"));
      }

      ArrayList<Double[]> matrix = new ArrayList<Double[]>();
      String name = "";
      int i = 0;
      try {
        Double.valueOf(inp_strings.get(0).replaceAll("\\s+"," ").split(" ")[0]);
      } catch (NumberFormatException e) {
        name = inp_strings.get(0);
        while (name.charAt(0) == '>' || name.charAt(0) == ' ' || name.charAt(0) == '\t') {
          name = name.substring(1, name.length());
        }
        i++;
      }

      for (; i < inp_strings.size(); ++i) {
        Double[] tmp = new Double[4];
        StringTokenizer parser = new StringTokenizer(inp_strings.get(i).replaceAll("\\s+"," "));
        for (int j = 0; j < 4; ++j) {
          tmp[j] = Double.valueOf(parser.nextToken(" "));
        }
        matrix.add(tmp);
      }

      PWM pwm;
      if (data_model.equals("pwm")) {
        pwm = new PWM(matrix.toArray(new Double[0][0]), background, name);
      } else {
        PCM pcm = new PCM(matrix.toArray(new Double[0][0]), background, name);
        pwm = pcm.to_pwm();
      }

      pwm = new PWM(pwm.discrete(discretization));
      pwm.max_hash_size = max_hash_size;

      HashMap<Double, Double[]> results;
      if (pvalue_boundary.equals("lower")) {
        results = pwm.thresholds(pvalues.toArray(new Double[0]));
      } else {
        results = pwm.weak_thresholds(pvalues.toArray(new Double[0]));
      }

      ArrayList<HashMap<String, Double>> infos = new ArrayList<HashMap<String, Double>>();
      for (Double pvalue: results.keySet()) {
        Double threshold = results.get(pvalue)[0];
        Double real_pvalue = results.get(pvalue)[1];
        HashMap<String,Double> tmp = new HashMap<String,Double>();
        tmp.put("expected_pvalue", pvalue);
        tmp.put("threshold", threshold / discretization);
        tmp.put("real_pvalue", real_pvalue);
        tmp.put("recognized_words", pwm.vocabulary_volume() * real_pvalue);
        infos.add(tmp);
      }


      HashMap<String, Object> parameters = new HashMap<String,Object>();
      parameters.put("discretization", discretization);
      parameters.put("background", background);
      parameters.put("pvalue_boundary", pvalue_boundary);

      System.out.println(Helper.threshold_infos_string(infos, parameters));
    } catch(Exception err) {
      System.err.println("\n" + err + "\n");
      err.printStackTrace();
      System.err.println("\n\nUse --help option for help\n\n" + DOC);

    }
  }
}
