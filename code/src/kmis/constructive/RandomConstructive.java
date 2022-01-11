package kmis.constructive;

import kmis.structure.Instance;
import kmis.structure.RandomManager;
import kmis.structure.Solution;

import java.util.*;

public class RandomConstructive implements IConstructive{

    public Solution construct(Instance instance) {
        List<Integer> elements=new ArrayList<>(instance.getNumElementsL());
        Random rnd= RandomManager.getRandom();

        for (int i=0;i<instance.getNumElementsL();i++){
            elements.add(i);
        }
        Collections.shuffle(elements,rnd);
        Solution solution=new Solution(instance);

        for (int i=0; i<instance.getNumElementsSol();i++){
            solution.add(elements.get(i));
        }

        return solution;
    }

    public String toString() {
        return this.getClass().getSimpleName();
    }
}
