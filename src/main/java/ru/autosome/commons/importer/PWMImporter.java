package ru.autosome.commons.importer;

import ru.autosome.commons.backgroundModel.mono.BackgroundModel;
import ru.autosome.commons.importer_two.matrixLoaders.ChIPMunkMatrixLoader;
import ru.autosome.commons.importer_two.matrixLoaders.NormalMatrixLoader;
import ru.autosome.commons.importer_two.matrixLoaders.TransposedMatrixLoader;
import ru.autosome.commons.model.Named;
import ru.autosome.commons.model.PseudocountCalculator;
import ru.autosome.commons.motifModel.mono.PCM;
import ru.autosome.commons.motifModel.mono.PPM;
import ru.autosome.commons.motifModel.mono.PWM;
import ru.autosome.commons.motifModel.types.DataModel;
import ru.autosome.commons.support.StringExtensions;

import java.util.List;

public class PWMImporter extends MotifImporter<PWM> {
  final DataModel dataModel;
  final Double effectiveCount;
  final PseudocountCalculator pseudocountCalculator;
  final BackgroundModel background;
  final boolean transpose;

  public PWMImporter() {
    this.dataModel = DataModel.PWM;
    this.effectiveCount = null;
    this.pseudocountCalculator = PseudocountCalculator.logPseudocount;
    this.background = null;
    this.transpose = false;
  }

  public PWMImporter(BackgroundModel background, DataModel dataModel, Double effectiveCount, boolean transpose, PseudocountCalculator pseudocount) {
    this.dataModel = dataModel;
    this.effectiveCount = effectiveCount;
    this.pseudocountCalculator = pseudocount;
    this.background = background;
    this.transpose = transpose;
  }

  // constructs PWM from any source: pwm/pcm/ppm matrix
  @Override
  public PWM createMotif(double matrix[][]) {
    PWM pwm;
    switch (dataModel) {
      case PCM:
        pwm = new PCM(matrix).to_pwm(background, pseudocountCalculator);
        break;
      case PPM:
        pwm = new PPM(matrix).to_pwm(background, effectiveCount, pseudocountCalculator);
        break;
      case PWM:
        pwm = new PWM(matrix);
        break;
      default:
        throw new Error("This code never reached");
    }
    return pwm;
  }

  public Named<double[][]> parse(List<String> strings) {
    if (StringExtensions.startWith(strings.get(0), "PROG|ru.autosome.ChIPMunk")) {
      return new ChIPMunkMatrixLoader(4, "ru.autosome.ChIPMunk", "PWMA").loadMatrix(strings);
    } else { // load basic matrix
      if (transpose) {
        // TODO: make help strings about transposition in exceptions
        return new TransposedMatrixLoader(4).loadMatrix(strings);
      } else {
        return new NormalMatrixLoader(4).loadMatrix(strings);
      }
    }
  }
}
