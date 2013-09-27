package ru.autosome.macroape;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

public class FindPvalue {
    static String DOC =
            "Command-line format:\n" +
                    "java ru.autosome.macroape.FindPvalue <pat-file> <threshold list>... [options]\n" +
                    "\n" +
                    "Options:\n" +
                    "  [-d <discretization level>]\n" +
                    "  [--pcm] - treat the input file as Position Count Matrix. PCM-to-PWM transformation to be done internally.\n" +
                    "  [-b <background probabilities] ACGT - 4 numbers, comma-delimited(spaces not allowed), sum should be equal to 1, like 0.25,0.24,0.26,0.25\n" +
                    "  [--precalc <folder>] - specify folder with thresholds for PWM collection (for fast-and-rough calculation).\n" +
                    "\n" +
                    "Examples:\n" +
                    "  java ru.autosome.macroape.FindPvalue motifs/KLF4_f2.pat 7.32\n" +
                    "  java ru.autosome.macroape.FindPvalue motifs/KLF4_f2.pat 7.32 4.31 5.42 -d 1000 -b 0.2,0.3,0.3,0.2\n";

    public static void main(String[] args) {
        try {
            ArrayList<String> argv = new ArrayList<String>();
            Collections.addAll(argv, args);

            if (argv.isEmpty() || ArrayExtensions.contain(argv, "-h") || ArrayExtensions.contain(argv, "--h")
                    || ArrayExtensions.contain(argv, "-help") || ArrayExtensions.contain(argv, "--help")) {
                System.err.println(DOC);
                System.exit(1);
            }

            double discretization = 10000.0;
            BackgroundModel background = new WordwiseBackground();
            ArrayList<Double> thresholds_list = new ArrayList<Double>();
            Integer max_hash_size = 10000000;
            String data_model = "pwm";
            String thresholds_folder = null;

            if (argv.isEmpty()) {
                throw new IllegalArgumentException("No input. You should specify input file");
            }
            String filename = argv.remove(0);

            try {
                while (!argv.isEmpty()) {
                    thresholds_list.add(Double.valueOf(argv.get(0)));
                    argv.remove(0);
                }
            } catch (NumberFormatException e) {
            }
            if (thresholds_list.isEmpty()) {
                throw new IllegalArgumentException("You should specify at least one threshold");
            }
            double[] thresholds = ArrayExtensions.toPrimitiveArray(thresholds_list);

            while (argv.size() > 0) {
                String opt = argv.remove(0);
                if (opt.equals("-b")) {
                    background = Background.fromString(argv.remove(0));
                } else if (opt.equals("--max-hash-size")) {
                    max_hash_size = Integer.valueOf(argv.remove(0));
                } else if (opt.equals("-d")) {
                    discretization = Double.valueOf(argv.remove(0));
                } else if (opt.equals("--pcm")) {
                    data_model = "pcm";
                } else if (opt.equals("--precalc")) {
                    thresholds_folder = argv.remove(0);
                } else {
                    throw new IllegalArgumentException("Unknown option '" + opt + "'");
                }
            }

            PWM pwm;
            if (data_model.equals("pcm")) {
                pwm = PCM.new_from_file_or_stdin(filename).to_pwm(background);
            } else {
                pwm = PWM.new_from_file_or_stdin(filename);
            }

            CanFindPvalue calculation;
            if (thresholds_folder != null) {
                filename = thresholds_folder + File.separator + "thresholds_" + (new File(filename)).getName();
                calculation = FindPvalueBsearch.load_from_file(pwm, filename);
            } else {
                calculation = new FindPvalueAPE(pwm);
            }

            calculation.set_discretization(discretization);
            calculation.set_background(background);
            calculation.set_max_hash_size(max_hash_size);

            HashMap<String, Object> parameters = calculation.parameters();

            ArrayList<PvalueInfo> infos = calculation.pvalues_by_thresholds(thresholds);

            System.out.println(Helper.find_pvalue_info_string(infos, parameters));

        } catch (Exception err) {
            System.err.println("\n" + err.getMessage() + "\n--------------------------------------\n");
            err.printStackTrace();
            System.err.println("\n--------------------------------------\nUse --help option for help\n\n" + DOC);
            System.exit(1);
        }

    }

}
