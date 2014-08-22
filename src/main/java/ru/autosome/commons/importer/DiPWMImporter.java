package ru.autosome.commons.importer;

import ru.autosome.commons.backgroundModel.di.DiBackgroundModel;
import ru.autosome.commons.model.PseudocountCalculator;
import ru.autosome.commons.motifModel.di.DiPCM;
import ru.autosome.commons.motifModel.di.DiPPM;
import ru.autosome.commons.motifModel.di.DiPWM;
import ru.autosome.commons.motifModel.types.DataModel;
import ru.autosome.commons.support.StringExtensions;

import java.util.List;

public class DiPWMImporter extends MotifImporter<DiPWM, DiBackgroundModel> {
  final boolean transpose;

  public DiPWMImporter() {
    super(null, DataModel.PWM, null, PseudocountCalculator.logPseudocount);
    this.transpose = false;
  }

  public DiPWMImporter(DiBackgroundModel background, DataModel dataModel, Double effectiveCount, boolean transpose, PseudocountCalculator pseudocount) {
    super(background, dataModel, effectiveCount, pseudocount);
    this.transpose = transpose;
  }

  // constructs DiPWM from any source: pwm/pcm/ppm matrix
  @Override
  public DiPWM createMotif(double matrix[][], String name) {
    DiPWM dipwm;
    switch (dataModel) {
      case PCM:
        dipwm = new DiPCM(matrix, name).to_pwm(background, pseudocountCalculator);
        break;
      case PPM:
        dipwm = new DiPPM(matrix, name).to_pwm(background, effectiveCount, pseudocountCalculator);
        break;
      case PWM:
        dipwm = new DiPWM(matrix, name);
        break;
      default:
        throw new Error("This code never reached");
    }
    return dipwm;
  }

  public ParsingResult parse(List<String> strings) {
    if (StringExtensions.startWith(strings.get(0), "PROG|ru.autosome.di.ChIPMunk")) {
      return new ChIPMunkParser(16, "ru.autosome.di.ChIPMunk", "PWAA").parse(strings);
    } else { // load basic matrix
      if (transpose) {
        return new TransposedMatrixParser(16).parse(strings);
      } else {
        return new NormalMatrixParser(16).parse(strings);
      }
    }
  }


}
