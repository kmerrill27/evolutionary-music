package evolmusic;

/**
 * 
 * Translates a 1-hot encoding of a melody into a JFugue-compatible string
 * for playback.
 * 
 * @author Kim Merrill
 * 
 * Bit string (dimension 424):
 * The first number is a user-added rating. 0.0 if unknown.
 * The next 12 bits are a 1-hot encoding of the first measure's chord, followed
 * by 12 bits for the second measure's chord (harmonic scale starting with C).
 * The last 400 bits are a 1-hot encoding of the melody. Each 25-bits
 * represents a note with the first bit indicating whether the note is
 * tied over and the remaining 24 bits a 2-octave harmonic scale.
 * 
 * 0.9 1 0 1 0 0 1 0 0 0 1 0 0 0 0 1 0 0 1 0 1 0 0 0 1 0 0 0 0 0 0 0 0 0
 * 0 0 0 0 0 0 0 0 0 0 0 0 0 1 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0
 * 0 0 0 0 0 1 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 1 0 0 0 0 0 0 0 0
 * 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 1 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0
 * 0 0 0 0 1 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 1 0 0 0 0 0 0 0 0
 * 0 0 0 0 0 0 0 0 0 0 0 0 0 0 1 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0
 * 0 0 0 1 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 1 0 0 0 0 0 0 0 0 0
 * 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 1 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0
 * 0 0 0 0 1 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 1 0 0 0 0 0 0 0
 * 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 1 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0
 * 0 0 0 0 0 0 0 0 0 0 1 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 1 0 0 0
 * 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 1 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0
 * 0 0 0 0 0 0
 * 
 * JFugue string:
 * "+" indicates that notes should be played together and "_" indicates
 * that notes should be played in sequence.
 * 
 * C5w+D5w+F5w+A5w+A6i_B6i_G6i_A6i_F6i_D6i_C6i_B5i
 * D5w+F5w+G5w+B5w+G#5i_B5i_G5i_F5i_G5i_G#5i_E5i_C5i
 *
 */
public class Translator {

	private static final int NUM_NOTES = 16; // two 4/4 measures with min of eighth notes
	private static final String NOTE = "1"; // 1-hot encoding of notes
	private static final int NOTE_BITS = 25; // number of bits representing a note
	private static final String EIGHTH_NOTE = "i";
	private static final String WHOLE_NOTE = "w";
	private static final String REST = "R";
	private static final String TIED = "T"; // indicates if a note is tied over
	private static final String SPACE = " ";
	private static final int[] OCTAVES = {5, 6}; // possible octaves
	private static final String[] NOTES = {"C", "C#", "D", "D#", "E", "F", "F#", "G",
		"G#", "A", "A#", "B"}; // one-octave harmonic scale

	private String chord1, chord2;
	private String measure1 = "";
	private String measure2 = "";

	public Translator() {
	}

	/**
	 * Translates a 424-dimension bit string encoding of a
	 * 4/4 two-measure song fragment into a JFugue-formatted
	 * melody representation.
	 * 
	 * @pre bitString has dimension of 424, length of 851
	 * 
	 * @param bitString 1-hot encoding of melody
	 * @return melody string formatted for JFugue playback
	 */
	public String translate(String bitString) {
		assert(bitString.length() == 851);
		bitString = preprocess(bitString);
		parse(bitString);
		String melody = formatMelody();
		System.out.println(melody);
		return melody;
	}

	/**
	 * Prepares bit string for parsing by removing spaces and
	 * user-rating characters.
	 * 
	 * @param bitString 1-hot encoding of melody
	 * @return parse-able bit string
	 */
	private String preprocess(String bitString) {
		// Ignore first number, which is the user rating
		int firstSpace = bitString.indexOf(SPACE);
		bitString = bitString.substring(firstSpace, bitString.length());
		// Remove all spaces
		return bitString.replaceAll("\\s","");
	}

	/**
	 * Separate the bit sequences for each chord and note and convert
	 * them to their corresponding pitches.
	 * 
	 * @param bitString 1-hot encoding of melody
	 */
	private void parse(String bitString) {
		chord1 = parseChord(bitString.substring(0, 12));
		chord2 = parseChord(bitString.substring(12, 24));
		String notes1 = bitString.substring(24, 224);
		String notes2 = bitString.substring(224, bitString.length());
		// Parse each note individually
		for(int i = 0; i < NUM_NOTES/2; i++) {
			measure1 += parseNote(notes1.substring(i*NOTE_BITS, i*NOTE_BITS+NOTE_BITS))
					+ EIGHTH_NOTE + "_";
			measure2 += parseNote(notes2.substring(i*NOTE_BITS, i*NOTE_BITS+NOTE_BITS))
					+ EIGHTH_NOTE + "_";
		}
	}

	/**
	 * Determines pitches of each note in the chord.
	 * 
	 * @param bitString 1-hot encoding of chord
	 * @return chord string of pitches - e.x. C+E+G
	 */
	private String parseChord(String bitString) {
		StringBuilder tempChord = new StringBuilder();
		int index = bitString.indexOf(NOTE);
		// If the chord hsa no notes, represent it as a rest
		if (index < 0) {
			return REST;
		}
		while(index >= 0) {
			tempChord.append(NOTES[index] + "+");
			index = bitString.indexOf(NOTE, index+1);
		}
		// Remove trailing "+"
		return tempChord.deleteCharAt(tempChord.length()-1).toString();
	}

	/**
	 * Determine pitch of note.
	 * NOTE: does not error-check - i.e. ignores 1s after the first if more
	 * than one bit is set
	 * @param bitString 1-hot encoding of a harmonic scale
	 * @return note string with pitch and octave - i.e. C5
	 */
	private String parseNote(String bitString) {
		String tempNote = "";
		// Indicate tied-over notes with a "T" for future processing
		if (bitString.indexOf(NOTE) == 0) {
			tempNote += "T";
		}
		bitString = bitString.substring(1, bitString.length());
		int index = bitString.indexOf(NOTE);
		int octave = OCTAVES[0];
		// If no bits are set, the note is a rest
		if (index < 0) {
			return REST;
		} else if (index > 11) {
			// Find the pitch and set to the higher octave
			index = index-12;
			octave = OCTAVES[1];
		}
		tempNote += NOTES[index] + octave;
		return tempNote;
	}


	/**
	 * Formats and combines notes into final JFugue format.
	 * 
	 * @return JFugue-formatted melody string
	 */
	private String formatMelody() {
		return formatChord(chord1) + "+" + formatMeasure(measure1) + " " +
				formatChord(chord2) + "+" + formatMeasure(measure2);
	}

	/**
	 * Adds octave and duration to notes to create chord.
	 * A chord is a grouping of whole notes played simultaneously.
	 * 
	 * @param chordString notes in chord - e.x. C+E+G
	 * @return JFugue-formatted chord - e.x. C5w+E5w+G5w
	 */
	private String formatChord(String chordString) {
		// If chord is a rest, do not specify octave
		if (chordString == REST) {
			return REST + WHOLE_NOTE;
		}
		StringBuilder formatChord = new StringBuilder();
		// Separate out each note in the chord
		String[] chordArray = chordString.split("\\+");
		for (String chord : chordArray) {
			// Chords are always played in the lower octave
			formatChord.append(chord + OCTAVES[0] + WHOLE_NOTE + "+");
		}
		// Remove trailing "+"
		return formatChord.deleteCharAt(formatChord.length()-1).toString();
	}

	/**
	 * Collapses tied notes to be listed as a single note with a longer duration.
	 * NOTE: does not error-check - i.e. if a note is not the same as the note
	 * to which it is tied over, it will take the pitch of the first note
	 * 
	 * @param noteString notes in a measure with ties - e.x. A6i_TA6i_G6i_A6i_F6i_D6i_C6i_B5i
	 * @return JFugue-formatted 4/4 measure - e.x. A6ii_G6i_A6i_F6i_D6i_C6i_B5i
	 */
	private String formatMeasure(String noteString) {
		StringBuilder formatNote = new StringBuilder(noteString);
		// Remove trailing "_"
		formatNote.deleteCharAt(formatNote.length()-1);
		// Find all tied notes, indicated by "T", and collapse them
		int tiedIndex = formatNote.indexOf(TIED);
		while (tiedIndex > 0) {
			int ind = formatNote.indexOf("_", tiedIndex);
			formatNote.delete(tiedIndex, ind+1);
			// "i" indicates eighth note, "ii" indicates quarter note, etc.
			formatNote.insert(tiedIndex-1, "i");
			tiedIndex = formatNote.indexOf(TIED);
		}
		return formatNote.toString();
	}
	
	public static void main(String[] args) {
		//String bitString = "0.9 1 0 1 0 0 1 0 0 0 1 0 0 0 0 1 0 0 1 0 1 0 0 0 1 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 1 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 1 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 1 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 1 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 1 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 1 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 1 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 1 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 1 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 1 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 1 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 1 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 1 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 1 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 1 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 1 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0";
		//String bitString = "0.6 1 0 1 0 1 1 0 0 0 1 0 0 0 1 1 0 0 1 0 1 1 0 0 1 0 0 0 0 0 0 0 0 0 0 0 0 1 0 0 0 0 0 0 0 0 0 0 0 0 1 0 0 0 0 0 0 0 0 0 0 0 1 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 1 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 1 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 1 0 0 0 0 0 0 0 0 0 0 0 1 0 0 0 0 0 0 0 0 0 0 0 0 1 0 0 0 0 0 0 0 0 0 0 0 1 0 0 0 0 0 0 0 0 0 0 0 0 1 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 1 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 1 0 0 0 0 0 0 0 0 0 0 1 0 0 0 0 0 0 0 0 0 0 0 0 0 1 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 1 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 1 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 1 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 1 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 1 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 1 0 0 0 0 0 0 0 0 0 0 0 0";
		//String bitString = "0.1 1 0 1 0 1 1 0 0 0 1 0 0 0 1 1 0 0 1 0 1 1 0 0 1 0 0 0 0 0 0 0 0 0 0 0 1 0 0 0 0 0 0 0 0 0 0 0 0 0 1 0 0 0 0 0 0 0 0 0 0 1 0 0 0 0 0 0 0 0 0 0 0 0 0 1 0 0 0 0 0 0 0 0 0 0 1 0 0 0 0 0 0 0 0 0 0 0 0 0 1 0 0 0 0 0 0 0 0 0 0 1 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 1 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 1 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 1 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 1 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 1 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 1 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 1 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 1 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 1 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 1 0 0 0 0 0 0 0 0 0 0 0 0 0";
		String bitString = "0.1 1 0 1 0 1 1 0 0 0 1 0 0 0 1 1 0 0 1 0 1 1 0 0 1 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 1 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 1 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 1 0 0 0 0 0 0 0 0 0 0 0 0 0 1 0 0 0 0 0 0 0 0 0 0 1 0 0 0 0 0 0 0 0 0 0 0 0 0 1 0 0 0 0 0 0 0 0 0 0 1 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 1 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 1 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 1 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 1 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 1 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 1 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 1 1 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 1 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 1 0 1 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 1 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 1 0 0 0 0 0 0 0 0 0 0 0 0 0 0";
		
		Translator translator = new Translator();
		String melody = translator.translate(bitString);
		MelodyPlayer test = new MelodyPlayer(melody);
		test.play();
	}
}
