package ru.autosome.perfectosape.cli;

import ru.autosome.perfectosape.*;

import java.io.File;
import java.util.ArrayList;

public class Helper {
  public static void print_help_if_requested(ArrayList<String> argv, String doc) {
    if (argv.isEmpty() || ArrayExtensions.contain(argv, "-h") || ArrayExtensions.contain(argv, "--h")
         || ArrayExtensions.contain(argv, "-help") || ArrayExtensions.contain(argv, "--help")) {
      System.err.println(doc);
      System.exit(1);
    }
  }
  static PWM load_pwm(PMParser parser, DataModel data_model, BackgroundModel background) {
    PWM pwm;
    switch (data_model) {
      case PCM:
        pwm = PCM.fromParser(parser).to_pwm(background);
        break;
      case PPM:
        pwm = PPM.fromParser(parser).to_pwm(background);
        break;
      case PWM:
        pwm = PWM.fromParser(parser);
        break;
      default:
        throw new Error("This code never reached");
    }

    return pwm;
  }
  static PWM load_pwm(File file, DataModel data_model, BackgroundModel background) {
    PWM pwm = load_pwm(PMParser.from_file(file), data_model,background);
    if (pwm.name == null || pwm.name.isEmpty()) {
      pwm.name = file.getName();
    }
    return pwm;
  }
}
