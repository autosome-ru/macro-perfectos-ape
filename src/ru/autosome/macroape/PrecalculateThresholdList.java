package ru.autosome.macroape;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.StringTokenizer;

public class PrecalculateThresholdList {
    private double discretization;
    private BackgroundModel background;
    private String pvalue_boundary;
    private int max_hash_size;
    private String data_model;

    private String collection_folder;
    private String output_folder;
    private double[] pvalues;

    private void initialize_defaults() {
        discretization = 1000;
        background = new WordwiseBackground();
        pvalue_boundary = "lower";
        max_hash_size = 10000000;
        pvalues = Helper.values_in_range_mul(1E-6, 0.3, 1.1);

        data_model = "pwm";
    }

    private PrecalculateThresholdList() {
        initialize_defaults();
    }

    private static PrecalculateThresholdList from_arglist(ArrayList<String> argv) {
        PrecalculateThresholdList result = new PrecalculateThresholdList();
        Helper.print_help_if_requested(argv, DOC);
        result.setup_from_arglist(argv);
        return result;
    }

    private static PrecalculateThresholdList from_arglist(String[] args) {
        ArrayList<String> argv = new ArrayList<String>();
        Collections.addAll(argv, args);
        return from_arglist(argv);
    }

    void setup_from_arglist(ArrayList<String> argv) {
        extract_collection_folder_name(argv);
        extract_output_folder_name(argv);

        while (argv.size() > 0) {
            extract_option(argv);
        }
        //load_pwm();
    }

    private FindThreshold find_threshold_calculator() {
        FindThreshold calculation = new FindThreshold();
        calculation.discretization = discretization;
        calculation.background = background;
        calculation.pvalue_boundary = pvalue_boundary;
        calculation.max_hash_size = max_hash_size;
        calculation.pvalues = pvalues;
        return calculation;
    }

    private void extract_collection_folder_name(ArrayList<String> argv) {
        try {
            collection_folder = argv.remove(0);
        } catch (IndexOutOfBoundsException e) {
            throw new IllegalArgumentException("Specify PWM-collection folder", e);
        }
    }

    private void extract_output_folder_name(ArrayList<String> argv) {
        try {
            output_folder = argv.remove(0);
        } catch (IndexOutOfBoundsException e) {
            throw new IllegalArgumentException("Specify output folder", e);
        }
    }

    private void extract_option(ArrayList<String> argv) {
        String opt = argv.remove(0);
        if (opt.equals("-b")) {
            background = Background.fromString(argv.remove(0));
        } else if (opt.equals("--pvalues")) {
            StringTokenizer parser = new StringTokenizer(argv.remove(0));
            double min_pvalue = Double.valueOf(parser.nextToken(","));
            double max_pvalue = Double.valueOf(parser.nextToken(","));
            double step = Double.valueOf(parser.nextToken(","));
            String progression_method = parser.nextToken();
            if (progression_method.equals("mul")) {
                pvalues = Helper.values_in_range_mul(min_pvalue, max_pvalue, step);
            } else if (progression_method.equals("add")) {
                pvalues = Helper.values_in_range_add(min_pvalue, max_pvalue, step);
            } else {
                throw new IllegalArgumentException("Progression method for pvalue-list is either add or mul, but you specified " + progression_method);
            }
        } else if (opt.equals("--max-hash-size")) {
            max_hash_size = Integer.valueOf(argv.remove(0));
        } else if (opt.equals("-d")) {
            discretization = Double.valueOf(argv.remove(0));
        } else if (opt.equals("--boundary")) {
            pvalue_boundary = argv.remove(0);
            if (!pvalue_boundary.equalsIgnoreCase("lower") &&
                    !pvalue_boundary.equalsIgnoreCase("upper")) {
                throw new IllegalArgumentException("boundary should be either lower or upper");
            }
        } else if (opt.equals("--pcm")) {
            data_model = "pcm";
        } else {
            throw new IllegalArgumentException("Unknown option '" + opt + "'");
        }
    }

    void calculate_thresholds_for_collection() {
        java.io.File dir = new File(collection_folder);
        java.io.File results_dir = new File(output_folder);
        if (!results_dir.exists()) {
            results_dir.mkdir();
        }
        for (File filename : dir.listFiles()) {
            System.err.println(filename);
            PWM pwm;
            if (data_model.equals("pcm")) {
                pwm = PCM.new_from_file(filename.getPath()).to_pwm(background);
            } else {
                pwm = PWM.new_from_file(filename.getPath());
            }

            FindThreshold calculation = find_threshold_calculator();
            calculation.pwm = pwm;

            ArrayList<ThresholdInfo> infos = calculation.find_thresholds_by_pvalues();
            File result_filename = new File(results_dir + File.separator + "thresholds_" + filename.getName());
            FindPvalueBsearch.new_from_threshold_infos(pwm, infos).save_to_file(result_filename.getPath());
        }
    }

    private static final String DOC =
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
            PrecalculateThresholdList calculation = PrecalculateThresholdList.from_arglist(args);
            calculation.calculate_thresholds_for_collection();
        } catch (Exception err) {
            System.err.println("\n" + err.getMessage() + "\n--------------------------------------\n");
            err.printStackTrace();
            System.err.println("\n--------------------------------------\nUse --help option for help\n\n" + DOC);
            System.exit(1);
        }
    }
}
