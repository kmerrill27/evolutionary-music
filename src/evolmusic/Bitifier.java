package evolmusic;

import java.util.BitSet;

/**
 * Converts a JFugue-compatible melody string to a 1-hot encoding
 * of a melody.
 * 
 * @author Kim Merrill
 * @date May 2, 2013
 *
 */
public class Bitifier implements Translator {

	private static final int CHORD_LENGTH = 12; // number of bits in chord
	private static final String USER_RATING = "0.0 "; // default (unknown) user rating of song
	private static final String CHORD_SUFFIX = OCTAVES[0] + WHOLE_NOTE;

	public Bitifier() {		
	}

	@Override
	public String translate(String song) {
		String[] measures = song.split(SPACE);
		String[] measure1 = splitChord(measures[0]);
		String[] measure2 = splitChord(measures[1]);
		return USER_RATING + formatChord(measure1[0]) + formatChord(measure2[0])
				+ formatMelody(measure1[1]) + formatMelody(measure2[1]);
	}

	/**
	 * Parses string to separate out chord prefix and melody string.
	 * 
	 * @pre chord strictly precedes melody in string
	 * 
	 * @param measure JFugue string with (optional) chord prepended to melody - 
	 * e.x. C5w+E5w+G5w+A5iiii_D5ii_G4ii
	 * @return 2-element array of chord separated chord and melody string
	 */
	private String[] splitChord(String measure) {
		String chord, melody;
		// Last "+" indicates end of chord string
		int index = measure.lastIndexOf(PLUS);
		if (index < 0) {
			chord = "";
			melody = measure;
		} else {
			chord = measure.substring(0, index+1);
			melody = measure.substring(index+1, measure.length());
		}
		return new String[]{chord, melody};
	}

	/**
	 * Creates bit string encoding of chord string.
	 * 
	 * @pre all notes in chord have pre-set octave and duration "5w"
	 * 
	 * @param chordString JFugue string representing chord - e.x. C5w+E5w+G5w
	 * @return bit string representing chord
	 */
	private String formatChord(String chordString) {
		chordString = chordString.replaceAll(CHORD_SUFFIX, "");
		String[] notes = chordString.split("\\+");
		return bitifyChord(notes);
	}

	/**
	 * Creates bit string encoding of melody string.
	 * 
	 * @pre string contains only sequential notes separated by "_"
	 * 
	 * @param melodyString JFugue string representing melody - e.x. C5iiii_F#6ii_G6ii
	 * @return bit string representing melody
	 */
	private String formatMelody(String melodyString) {
		String bitMelody = "";
		String[] notes = melodyString.split("_");
		for (String note : notes) {
			bitMelody += bitifyNote(note);
		}
		return bitMelody;
	}

	/**
	 * Creates bit string encoding of notes as chord.
	 * 
	 * @pre notes are valid JFugue strings
	 * 
	 * @param notes array of JFugue notes
	 * @return bit string representing chord
	 */
	private String bitifyChord(String[] notes) {
		BitSet bitVector = new BitSet(CHORD_LENGTH);
		for (String note : notes) {
			// Check for no-chord case
			if (!note.isEmpty()) {
				// Set bit corresponding to note in chord
				bitVector.set(NOTES.indexOf(note));
			}
		}
		return makeBitString(bitVector, CHORD_LENGTH);
	}

	/**
	 * Creates bit string encoding of note.
	 * 
	 * @pre note is valid JFugue string in one of pre-set octaves "4" or "5" with durations
	 * specified in only eighth notes - i.e. "i"s only
	 * 
	 * @param note JFugue string representing single note - e.x. C5ii
	 * @return bit string representing note
	 */
	private String bitifyNote(String note) {
		int offset = 1;
		BitSet bitVector = new BitSet(NOTE_BITS);

		// Check if note is a rest
		if(note.substring(0,1).equals(REST)) {
			return bitifyRest(note);
		}

		int durIndex = note.indexOf("i");
		String duration = note.substring(durIndex, note.length());
		String octave = note.substring(durIndex-1, durIndex);
		String pitch = note.substring(0, durIndex-1);

		// If note is in higher octave, set bit in second 12-bit section
		if (octave.equals(OCTAVES[1])) {
			offset += 12;
		}
		bitVector.set(NOTES.indexOf(pitch) + offset);
		String bitString = makeBitString(bitVector, NOTE_BITS);
		
		// If note has duration longer than one eighth note, "tie over" note by
		// appending bit strings for remaining eighth note durations.
		return bitString + bitifyTiedOver(bitString, duration.length());
	}

	/**
	 * Creates bit string encoding of tied-over note.
	 * e.x. C5ii is represented as 01000000000000000000000001100000000000000000000000 - 
	 * method returns 1100000000000000000000000
	 * 
	 * @pre noteBitString is valid JFugue note
	 * 
	 * @param noteBitString JFugue string representing single note
	 * @param duration duration of note in terms of eighth notes
	 * @return bit string representing tied-over note
	 */
	private String bitifyTiedOver(String noteBitString, int duration) {
		String bitString = "";
		// Setting first bit indicates note should be tied over from previous note.
		noteBitString = "1" + noteBitString.substring(1, noteBitString.length());
		for (int i = 1; i < duration; i++) {
			bitString += noteBitString;
		}
		return bitString;
	}

	/**
	 * Creates bit encoding of given rest.
	 * 
	 * @pre note is a JFugue rest
	 * 
	 * @param note JFugue-encoding of a rest - e.x. Riii
	 * @return bit string representing rest
	 */
	private String bitifyRest(String note) {
		String duration = note.substring(1, note.length());
		String bitString = makeBitString(new BitSet(NOTE_BITS), NOTE_BITS);
		return bitString + bitifyTiedOver(bitString, duration.length());
	}

	/**
	 * Constructs bit string representation of a bit vector.
	 * 
	 * @pre bit vector is of length at least "length"
	 * 
	 * @param bitVector
	 * @param length desired number of bits in bit string
	 * @return bit string equivalent
	 */
	private String makeBitString(BitSet bitVector, int length) {
		assert(bitVector.length() <= length);
		String bitString = "";
		for (int i = 0; i < length; i++) {
			boolean bit = bitVector.get(i);
			bitString +=  bit ? 1 : 0;
		}
		return bitString.trim();
	}

	public static void main(String[] args) {
		MelodyPlayer player = new MelodyPlayer();
		Notationizer notationizer = new Notationizer();
		Bitifier bitifier = new Bitifier();

		//String bitString = "0.2 1 0 1 0 1 1 0 0 0 1 0 0 0 1 1 0 0 1 0 1 1 0 0 1 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 1 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 1 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 1 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 1 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 1 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 1 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 1 0 0 0 0 0 0 0 0 0 0 0 0 1 0 0 0 0 0 0 0 0 0 0 0 1 0 0 0 0 0 0 0 0 0 0 0 0 1 0 0 0 0 0 0 0 0 0 0 0 1 0 0 0 0 0 0 0 0 0 0 0 0 1 0 0 0 0 0 0 0 0 0 0 0 1 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 1 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 1 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 1 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 1 0";
		//String bitString = "0.9 1 0 1 0 0 1 0 0 0 1 0 0 0 0 1 0 0 1 0 1 0 0 0 1 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 1 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 1 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 1 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 1 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 1 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 1 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 1 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 1 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 1 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 1 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 1 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 1 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 1 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 1 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 1 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 1 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0";
		//String bitString = "0.6 1 0 1 0 1 1 0 0 0 1 0 0 0 1 1 0 0 1 0 1 1 0 0 1 0 0 0 0 0 0 0 0 0 0 0 0 1 0 0 0 0 0 0 0 0 0 0 0 0 1 0 0 0 0 0 0 0 0 0 0 0 1 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 1 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 1 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 1 0 0 0 0 0 0 0 0 0 0 0 1 0 0 0 0 0 0 0 0 0 0 0 0 1 0 0 0 0 0 0 0 0 0 0 0 1 0 0 0 0 0 0 0 0 0 0 0 0 1 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 1 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 1 0 0 0 0 0 0 0 0 0 0 1 0 0 0 0 0 0 0 0 0 0 0 0 0 1 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 1 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 1 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 1 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 1 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 1 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 1 0 0 0 0 0 0 0 0 0 0 0 0";
		//String bitString = "0.1 1 0 1 0 1 1 0 0 0 1 0 0 0 1 1 0 0 1 0 1 1 0 0 1 0 0 0 0 0 0 0 0 0 0 0 1 0 0 0 0 0 0 0 0 0 0 0 0 0 1 0 0 0 0 0 0 0 0 0 0 1 0 0 0 0 0 0 0 0 0 0 0 0 0 1 0 0 0 0 0 0 0 0 0 0 1 0 0 0 0 0 0 0 0 0 0 0 0 0 1 0 0 0 0 0 0 0 0 0 0 1 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 1 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 1 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 1 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 1 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 1 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 1 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 1 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 1 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 1 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 1 0 0 0 0 0 0 0 0 0 0 0 0 0";
		//String bitString = "0.1 1 0 1 0 1 1 0 0 0 1 0 0 0 1 1 0 0 1 0 1 1 0 0 1 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 1 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 1 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 1 0 0 0 0 0 0 0 0 0 0 0 0 0 1 0 0 0 0 0 0 0 0 0 0 1 0 0 0 0 0 0 0 0 0 0 0 0 0 1 0 0 0 0 0 0 0 0 0 0 1 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 1 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 1 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 1 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 1 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 1 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 1 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 1 1 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 1 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 1 0 1 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 1 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 1 0 0 0 0 0 0 0 0 0 0 0 0 0 0";
		//String bitString = "0.9 1 0 1 0 1 1 0 0 0 1 0 0 0 1 1 0 0 1 0 1 1 0 0 1 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 1 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 1 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 1 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 1 0 0 0 0 0 0 0 0 0 0 0 0 1 0 0 0 0 0 0 0 0 0 0 0 1 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 1 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 1 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 1 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 1 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 1 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 1 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 1 0 0 0 0 0 0 0 1 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 1 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 1 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 1 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 1 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0";
		String bitString = "0.9 1 0 1 0 1 1 0 0 0 1 0 0 0 1 1 0 0 1 0 1 1 0 0 1 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 1 0 0 0 0 0 0 1 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 1 0 0 0 0 0 0 1 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 1 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 1 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 1 0 0 1 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 1 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 1 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 1 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 1 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 1 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 1 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 1 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 1 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 1 0 0 0 0 0 0 0 0 0 1 0 0 0 0 0 0 0 0 0 0 0 0 0 0 1 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 1 0 0 0 0 0 0 0 0 0 0 0 0";
		
		// Test given melody
		String melody = notationizer.translate(bitString);
		player.play(melody);
		bitString = bitifier.translate(melody);
		melody = notationizer.translate(bitString);
		player.save(melody, "test.mid");
		melody = player.load("test.mid");
		player.play(melody);

		// Test random melody
		melody = new RandomMelody(2, 4).getMelodyString();
		System.out.println(melody);
		player.play(melody);
		bitString = bitifier.translate(melody);
		melody = notationizer.translate(bitString);
		System.out.println(melody);
		player.play(melody);
		player.save(melody, "test.mid");
	}

}
