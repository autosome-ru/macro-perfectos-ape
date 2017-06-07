package ru.autosome.perfectosape.model;

import ru.autosome.commons.model.Position;

public class RegionAffinityVariantInfo {
  final Position position;
  final Sequence word;
  final Character allele;
  final double pvalue;

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
