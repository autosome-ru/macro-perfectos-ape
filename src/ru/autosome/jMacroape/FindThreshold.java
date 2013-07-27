package ru.autosome.jMacroape;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.StringTokenizer;

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
      double discretization = 10000;
      double max_hash_size = 10000000;
      String pvalue_boundary = "lower";

      // data_model = argv.delete('--pcm') ? Bioinform::PCM : Bioinform::PWM

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
      } catch(NumberFormatException e) {
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
          ArrayList<Double> background_tmp = new ArrayList<Double>();
          try {
            for (int i = 0; i < 4; ++i) {
              background_tmp.add(Double.valueOf(parser.nextToken(",")));
            }
            background = background_tmp.toArray(new Double[0]);
          } catch(Exception e) {
            throw new IllegalArgumentException("Error in parsing background option");
          }
        } else if (opt.equals("--max-hash-size")) {
          //max_hash_size = argv.shift.to_i;
        } else if (opt.equals("-d")) {
          arg = argv[0];
          argv = Arrays.copyOfRange(argv, 1, argv.length);
          discretization = Double.valueOf(arg);
        } else if (opt.equals("--boundary")) {
          //pvalue_boundary = argv.shift.to_sym;
          pvalue_boundary = argv[0];
          argv = Arrays.copyOfRange(argv, 1, argv.length);
          if (! pvalue_boundary.equalsIgnoreCase("lower") && ! pvalue_boundary.equalsIgnoreCase("upper")) {
            throw new IllegalArgumentException("boundary should be either lower or upper");
          }
        } else {
          throw new IllegalArgumentException("Unknown option '" + opt + "'");
        }
      }

      String input = "";
      if (filename.equals(".stdin")) {
        //    input = $stdin.read
      } else {
        //if(!File.exist?(filename)) {
        //    throw new RuntimeException("Error! File #{filename} doesn't exist");
        //}
        //input = File.read(filename)
      }
            /*pwm = data_model.new(input).to_pwm
            pwm.set_parameters(background: background, max_hash_size: max_hash_size).discrete!(discretization)

                    infos = []
            collect_infos_proc = ->(pvalue, threshold, real_pvalue) do
                infos << {expected_pvalue: pvalue,
                    threshold: threshold / discretization,
                    real_pvalue: real_pvalue,
                    recognized_words: pwm.vocabulary_volume * real_pvalue }
            end
            if pvalue_boundary == :lower
            pwm.thresholds(*pvalues, &collect_infos_proc)
            else
            pwm.weak_thresholds(*pvalues, &collect_infos_proc)
            end
            puts Helper.threshold_infos_string(infos,
                    {discretization: discretization,
                    background: background,
                    pvalue_boundary: pvalue_boundary} )
              */
    } catch(Exception err) {
      System.out.println("\n" + err + "\n" + err.getStackTrace() + "\n\nUse --help option for help\n\n"+DOC);

    }
  }
}
