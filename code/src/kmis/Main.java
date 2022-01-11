package kmis;

import kmis.algorithm.AlgConstructive;
import kmis.algorithm.AlgIteratedGreedy;
import kmis.algorithm.AlgIteratedGreedyBestSol;
import kmis.algorithm.IAlgorithm;
import kmis.constructive.GRASPGRConstructive;
import kmis.constructive.GRASPRGConstructive;
import kmis.constructive.IConstructive;
import kmis.constructive.RandomConstructive;
import kmis.localSearch.*;
import kmis.structure.Instance;
import kmis.structure.RandomManager;
import kmis.structure.Result;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

public class Main {

    final static String pathFolder = "./instances";
    static ArrayList<Instance> instances;

    final static boolean readAllFolders = true;
    final static boolean readAllInstances = true;
    final static boolean readFromInput = false;

    final static String folderIndex = "type3";
    final static String instanceIndex = "classe_2_192_240.txt";

    static List<String> foldersNames;
    static List<String> instancesNames;
    static String instanceFolderPath;

    static final int numSolutions=1000;
    static public final boolean de100en100=false;

    final public static boolean DEBUG = true;

    static IConstructive randomConstructive = new RandomConstructive();
    static IConstructive graspGRConstructive =new GRASPGRConstructive();
    static IConstructive graspRGConstructive =new GRASPRGConstructive();

    static ILocalSearch localSearch=new LocalSearch();
    static ILocalSearch localSearchEfficient=new LocalSearchEfficient();
    static ILocalSearch tabuSearch=new TabuSearch();

    static float [] alphas=new float[]{0.25f,0.5f,0.75f,1f}; //alpha=1->random
    static public float alpha=0.75f; //alpha= 0.75 para el artículo, alpha= RND para el congreso
    private static final boolean multipleAlpha=false;

    static float [] betas=new float[]{0.1f,0.2f,0.3f,0.4f,0.5f};
    static public float beta=0.3f;
    private static final boolean multipleBeta=false;

    static float [] tenures=new float[]{0.1f,0.2f,0.3f,0.4f,0.5f};
    static public float tenure=0.5f;
    private static final boolean multipleTenure=false;

    static Integer [] itersWithoutImprove=new Integer[]{5,10,15,20,25};
    static public int iterWithoutImprove=5;
    private static final boolean multipleItersWithoutImprove=false;

    final static int seed=13;
    public static int iter=0;

    public static void main(String[] args) {
        readData();
        //AlgConstructive algConstructive=new AlgConstructive(numSolutions, graspGRConstructive);
        //AlgIteratedGreedyBestSol algConstructive=new AlgIteratedGreedyBestSol(numSolutions, graspGRConstructive, localSearchEfficient);
        AlgConstructive algConstructive=new AlgConstructive(numSolutions, graspRGConstructive, tabuSearch);
        //AlgIteratedGreedyBestSol algConstructive=new AlgIteratedGreedyBestSol(numSolutions, graspGRConstructive,localSearchEfficient);
        //AlgConstructive algConstructive=new AlgConstructive(numSolutions, graspRGConstructive,localSearchEfficient);
        //AlgIteratedGreedy algConstructive=new AlgIteratedGreedy(numSolutions, graspRGConstructive,localSearchEfficient);
        //AlgIteratedGreedyBestSol algConstructive=new AlgIteratedGreedyBestSol(numSolutions, graspRGConstructive,tabuSearch);

        RandomManager.setSeed(seed);
        
        for(int i=0;i<10;i++){
            iter++;
            if(multipleAlpha){
                for (float a : alphas) {
                    alpha=a;
                    executeInstances(algConstructive);
                }
            } else if(multipleTenure){
                for (float t : tenures) {
                    tenure=t;
                    if(multipleItersWithoutImprove){
                        for(int iter: itersWithoutImprove){
                            iterWithoutImprove=iter;
                            executeInstances(algConstructive);
                        }
                        executeInstances(algConstructive);
                    } else{
                        executeInstances(algConstructive);
                    }

                }
            } else{
                if(multipleBeta){
                    for (float b : betas) {
                        beta=b;
                        executeInstances(algConstructive);
                    }
                } else{
                    executeInstances(algConstructive);
                }
            }
        }

    }

    private static void executeInstances(IAlgorithm algConstructive){
        List<Result> results = new ArrayList<>();
        for (Instance instance:instances) {
            Result result=algConstructive.execute(instance);
            results.add(result);
        }
        printResults("./results/"+algConstructive.toString()+".csv", results);
    }

    private static void printResults(String path, List<Result> results) {
        try (PrintWriter pw = new PrintWriter(path)) {
            List<String> headers = new ArrayList<>(results.get(0).getKeys());
            //pw.print("Instance");
            int nElems = 0;
            for (int i = 0; i < headers.size(); i++) {
                String header = headers.get(i);
                pw.print(header);
                //pw.print(","+header);
                if (i < headers.size()-1) pw.print(",");
                nElems++;
            }
            pw.println();

            for (Result result : results) {
                //pw.print(result.getInstanceName());
                for (int i = 0; i < nElems; i++) {
                    //pw.print(","+result.get(i));
                    pw.print(result.get(i));
                    if (i < nElems-1) pw.print(",");
                }
                pw.println();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void readData(){
        instances = new ArrayList<>();
        foldersNames = Arrays.asList(new File(pathFolder).list());

        if(readFromInput){
            readInstanceFromInput();
        } else {
            if(readAllFolders) readAllFolders();
            else if (foldersNames.contains(folderIndex)) readFolder(folderIndex);
            else System.out.println("Folder index exceeds the bounds of the array");
        }
    }

    private static void readInstanceFromInput(){
        Scanner sc = new Scanner(System.in);
        String line;
        String[] lineContent;
        int elementL;
        int elementR;
        int numElementsL;
        int numElementsR;
        int numEdges;
        int numElementsSol;
        BitSet []connections;
        line = sc.nextLine();
        lineContent = line.split(" ");

        numElementsL = Integer.parseInt(lineContent[0]);
        numElementsR = Integer.parseInt(lineContent[1]);
        numEdges=Integer.parseInt(lineContent[2]);
        numElementsSol=Integer.parseInt(lineContent[3]);
        connections = new BitSet[numEdges];

        for (int i=0; i< connections.length;i++){
            connections[i]=new BitSet(numElementsR);
            line = sc.nextLine();
            lineContent = line.split(" ");
            elementL = (Integer.parseInt(lineContent[0]))-1;
            elementR = (Integer.parseInt(lineContent[1]))-1;
            connections[elementL].set(elementR);
        }
        instances.add(new Instance(connections, numElementsSol, numEdges, numElementsR, numElementsL));
    }

    private static void readAllFolders(){
        instances = new ArrayList<>();
        String [] folders =new File(pathFolder).list();

        for(String fileName : folders){
            readFolder(fileName);
        }
    }

    private static void readFolder(String fileName){
        File file;
        file=new File(pathFolder+"/"+fileName);
        if(!fileName.startsWith(".") && !fileName.startsWith("..") && file.isDirectory()){
            instancesNames = Arrays.asList(file.list());
            instanceFolderPath = file.getPath() + "/";
            if(readAllInstances) readAllInstances();
            else if (instancesNames.contains(instanceIndex)) readInstance(instanceIndex);
            else System.out.println("Instance index exceeds the bounds of the array");
        }
    }

    private static void readAllInstances(){
        for(String instanceName : instancesNames){
            if(!instanceName.startsWith(".") && !instanceName.startsWith(".."))
                readInstance(instanceName);
        }
    }

    private static void readInstance(String instanceName){
        instances.add(new Instance(instanceFolderPath +instanceName));
    }
}
