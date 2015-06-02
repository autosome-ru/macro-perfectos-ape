package ru.autosome.commons.importer;

import ru.autosome.commons.backgroundModel.di.DiBackgroundModel;
import ru.autosome.commons.backgroundModel.mono.Background;
import ru.autosome.commons.backgroundModel.mono.BackgroundModel;
import ru.autosome.commons.model.Named;
import ru.autosome.commons.model.PseudocountCalculator;
import ru.autosome.commons.motifModel.di.DiPWM;
import ru.autosome.commons.motifModel.types.DataModel;

import java.util.List;

public class DiPWMFromMonoImporter extends MotifImporter<DiPWM> {
  final PWMImporter monoImporter;

  public DiPWMFromMonoImporter(PWMImporter monoImporter) {
    this.monoImporter = monoImporter;
  }

  public DiPWMFromMonoImporter(DiBackgroundModel dibackground, DataModel dataModel, Double effectiveCount, boolean transpose, PseudocountCalculator pseudocount) {
    this.monoImporter = new PWMImporter(Background.fromDiBackground(dibackground), dataModel, effectiveCount, transpose, pseudocount);
  }

  public DiPWMFromMonoImporter(BackgroundModel background, DataModel dataModel, Double effectiveCount, boolean transpose, PseudocountCalculator pseudocount) {
    this.monoImporter = new PWMImporter(background, dataModel, effectiveCount, transpose, pseudocount);
  }

  @Override
  public DiPWM createMotif(double[][] matrix) {
    return DiPWM.fromPWM(monoImporter.createMotif(matrix));
  }

  @Override
  public Named<double[][]> parse(List<String> strings) {
    return monoImporter.parse(strings);
  }
}
