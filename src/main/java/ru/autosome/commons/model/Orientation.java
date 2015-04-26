package ru.autosome.commons.model;

public enum Orientation {
  direct, revcomp;

  public boolean isDirect() {
    return (this == direct);
  }

  public boolean isReverseComplement() {
    return (this == revcomp);
  }

  String toStringShort() {
    switch(this) {
      case direct:
        return "+";
      case revcomp:
        return "-";
      default:
        throw new RuntimeException("Can't be here");
    }
  }
}
