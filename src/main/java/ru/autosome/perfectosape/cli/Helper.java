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
}
