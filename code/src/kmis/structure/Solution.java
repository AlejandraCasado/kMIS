package kmis.structure;
import com.sun.source.tree.Tree;
import kmis.Main;

import java.lang.reflect.Array;
import java.util.*;

public class Solution {

    private Instance instance;
    private BitSet sol;
    private List<Integer> elementsSol;
    private Map<Integer,Integer> alternativeOF;

    public Solution(Instance instance){
        this.instance=instance;
        this.elementsSol=new ArrayList<>(instance.getNumElementsSol());
        sol=new BitSet();
        alternativeOF=new HashMap<Integer, Integer>();

    }

    public Solution(Solution sol) {
        copy(sol);
    }

    public void printSol(){
        System.out.print("Los elementos escogidos son: ");
        for (int e: elementsSol){
            System.out.print((e+1)+" ");
        }

        System.out.println();
        System.out.print("Tienen en común "+ sol.cardinality()+" elementos y son: ");
        for(int i=0;i<sol.length();i++){
            if(sol.get(i)){
                System.out.print(i+1+" ");
            }
        }
    }

    public void calculateAllJoins(){
        sol.clear();
        sol.or(instance.getConnections()[elementsSol.get(0)]);
        int size = elementsSol.size();
        for(int i=1; i<size;i++){
            sol.and(instance.getConnections()[elementsSol.get(i)]);
        }
    }

    public void add(int elem) {
        BitSet elemConn = instance.getConnections()[elem];
        if(elementsSol.isEmpty()){
            sol.or(elemConn);
        } else {
            sol.and(elemConn);
        }
        elementsSol.add(elem);
    }

    public Map<Integer, Integer> createAlternativeOF(){
        alternativeOF.clear();
        int []array=new int[instance.getNumElementsR()];

        for (int selected : elementsSol) {
            for (int i=0; i<instance.getNumElementsR(); i++){
                if(instance.getConnections()[selected].get(i))
                    array[i]++;
            }
        }
        for (int value : array) {
            int count = alternativeOF.getOrDefault(value, 0);
            alternativeOF.put(value, count+1);
        }
        return alternativeOF;
    }

    public int compare(Solution other){
        return compare(other.alternativeOF);
    }

    public int compare(Map<Integer, Integer> otherAlternativeOF) {

        for(int i=instance.getNumElementsR()-1;i>=0;i--){

            int solVal = alternativeOF.getOrDefault(i,0);
            int otherVal = otherAlternativeOF.getOrDefault(i, 0);
            if (solVal > otherVal) {
                //System.out.println(i+": "+solVal+" vs "+otherVal);
                return +1;
            } else if (solVal < otherVal) {
                return -1;
            }
        }
        return 0;
    }


    public void remove(int elem) {
        elementsSol.remove(Integer.valueOf(elem));
    }

    public int removeByPos(int pos) {
        return elementsSol.remove(pos);
    }

    // TODO solo copia referencias no clona
//    public Solution clone(){
//        Solution solution=new Solution(instance);
//        solution.sol= (BitSet) this.sol.clone();
//        solution.elementsSol= new ArrayList<>(this.elementsSol);
//        return solution;
//    }
    public void copy(Solution solution){
        this.instance = solution.instance;
        this.sol = new BitSet(instance.getNumElementsR());
        this.sol.or(solution.sol);
        this.elementsSol = new ArrayList<>(solution.elementsSol);
    }

    public List<Integer> getElementsSol() {
        return elementsSol;
    }

    public int getObjectiveFunction(){
        return sol.cardinality();
    }

    public BitSet getSol() {
        return sol;
    }

    public void setSol(BitSet sol) {
        this.sol = sol;
    }

    public void copySol(BitSet sol) {
        this.sol.clear();
        this.sol.or(sol);
    }


}
