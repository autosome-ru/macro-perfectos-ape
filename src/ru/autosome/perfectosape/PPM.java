package ru.autosome.perfectosape;

public class PPM extends PM {
  public PPM(double[][] matrix, String name) throws IllegalArgumentException {
    super(matrix, name);
  }

  public PWM to_pwm(BackgroundModel background) {
    PPM2PWMConverter converter = new PPM2PWMConverter(this);
    converter.background = background;
    return converter.convert();
  }

  public static PPM fromParser(PMParser parser) {
    if (parser == null)  return null;
    double[][] matrix = parser.matrix();
    String name = parser.name();
    return new PPM(matrix, name);
  }
}
