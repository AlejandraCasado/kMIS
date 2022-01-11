package kmis.constructive;

import kmis.Main;
import kmis.structure.Instance;
import kmis.structure.RandomManager;
import kmis.structure.Solution;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.Random;

public class GRASPGRConstructive implements IConstructive{
    private class Candidate{
        public int element;
        public int value;

        public Candidate(int element){
            this.element=element;
        }
    }

    private int gmax;
    private int gmin;
    private float alpha;
    private int[] rcl;
    private int lastInRCL;
    private boolean randomAlpha=false;

    private List<Candidate> candidates;

    @Override
    public Solution construct(Instance instance) {

        alpha= Main.alpha;

        Random rnd=RandomManager.getRandom();

        if(alpha==1) {
            alpha=rnd.nextFloat();
            randomAlpha=true;
        }

        candidates=new ArrayList<>(instance.getNumElementsL());
        rcl=new int[instance.getNumElementsL()];
        lastInRCL = 0;
        Solution sol=new Solution(instance);

        int selectedNode= rnd.nextInt(instance.getNumElementsL());
        sol.add(selectedNode);

        for (int i = 0; i < instance.getNumElementsL(); i++) {
            if(selectedNode!=i){
                candidates.add(new Candidate(i));
            }
        }

        while(sol.getElementsSol().size()<instance.getNumElementsSol()){
            calculateGreedyFunction(instance,sol);
            chooseBestCandidates();
            selectedNode=rcl[rnd.nextInt(lastInRCL)];
            Candidate candidate = candidates.remove(selectedNode);
            sol.add(candidate.element);
            lastInRCL = 0;
        }

        return sol;
    }

    private void calculateGreedyFunction(Instance instance, Solution sol){
        BitSet candidateBitset = new BitSet(instance.getNumElementsR());

        gmin=0x3f3f3f;
        gmax=0;
        for (Candidate candidate : candidates) {
            candidateBitset.or(instance.getConnections()[candidate.element]);
            candidateBitset.and(sol.getSol());
            candidate.value=candidateBitset.cardinality();
            if(candidate.value>gmax) gmax=candidate.value;
            if(candidate.value<gmin) gmin=candidate.value;
            candidateBitset.clear();
        }

    }

    private void chooseBestCandidates(){
        float limit=gmax-alpha*(gmax-gmin);

        for (int i = 0; i < candidates.size(); i++) {
            Candidate candidate = candidates.get(i);
            if(candidate.value>=limit) {
                rcl[lastInRCL] = i;
                lastInRCL++;
            };
        }
    }

    @Override
    public String toString() {
        String alphaStr=randomAlpha?"(random)":"("+alpha+")";
        return this.getClass().getSimpleName()+ alphaStr;
    }
}
