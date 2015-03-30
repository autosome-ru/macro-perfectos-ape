package ru.autosome.commons.model;

public enum Orientation {
  direct, revcomp;

  public boolean isDirect() {
    return (this == direct);
  }

  public boolean isReverseComplement() {
    return (this == revcomp);
  }

}
