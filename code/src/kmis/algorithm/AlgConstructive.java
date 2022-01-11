package kmis.algorithm;

import kmis.Main;
import kmis.constructive.IConstructive;
import kmis.localSearch.ILocalSearch;
import kmis.structure.Instance;
import kmis.structure.Result;
import kmis.structure.Solution;

public class AlgConstructive implements IAlgorithm {

    private final int numSolutions;
    private final IConstructive constructive;
    private final ILocalSearch localSearch;
    private boolean useLocalSearch=true;

    public AlgConstructive(int numSolutions, IConstructive constructive,ILocalSearch localSearch){
        this.numSolutions=numSolutions;
        this.constructive=constructive;
        this.localSearch=localSearch;
        useLocalSearch=true;
    }

    public AlgConstructive(int numSolutions, IConstructive constructive){
        this.numSolutions=numSolutions;
        this.constructive=constructive;
        this.localSearch = null;
        useLocalSearch=false;
    }

    @Override
    public Result execute(Instance instance) {
        Solution bestSolution=null;
        int bestCardinality=0;
        long totalTime=System.currentTimeMillis();
        Result result=new Result(instance.getName());
        float secs;

        for(int i=1; i<=numSolutions;i++){
            Solution sol = constructive.construct(instance);
            if(useLocalSearch) sol=localSearch.execute(sol,instance);
            int solCardinality=sol.getObjectiveFunction();
            if(bestSolution==null || solCardinality>bestCardinality){
                bestCardinality=solCardinality;
                bestSolution=sol;
            }

            /*if (Main.de100en100 && i % 100 == 0 && i > 0) {
                long currentTime = System.currentTimeMillis() - totalTime;
                secs = currentTime / 1000f;
                result.add("OF_"+i,bestSolution.getObjectiveFunction());
                result.add("time_"+i,secs);
            }*/
        }
        totalTime = System.currentTimeMillis() - totalTime;
        secs = totalTime / 1000f;

        if(Main.DEBUG){
            System.out.print(instance.getName()+"\t");
            System.out.println(bestSolution.getObjectiveFunction()+"\t"+secs);
        } else{
            System.out.println(bestSolution.getObjectiveFunction());
        }

        result.add("OF"+Main.iter, bestSolution.getObjectiveFunction());
        result.add("time"+Main.iter, secs);

        return result;
    }

    public String toString() {

        String localSearchStr=useLocalSearch?","+localSearch.toString()+",":",";
        return this.getClass().getSimpleName()+"("+constructive.toString()+localSearchStr+numSolutions+")";
    }
}
