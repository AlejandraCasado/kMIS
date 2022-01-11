package kmis.constructive;

import kmis.Main;
import kmis.structure.Instance;
import kmis.structure.RandomManager;
import kmis.structure.Solution;

import java.util.*;

public class GRASPRGConstructive implements IConstructive{
    private class Candidate{
        public int element;
        public int value;

        public Candidate(int element){
            this.element=element;
        }
    }

    private float alpha;
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

        int numRandomSelect=(int)Math.ceil(alpha*instance.getNumElementsL());

        candidates=new ArrayList<>(instance.getNumElementsL());
        Solution sol=new Solution(instance);

        int selectedNode= rnd.nextInt(instance.getNumElementsL());
        sol.add(selectedNode);

        for (int i = 0; i < instance.getNumElementsL(); i++) {
            if(selectedNode!=i){
                candidates.add(new Candidate(i));
            }
        }

        while(sol.getElementsSol().size()<instance.getNumElementsSol()){
            selectCandidate(numRandomSelect, instance, sol);
            numRandomSelect=(int)Math.ceil(alpha*candidates.size());
        }

        return sol;
    }

    private void selectCandidate(int num, Instance instance, Solution sol){
        BitSet candidateBitset = new BitSet(instance.getNumElementsR());
        int max=-1;
        Candidate bestCandidate=null;
        int bestCandidateIndex=-1;
        Random rnd = RandomManager.getRandom();
        for (int i = 0; i < num; i++) {
            int idx = rnd.nextInt(candidates.size());
            Candidate candidate = candidates.get(idx);
            candidateBitset.or(instance.getConnections()[candidate.element]);
            candidateBitset.and(sol.getSol());
            candidate.value=candidateBitset.cardinality();
            if(candidate.value>max) {
                max=candidate.value;
                bestCandidate=candidate;
                bestCandidateIndex=idx;
            }
            candidateBitset.clear();
        }
        sol.add(bestCandidate.element);
        candidates.remove(bestCandidateIndex);

    }

    @Override
    public String toString() {
        String alphaStr=randomAlpha?"(random)":"("+alpha+")";
        return this.getClass().getSimpleName()+ alphaStr;
    }
}
