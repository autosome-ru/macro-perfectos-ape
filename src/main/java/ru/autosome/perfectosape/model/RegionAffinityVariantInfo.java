package ru.autosome.perfectosape.model;

import ru.autosome.commons.model.Position;

public class RegionAffinityVariantInfo {
  public final Position position;
  public final Sequence word;
  public final Character allele;
  public final double pvalue;

  public Position getPosition() {
    return position;
  }
  public Sequence getWord() {
    return word;
  }
  public Character getAllele() {
    return allele;
  }
  public double getPvalue() {
    return pvalue;
  }

  public RegionAffinityVariantInfo(Position position, Character allele, double pvalue, Sequence word) {
    this.position = position;
    this.allele = allele;
    this.pvalue = pvalue;
    this.word = word;
  }
}
