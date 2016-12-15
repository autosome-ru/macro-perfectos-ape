package ru.autosome.perfectosape.di;

import ru.autosome.commons.backgroundModel.di.DiBackground;
import ru.autosome.commons.backgroundModel.di.DiBackgroundModel;
import ru.autosome.commons.backgroundModel.di.DiWordwiseBackground;
import ru.autosome.commons.importer.DiPWMFromMonoImporter;
import ru.autosome.commons.importer.DiPWMImporter;
import ru.autosome.commons.importer.MotifImporter;
import ru.autosome.commons.model.Named;
import ru.autosome.commons.motifModel.di.DiPWM;
import ru.autosome.commons.scoringModel.DiPWMOnBackground;
import ru.autosome.perfectosape.model.SequenceWithSNP;
import ru.autosome.perfectosape.model.encoded.di.SequenceDiEncoded;
import ru.autosome.perfectosape.model.encoded.di.SequenceWithSNPDiEncoded;

import java.util.List;

public class SNPScan extends ru.autosome.perfectosape.cli.generalized.SNPScan<SequenceDiEncoded, SequenceWithSNPDiEncoded, DiPWM, DiPWMOnBackground, DiBackgroundModel> {
  @Override
  protected String DOC_run_string(){
    return "java ru.autosome.perfectosape.di.SNPScan";
  }
  @Override
  protected String DOC_background_option() {
    return "ACGT - 16 numbers, comma-delimited(spaces not allowed), sum should be equal to 1, like 0.02,0.03,0.03,0.02,0.08,0.12,0.12,0.08,0.08,0.12,0.12,0.08,0.02,0.03,0.03,0.02";
  }

  @Override
  protected String DOC_additional_options() {
    return "  [--from-mono] - obtain collection DiPWMs from mono PWM/PCM/PPMs.\n";
  }

  boolean fromMononucleotide;

  @Override
  protected void initialize_defaults() {
    super.initialize_defaults();
    fromMononucleotide = false;
  }

  @Override
  protected boolean failed_to_recognize_additional_options(String opt, List<String> argv) {
    if (opt.equals("--from-mono")) {
      fromMononucleotide = true;
      return false;
    } else {
      return true;
    }
  }

  private SNPScan() {
    super();
  }

  @Override
  protected void extract_background(String str) {
    background = DiBackground.fromString(str);
  }
  @Override
  protected void initialize_default_background() {
    background = new DiWordwiseBackground();
  }

  @Override
  protected List<Named<DiPWM>> load_collection_of_pwms() {
    MotifImporter<DiPWM> importer;
    if (fromMononucleotide) {
      importer = new DiPWMFromMonoImporter(background, dataModel, effectiveCount, transpose, pseudocount);
    } else {
      importer = new DiPWMImporter(background, dataModel, effectiveCount, transpose, pseudocount);
    }
    return importer.loadMotifCollectionWithNames(path_to_collection_of_pwms);
  }

  protected SequenceWithSNPDiEncoded encodeSequenceWithSNV(SequenceWithSNP sequenceWithSNV){
    return sequenceWithSNV.diEncode();
  }

  protected static ru.autosome.perfectosape.cli.generalized.SNPScan from_arglist(String[] args) {
    ru.autosome.perfectosape.di.SNPScan result = new ru.autosome.perfectosape.di.SNPScan();
    result.setup_from_arglist(args);
    return result;
  }

  public static void main(String[] args) {
    try {
      ru.autosome.perfectosape.cli.generalized.SNPScan calculation = ru.autosome.perfectosape.di.SNPScan.from_arglist(args);
      calculation.process();
    } catch (Exception err) {
      System.err.println("\n" + err.getMessage() + "\n--------------------------------------\n");
      err.printStackTrace();
      System.err.println("\n--------------------------------------\nUse --help option for help\n\n" + new SNPScan().documentString());
      System.exit(1);

    }
  }
}
