package ru.autosome.perfectosape.model;

public class RegionAffinityInfos {
  private final RegionAffinityVariantInfo info_1;
  private final RegionAffinityVariantInfo info_2;

  public RegionAffinityVariantInfo getInfo_1() {
   return info_1;
  }
  public RegionAffinityVariantInfo getInfo_2() {
    return info_2;
  }

  public RegionAffinityInfos(RegionAffinityVariantInfo info_1, RegionAffinityVariantInfo info_2) {
    this.info_1 = info_1;
    this.info_2 = info_2;
  }

  public double foldChange() {
    return info_1.pvalue / info_2.pvalue;
  }

  public double logFoldChange() {
    return Math.log(info_1.pvalue / info_2.pvalue) / Math.log(2);
  }

  @Override

  public String toString() {
    return toString(false);
  }
  public String toString(boolean useLogFoldChange) {
    StringBuilder result = new StringBuilder();
    result.append(info_1.position.toString()).append("\t").append(info_1.word).append("\t");
    result.append(info_2.position.toString()).append("\t").append(info_2.word).append("\t");

    result.append(info_1.allele).append("/").append(info_2.allele).append("\t");
    result.append(info_1.pvalue).append("\t").append(info_2.pvalue).append("\t");

    if (useLogFoldChange) {
      result.append(logFoldChange());
    } else {
      result.append(foldChange());
    }
    return result.toString();
  }

  public String toStringShort() {
    return String.format("%.2e", info_1.pvalue) + "\t" +
                        String.format("%.2e", info_2.pvalue) + "\t" +
                        info_1.position.toStringShort() + "\t" +
                        info_2.position.toStringShort();
  }

  public boolean hasSiteOnAnyAllele(double max_pvalue_cutoff) {
    return info_1.pvalue <= max_pvalue_cutoff || info_2.pvalue <= max_pvalue_cutoff;
  }
}
