package kmis.algorithm;

import kmis.Main;
import kmis.constructive.IConstructive;
import kmis.localSearch.ILocalSearch;
import kmis.structure.Instance;
import kmis.structure.RandomManager;
import kmis.structure.Result;
import kmis.structure.Solution;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.Random;

public class AlgIteratedGreedyBestSol implements IAlgorithm {

    private float beta;
    private int numSolutions;
    private IConstructive constructive;
    private ILocalSearch localSearch;
    private int maxIterWithoutImprove=10;
    private final boolean randomDestruct=true;
    private final boolean randomConstruct=false;
    private Random rnd;

    public AlgIteratedGreedyBestSol(int numSolutions, IConstructive constructive, ILocalSearch localSearch){
        this.numSolutions=numSolutions;
        this.constructive=constructive;
        this.localSearch=localSearch;
    }

    public AlgIteratedGreedyBestSol(int numSolutions, IConstructive constructive, ILocalSearch localSearch, float beta){
        this.numSolutions=numSolutions;
        this.constructive=constructive;
        this.localSearch=localSearch;
        this.beta=beta;
    }

    public Result execute(Instance instance){

        beta=Main.beta;

        rnd= RandomManager.getRandom();

        int numElemsToDestruct=(int)Math.ceil(beta*instance.getNumElementsSol());
        List<Integer> nonselected=new ArrayList<>(instance.getNumElementsL());

        long totalTime=System.currentTimeMillis();
        Result result=new Result(instance.getName());
        float secs;
        int countWithoutImprove=0;
        int bestCardinality=0;
        int objetiveValue;

        Solution bestSolution=null;
        Solution bestSolutionIterated;

        for(int i=0; i<numSolutions; i++){
            Solution solution = constructive.construct(instance);
            solution=localSearch.execute(solution,instance);

            if(bestSolution==null) bestSolution=new Solution(solution);
            else bestCardinality=checkBestSol(bestSolution,solution);

        }

        bestSolutionIterated=new Solution(bestSolution);

        while(countWithoutImprove<maxIterWithoutImprove){
            destruct(bestSolutionIterated,numElemsToDestruct, instance);
            construct(bestSolutionIterated,instance,nonselected);
            bestSolutionIterated=localSearch.execute(bestSolutionIterated,instance);
            objetiveValue=bestSolutionIterated.getObjectiveFunction();
            if(objetiveValue>bestCardinality){
                bestCardinality=objetiveValue;
                bestSolution.copy(bestSolutionIterated);
                countWithoutImprove=0;
            } else{
                countWithoutImprove++;
                bestSolutionIterated.copy(bestSolution);
            }
        }

        totalTime = System.currentTimeMillis() - totalTime;
        secs = totalTime / 1000f;
        bestSolution.calculateAllJoins();
        if(Main.DEBUG){
            System.out.print(instance.getName()+"\t");
            System.out.println(bestSolution.getObjectiveFunction()+"\t"+secs);
        } else{
            System.out.println(bestSolution.getObjectiveFunction());
        }

        result.add("OF",bestSolution.getObjectiveFunction());
        result.add("time",secs);
        return result;
    }

    private void destruct(Solution sol, int numElemsToDestruct, Instance instance){

        if(randomDestruct){
            int removeIndex=-1;
            for (int i = 0; i < numElemsToDestruct; i++) {
                removeIndex=rnd.nextInt(sol.getElementsSol().size());
                sol.removeByPos(removeIndex);
            }

        } else{

            for (int i = 0; i < numElemsToDestruct; i++) {
                int bestCardinality=-1;
                int worstElementIndex=-1;
                BitSet bitSet=new BitSet(sol.getElementsSol().size());
                for (int j=0;j<sol.getElementsSol().size();j++) {
                    int currentCardinality=0;
                    bitSet.clear();
                    bitSet.set(0, sol.getElementsSol().size(), true);
                    for(int k=0; k<sol.getElementsSol().size();k++){
                        if(j!=k){
                            int selectedNode=sol.getElementsSol().get(k);
                            bitSet.and(instance.getConnections()[selectedNode]);
                        }

                    }
                    currentCardinality=bitSet.cardinality();
                    if(currentCardinality>bestCardinality){
                        bestCardinality=currentCardinality;
                        worstElementIndex=j;
                    }
                }

                sol.removeByPos(worstElementIndex);
            }
        }
        sol.calculateAllJoins();

    }

    private void construct(Solution sol,Instance instance,List<Integer> nonSelected){

        for (int i = 0; i < instance.getNumElementsL(); i++) {
            if(!sol.getElementsSol().contains(i)){
                nonSelected.add(i);
            }
        }

        if(randomConstruct){
            while(sol.getElementsSol().size()<instance.getNumElementsSol()){
                sol.add(nonSelected.remove(rnd.nextInt(nonSelected.size())));
            }
        } else{
            while(sol.getElementsSol().size()<instance.getNumElementsSol()){
                BitSet bitSet=new BitSet(sol.getElementsSol().size());
                int bestIndex=-1;
                int bestCardinality=-1;
                for (int j=0;j<nonSelected.size();j++) {
                    bitSet.clear();
                    bitSet.or(sol.getSol());
                    int nonSelectedNode=nonSelected.get(j);
                    bitSet.and(instance.getConnections()[nonSelectedNode]);

                    int currentCardinality=bitSet.cardinality();

                    if(bestCardinality<currentCardinality){
                        bestCardinality=currentCardinality;
                        bestIndex=j;
                    }
                }
                sol.add(nonSelected.remove(bestIndex));
            }
        }
        nonSelected.clear();

    }

    private int checkBestSol(Solution bestSolution, Solution solution){
        int objetiveValue=solution.getObjectiveFunction();
        int bestCardinality=bestSolution.getObjectiveFunction();
        if(objetiveValue>bestCardinality){
            bestCardinality=objetiveValue;
            bestSolution.copy(solution);
        }
        return bestCardinality;
    }

    public String toString() {
        String typeDestruct = randomDestruct ? "Random" : "Greedy";
        String typeConstruct = randomConstruct ? "Random" : "Greedy";
        return this.getClass().getSimpleName()+typeDestruct+typeConstruct+"beta"+beta+"iteraciones"+maxIterWithoutImprove+"("+constructive.toString()+localSearch.toString()+numSolutions+")";
    }
}
