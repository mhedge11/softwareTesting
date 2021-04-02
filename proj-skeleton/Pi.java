import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
//import java.util.StringTokenizer;
import java.util.*;
import java.io.*;

//import jdk.internal.org.jline.utils.InputStreamReader;

public class Pi {



    public static void main(String[] args) throws IOException {
        File file = new File("./test2/main.c.callgraph.readme");
        Scanner in = new Scanner(file);
         
        String line = in.nextLine();

        // soloSupport will hold function names and the number
        // of uses for the function as a hasmap <String,int>
        HashMap<String,Integer> soloSupport = new HashMap<>();

        // pairsSet gets the set of string pairs of callees within a
        // caller function - will be added to callerPairs for the caller name
        // May need to be a set????
        HashSet<Pair> pairsSet = new HashSet<Pair>();
        //HashMap<String,String> pairsSet = new HashMap<>();

        // callerPairs will set up a hashmap of the unique pairs of
        // callee string function names within a caller
        HashMap<String,HashSet<Pair>> callerPairs = new HashMap<String,HashSet<Pair>>();

        // callerContains is hashmap <caller name, set of functions it calls>
        // will add each individual function to the hashmap
        //HashMap<String,HashSet<String>> callerContains = new HashMap<String,HashSet<String>>();
        HashMap<String,HashSet<Pair>> callerContains = new HashMap<String,HashSet<Pair>>();

        // pairSupport<Pairs,# of appearances in all scopes>
        // can only be incremented once per call block - for support of the pair
        HashMap<Pair,Integer> pairSupport = new HashMap<Pair,Integer>();

        if (line.contains("null function")) {
            line = in.nextLine();
            
        }
        while (!line.contains("Call graph node")) {
            line = in.nextLine();
        }

        if (line.indexOf("Call graph node") != -1) {
            //System.out.println("start");
            int useIndex = line.indexOf("uses=");
            useIndex = useIndex + 5;
            //System.out.println(useIndex);
            int uses = Integer.parseInt(line.substring(useIndex));
            uses--;
            //System.out.println(uses);
            int firstOfName = line.indexOf("\'") + 1;
            int lastOfName = line.lastIndexOf("\'");
            // System.out.println(line);
            // System.out.println("f:" + firstOfName + " l:" + lastOfName);
            String callerName = line.substring(firstOfName,lastOfName);
            // System.out.println("cn:" + callerName);

            
            
            soloSupport.put(callerName,uses);

            line = in.nextLine();
            //System.out.println("line:" + line);

            while (line.contains("CS<")) {
                firstOfName = line.indexOf("\'") + 1;
                lastOfName = line.lastIndexOf("\'");
                String func1 = line.substring(firstOfName,lastOfName);
                Pair newPair = new Pair(callerName,func1);
                //System.out.println("a:" + newPair.getA() + " b:" + newPair.getB());
                //callerContains.put(callerName, func1);
                pairsSet.add(newPair);
                String nestedLoopLine = in.nextLine();

                while(nestedLoopLine.contains("CS<")) {
                    firstOfName = line.indexOf("\'") + 1;
                    lastOfName = line.lastIndexOf("\'");
                    String func2 = line.substring(firstOfName,lastOfName);
                    Pair newPair2 = new Pair(func1,func2);
                    Boolean isItNew = pairsSet.add(newPair);
                    if (isItNew) {
                        int val = pairSupport.get(newPair);
                        pairSupport.put(newPair, ++val);
                    }
                    nestedLoopLine = in.nextLine();
                }
                callerContains.put(callerName, pairsSet);
                line = in.nextLine();

                
            }


        }
        //System.out.println(line);
        in.close();
    }
}

class Pair {
        String a;
        String b;
        public Pair(String a, String b) {
            if (a.compareTo(b) < 0) {
                this.a = a;
                this.b = b;
            } else {
                this.a = b;
                this.b = a;
            }
        } 

        public String getB () {
            return b;
        }
        public String getA() {
            return a;
        }
}