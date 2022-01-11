package kmis.structure;

import java.util.Random;

public abstract class RandomManager {

	private static Random rnd;
	private static Random[] parallelRnd;
	
	public static void setSeed(long value) {
		rnd = new Random(value);
	}
	
	public static Random getRandom() {
		if (rnd == null) {
			System.err.println("Random seed not initialized.");
			System.exit(-1);
		}
		return rnd;
	}
	
	public static void initParallel(int nRandoms, long initSeed) {
	    Random rndStart = new Random(initSeed);
	    parallelRnd = new Random[nRandoms];
        for (int i = 0; i < parallelRnd.length; i++) {
            parallelRnd[i] = new Random(rndStart.nextInt());
        }
    }

    public static Random getRandom(int i) {
	    return parallelRnd[i];
    }
}
