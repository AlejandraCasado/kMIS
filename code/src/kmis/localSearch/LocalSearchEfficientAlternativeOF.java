package kmis.localSearch;

import kmis.structure.Instance;
import kmis.structure.RandomManager;
import kmis.structure.Solution;

import java.util.*;

public class LocalSearchEfficientAlternativeOF implements ILocalSearch{

    private final boolean firstImprovement=true;

    public Solution execute(Solution sol, Instance instance){

        List<Integer> unSelectedCopy=createUnselectedList(instance,sol);
        List<Integer> selectedCopy = copySelectedPosList(instance);

        int numUnselected=unSelectedCopy.size();
        int numSelected=sol.getElementsSol().size();
        boolean improvement=true;

        BitSet testBitSet = new BitSet(instance.getNumElementsR());
        BitSet originalBitSet = new BitSet(instance.getNumElementsR());
        Map<Integer, Integer> alternativeOF;
        while(improvement){
            improvement = false;
            Collections.shuffle(unSelectedCopy, RandomManager.getRandom());
            Collections.shuffle(selectedCopy, RandomManager.getRandom());
            for (int i = 0; i < numSelected; i++) {
                int posSelectedNode = selectedCopy.get(i);
                int selectedNode = sol.getElementsSol().get(posSelectedNode);
                clearBitSet(testBitSet, instance, sol, selectedNode);
                originalBitSet.clear();
                originalBitSet.or(testBitSet);
                for(int j = 0; j < numUnselected; j++){
                    int unselectedNode = unSelectedCopy.get(j);
                    testBitSet.and(instance.getConnections()[unselectedNode]);
                    alternativeOF = createAlternativeOF(sol,posSelectedNode,unselectedNode,testBitSet);
                    //alternativeOF = createAlternativeOFEfficient(sol,instance, selectedNode,unselectedNode);

                    int solCard = sol.getSol().cardinality();
                    int testBitCard = testBitSet.cardinality();

                    if(solCard<testBitCard || (solCard == testBitCard && sol.compare(alternativeOF) > 0) ) {
                        improvement=true;
                        unSelectedCopy.remove(j);
                        unSelectedCopy.add(selectedNode);
                        sol.removeByPos(posSelectedNode);
                        sol.add(unselectedNode);
                        sol.copySol(testBitSet);
                        sol.createAlternativeOF();
                        break;
                    }

                    testBitSet.clear();
                    testBitSet.or(originalBitSet);
                }

                if(improvement && firstImprovement) {
                    break;
                }
            }
        }

        return sol;
    }

    private void clearBitSet(BitSet testBitSet, Instance instance, Solution sol, int remove) {
        testBitSet.clear();
        boolean first = true;
        for (int s : sol.getElementsSol()) {
            if (s != remove) {
                if (first) {
                    testBitSet.or(instance.getConnections()[s]);
                    first = false;
                } else {
                    testBitSet.and(instance.getConnections()[s]);
                }
            }
        }
    }

    public Map<Integer, Integer> createAlternativeOF(Solution sol,int removeNode, int addNode,BitSet testBitset){

        BitSet bitset= (BitSet) testBitset.clone();
        Solution solAux = new Solution(sol);
        solAux.removeByPos(removeNode);
        solAux.add(addNode);
        solAux.setSol(bitset);

        return solAux.createAlternativeOF();
    }

    public Map<Integer, Integer> createAlternativeOFEfficient(Solution sol, Instance instance, int removeNode, int addNode) {
        int []array=new int[instance.getNumElementsR()];

        for (int selected : sol.getElementsSol()) {
            if (selected != removeNode) {
                for (int i = 0; i < instance.getNumElementsR(); i++) {
                    if (instance.getConnections()[selected].get(i))
                        array[i]++;
                }
            }
        }
        for (int i = 0; i < instance.getNumElementsR(); i++) {
            if (instance.getConnections()[addNode].get(i))
                array[i]++;
        }
        Map<Integer, Integer> alternativeOF = new HashMap<>();
        for (int value : array) {
            int count = alternativeOF.getOrDefault(value, 0);
            alternativeOF.put(value, count+1);
        }
        return alternativeOF;
    }

    private List<Integer> createUnselectedList(Instance instance, Solution sol){
        List<Integer> unSelected=new ArrayList<>(instance.getNumElementsL());
        for (int i = 0; i < instance.getNumElementsL(); i++) {
            if(!sol.getElementsSol().contains(i)){
                unSelected.add(i);
            }
        }
        return unSelected;
    }

    private List<Integer> copySelectedList(List<Integer> selected){
        List<Integer> selectedCopy=new ArrayList<>(selected.size());
        selectedCopy.addAll(selected);
        return selectedCopy;
    }

    private List<Integer> copySelectedPosList(Instance instance) {
        int p = instance.getNumElementsSol();
        List<Integer> selectedCopy = new ArrayList<>(p);
        for (int i = 0; i < p; i++) {
            selectedCopy.add(i);
        }
        return selectedCopy;
    }

    @Override
    public String toString() {
        String improvement=firstImprovement? "firstImprovement":"bestImprovement";
        return " LocalSearchEfficientAlternativeOF(" + improvement+ ')';
    }
}
