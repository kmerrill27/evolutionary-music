package evolmusic;

import java.util.Random;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;

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

    //    private String[] getMelody(String[] measures) {
    //        // Holds the melody as an array of beats (quarter note in this
    // case)
    //        String[] beats = new String[beatsPerMeasure * measures.length];
    //
    //        for (int i = 0; i < measures.length; i++) {
    //            // The single note melody in a measure starts after the "+"
    //            int melodyStartIndex = measures[i].lastIndexOf("+") + 1;
    //            String melody = measures[i].substring(melodyStartIndex);
    //
    //            for (int j = 0; j < beatsPerMeasure * measures.length; j++) {
    //                // One "i" is an eighth note, so we need two per beat.
    //                int firstIndex = melody.indexOf("i");
    //                int secondIndex = melody.indexOf("i", firstIndex+1);
    //
    //                // Add the beat to the beats array,
    // then cut it out of melody.
    //                beats[j] = melody.substring(0, secondIndex + 1);
    //                melody = melody.substring(secondIndex + 1);
    //            }
    //        }
    //
    //        return beats;
    //    }

    private String[] getMelody(String[] measures) {
        List<String> melody = new ArrayList<String>();

        // Take each note in each measure and add it to a list of the melody.
        for (int i = 0; i < measures.length; i++) {
            int index = measures[i].lastIndexOf("+") + 1;
            String[] notes = measures[i].substring(index).trim().split("_");
            melody.addAll(Arrays.asList(notes));
        }

        // Convert it back to an array before returning.
        return melody.toArray(new String[melody.size()]);
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
        String[][] melodies = {
                melody1,
                melody2
        };

        // Copy over chords from both melodies into a single array.
        String[] chords = new String[2 * numberMeasures];
        System.arraycopy(chords1, 0, chords, 0, chords1.length);
        System.arraycopy(chords2, 0, chords, chords1.length, chords2.length);


        // Put together the melody measure-by-measure.
        for (int i = 0; i < numberMeasures; i++) {
            // Pick the chord for the measure.
            offspring += chords[random.nextInt(chords.length)];

            // Replace some section of the first melody.
            int numNotes = melody1.length;
            int startIndex = random.nextInt(numNotes - 1);
            int endIndex = startIndex + random.nextInt(numNotes - startIndex);

            int index = 0;
            int noteIndex = 0;

            for (; index < startIndex;) {
                String nextNote = melody1[noteIndex++];
                int noteLength = nextNote.replaceAll("[^i]", "").length();
                if (index + noteLength > startIndex) break;
                else {
                    offspring += nextNote + "_";
                    index += noteLength;
                }
            }

            int limiter = 0;
            for (; index < endIndex;) {
                String randomNote = melody2[random.nextInt(melody2.length)];
                int noteLength = randomNote.replaceAll("[^i]", "").length();
                if (noteLength > (endIndex - startIndex)) {
                    if (limiter < 10) continue;
                    else {
                        offspring += PITCHES.get(
                                random.nextInt(PITCHES.size())
                        ) + "i_";
                        index += 1;
                    }
                } else {
                    offspring += randomNote + "_";
                    index += noteLength;
                }
            }

            for (; index < 8;) {

            }
        }


        //        // Put together the melody measure-by-measure.
        //        for (int i = 0; i < numberMeasures; i++) {
        //            // Pick the chord for the measure.
        //            offspring += chords[random.nextInt(chords.length)];
        //
        //            // The number of eighths added to offspring so far.
        //            int numberEighths = 0;
        //
        //            // Add notes from one of the two melodies until the
        // measure's full.
        //            while (numberEighths < 8) {
        //                // Pick which melody to get notes from.
        //                int melodyNumber = random.nextInt(1) + 1;
        //
        //                int noteIndex = 0;
        //                for (String note : melodies[melodyNumber]) {
        //                    int noteLength = note.replaceAll("[^i]",
        // "").length();
        //
        //                    if (numberEighths + noteLength > 8) continue;
        //                    else {
        //                        System.out.println(note); // TODO remove
        //                        offspring += note + "_";
        //
        //                        numberEighths += noteLength;
        //                    }
        //                }
        //            }
        //
        //            offspring += " ";
        //        }

        //        // Put together the melody measure-by-measure.
        //        for (int i = 0; i < numberMeasures; i++) {
        //            // Pick the chord for the measure.
        //            offspring += chords[random.nextInt(chords.length)];
        //
        //            // The number of eighths added to offspring so far.
        //            int numberEighths = 0;
        //
        //            // Add notes from one of the two melodies until the
        // measure's full.
        //            while (numberEighths < 8) {
        //                // Pick which melody to get notes from.
        //                int melodyNumber = random.nextInt(1) + 1;
        //
        //                // Get the maximum number of eighths to get from
        // the melody.
        //                int eighthsToInsert = random.nextInt(8 -
        // numberEighths) + 1;
        //
        //                for (int j = 0; j < melodies[melodyNumber].length;
        // j++) {
        //                    String note = melodies[melodyNumber][j];
        //                    int noteLength = note.replaceAll("[^i]",
        // "").length();
        //
        //                    if (eighthsToInsert - noteLength >= 0) {
        //                        offspring += note + "_";
        //                        eighthsToInsert -= noteLength;
        //                        numberEighths += noteLength;
        //                    }
        //
        //                    if (eighthsToInsert == 0) break;
        //                }
        //            }
        //
        //            offspring += " ";
        //        }

        //        // Put together the melody measure-by-measure.
        //        for (int i = 0; i < numberMeasures; i++) {
        //            offspring += chords[random.nextInt(chords.length)];
        //
        //            // The number of beats inserted into offspring so far.
        //            int numberBeats = 0;
        //
        //            // Add some number of beats from each melody until
        // measure is full.
        //            while (numberBeats < 4) {
        //                int beatsToInsert = random.nextInt(5 - numberBeats);
        //
        //                // Pick which melody to get the beats from,
        // then add to melody.
        //                int melodyNumber = random.nextInt(1) + 1;
        //                for (int j = 0; j < beatsToInsert; j++) {
        //                    offspring +=
        // melodies[melodyNumber][numberBeats+j];
        //                }
        //
        //                numberBeats += beatsToInsert;
        //            }
        //
        //            // End the measure with a space.
        //            offspring += " ";
        //        }

        return offspring;
    }
}
