package kmis.localSearch;

import kmis.structure.Instance;
import kmis.structure.RandomManager;
import kmis.structure.Solution;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.List;

public class LocalSearch implements ILocalSearch{

    private final boolean firstImprovement=true;

    public Solution execute(Solution sol, Instance instance){


        List<Integer> unSelectedCopy=createUnselectedList(instance,sol);
        //List<Integer> selectedCopy=copySelectedList(sol.getElementsSol());
        List<Integer> selectedCopy = copySelectedPosList(instance);

        int numUnselected=unSelectedCopy.size();
        int numSelected=sol.getElementsSol().size();
        int solValue=sol.getSol().cardinality();
        boolean improvement=true;

        while(improvement){
            improvement = false;
            Collections.shuffle(unSelectedCopy, RandomManager.getRandom());
            Collections.shuffle(selectedCopy, RandomManager.getRandom());
            for (int i = 0; i < numSelected; i++) {
                //BitSet solBitset= new BitSet(instance.getNumElementsR());
                //solBitset.or(sol.getSol());
                int posSelectedNode = selectedCopy.get(i);
                int selectedNode = sol.getElementsSol().get(posSelectedNode);
                sol.removeByPos(posSelectedNode);
                sol.calculateAllJoins();
                for(int j = 0; j < numUnselected; j++){
                    int unselectedNode = unSelectedCopy.get(j);
                    sol.add(unselectedNode);

                    if(solValue<sol.getSol().cardinality()) {
                        solValue=sol.getSol().cardinality();
                        improvement=true;
                        unSelectedCopy.remove(j);
                        unSelectedCopy.add(selectedNode);
                        break;
                    }
                    sol.removeByPos(numSelected-1);
                    sol.calculateAllJoins();

                }

                //sol.setSol(solBitset);
                if(improvement && firstImprovement) {
                    break;
                }
                sol.add(selectedNode);
            }

        }
        //sol.calculateAllJoins();

        return sol;
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
        return " LocalSearch(" + improvement+ ')';
    }
}
