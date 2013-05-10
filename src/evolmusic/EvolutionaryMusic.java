package evolmusic;

/**
 * @author Nicholas Cho
 */
public class EvolutionaryMusic {

    /**
     * Main method that checks commandline parameters and then runs the program
     * with either default or given values.
     *
     * @param args Commandline arguments. Use "-i" to run interactively.
     */
    public static void main(String[] args) {
        // Constants to determine constraints.
        final int POPULATION_SIZE = 100;
        final int NUMBER_MEASURES = 2;
        final int BEATS_PER_MEASURE = 4;

        // Populate our population with random melodies.
        String[] population = new String[POPULATION_SIZE];
        RandomMelody randomGenerator = new RandomMelody(
                NUMBER_MEASURES,
                BEATS_PER_MEASURE
        );
        for (int i = 0; i < population.length; i++) {
            population[i] = randomGenerator.getMelodyString();
        }

        // TODO: rest of program
    }
}
