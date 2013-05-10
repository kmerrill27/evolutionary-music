package evolmusic;

import java.io.*;
import java.util.*;


/**
 * @author Nicholas Cho
 */
public class EvolutionaryMusic {

    // Random number generator for mutation and for picking seeds.
    private static Random random = new Random();

    // Constants that determine evolutionary rate and length.
    private static final int NUMBER_GENERATIONS = 1000;
    private static final int MUTATION_RATE = 10;
    private static final int NUMBER_SEEDS = 40;

    // Constants to determine constraints.
    private static final int POPULATION_SIZE = 200;
    private static final int NUMBER_MEASURES = 2;
    private static final int BEATS_PER_MEASURE = 4;

    // Random melody generator.
    private static final RandomMelody randomGenerator = new RandomMelody(
            NUMBER_MEASURES,
            BEATS_PER_MEASURE
    );

    // Breeder to breed melodies.
    private static Breeder breeder = new Breeder(
            NUMBER_MEASURES,
            BEATS_PER_MEASURE
    );

    // Milestones to save midi files.
    private static final Set<Integer> MILESTONES =
            new HashSet<Integer>(Arrays.asList(new Integer[]{
                    1,
                    20,
                    50,
                    100,
                    200,
                    500,
                    1000
            }));

    // MelodyPlayer instance used to save midi files.
    private static final MelodyPlayer player = new MelodyPlayer();

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
        File[] contents = file.listFiles();
        if (file.isDirectory() && contents != null) {
            for (File c : contents)
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
     * Takes the population of melodies and writes them all to a file to be
     * tested by the neural net.
     *
     * @param filename The name of the file to write the melodies to.
     * @param population The population of melodies.
     */
    private static void writeMelodiesToFile(String filename,
                                            String[] population) {
        final String DEFAULT_SCORE = "0.0 ";

        // Create bitifier to translate JFugue compatible melodies to a
        // string of bits that can be read by the neural net.
        Bitifier bitifier = new Bitifier();

        PrintWriter w = null;
        try {
            w = new PrintWriter(new FileWriter(filename));
            w.println("1 424");

            for (String melody : population) {
                String bitString;

                // Translate melody and insert spaces between each bit.
                System.out.println(melody); // TODO REMOVE
                bitString = bitifier.translate(melody);
                bitString = bitString.replace("", " ").trim();

                // Write to melody file.
                w.println(DEFAULT_SCORE + bitString);
            }
        } catch (IOException e) {
            System.out.println("Error writing melodies to file for " +
                    "neural net. Exiting program...");
            System.exit(1);
        } finally {
            if (w != null)
                w.close();
        }
    }

    /**
     * Test the melodies in <inFile> and saves the scores to <outFile> using
     * the neural net.
     *
     * @param inFile Name of file with melodies.
     * @param outFile Name of file to write scores.
     * @param printOutput True if output of command should be written.
     */
    private static void testMelodies(String inFile, String outFile,
                                     boolean printOutput) {

        // Construct command to run the neural net.
        final String[] COMMAND = {
                "vendor/neural-net/test",
                "vendor/neural-net/licks.weights.save",
                inFile,
                outFile
        };

        // Run the command, and print output if user wanted it.
        Process process = null;
        try {
            process = Runtime.getRuntime().exec(COMMAND);

            if (printOutput) {
                BufferedReader in = new BufferedReader(
                        new InputStreamReader(process.getInputStream())
                );
                String line;
                while ((line = in.readLine()) != null) {
                    System.out.println(line);
                }
            }
        } catch (IOException e) {
            System.out.println(
                    "Error running command: " + Arrays.toString(COMMAND)
            );
            e.printStackTrace();
            System.exit(1);
        } finally {
            try {
                if (process != null) {
                    process.waitFor();
                }
            } catch (InterruptedException e) {
                System.out.println("Command was interrupted while executing." +
                        " Exiting...");
                e.printStackTrace();
                System.exit(1);
            }
        }
    }

    /**
     * Get the indices in <population> of the melodies with the highest
     * scores.
     *
     * @param scoreFile The file containing the scores of the melodies.
     * @param numSeeds The number of seeds we want (the top _ scores).
     * @return An array of the indices of the top melodies (in descending order
     *         of score).
     */
    private static int[] getSeedIndices(String scoreFile, int numSeeds) {
        // Read all the scores from the file
        HashMap<Double, Integer> scoreMap = new HashMap<Double, Integer>();
        int lineNumber = 1;
        BufferedReader r = null;
        try {
            r = new BufferedReader(new FileReader(scoreFile));

            // Read each line and add the score to hashmap with its index.
            String line;
            while ((line = r.readLine()) != null) {
                scoreMap.put(Double.parseDouble(line), lineNumber);
                lineNumber++;
            }
        } catch (FileNotFoundException e) {
            System.out.println("File not found. Exiting.");
            System.exit(1);
        } catch (IOException e) {
            System.out.println("Error reading file. Exiting");
            System.exit(1);
        } finally {
            // Close the read stream.
            if (r != null) {
                try {
                    r.close();
                } catch (IOException e) {
                    System.out.println("Error while closing file");
                    System.exit(1);
                }
            }
        }

        // Array of indices of highest scored melodies.
        int[] seeds = new int[numSeeds];

        // Get all the scores by themselves
        Set<Double> scores = scoreMap.keySet();

        for (int i = 0; i < numSeeds; i++) {
            // Sort the list to get the max score.
            List<Double> scoreList = new ArrayList<Double>(scores);
            Collections.sort(scoreList);
            double maxScore = scoreList.get(scoreList.size() - 1);

            // Grab the index of the max score in the map.
            int maxIndex = scoreMap.get(maxScore) - 1;

            // TODO remove
            System.out.println("Max Score: " + maxScore);
            System.out.println("Max Index: " + maxIndex);

            seeds[i] = maxIndex;
            scoreMap.remove(maxScore);
        }

        return seeds;
    }

    private static String getNewMelody(String[] seeds, int MUTATION_RATE) {
        String melodyOne, melodyTwo;
        int numSeeds = seeds.length;

        // First melody to breed.
        melodyOne = seeds[random.nextInt(numSeeds)];

        // Either breed the first melody with a random one (mutation),
        // or pick another of the seeds to breed with.
        if (random.nextInt(100) < (MUTATION_RATE - 1)) {
            melodyTwo = randomGenerator.getMelodyString();
        } else {
            melodyTwo = seeds[random.nextInt(numSeeds)];
            while (melodyTwo.equals(melodyOne)) {
                melodyTwo = seeds[random.nextInt(numSeeds)];
            }
        }

        // Breed them in the Breeder class then return the result.
        return breeder.breed(melodyOne, melodyTwo);
    }

    /**
     * Main method that checks commandline parameters and then runs the program
     * with either default or given values.
     *
     * @param args Commandline arguments. Use "-i" to run interactively.
     */
    public static void main(String[] args) {        // Hashmap of the score
        // of seeds for next generation, and the seeds.
        int[] seedIndices;

        // Populate our population with random melodies.
        String[] population = new String[POPULATION_SIZE];
        for (int i = 0; i < population.length; i++) {
            population[i] = randomGenerator.getMelodyString();
        }

        // Create a temporary folder for our temp files for the neural net.
        final String BASE_DIRECTORY = "melodies";
        deleteFolder(BASE_DIRECTORY);
        createFolder(BASE_DIRECTORY);

        // Evolve for {NUMBER_GENERATIONS} generations.
        for (int i = 0; i < NUMBER_GENERATIONS; i++) {
            final String GENERATION_DIR = BASE_DIRECTORY + "/" + (i + 1);
            final String MELODY_FILE = GENERATION_DIR + "/" + "melodies.in";
            final String SCORE_FILE = GENERATION_DIR + "/" + "scores.save";


            // Write melodies to a file to run the neural net on.
            createFolder(GENERATION_DIR);
            writeMelodiesToFile(MELODY_FILE, population);

            // Run the neural net.
            testMelodies(MELODY_FILE, SCORE_FILE, true);

            // Get the indices of the highest scores.
            seedIndices = getSeedIndices(SCORE_FILE, NUMBER_SEEDS);

            // If we have reached a milestone, save some midi files.
            if (MILESTONES.contains(i + 1)) {
                final String SAVE_FOLDER =
                        BASE_DIRECTORY + "/0GEN_" + (i + 1) + "SAVED";
                createFolder(SAVE_FOLDER);

                for (int index : seedIndices) {
                    final String SAVE_FILE =
                            SAVE_FOLDER + "/RANK_" + index + ".mid";
                    player.save(population[index], SAVE_FILE);
                }
            }

            // If we are on the last generation, we can break at this point.
            if (i + 1 == NUMBER_GENERATIONS) break;

            // Get the top melodies of this generation.
            String[] seeds = new String[NUMBER_SEEDS];
            for (int j = 0; j < seeds.length; j++) {
                seeds[j] = population[seedIndices[j]];
            }

            // Recreate the population for the next generation.
            population = new String[POPULATION_SIZE];

            // Copy over the first few from the seeds of last generation.
            System.arraycopy(seeds, 0, population, 0, seeds.length);

            // Breed the rest of the population.
            for (int n = seeds.length; n < population.length; n++) {
                System.out.println("hello"); // TODO remove
                population[n] = getNewMelody(seeds, MUTATION_RATE);
            }

            // TODO
        }

        // TODO: rest of program
    }
}
