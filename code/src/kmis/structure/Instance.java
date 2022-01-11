package kmis.structure;

import java.io.*;
import java.util.BitSet;

public class Instance {

    private String path;
    private String name;

    private int numElementsL;
    private int numElementsR;
    private int numEdges;
    private int numElementsSol;
    private BitSet[] connections;

    public Instance(String path){
        this.path = path;
        this.name = path.substring(Math.max(path.lastIndexOf("/"), path.lastIndexOf("\\"))+1).replace(".txt", "");
        readInstance();
    }

    public Instance(BitSet[] connections, int numElementsSol, int numEdges, int numElementsR, int numElementsL){
        this.connections =connections;
        this.numEdges=numEdges;
        this.numElementsSol=numElementsSol;
        this.numElementsR=numElementsR;
        this.numElementsL=numElementsL;
    }

    private void readInstance(){
        try (BufferedReader br = new BufferedReader(new FileReader(path))){
            String line;
            String[] lineContent;
            int elementL;
            int elementR;
            line = br.readLine();
            lineContent = line.split(" ");

            numElementsL = Integer.parseInt(lineContent[0]);
            numElementsR = Integer.parseInt(lineContent[1]);
            numEdges=Integer.parseInt(lineContent[2]);
            numElementsSol=Integer.parseInt(lineContent[3]);
            connections = new BitSet[numEdges];

            for (int i = 0; i< connections.length; i++){
                connections[i]=new BitSet(numElementsR);
                line = br.readLine();
                lineContent = line.split(" ");
                elementL = (Integer.parseInt(lineContent[0]))-1;
                elementR = (Integer.parseInt(lineContent[1]))-1;
                connections[elementL].set(elementR);
            }

        } catch (FileNotFoundException e){
            System.out.println(("File not found " + path));
        } catch (IOException e){
            System.out.println("Error reading line");
        }
    }

    public int getNumElementsSol() {
        return numElementsSol;
    }

    public int getNumElementsL() {
        return numElementsL;
    }

    public int getNumElementsR() {
        return numElementsR;
    }

    public BitSet[] getConnections() {
        return connections;
    }

    public String getName() {
        return name;
    }
}
