package ru.autosome.macroape;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

public class SNPScan {
    BackgroundModel background;
    double discretization;
    Integer max_hash_size;

    public SNPScan() {
        background = new WordwiseBackground();
        discretization = 100;
        max_hash_size = 10000000;
    }

    public HashMap<String, Object> parameters() {
        HashMap<String, Object> result = new HashMap<String, Object>();
        result.put("discretization", discretization);
        result.put("background", background);
        result.put("max_hash_size", max_hash_size);
        return result;
    }

    // Split by spaces and return last part
    // Input: "rs9929218 [Homo sapiens] GATTCAAAGGTTCTGAATTCCACAAC[a/g]GCTTTCCTGTGTTTTTGCAGCCAGA"
    // Output: "GATTCAAAGGTTCTGAATTCCACAAC[a/g]GCTTTCCTGTGTTTTTGCAGCCAGA"
    public static String last_part_of_string(String s) {
        String[] string_parts = s.replaceAll("\\s+", " ").split(" ");
        String result = string_parts[string_parts.length - 1];
        if (result.matches("[ACGT]+(/[ACGT]+)+") || result.matches("[ACGT]+\\[(/?[ACGT]+)+\\][ACGT]+")) {
            return result;
        } else {
            return string_parts[string_parts.length - 3] + "[" + string_parts[string_parts.length - 2].replaceAll("\\[|\\]", "") + "]" + string_parts[string_parts.length - 1];
        }
    }

    // Output: "rs9929218"
    public static String first_part_of_string(String s) {
        return s.replaceAll("\\s+", " ").split(" ")[0];
    }

    public String pwm_influence_infos(SequenceWithSNP seq_w_snp, PWM pwm, CanFindPvalue pvalue_calculation) {
        Sequence[] trimmed_sequence_variants = seq_w_snp.trimmed_sequence_variants(pwm);

        if (seq_w_snp.num_cases() != 2)
            return null; // Unable to process more than two variants(which fractions to return)

        String result = "";

        ScanSequence scan_seq_1 = new ScanSequence(trimmed_sequence_variants[0], pwm);
        double score_1 = scan_seq_1.best_score_on_sequence();
        double pvalue_1 = pvalue_calculation.pvalue_by_threshold(score_1).pvalue;

        ScanSequence scan_seq_2 = new ScanSequence(trimmed_sequence_variants[1], pwm);
        double score_2 = scan_seq_2.best_score_on_sequence();
        double pvalue_2 = pvalue_calculation.pvalue_by_threshold(score_2).pvalue;
        // We print position from the start of seq, not from the start of overlapping region, thus should calculate the shift
        int left_shift = seq_w_snp.left_shift(pwm.length());
        result = scan_seq_1.best_match_info_string(left_shift) + "\t" + pvalue_1 + "\t";
        result += scan_seq_2.best_match_info_string(left_shift) + "\t" + pvalue_2 + "\t";
        result += pvalue_2 / pvalue_1;
        return result;
    }

    public static ArrayList<PwmWithFilename> load_collection_of_pwms(String dir_name) throws FileNotFoundException {
        ArrayList<PwmWithFilename> result = new ArrayList<PwmWithFilename>();
        java.io.File dir = new java.io.File(dir_name);
        for (File file : dir.listFiles()) {
            PWM pwm = PWM.new_from_file(file);
            if (pwm.name == null || pwm.name.isEmpty()) {
                pwm.name = file.getName();
            }
            result.add(new PwmWithFilename(pwm, file.getPath()));
        }
        return result;
    }

    public static ArrayList<PwmWithFilename> load_collection_of_pwms_from_pcms(String dir_name, BackgroundModel background) throws FileNotFoundException {
        ArrayList<PwmWithFilename> result = new ArrayList<PwmWithFilename>();
        java.io.File dir = new java.io.File(dir_name);
        for (File file : dir.listFiles()) {
            PWM pwm = PCM.new_from_file(file).to_pwm(background);
            if (pwm.name == null || pwm.name.isEmpty()) {
                pwm.name = file.getName();
            }
            result.add(new PwmWithFilename(pwm, file.getPath()));
        }
        return result;
    }

    static String DOC =
            "Command-line format:\n" +
                    "java ru.autosome.macroape.SNPScan <folder with pwms> <file with SNPs> <folder for results>\n" +
                    "\n" +
                    "Options:\n" +
                    "  [-d <discretization level>]\n" +
                    "  [--pcm] - treat the input file as Position Count Matrix. PCM-to-PWM transformation to be done internally.\n" +
                    "  [-b <background probabilities] ACGT - 4 numbers, comma-delimited(spaces not allowed), sum should be equal to 1, like 0.25,0.24,0.26,0.25\n" +
                    "  [--precalc <folder>] - specify folder with thresholds for PWM collection (for fast-and-rough calculation).\n" +
                    "\n" +
                    "Example:\n" +
                    "  java ru.autosome.macroape.SNPScan ./hocomoco/pwms/ snp.txt ./results --precalc ./collection_thresholds\n" +
                    "  java ru.autosome.macroape.SNPScan ./hocomoco/pcms/ snp.txt ./results --pcm -d 10\n";

    public static void main(String[] args) {
        try {
            ArrayList<String> argv = new ArrayList<String>();
            Collections.addAll(argv, args);

            if (argv.isEmpty() || ArrayExtensions.contain(argv, "-h") || ArrayExtensions.contain(argv, "--h")
                    || ArrayExtensions.contain(argv, "-help") || ArrayExtensions.contain(argv, "--help")) {
                System.err.println(DOC);
                System.exit(1);
            }

            String path_to_collection_of_pwms;
            String path_to_file_w_snps;
            String path_to_results_folder;
            try {
                path_to_collection_of_pwms = argv.remove(0);
            } catch (IndexOutOfBoundsException e) {
                throw new IllegalArgumentException("Specify PWM-collection folder", e);
            }
            try {
                path_to_file_w_snps = argv.remove(0);
            } catch (IndexOutOfBoundsException e) {
                throw new IllegalArgumentException("Specify file with SNPs", e);
            }

            try {
                path_to_results_folder = argv.remove(0);
            } catch (IndexOutOfBoundsException e) {
                throw new IllegalArgumentException("Specify output folder", e);
            }

            File results_folder = new File(path_to_results_folder);
            if (!results_folder.exists()) {
                results_folder.mkdir();
            }

            BackgroundModel background = new WordwiseBackground();
            SNPScan calculation = new SNPScan();

            String data_model = "pwm";
            String thresholds_folder = null;


            while (argv.size() > 0) {
                String opt = argv.remove(0);
                if (opt.equals("-b")) {
                    background = Background.fromString(argv.remove(0));
                } else if (opt.equals("--max-hash-size")) {
                    calculation.max_hash_size = Integer.valueOf(argv.remove(0));
                } else if (opt.equals("-d")) {
                    calculation.discretization = Double.valueOf(argv.remove(0));
                } else if (opt.equals("--pcm")) {
                    data_model = "pcm";
                } else if (opt.equals("--precalc")) {
                    thresholds_folder = argv.remove(0);
                } else {
                    throw new IllegalArgumentException("Unknown option '" + opt + "'");
                }
            }

            calculation.background = background;
            ArrayList<PwmWithFilename> collection;
            try {
                if (data_model.equals("pcm")) {
                    collection = load_collection_of_pwms_from_pcms(path_to_collection_of_pwms, background);
                } else {
                    collection = load_collection_of_pwms(path_to_collection_of_pwms);
                }
            } catch (Exception e) {
                throw new IllegalArgumentException("Failed to load collection of PWMs", e);
            }
            for (PwmWithFilename pwm : collection) {
                CanFindPvalue pvalue_calculation;
                if (thresholds_folder == null) {
                    pvalue_calculation = new FindPvalueAPE(pwm.pwm);
                } else {
                    String filename = thresholds_folder + File.separator + "thresholds_" + (new File(pwm.filename)).getName();
                    pvalue_calculation = FindPvalueBsearch.load_from_file(pwm.pwm, filename);
                }
                pvalue_calculation.set_parameters(calculation.parameters());
                pwm.pvalue_calculation = pvalue_calculation;
            }

            ArrayList<String> snp_list;
            try {
                InputStream reader = new FileInputStream(path_to_file_w_snps);
                snp_list = InputExtensions.filter_empty_strings(InputExtensions.readLinesFromInputStream(reader));
            } catch (Exception e) {
                throw new IllegalArgumentException("Failed to load pack of SNPs", e);
            }

            for (String snp_input : snp_list) {
                String snp_name = first_part_of_string(snp_input);
                SequenceWithSNP seq_w_snp = new SequenceWithSNP(last_part_of_string(snp_input));
                FileWriter fw;
                String resulting_filename = (path_to_results_folder + File.separator + snp_name + ".txt");
                fw = new FileWriter(new java.io.File(resulting_filename));
                try {
                    System.out.println(snp_name);
                    fw.write(seq_w_snp + "\n");
                    fw.write("PWM-name\t||Normal pos\torientation\tword\tpvalue\t||Changed pos\torientation\tword\tpvalue\t||changed_pvalue/normal_pvalue\n");

                    for (PwmWithFilename pwm : collection) {
                        String infos = calculation.pwm_influence_infos(seq_w_snp, pwm.pwm, pwm.pvalue_calculation);
                        if (infos != null) {
                            fw.write(pwm.pwm.name + "\t" + infos + "\n");
                        }
                    }
                } finally {
                    fw.close();
                }
            }
        } catch (Exception err) {
            System.err.println("\n" + err.getMessage() + "\n--------------------------------------\n");
            err.printStackTrace();
            System.err.println("\n--------------------------------------\nUse --help option for help\n\n" + DOC);
            System.exit(1);

        }
    }

}
