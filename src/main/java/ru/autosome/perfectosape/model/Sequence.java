package ru.autosome.perfectosape.model;

import gnu.trove.impl.unmodifiable.TUnmodifiableCharCharMap;
import gnu.trove.map.TCharCharMap;
import gnu.trove.map.hash.TCharCharHashMap;
import gnu.trove.set.TCharSet;
import gnu.trove.set.hash.TCharHashSet;
import ru.autosome.commons.model.Alphabet;
import ru.autosome.commons.model.Position;
import ru.autosome.perfectosape.model.encoded.di.SequenceDiEncoded;
import ru.autosome.perfectosape.model.encoded.mono.SequenceMonoEncoded;

public class Sequence {
  private static final TCharSet allowedLetters = new TCharHashSet(new char[]{'A','C','G','T','a','c','g','t', 'n', 'N'});
  private static final TCharCharMap complements =
   new TUnmodifiableCharCharMap( new TCharCharHashMap(new char[]{'A','C','G','T','a','c','g','t', 'n', 'N'},
                                                      new char[]{'T','G','C','A','t','g','c','a', 'n', 'N'}) );

  final public String sequence;
  private String cache_reverseComplementString;
  private SequenceMonoEncoded cache_monoEncode;
  private SequenceDiEncoded cache_diEncode;

  public Sequence(String sequence, boolean checked) {
    if (!checked) {
      for(int i = 0; i < sequence.length(); ++i) {
        if ( !allowedLetters.containsAll(sequence.toCharArray()) ) {
          throw new IllegalArgumentException("Sequence '" + sequence + "' contains unallowed character (only A,C,G,T,N letters are allowed).");
        }
      }
    }
    this.sequence = sequence;
  }

  public Sequence(String sequence) {
    this(sequence, false);
  }

  public int length() {
    return sequence.length();
  }

  public Sequence reverse() {
    return new Sequence(new StringBuilder(sequence).reverse().toString(), true);
  }

  public Sequence reverseComplement() {
    int len = sequence.length();
    StringBuilder revCompSeq = new StringBuilder(len);
    for (int i = 0; i < len; ++i) {
      revCompSeq.append(complements.get(sequence.charAt(len - 1 - i)));
    }
    return new Sequence(revCompSeq.toString(), true);
  }

  private String reverseComplementString() {
    if (cache_reverseComplementString == null) {
      String revSeq = new StringBuilder(sequence).reverse().toString();
      StringBuilder revCompSeq = new StringBuilder(revSeq.length());
      for (int i = 0; i < revSeq.length(); ++i) {
        revCompSeq.append(complements.get(revSeq.charAt(i)));
      }
      cache_reverseComplementString = revCompSeq.toString();
    }
    return cache_reverseComplementString;
  }

  public Sequence complement() {
    return new Sequence(reverseComplementString(), true);
  }

  // works on direct strand
  public Sequence substring(int beginIndex, int endIndex) {
    return new Sequence(sequence.substring(beginIndex, endIndex), true);
  }

  // returns subsequence of given length on according strand, starting from given left boundary (on positive strand)
  public Sequence substring(Position left_boundary, int substring_length) {
    Sequence unorientedWord = substring(left_boundary.position(), Math.min(left_boundary.position() + substring_length, length()));
    if (left_boundary.isDirect()) {
      return unorientedWord;
    } else {
      return unorientedWord.reverseComplement();
    }
  }

//  // (upstream) positions at which subsequence of given length can start
//  public ArrayList<Position> subsequence_positions(int subsequence_length) {
//    return Position.positions_between(0, length(), subsequence_length);
//  }

  public SequenceMonoEncoded monoEncode() {
    if (cache_monoEncode == null) {
      byte[] directSeq = Alphabet.monoACGTN.convertString(sequence);
      byte[] revcompSeq = Alphabet.monoACGTN.convertString(reverseComplementString());
      cache_monoEncode = new SequenceMonoEncoded(directSeq, revcompSeq);
    }
    return cache_monoEncode;
  }

  public SequenceDiEncoded diEncode() {
    if (cache_diEncode == null) {
      byte[] directSeq = Alphabet.diACGTN.convertString(sequence);
      byte[] revcompSeq = Alphabet.diACGTN.convertString(reverseComplementString());
      cache_diEncode = new SequenceDiEncoded(directSeq, revcompSeq);
    }
    return cache_diEncode;
  }

  @Override
  public String toString() {
    return sequence;
  }
}
