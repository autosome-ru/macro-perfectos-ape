package ru.autosome.perfectosape.di;

import ru.autosome.commons.backgroundModel.di.DiBackground;
import ru.autosome.commons.backgroundModel.di.DiBackgroundModel;
import ru.autosome.commons.backgroundModel.di.DiWordwiseBackground;
import ru.autosome.commons.backgroundModel.mono.Background;
import ru.autosome.commons.backgroundModel.mono.BackgroundModel;
import ru.autosome.commons.cli.Helper;
import ru.autosome.commons.importer.DiPWMImporter;
import ru.autosome.commons.importer.PWMImporter;
import ru.autosome.commons.motifModel.di.DiPWM;
import ru.autosome.commons.motifModel.mono.PWM;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SNPScan extends ru.autosome.perfectosape.cli.generalized.SNPScan<DiPWM, DiBackgroundModel> {
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
  protected List<DiPWM> load_collection_of_pwms() {
    if (fromMononucleotide) {
      BackgroundModel backgroundMononucleotide = Background.fromDiBackground(background);
      PWMImporter importer = new PWMImporter(backgroundMononucleotide, dataModel, effectiveCount, transpose, pseudocount);
      List<PWM> monoCollection = importer.loadMotifCollection(path_to_collection_of_pwms);
      List<DiPWM> diCollection = new ArrayList<DiPWM>(monoCollection.size());
      for(PWM monoPWM: monoCollection) {
        diCollection.add(DiPWM.fromPWM(monoPWM));
      }
      return diCollection;
    } else {
      DiPWMImporter importer = new DiPWMImporter(background, dataModel, effectiveCount, transpose, pseudocount);
      return importer.loadMotifCollection(path_to_collection_of_pwms);
    }
  }

  protected static ru.autosome.perfectosape.cli.generalized.SNPScan from_arglist(ArrayList<String> argv) {
    ru.autosome.perfectosape.di.SNPScan result = new ru.autosome.perfectosape.di.SNPScan();
    Helper.print_help_if_requested(argv, result.documentString());
    result.setup_from_arglist(argv);
    return result;
  }

  protected static ru.autosome.perfectosape.cli.generalized.SNPScan from_arglist(String[] args) {
    ArrayList<String> argv = new ArrayList<String>();
    Collections.addAll(argv, args);
    return from_arglist(argv);
  }

  public static void main(String[] args) {
    try {
      ru.autosome.perfectosape.cli.generalized.SNPScan calculation = ru.autosome.perfectosape.di.SNPScan.from_arglist(args);
      calculation.process();
    } catch (Exception err) {
      System.err.println("\n" + err.getMessage() + "\n--------------------------------------\n");
      err.printStackTrace();
      System.err.println("\n--------------------------------------\nUse --help option for help\n\n" + new ru.autosome.perfectosape.di.SNPScan().documentString());
      System.exit(1);

    }
  }
}
