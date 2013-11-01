package ru.autosome.perfectosape.examples;

import ru.autosome.perfectosape.*;
import ru.autosome.perfectosape.api.PrecalculateThresholdLists;
import ru.autosome.perfectosape.calculations.CanFindPvalue;
import ru.autosome.perfectosape.calculations.SNPScan;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MultiSNPScan {
  public static void main(String[] args) {

    // Collection of PWMs to test on sequences
    List<PWM> pwmCollection = new ArrayList<PWM>();

    // One way is to load PWMs from files
    pwmCollection.add(PWM.fromParser(PMParser.from_file_or_stdin("test_data/pwm/KLF4_f2.pwm")));
    pwmCollection.add(PWM.fromParser(PMParser.from_file_or_stdin("test_data/pwm/SP1_f1.pwm")));

    // Another way is to create PWM by specifying (Nx4)-matrix and PWM name
    double[][] matrix_cAVNCT = { {1.0, 2.0, 1.0, 1.0},
                                {10.5, -3.0, 0.0, 0.0},
                                {5.0, 5.0, 5.0, -10.0},
                                {0.0, 0.0, 0.0, 0.0},
                                {-1.0, 10.5, -1.0, 0.0},
                                {0.0, 0.0, 0.0, 2.0} };
    PWM pwm_manual_constructed = new PWM(matrix_cAVNCT, "PWM for cAVNCt consensus sequence (name of PWM)");

    pwmCollection.add(pwm_manual_constructed);

    //////////////////////////////////////////////////////////////////////////////////////////////////

    // Now we should get collection of SNPs with sequence around them. Each SNP should have only two variants (if one need 3 or 4 variants, it's possible to add several SNPs: 1-2, 2-3, 1-3 for instance)
    // There are two ways to construct SNP: with constructor taking left part, array of nucleotide characters and right part as parameters
    // left and right part should be long enough to embed PWM (30 characters is enough for this implementation,
    // but may be in latter implementations we'll use more information so it's better to keep, say 50 nucleotides from each side where possible).
    // Another way is to construct object from string in this way:
    List<SequenceWithSNP> snpCollection = new ArrayList<SequenceWithSNP>();
    snpCollection.add(SequenceWithSNP.fromString("AAGGTCAATACTCAACATCATAAAAACAGACAAAAGTATAAAACTTACAG[C/G]GTCTTACAAAAAGGATGATCCAGTAATATGCTGCTTACAAGAAACCCACC"));
    snpCollection.add(new SequenceWithSNP("AGGGAAACAAAAATTGTTCGGAAAGGAGAACTAAGATGTATGAATGTTTC",
                                          new char[]{'G','T'},
                                          "TTTTTAAGTGAAAAGTGTATAGTTCAGAGTGTAATATTTATTACCAGTAT"));

    //////////////////////////////////////////////////////////////////////////////////////////////////

    // Reasonable defaults
    //    List of pvalues to be precalculated (One can rely on this default value)
    double[] pvalues = ru.autosome.perfectosape.calculations.PrecalculateThresholdList.PVALUE_LIST;
    // Higher discretization - better precision of score to Pvalue calculation and higher precalculation time
    // (given discretization is high enough, precalculation step though can take about a pair of minutes on large PWM collection)
    // But precalculation step should be done once
    double discretization = 10000;
    // Wordwise background means that we calculate number of words instead of probabilities, this is a default mode
    // If one need to work with certain nucleotide background probabilities he should use:
    // BackgroundModel background = new Background(pA, pC, pG, pT); where {pA,... pT} are probabilities of respective nucleotides
    BackgroundModel background = new WordwiseBackground();
    // A actual pvalue will be less than requested. Not very important setting in this task
    BoundaryType pvalue_boundary = BoundaryType.LOWER;
    // Setting max_hash_size allows one to restrict memory exhausting during Pvalue calculation (using APE algorithm, not binary search)
    // Reasonable value is about 10 millions elements. But actually only specially constructed matrices can reach such high hash sizes.
    // So in standalone calculations we use null (not to check hash size) and in web version we use 1e7
    Integer max_hash_size = null;

    //////////////////////////////////////////////////////////////////////////////////////////////////

    // Threshold precalculation step (you can skip it, read commented block just below this one)
    PrecalculateThresholdLists.Parameters listCalculationParams = new PrecalculateThresholdLists.Parameters(pwmCollection,
                                                                                                            pvalues,
                                                                                                            discretization,
                                                                                                            background,
                                                                                                            pvalue_boundary,
                                                                                                            max_hash_size);
    PrecalculateThresholdLists listCalculator = new PrecalculateThresholdLists(listCalculationParams);

    Map<PWM, CanFindPvalue> pwmCollectionWithPvalueCalculators = listCalculator.call();
    // Result of this step (pwmCollectionWithPvalueCalculators) should be cached. You need to do it once for a collection of PWMs
    // It carries PWMs of collection with their lists of precalculated values so latter calculations can perform binary search by threshold

    // In order to stop calculation one should do
    //    listCalculator.setStatus(Task.Status.INTERRUPTED);
    // In order to check status one can use
    //    listCalculator.getStatus() or listCalculator.completionPercent()

    //////////////////////////////////////////////////////////////////////////////////////////////////

/*
//    Skip threshold precalculation step
//
//    If one want to skip precalculation step (not recomended, use only for small sets of SNPs or for PWMs which are not in a collection)
//    one can store collection with objects calculating Pvalue from scratch (without storing precalculated lists)
//    Both ways are realized via the same interface so you can just replace previous block with this block

    Map<PWM,CanFindPvalue> pwmCollectionWithPvalueCalculators = new HashMap<PWM, CanFindPvalue>();
    for (PWM pwm: pwmCollection) {
      pwmCollectionWithPvalueCalculators.put(pwm, new FindPvalueAPE(pwm, discretization, background, max_hash_size));
    }
*/

    //////////////////////////////////////////////////////////////////////////////////////////////////

    // Scanning the pack of SNPs for changes of affinity on each PWM in collection using precalculated threshold - P-value lists
    ru.autosome.perfectosape.api.MultiSNPScan.Parameters scan_parameters = new ru.autosome.perfectosape.api.MultiSNPScan.Parameters(snpCollection,
                                                                                                                                    pwmCollectionWithPvalueCalculators);
    ru.autosome.perfectosape.api.MultiSNPScan scan_calculator = new ru.autosome.perfectosape.api.MultiSNPScan(scan_parameters);

    Map<PWM, Map<SequenceWithSNP, SNPScan.RegionAffinityInfos> > results = scan_calculator.call();

    //////////////////////////////////////////////////////////////////////////////////////////////////

    // output results
    for (PWM pwm: results.keySet()) {
      for (SequenceWithSNP seq: results.get(pwm).keySet()) {
        // RegionAffinityInfos is a special object carrying information about each allele form (in public variables RegionAffinityVariantInfo: info_1, info_2)
        // RegionAffinityVariantInfo carries type of allele, binding position and word under PWM, and Pvalue of binding of PWM to this site
        // (take a look at calculations.SNPScan.RegionAffinityInfos, calculations.SNPScan.RegionAffinityVariantInfo)
        SNPScan.RegionAffinityInfos affinityInfos = results.get(pwm).get(seq);
        System.out.println(pwm.name + " " + seq.toString() + " " + affinityInfos.toString());
      }
    }

  }
}
