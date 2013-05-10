package evolmusic;

import java.util.Random;
import java.util.HashMap;
import java.io.File;

/**
 * @author Nicholas Cho
 */
public class EvolutionaryMusic {

    /**
     * Creates a directory at path/name. All neccessary parent directories will
     * be created.
     *
     * @param path Directory to create, including relative path. DO NOT include
     * a leading slash.
     */
    private static void createFolder(String path) {
        File directory = new File(path);

        // Make sure it doesn't already exist.
        if (!directory.exists()) {
            boolean result = directory.mkdirs();

            // If making the directory failed, exit program.
            if (!result) {
                System.out.println("Error creating directory: " + path);
                System.exit(1);
            }
        }
    }

    /**
     * Takes a directory path and deletes it recursively using a helper
     * method.
     *
     * @param path The path of the directory to delete.
     */
    private static void deleteFolder(String path) {
        File file = new File(path);

        // Make sure to check if it exists.
        if (file.exists()) {
            delete(file);
        }
    }

    /**
     * Takes a file and deletes it, recursively deleting contents if the file
     * is a directory.
     *
     * @param file The file object to delete.
     */
    private static void delete(File file) {
        // Recursively delete files in folders.
        if (file.isDirectory()) {
            for (File c : file.listFiles())
                delete(c);
        }

        // Delete the directory, and if it fails, exit program.
        boolean result = file.delete();
        if (!result) {
            System.out.println("Error deleting directory: " + file.toString());
            System.exit(1);
        }
    }

    /**
     * Main method that checks commandline parameters and then runs the program
     * with either default or given values.
     *
     * @param args Commandline arguments. Use "-i" to run interactively.
     */
    public static void main(String[] args) {
        // Constants that determine evolutionary rate and length.
        final int NUMBER_GENERATIONS = 10;
        final int MUTATION_RATE = 5;
        final int NUMBER_SEEDS = 5;

        // Constants to determine constraints.
        final int POPULATION_SIZE = 100;
        final int NUMBER_MEASURES = 2;
        final int BEATS_PER_MEASURE = 4;

        // Random number generator used to mutate melodies.
        Random random = new Random();

        // Hashmap of the score of seeds for next generation, and the seeds.
        HashMap<Double, String> seeds = new HashMap<Double, String>();

        // Populate our population with random melodies.
        String[] population = new String[POPULATION_SIZE];
        RandomMelody randomGenerator = new RandomMelody(
                NUMBER_MEASURES,
                BEATS_PER_MEASURE
        );
        for (int i = 0; i < population.length; i++) {
            population[i] = randomGenerator.getMelodyString();
        }

        // Create a temporary folder for our temp files for the neural net.
        createFolder("temp");

        // Evolve for {NUMBER_GENERATIONS} generations.
        for (int i = 0; i < NUMBER_GENERATIONS; i++) {

        }

        // TODO: rest of program

        deleteFolder("temp");
    }
}
