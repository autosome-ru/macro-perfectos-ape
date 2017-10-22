package ru.autosome.macroape.di;

import ru.autosome.commons.backgroundModel.di.DiBackground;
import ru.autosome.commons.backgroundModel.di.DiBackgroundModel;
import ru.autosome.commons.backgroundModel.di.DiWordwiseBackground;
import ru.autosome.commons.importer.DiPWMFromMonoImporter;
import ru.autosome.commons.importer.DiPWMImporter;
import ru.autosome.commons.importer.MotifImporter;
import ru.autosome.commons.model.Named;
import ru.autosome.commons.motifModel.di.DiPWM;
import ru.autosome.macroape.calculation.di.AlignedModelIntersection;
import ru.autosome.macroape.model.PairAligned;
import ru.autosome.macroape.model.ScanningSimilarityInfo;

import java.io.FileNotFoundException;
import java.util.List;
import java.util.function.Function;

public class ScanCollection extends ru.autosome.macroape.cli.generalized.ScanCollection<DiPWM, DiBackgroundModel> {

  @Override
  protected String DOC_background_option() {
    return "ACGT - 16 numbers, comma-delimited(spaces not allowed), sum should be equal to 1, like 0.02,0.03,0.03,0.02,0.08,0.12,0.12,0.08,0.08,0.12,0.12,0.08,0.02,0.03,0.03,0.02";
  }
  @Override
  protected String DOC_run_string() {
    return "java ru.autosome.macroape.di.ScanCollection";
  }

  @Override
  protected String DOC_additional_options() {
    return "  [--query-from-mono]      - obtain query DiPWM from mono PWM/PCM/PPM.\n" +
           "  [--collection-from-mono] - obtain collection DiPWMs from mono PWM/PCM/PPMs.\n";
  }

  boolean queryFromMononucleotide, collectionFromMononucleotide;

  @Override
  protected void initialize_defaults() {
    super.initialize_defaults();
    queryFromMononucleotide = false;
    collectionFromMononucleotide = false;
  }

  @Override
  protected boolean failed_to_recognize_additional_options(String opt, List<String> argv) {
    if (opt.equals("--query-from-mono")) {
      queryFromMononucleotide = true;
      return false;
    } else if (opt.equals("--collection-from-mono")) {
      collectionFromMononucleotide = true;
      return false;
    } else {
      return true;
    }
  }

  @Override
  protected DiBackgroundModel extractBackground(String str) {
    return DiBackground.fromString(str);
  }

  @Override
  protected void initialize_default_background() {
    background = new DiWordwiseBackground();
  }

  private static ScanCollection from_arglist(String[] args) throws FileNotFoundException {
    ScanCollection result = new ScanCollection();
    result.setup_from_arglist(args);
    return result;
  }

  @Override
  protected List<Named<DiPWM>> loadMotifCollection() {
    MotifImporter<DiPWM> importer;
    if (collectionFromMononucleotide) {
      importer = new DiPWMFromMonoImporter(background, collectionDataModel, collectionEffectiveCount, collectionTranspose, collectionPseudocount);
    } else {
      importer = new DiPWMImporter(background, collectionDataModel, collectionEffectiveCount, collectionTranspose, collectionPseudocount);
    }
    return importer.loadMotifCollectionWithNames(pathToCollectionOfPWMs);
  }

  @Override
  protected DiPWM loadQueryMotif() {
    MotifImporter<DiPWM> importer;
    if (queryFromMononucleotide) {
      importer = new DiPWMFromMonoImporter(background, queryDataModel, queryEffectiveCount, queryTranspose, queryPseudocount);
    } else {
      importer = new DiPWMImporter(background, queryDataModel, queryEffectiveCount, queryTranspose, queryPseudocount);
    }
    return importer.loadMotif(queryPMFilename);
  }

  @Override
  protected Function<PairAligned<DiPWM>, AlignedModelIntersection> calc_alignment() {
    return (PairAligned<DiPWM> alignment) -> new AlignedModelIntersection(alignment, background);
  }

  public static void main(String[] args) {
    try {
      ScanCollection calculation = ScanCollection.from_arglist(args);
      List<ScanningSimilarityInfo> infos = calculation.process();
      System.out.println(calculation.report(infos));
    } catch (Exception err) {
      System.err.println("\n" + err.getMessage() + "\n--------------------------------------\n");
      err.printStackTrace();
      System.err.println("\n--------------------------------------\nUse --help option for help\n\n" + new ScanCollection().documentString());
      System.exit(1);
    }
  }
}
