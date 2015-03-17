package ru.autosome.commons.motifModel;

import ru.autosome.perfectosape.model.Sequence;
import ru.autosome.perfectosape.model.SequenceWithSNP;

public interface Encodable<EncodedSequenceType, EncodedSequenceWithSNVType> {
  EncodedSequenceType encodeSequence(Sequence sequence);
  EncodedSequenceWithSNVType encodeSequenceWithSNP(SequenceWithSNP sequenceWithSNP);
}
