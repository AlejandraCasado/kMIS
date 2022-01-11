package kmis.localSearch;

import kmis.Main;
import kmis.structure.Instance;
import kmis.structure.RandomManager;
import kmis.structure.Solution;

import java.util.*;

public class TabuSearch implements ILocalSearch{


    private Deque<Integer> tabuAddedInSol;
    private Set<Integer> tabuSetAddedInSol;
    private Deque<Integer> tabuRemovedFromSol;
    private Set<Integer> tabuSetRemovedFromSol;

    private boolean tabuListAdded=true;
    private boolean tabuListRemoved=false;

    private Solution bestSol;

    private float percentageMaxSolSave;
    private int maxAddedSave;
    private int maxRemovedSave;
    private int maxIterWithoutImprove=15;

    private final boolean reactive=false;
    private final boolean aspiration=false;

    private float minPercentageReactiveTabu=0.1f;
    private float maxPercentageReactiveTabu=0.5f;
    private float increaseValue=0.05f;

    public Solution execute(Solution sol, Instance instance){

        percentageMaxSolSave= Main.tenure;
        maxIterWithoutImprove= Main.iterWithoutImprove;

        float percentageReactive=0.25f;
        boolean improvement;

        List<Integer> unSelectedCopy=createUnselectedList(instance,sol);
        List<Integer> selectedCopy = copySelectedPosList(instance);

        int numUnselected=unSelectedCopy.size();
        int numSelected=sol.getElementsSol().size();

        maxAddedSave =(int)Math.ceil(numSelected* (percentageMaxSolSave+percentageReactive));
        maxRemovedSave =(int)Math.ceil(numUnselected* (percentageMaxSolSave+percentageReactive));

        tabuAddedInSol =new ArrayDeque<>(numSelected);
        tabuSetAddedInSol =new HashSet<>(numSelected);
        tabuRemovedFromSol =new ArrayDeque<>(numUnselected);
        tabuSetRemovedFromSol =new HashSet<>(numUnselected);

        BitSet testBitSet = new BitSet(instance.getNumElementsR());
        BitSet originalBitSet = new BitSet(instance.getNumElementsR());
        bestSol = new Solution(sol);

        int countWithoutImprove=0;
        while(countWithoutImprove < maxIterWithoutImprove){
            if(reactive) {
                maxAddedSave =(int)Math.ceil(numSelected* (percentageMaxSolSave+percentageReactive));

                while (tabuListAdded && tabuAddedInSol.size() > maxAddedSave) {
                    tabuSetAddedInSol.remove(tabuAddedInSol.removeFirst());
                }
                while (tabuListRemoved && tabuRemovedFromSol.size() > maxAddedSave) {
                    tabuSetRemovedFromSol.remove(tabuRemovedFromSol.removeFirst());
                }
            }
            improvement = false;
            Collections.shuffle(unSelectedCopy, RandomManager.getRandom());
            Collections.shuffle(selectedCopy, RandomManager.getRandom());
            BitSet bestBitSet = null;
            int bestRemoveNodePos=-1;
            int bestAddNodePos=-1;
            for (int i = 0; i < numSelected; i++) {
                int posSelectedNode = selectedCopy.get(i);
                int selectedNode = sol.getElementsSol().get(posSelectedNode);
                if(!aspiration && tabuListAdded && tabuSetAddedInSol.contains(selectedNode)) continue;
                clearBitSet(testBitSet, instance, sol, selectedNode);
                //testBitset contains the bitset of sol if selectedNode is removed
                originalBitSet.clear();
                originalBitSet.or(testBitSet);
                for(int j = 0; j < numUnselected; j++){
                    int unselectedNode = unSelectedCopy.get(j);
                    if(tabuListRemoved && tabuSetRemovedFromSol.contains(unselectedNode)) continue;
                    testBitSet.and(instance.getConnections()[unselectedNode]);
                    int solCard = bestSol.getSol().cardinality();
                    int testBitCard = testBitSet.cardinality();
                    if(solCard<testBitCard) {
                        countWithoutImprove=0;
                        improvement=true;
                        unSelectedCopy.remove(j);
                        unSelectedCopy.add(selectedNode);
                        sol.removeByPos(posSelectedNode);
                        sol.add(unselectedNode);
                        sol.copySol(testBitSet);
                        if(tabuSetAddedInSol.contains(selectedNode)){
                            tabuSetAddedInSol.remove(selectedNode);
                            removeFromTabu(selectedNode,tabuAddedInSol);
                        }
                        if(tabuSetRemovedFromSol.contains(selectedNode)){
                            tabuSetRemovedFromSol.remove(selectedNode);
                            removeFromTabu(selectedNode,tabuRemovedFromSol);
                        }
                        updateTabuLists(unselectedNode,selectedNode);
                        bestSol.copy(sol);
                        if(reactive && percentageMaxSolSave+percentageReactive>=minPercentageReactiveTabu) {
                            percentageReactive-=increaseValue;
                        }
                        break;
                    }else {
                        if(bestBitSet == null || bestBitSet.cardinality()<testBitCard){
                            if (bestBitSet == null) {
                                bestBitSet = new BitSet(instance.getNumElementsR());
                            } else {
                                bestBitSet.clear();
                            }

                            bestBitSet.or(testBitSet);
                            bestRemoveNodePos=posSelectedNode;
                            bestAddNodePos=j;
                        }
                    }

                    testBitSet.clear();
                    testBitSet.or(originalBitSet);
                }
                if(improvement){
                    break;
                }

            }
            if (!improvement) {
                if(reactive && percentageMaxSolSave+percentageReactive<maxPercentageReactiveTabu) {
                    percentageReactive+=increaseValue;
                }
                countWithoutImprove++;
                // If not improve, perform the less deteriorating movement

                int bestAddNode = unSelectedCopy.get(bestAddNodePos);
                int removed = sol.removeByPos(bestRemoveNodePos);

                updateTabuLists(bestAddNode,removed);

                sol.add(bestAddNode);
                sol.copySol(bestBitSet);
                unSelectedCopy.remove(bestAddNodePos);
                unSelectedCopy.add(removed);
            }
        }
        sol.copy(bestSol);

        return sol;
    }

    private void updateTabu(Deque <Integer> tabuList, Set <Integer> tabuSet,int bestAddNode, int max){
        if(tabuList.size()== max){
            tabuSet.remove(tabuList.removeFirst());
        }
        tabuList.add(bestAddNode);
        tabuSet.add(bestAddNode);
    }

    private void removeFromTabu(int node, Deque<Integer> tabuList){

        boolean found=false;
        int removed=-1;
        Deque<Integer> stack =new ArrayDeque<>(tabuList.size());

        while (!found){
            removed=tabuList.removeFirst();
            if(removed==node) found=true;
            else stack.add(removed);
        }

        while(!stack.isEmpty()){
            tabuList.addFirst(stack.removeLast());
        }
    }

    private void updateTabuLists(int add, int remove){
        if(tabuListAdded) {
            updateTabu(tabuAddedInSol, tabuSetAddedInSol, add, maxAddedSave);
        }
        if(tabuListRemoved){
            updateTabu(tabuRemovedFromSol,tabuSetRemovedFromSol,remove, maxRemovedSave);
        }
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

    private List<Integer> createUnselectedList(Instance instance, Solution sol){
        List<Integer> unSelected=new ArrayList<>(instance.getNumElementsL());
        for (int i = 0; i < instance.getNumElementsL(); i++) {
            if(!sol.getElementsSol().contains(i)){
                unSelected.add(i);
            }
        }
        return unSelected;
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
        String type="";
        if(reactive) type+="Reactive";
        if(aspiration) type+= "AspirationCriterion";
        if(tabuListRemoved) type+="RemovedTabuList";

        return " TabuSearch"+type+'('+percentageMaxSolSave+','+maxIterWithoutImprove+')';
    }
}
