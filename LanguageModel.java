import java.util.HashMap;
import java.util.Random;


public class LanguageModel {

    // The map of this model. Maps windows to lists of character data objects.
    HashMap<String, List> CharDataMap;
    
    // The window length used in this model.
    int windowLength;
    
    // The random number generator used by this model. 
    private Random randomGenerator;

    /** Constructs a language model with a fixed seed value. */
    public LanguageModel(int windowLength, int seed) {
        this.windowLength = windowLength;
        randomGenerator = new Random(seed);
        CharDataMap = new HashMap<String, List>();
    }

    /** Constructs a language model with a random seed. */
    public LanguageModel(int windowLength) {
        this.windowLength = windowLength;
        randomGenerator = new Random();
        CharDataMap = new HashMap<String, List>();
    }

    /** Builds the language model from the corpus file. */
    public void train(String fileName) {
        String window = "";
        char c;
        In in = new In(fileName);

        // Form the first window [cite: 374, 375]
        for (int i = 0; i < windowLength; i++) {
            if (!in.isEmpty()) {
                window += in.readChar();
            }
        }

        // Process the rest of the file [cite: 376]
        while (!in.isEmpty()) {
            c = in.readChar();
            List probs = CharDataMap.get(window);
            if (probs == null) {
                probs = new List();
                CharDataMap.put(window, probs);
            }
            probs.update(c);
            window = window.substring(1) + c; // Shift window [cite: 392]
        }

        // Calculate probabilities for all lists in the map [cite: 397-399]
        for (List probs : CharDataMap.values()) {
            calculateProbabilities(probs);
        }
    }

    /** Computes p and cp fields for all characters in the list[cite: 119, 120]. */
    void calculateProbabilities(List probs) { 
        int totalCount = 0;
        for (int i = 0; i < probs.getSize(); i++) {
            totalCount += probs.get(i).count;
        }

        double cp = 0;
        for (int i = 0; i < probs.getSize(); i++) {
            CharData cd = probs.get(i);
            cd.p = (double) cd.count / totalCount;
            cp += cd.p;
            cd.cp = cp;
        }
    }

    /** Returns a random character using Monte Carlo technique[cite: 136, 143]. */
    char getRandomChar(List probs) {
        double r = randomGenerator.nextDouble(); // [cite: 259]
        for (int i = 0; i < probs.getSize(); i++) {
            if (probs.get(i).cp > r) {
                return probs.get(i).chr;
            }
        }
        return probs.get(probs.getSize() - 1).chr;
    }

    /** Generates random text based on learned patterns[cite: 205, 233]. */
    public String generate(String initialText, int textLength) {
        if (initialText.length() < windowLength) {
            return initialText;
        }

        StringBuilder generatedText = new StringBuilder(initialText);
        String window = initialText.substring(initialText.length() - windowLength);

        while (generatedText.length() < textLength) {
            List probs = CharDataMap.get(window);
            if (probs == null) {
                break; 
            }
            char nextChar = getRandomChar(probs);
            generatedText.append(nextChar);
            window = generatedText.substring(generatedText.length() - windowLength);
        }

        return generatedText.toString();
    }

    public String toString() {
        StringBuilder str = new StringBuilder();
        for (String key : CharDataMap.keySet()) {
            List keyProbs = CharDataMap.get(key);
            str.append(key + " : " + keyProbs + "\n");
        }
        return str.toString();
    }

    public static void main(String[] args) {
        int windowLength = Integer.parseInt(args[0]);
        String initialText = args[1];
        int generatedTextLength = Integer.parseInt(args[2]);
        boolean randomGeneration = args[3].equals("random");
        String fileName = args[4];

        LanguageModel lm;
        if (randomGeneration) {
            lm = new LanguageModel(windowLength);
        } else {
            lm = new LanguageModel(windowLength, 20);
        }

        lm.train(fileName);
        System.out.println(lm.generate(initialText, generatedTextLength));
    }
}