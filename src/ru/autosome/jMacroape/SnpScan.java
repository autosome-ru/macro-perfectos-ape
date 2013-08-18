package ru.autosome.jMacroape;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.StringTokenizer;

public class SnpScan {
  double[] background;
  double discretization;
  Integer max_hash_size;

  public SnpScan() {
    background = Helper.wordwise_background();
    discretization = 100;
    max_hash_size = 10000000;
  }

  public HashMap<String, Object> parameters() {
    HashMap<String, Object> result = new HashMap<String,Object>();
    result.put("discretization", discretization);
    result.put("background", background);
    result.put("max_hash_size", max_hash_size);
    return result;
  }

  // Split by spaces and return last part
  // Input: "rs9929218 [Homo sapiens] GATTCAAAGGTTCTGAATTCCACAAC[a/g]GCTTTCCTGTGTTTTTGCAGCCAGA"
  // Output: "GATTCAAAGGTTCTGAATTCCACAAC[a/g]GCTTTCCTGTGTTTTTGCAGCCAGA"
  public static String last_part_of_string(String s) {
    String[] string_parts = s.replaceAll("\\s+"," ").split(" ");
    String result = string_parts[string_parts.length - 1];
    if (result.matches("[ACGT]+(/[ACGT]+)+") || result.matches("[ACGT]+\\[(/?[ACGT]+)+\\][ACGT]+")) {
      return result;
    } else {
      return string_parts[string_parts.length - 3] + "[" + string_parts[string_parts.length - 2].replaceAll("\\[|\\]","") + "]" + string_parts[string_parts.length - 1];
    }
  }
  // Output: "rs9929218"
  public static String first_part_of_string(String s) {
    return s.replaceAll("\\s+"," ").split(" ")[0];
  }

  public String pwm_influence_infos(SequenceWithSNP seq_w_snp, PWM pwm) {
    Sequence[] trimmed_sequence_variants = seq_w_snp.trimmed_sequence_variants(pwm);

    if (seq_w_snp.num_cases() != 2) return null; // Unable to process more than two variants(which fractions to return)

    String result = "";

    FindPvalue calculation = new FindPvalue(pwm);
    calculation.set_parameters(parameters());

    ScanSequence scan_seq_1 = new ScanSequence(trimmed_sequence_variants[0], pwm);
    double score_1 = scan_seq_1.best_score_on_sequence();
    double pvalue_1 = calculation.pvalue_by_threshold(score_1).pvalue;

    ScanSequence scan_seq_2 = new ScanSequence(trimmed_sequence_variants[1], pwm);
    double score_2 = scan_seq_2.best_score_on_sequence();
    double pvalue_2 = calculation.pvalue_by_threshold(score_2).pvalue;

    // We print position from the start of seq, not from the start of overlapping region, thus should calculate the shift
    int left_shift = seq_w_snp.left_shift(pwm.length());
    result = scan_seq_1.best_match_info_string(left_shift) + "\t" + pvalue_1 + "\t";
    result += scan_seq_2.best_match_info_string(left_shift) + "\t" + pvalue_2 + "\t";
    result += pvalue_2 / pvalue_1;
    return result;
  }

  public static ArrayList<PWM> load_collection_of_pwms(String dir_name, double[] background, boolean from_pcm) throws FileNotFoundException {
    ArrayList<PWM> result = new ArrayList<PWM>();
    java.io.File dir = new java.io.File(dir_name);
    for(File file: dir.listFiles()) {
      PWM pwm = PWM.new_from_file(file, background, from_pcm);
      if (pwm.name == null || pwm.name.isEmpty()) {
        pwm.name = file.getName();
      }
      result.add(pwm);
    }
    return result;
  }

  static String DOC =
          "Command-line format:\n" +
          "java ru.autosome.jMacroape.SnpScan <folder with pwms> <file with SNPs> <folder for results>\n"+
          "\n" +
          "Options:\n" +
          "  [-d <discretization level>]\n" +
          "  [--pcm] - treat the input file as Position Count Matrix. PCM-to-PWM transformation to be done internally.\n" +
          "  [-b <background probabilities] ACGT - 4 numbers, comma-delimited(spaces not allowed), sum should be equal to 1, like 0.25,0.24,0.26,0.25\n" +
          "\n" +
          "Example:\n" +
          "  java ru.autosome.jMacroape.SnpScan ./hocomoco/pwms/ snp.txt ./results\n" +
          "  java ru.autosome.jMacroape.SnpScan ./hocomoco/pcms/ snp.txt ./results --pcm -d 10\n";

  public static void main(String[] args) {
    try{
      ArrayList<String> argv = new ArrayList<String>();
      Collections.addAll(argv, args);

      if (argv.isEmpty()  || ArrayExtensions.contain(argv,"-h") || ArrayExtensions.contain(argv,"--h")
                          || ArrayExtensions.contain(argv,"-help") || ArrayExtensions.contain(argv,"--help")) {
        System.err.println(DOC);
        System.exit(1);
      }

      String path_to_collection_of_pwms = argv.remove(0);
      String path_to_file_w_snps = argv.remove(0);
      String path_to_results_folder = argv.remove(0);

      File results_folder = new File(path_to_results_folder);
      if (!results_folder.exists()) {
        results_folder.mkdir();
      }

      double[] background = {1.0, 1.0, 1.0, 1.0};
      SnpScan calculation = new SnpScan();

      String data_model = "pwm";

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
        } else if (opt.equals("--pcm")) {
          data_model = "pcm";
        } else {
          throw new IllegalArgumentException("Unknown option '" + opt + "'");
        }
      }

      calculation.background = background;
      ArrayList<PWM> collection = load_collection_of_pwms(path_to_collection_of_pwms, background, data_model.equals("pcm"));

      InputStream reader = new FileInputStream(path_to_file_w_snps);
      ArrayList<String> snp_list = InputExtensions.filter_empty_strings(InputExtensions.readLinesFromInputStream(reader));

      for(String snp_input: snp_list) {
        String snp_name = first_part_of_string(snp_input);
        SequenceWithSNP seq_w_snp = new SequenceWithSNP(last_part_of_string(snp_input));
        FileWriter fw;
        String resulting_filename = (path_to_results_folder + File.separator + snp_name + ".txt");
        fw = new FileWriter(new java.io.File(resulting_filename));
        try{
          System.out.println(snp_name);
          fw.write(seq_w_snp + "\n");
          fw.write("PWM-name\t||Normal pos\torientation\tword\tpvalue\t||Changed pos\torientation\tword\tpvalue\t||changed_pvalue/normal_pvalue\n");

          for(PWM pwm: collection) {
            String infos = calculation.pwm_influence_infos(seq_w_snp, pwm);
            if (infos != null) {
              fw.write(pwm.name + "\t" + infos + "\n");
            }
          }
        } finally {
          fw.close();
        }
      }
    } catch(Exception err) {
      System.err.println("\n" + err + "\n");
      err.printStackTrace();
      System.err.println("\n\nUse --help option for help\n\n" + DOC);
    }
  }

}
