package ru.autosome.commons.importer;

import ru.autosome.commons.backgroundModel.mono.BackgroundModel;
import ru.autosome.commons.model.PseudocountCalculator;
import ru.autosome.commons.motifModel.mono.PCM;
import ru.autosome.commons.motifModel.mono.PPM;
import ru.autosome.commons.motifModel.mono.PWM;
import ru.autosome.commons.motifModel.types.DataModel;
import ru.autosome.commons.support.StringExtensions;

import java.util.List;

public class PWMImporter extends MotifImporter<PWM, BackgroundModel> {
  final boolean transpose;

  public PWMImporter() {
    super(null, DataModel.PWM, null, PseudocountCalculator.logPseudocount);
    this.transpose = false;
  }

  public PWMImporter(BackgroundModel background, DataModel dataModel, Double effectiveCount, boolean transpose, PseudocountCalculator pseudocount) {
    super(background, dataModel, effectiveCount, pseudocount);
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

  public ParsingResult parse(List<String> strings) {
    if (StringExtensions.startWith(strings.get(0), "PROG|ru.autosome.ChIPMunk")) {
      return new ChIPMunkParser(4, "ru.autosome.ChIPMunk", "PWMA").parse(strings);
    } else { // load basic matrix
      if (transpose) {
        // TODO: make help strings about transposition in exceptions
        return new TransposedMatrixParser(4).parse(strings);
      } else {
        return new NormalMatrixParser(4).parse(strings);
      }
    }
  }
}
