package evolmusic;

import java.util.Random;
import java.util.ArrayList;

/**
 * NOTE: Only works with two measure melodies.
 *
 * @author Nicholas Cho
 */
public class Breeder {
    private int numberMeasures;
    private int beatsPerMeasure;

    private static final ArrayList<String> PITCHES = Translator.NOTES;

    private Random random = new Random();

    public Breeder(int numberMeasures, int beatsPerMeasure) {
        this.numberMeasures = numberMeasures;
        this.beatsPerMeasure = beatsPerMeasure;
    }

    /**
     * Returns an array of the chords in the measures.
     *
     * @param measures The measures representing a song.
     * @return An array of chords in the song.
     */
    private String[] getChords(String[] measures) {
        String[] chords = new String[measures.length];

        // The end of a chord is denoted by the last "+" in the measure.
        for (int i = 0; i < measures.length; i++) {
            int chordEndIndex = measures[i].lastIndexOf("+") + 1;
            chords[i] = measures[i].substring(0, chordEndIndex);
        }

        return chords;
    }

    private String[] getMelody(String[] measures) {
        String[] melody = new String[measures.length];

        // Take each note in each measure and add it to a list of the melody.
        for (int i = 0; i < measures.length; i++) {
            int melodyStartIndex = measures[i].lastIndexOf("+") + 1;
            melody[i] = measures[i].substring(melodyStartIndex);
        }

        // Convert it back to an array before returning.
        return melody;
    }

    public String breed(String melodyOne, String melodyTwo) {
        String offspring = "";

        // Split up the two measures.
        String[] measures1 = melodyOne.split(" ");
        String[] measures2 = melodyTwo.split(" ");

        // Make sure the passed in melodies are of the right size.
        if ((measures1.length != numberMeasures) ||
                (measures2.length != numberMeasures)) {
            System.out.println("Measures are of the wrong length. Exiting.");
            System.exit(1);
        }

        // Arrays holding the chords of the melodies.
        String[] chords1 = getChords(measures1);
        String[] chords2 = getChords(measures2);

        // Arrays holding the single-note melody.
        String[] melody1 = getMelody(measures1);
        String[] melody2 = getMelody(measures2);

        // Copy over chords from both melodies into a single array.
        String[] chords = new String[2 * numberMeasures];
        System.arraycopy(chords1, 0, chords, 0, chords1.length);
        System.arraycopy(chords2, 0, chords, chords1.length, chords2.length);

        // Copy over melodies into a single array.
        String[] melodies = new String[2 * numberMeasures];
        System.arraycopy(melody1, 0, melodies, 0, melody1.length);
        System.arraycopy(melody2, 0, melodies, melody1.length, melody2.length);

        // Put together the melody measure-by-measure.
        for (int i = 0; i < numberMeasures; i++) {
            // Pick the chord for the measure.
            offspring += chords[random.nextInt(chords.length)];

            // Pick the measure to substitute in.
            offspring += melodies[random.nextInt(melodies.length)];

            // Separate measures with a space.
            offspring += " ";
        }

        return offspring;
    }
}
