package evolmusic;

/**
 *
 * Translates a 1-hot encoding of a melody into a JFugue-compatible string
 * for playback.
 *
 * @author Kim Merrill
 * @date May 2, 2013
 *
 */
public class Notationizer implements Translator {

	private static final int NUM_NOTES = 16; // two 4/4 measures with min of eighth notes
	private static final String NOTE = "1"; // 1-hot encoding of notes
	private static final String TIED = "T"; // indicates if a note is tied over

    public Notationizer() {
    }

    /**
     * @pre bitString has dimension of 424, length of 851
     */
    @Override
    public String translate(String bitString) {
        assert(bitString.length() == 851);
        bitString = preprocess(bitString);
        return formatSong(bitString);
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
     * @return final JFugue-formatted song string
     */
    private String formatSong(String bitString) {
        String melody1 = "";
        String melody2 = "";
        String chord1 = parseChord(bitString.substring(0, 12));
        String chord2 = parseChord(bitString.substring(12, 24));
        String notes1 = bitString.substring(24, 224);
        String notes2 = bitString.substring(224, bitString.length());

        // Parse each note individually
        for(int i = 0; i < NUM_NOTES/2; i++) {
            melody1 += parseNote(notes1.substring(i*NOTE_BITS, i*NOTE_BITS+NOTE_BITS))
                    + EIGHTH_NOTE + "_";
            melody2 += parseNote(notes2.substring(i*NOTE_BITS, i*NOTE_BITS+NOTE_BITS))
                    + EIGHTH_NOTE + "_";
        }
        return formatChord(chord1) + formatMelody(melody1) + " " +
                formatChord(chord2) + formatMelody(melody2);
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
        // Check if the chord has no notes
        if (index < 0) {
            return "";
        }

        while(index >= 0) {
            tempChord.append(NOTES.get(index) + PLUS);
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
        String octave = OCTAVES[0];

        // If no bits are set, the note is a rest
        if (index < 0) {
            return REST;
        } else if (index > 11) {
            // Find the pitch and set to the higher octave
            index = index-12;
            octave = OCTAVES[1];
        }
        tempNote += NOTES.get(index) + octave;
        return tempNote;
    }

    /**
     * Adds octave and duration to notes to create chord.
     * A chord is a grouping of whole notes played simultaneously.
     *
     * @param chordString notes in chord - e.x. C+E+G
     * @return JFugue-formatted chord for prepending - e.x. C5w+E5w+G5w+
     */
    private String formatChord(String chordString) {
        // If chord is a rest, do not specify octave
        if (chordString == "") {
            return "";
        }
        StringBuilder formatChord = new StringBuilder();
        // Separate out each note in the chord
        String[] chordArray = chordString.split("\\+");

        for (String chord : chordArray) {
            // Chords are always played in the lower octave
            formatChord.append(chord + OCTAVES[0] + WHOLE_NOTE + PLUS);
        }
        return formatChord.toString();
    }

	/**
	 * Collapses tied notes to be listed as a single note with a longer duration.
	 *
	 * @ pre tied notes have the same pitch
	 *
	 * @param noteString notes in a measure with ties - e.x. A6i_TA6i_G6i_A6i_F6i_D6i_C6i_B5i
	 * @return JFugue-formatted 4/4 measure - e.x. A6ii_G6i_A6i_F6i_D6i_C6i_B5i
	 */
	private String formatMelody(String melodyString) {
		StringBuilder formatMelody = new StringBuilder(melodyString);
		// Remove trailing "_"
		formatMelody.deleteCharAt(formatMelody.length()-1);

        // Find all tied notes, indicated by "T", and collapse them
        int tiedIndex = formatMelody.indexOf(TIED);
        while (tiedIndex > 0) {
            int ind = formatMelody.indexOf("_", tiedIndex);
            // Check if tied note is the last note of the measure
            if (ind < 0) {
                formatMelody.delete(tiedIndex-1, formatMelody.length());
            } else {
                formatMelody.delete(tiedIndex, ind+1);
            }
            // "i" indicates eighth note, "ii" indicates quarter note, etc.
            formatMelody.insert(tiedIndex-1, "i");
            tiedIndex = formatMelody.indexOf(TIED);
        }
        return formatMelody.toString();
    }
}
