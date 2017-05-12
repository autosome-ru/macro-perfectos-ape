package ru.autosome.perfectosape.example;


import ru.autosome.ape.calculation.PrecalculateThresholdList;
import ru.autosome.ape.calculation.findPvalue.CanFindPvalue;
import ru.autosome.ape.calculation.findPvalue.FindPvalueBsearch;
import ru.autosome.commons.backgroundModel.mono.BackgroundModel;
import ru.autosome.commons.backgroundModel.mono.WordwiseBackground;
import ru.autosome.commons.importer.PWMImporter;
import ru.autosome.commons.model.BoundaryType;
import ru.autosome.commons.model.Discretizer;
import ru.autosome.commons.model.Named;
import ru.autosome.commons.model.PseudocountCalculator;
import ru.autosome.commons.motifModel.mono.PPM;
import ru.autosome.commons.motifModel.mono.PWM;
import ru.autosome.commons.scoringModel.PWMOnBackground;
import ru.autosome.perfectosape.calculation.SingleSNPScan;
import ru.autosome.perfectosape.model.SequenceWithSNP;
import ru.autosome.perfectosape.model.encoded.mono.SequenceMonoEncoded;
import ru.autosome.perfectosape.model.encoded.mono.SequenceWithSNPMonoEncoded;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MultiSNPScan {
  public static void main(String[] args) {

    //////////////////////////////////////////////////////////////////////////////////////////////////

    // Reasonable defaults
    //    List of pvalues to be precalculated (One can rely on this default value)
    List<Double> pvalues = PrecalculateThresholdList.PVALUE_LIST;
    // Higher discretization - better precision of score to Pvalue calculation and higher precalculation time
    // (given discretization is high enough, precalculation step though can take about a pair of minutes on large PWM collection)
    // But precalculation step should be done once
    Discretizer discretizer = new Discretizer(10000.0);
    // Wordwise background means that we calculate number of words instead of probabilities, this is a default mode
    // If one need to work with certain nucleotide background probabilities he should use:
    // BackgroundModel background = new Background(new double[]{pA, pC, pG, pT}); where {pA,... pT} are probabilities of respective nucleotides
    BackgroundModel background = new WordwiseBackground();
    // A actual pvalue will be less than requested. Not very important setting in this task
    BoundaryType pvalue_boundary = BoundaryType.LOWER;

    // It sets effective count for ppm-->pcm (-->pwm) conversion when we load matrix from ppm
    double ppm_effective_count = 100;

    ///////////////////////////////////////////////////////////////////////////////////////////////////

    // Collection of PWMs to test on sequences
    List<Named<PWM>> pwmCollection = new ArrayList<Named<PWM>>();

    // One way is to load PWMs from files
    pwmCollection.add(new PWMImporter().loadMotifWithName("test_data/pwm/KLF4_f2.pwm"));
    pwmCollection.add(new PWMImporter().loadMotifWithName("test_data/pwm/SP1_f1.pwm"));

    // Another way is to create PWM by specifying (Nx4)-matrix and PWM name
    double[][] matrix_cAVNCT = { {1.0, 2.0, 1.0, 1.0},
                                {10.5, -3.0, 0.0, 0.0},
                                {5.0, 5.0, 5.0, -10.0},
                                {0.0, 0.0, 0.0, 0.0},
                                {-1.0, 10.5, -1.0, 0.0},
                                {0.0, 0.0, 0.0, 2.0} };
    Named<PWM> pwm_manual_constructed = new Named<PWM>(new PWM(matrix_cAVNCT),
                                                    "PWM for cAVNCt consensus sequence (name of PWM)");

    pwmCollection.add(pwm_manual_constructed);

    // PWM from PPM
    double[][] ppm_matrix = { {0.2, 0.4, 0.2, 0.2},
                             {0.9, 0, 0.05, 0.05},
                             {0.3, 0.3, 0.3, 0.1},
                             {0.25, 0.25, 0.25, 0.25},
                             {0, 0.9, 0, 0.1},
                             {0.2, 0.2, 0.2, 0.4} };
    Named<PPM> ppm = new Named<PPM>(new PPM(ppm_matrix),
                                 "cAVNCt PPM (slightly different from another cAVNCt matrix)");
    Named<PWM> pwm_from_ppm = new Named<PWM>(ppm.getObject().to_pwm(background, ppm_effective_count, PseudocountCalculator.logPseudocount),
                                          ppm.getName());
    pwmCollection.add(pwm_from_ppm);

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

    // Threshold precalculation step (you can skip it, read commented block just below this one)

    Map<String, CanFindPvalue> pwmCollectionWithPvalueCalculators = new HashMap<String, CanFindPvalue>();
    Map<String, PWM> pwmCollectionByName = new HashMap<String, PWM>();
    PrecalculateThresholdList<PWM, BackgroundModel> listCalculator;
    listCalculator = new PrecalculateThresholdList<PWM, BackgroundModel>(pvalues,
                                                     discretizer,
                                                     background,
                                                     pvalue_boundary);

    for (Named<PWM> motif: pwmCollection) {
      CanFindPvalue findPvalue = new FindPvalueBsearch(listCalculator.bsearch_list_for_pwm(motif.getObject()));
      pwmCollectionWithPvalueCalculators.put(motif.getName(), findPvalue);
      pwmCollectionByName.put(motif.getName(), motif.getObject());
    }

    // Result of this step (pwmCollectionWithPvalueCalculators) should be cached. You need to do it once for a collection of PWMs
    // It carries PWMs of collection with their lists of precalculated values so latter calculation can perform binary search by threshold

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
      pwmCollectionWithPvalueCalculators.put(pwm, new FindPvalueAPE(pwm, discretizer, background));
    }
*/

    //////////////////////////////////////////////////////////////////////////////////////////////////

    // Scanning the pack of SNPs for changes of affinity on each PWM in collection using precalculated threshold - P-value lists

    Map<String, Map<SequenceWithSNP, SingleSNPScan.RegionAffinityInfos> > results;
    results = new HashMap<String, Map<SequenceWithSNP, SingleSNPScan.RegionAffinityInfos>>();
    for (String pwmName: pwmCollectionWithPvalueCalculators.keySet()) {
      PWM pwm = pwmCollectionByName.get(pwmName);
      CanFindPvalue pvalueCalculator = pwmCollectionWithPvalueCalculators.get(pwmName);

      Map<SequenceWithSNP,SingleSNPScan.RegionAffinityInfos> result_part;
      result_part = new HashMap<SequenceWithSNP, SingleSNPScan.RegionAffinityInfos>();
      for (SequenceWithSNP sequenceWithSNP: snpCollection) {
        if (sequenceWithSNP.length() >= pwm.length()) {
          PWMOnBackground scoringModel = pwm.onBackground(new WordwiseBackground());
          result_part.put(sequenceWithSNP,
                          new SingleSNPScan<SequenceMonoEncoded, SequenceWithSNPMonoEncoded, PWMOnBackground>(scoringModel, sequenceWithSNP, pvalueCalculator, 0).affinityInfos());
        } else {
          System.err.println("Can't scan sequence '" + sequenceWithSNP + "' (length " + sequenceWithSNP.length() + ") with motif of length " + pwm.length());
        }
      }

      results.put(pwmName, result_part);
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    // output results
    for (String pwmName: results.keySet()) {
      for (SequenceWithSNP seq: results.get(pwmName).keySet()) {
        // RegionAffinityInfos is a special object carrying information about each allele form (in public variables RegionAffinityVariantInfo: info_1, info_2)
        // RegionAffinityVariantInfo carries type of allele, binding position and word under PWM, and Pvalue of binding of PWM to this site
        // (take a look at calculation.SingleSNPScan.RegionAffinityInfos, calculation.SingleSNPScan.RegionAffinityVariantInfo)
        SingleSNPScan.RegionAffinityInfos affinityInfos = results.get(pwmName).get(seq);
        System.out.println(pwmName + " " + seq.toString() + " " + affinityInfos.toString());
      }
    }

  }
}
