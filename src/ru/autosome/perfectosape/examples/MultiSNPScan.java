package ru.autosome.perfectosape.examples;

import ru.autosome.perfectosape.*;
import ru.autosome.perfectosape.api.PrecalculateThresholdLists;
import ru.autosome.perfectosape.calculations.CanFindPvalue;
import ru.autosome.perfectosape.calculations.SNPScan;

import java.util.Map;

public class MultiSNPScan {
  public static void main(String[] args) {
    PWM[] pwms = new PWM[2];
    pwms[0] = PWM.fromParser(PMParser.from_file_or_stdin("test_data/pwm/KLF4_f2.pwm"));
    pwms[1] = PWM.fromParser(PMParser.from_file_or_stdin("test_data/pwm/SP1_f1.pwm"));
    double[] pvalues = ru.autosome.perfectosape.calculations.PrecalculateThresholdList.PVALUE_LIST;
    double discretization = 10000;
    BackgroundModel background = new WordwiseBackground();
    BoundaryType pvalue_boundary = BoundaryType.LOWER;
    Integer max_hash_size = null;

    PrecalculateThresholdLists.Parameters list_calculation_params = new PrecalculateThresholdLists.Parameters(pwms,
                                                                                                              pvalues,
                                                                                                              discretization,
                                                                                                              background,
                                                                                                              pvalue_boundary,
                                                                                                              max_hash_size);
    PrecalculateThresholdLists listCalculator = new PrecalculateThresholdLists(list_calculation_params);
    Map<PWM, CanFindPvalue> collection = listCalculator.launch(); // This data should be cached

    SequenceWithSNP[] snps = new SequenceWithSNP[2]; // TODO: make List
    snps[0] = SequenceWithSNP.fromString("AAGGTCAATACTCAACATCATAAAAACAGACAAAAGTATAAAACTTACAG[C/G]GTCTTACAAAAAGGATGATCCAGTAATATGCTGCTTACAAGAAACCCACC");
    snps[1] = SequenceWithSNP.fromString("AGGGAAACAAAAATTGTTCGGAAAGGAGAACTAAGATGTATGAATGTTTC[G/T]TTTTTAAGTGAAAAGTGTATAGTTCAGAGTGTAATATTTATTACCAGTAT");


    ru.autosome.perfectosape.api.MultiSNPScan.Parameters scan_parameters =
     new ru.autosome.perfectosape.api.MultiSNPScan.Parameters(snps, collection);

    ru.autosome.perfectosape.api.MultiSNPScan scan_calculator = new ru.autosome.perfectosape.api.MultiSNPScan(scan_parameters);
    Map<PWM, Map<SequenceWithSNP, SNPScan.RegionAffinityInfos> > results = scan_calculator.launch();


    for (PWM pwm: results.keySet()) {
      for (SequenceWithSNP seq: results.get(pwm).keySet()) {
        SNPScan.RegionAffinityInfos affinityInfos = results.get(pwm).get(seq);
        System.out.println(pwm.name + " " + seq.toString() + " " + affinityInfos.toString());
      }
    }
  }


}
