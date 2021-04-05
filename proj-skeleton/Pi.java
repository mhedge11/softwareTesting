import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
//import java.util.StringTokenizer;
import java.util.*;
import java.io.*;

//import jdk.internal.org.jline.utils.InputStreamReader;

public class Pi {


    // Example on how to run: java Pi a 3 65
    public static void main(String[] args) throws IOException {
        System.out.println("its running");
        int t_support = Integer.parseInt(args[1]);
        int t_confidence = Integer.parseInt(args[2]);
        System.out.println("args 1: " + args[1]);
        System.out.println("args 2: " + args[2]);
        File file = new File("./test2/main.c.callgraph.readme");
        Scanner in = new Scanner(file);
        Scanner nestedIn = new Scanner(file);
         
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
        HashMap<String,HashSet<String>> callerContains = new HashMap<String,HashSet<String>>();

        // callerContainsSet is a HashSet<String> that will be the set of
        // String function names that are in the caller function.
        // This set is the value for the callerContains hashmap.
        HashSet<String> callerContainsSet = new HashSet<String>();

        // pairSupport<Pairs,# of appearances in all scopes>
        // can only be incremented once per call block - for support of the pair
        HashMap<Pair,Integer> pairSupport = new HashMap<Pair,Integer>();
        while (in.hasNextLine()) {
            if (line.contains("null function")) {
                line = in.nextLine();
                
            }
            while (!line.contains("Call graph node")) {
                line = in.nextLine();
            }
            if (line.indexOf("Call graph node") != -1) {
                System.out.println("start");
                int useIndex = line.indexOf("uses=");
                useIndex = useIndex + 5;
                //System.out.println("uses # = " + useIndex);
                int uses = Integer.parseInt(line.substring(useIndex));
                uses--;
                System.out.println("uses = " + uses);
                int firstOfName = line.indexOf("\'") + 1;
                int lastOfName = line.lastIndexOf("\'");
                // System.out.println(line);
                // System.out.println("f:" + firstOfName + " l:" + lastOfName);
                String callerName = line.substring(firstOfName,lastOfName);
                System.out.println("cn:" + callerName);

                
                /* Add # uses to soloSupport */
                soloSupport.put(callerName,uses);
                ArrayList<String> arr = new ArrayList<String>();

                line = in.nextLine();
                System.out.println("line:" + line);
                nestedIn = in;
                
                while (line.contains("CS<") && (in.hasNextLine())) {
                    nestedIn = in;
                    System.out.println("inside while contains CS");
                    if (line.indexOf("\'") == -1) {
                        System.out.println("no \' :" + line);
                        line = in.nextLine();
                        break;
                    }
                    firstOfName = line.indexOf("\'") + 1;
                    lastOfName = line.lastIndexOf("\'");
                    String func1 = line.substring(firstOfName,lastOfName);
                    //Pair newPair = new Pair(callerName,func1);
                    //System.out.println("a:" + newPair.getA() + " b:" + newPair.getB());
                    //callerContains.put(callerName, func1);
                    //pairsSet.add(newPair);
                    callerContainsSet.add(func1);
                    String nestedLoopLine = nestedIn.nextLine();
                    System.out.println("nll: " + nestedLoopLine);
                    arr.add(func1);
                    while(nestedLoopLine.contains("CS<") && nestedIn.hasNextLine()) {
                        //System.out.println("curLine:" + line);
                        firstOfName = nestedLoopLine.indexOf("\'") + 1;
                        lastOfName = nestedLoopLine.lastIndexOf("\'");
                        String func2 = nestedLoopLine.substring(firstOfName,lastOfName);
                        //System.out.print
                        arr.add(func2);
                        /*
                        Pair newPair2 = new Pair(func1,func2);
                        System.out.println("newpair2 = " + newPair2);
                        pairsSet.add(newPair2);
                        Boolean itsNotNew = pairSupport.containsKey(newPair2);
                        if (itsNotNew) {
                            System.out.println("its not new");
                            int val = pairSupport.get(newPair2);
                            pairSupport.put(newPair2, val + 1);
                        } else {
                            System.out.println("its new");
                            pairSupport.put(newPair2, 1);
                        }
                        */
                        nestedLoopLine = nestedIn.nextLine();
                        
                    }

                    for (int i = 0; i < arr.size(); i++) {
                        for (int j = i+1; j < arr.size(); j++) {
                            
                            Pair newPair2 = new Pair(arr.get(i),arr.get(j));
                            System.out.println("newpair2 = " + newPair2);
                            pairsSet.add(newPair2);
                            Boolean itsNotNew = pairSupport.containsKey(newPair2);
                            if (itsNotNew) {
                                System.out.println("its not new");
                                int val = pairSupport.get(newPair2);
                                pairSupport.put(newPair2, val + 1);
                            } else {
                                System.out.println("its new");
                                pairSupport.put(newPair2, 1);
                            }

                        }
                    }
                    // Testing Matt
                    

                    callerPairs.put(callerName, pairsSet);
                    callerContains.put(callerName, callerContainsSet);
                    System.out.println("curLine:" + line);
                    //System.out.println("curLine:" + line);
                    if (in.hasNextLine()) {
                        line = in.nextLine();
                        System.out.println("nextLine: " + line);
                    }
                        

                    //System.out.println("curLine:" + line);
                    //System.out.println("nll: " + nestedLoopLine);
                }

            }
        }
        //System.out.println(line);
        in.close();
        System.out.println("into the for loop area");
        System.out.println(pairSupport.entrySet());
        for (Map.Entry<Pair,Integer> entry : pairSupport.entrySet()) {
            /* If the support threshold is met for possible bug */
            System.out.println("\n--entry " + entry.getKey() + ", " + entry.getValue());
            if (entry.getValue() >= t_support) {
                System.out.println("value over t support");
                /* Then get the information about that pair */
                int pairSupp = entry.getValue();
                Pair bugPair = entry.getKey();
                String a = bugPair.getA();
                String b = bugPair.getB();
                /* Get each soloSupport to calculate confidence */ 
                int aSoloSupp = soloSupport.get(a);
                System.out.println("asolo = " + aSoloSupp);
                int bSoloSupp = soloSupport.get(b);
                /* Check the confidence of the first member of the pair */
                double aConfidence = 100*(((double)pairSupp)/(aSoloSupp));
                System.out.println("aconf = " + aConfidence);
                double bConfidence = 100*(((double)pairSupp)/(bSoloSupp));
                System.out.println("bconf = " + bConfidence);
                /* If above threshold, it is a bug and needs to be printed */
                ArrayList<String> namesOfBuggedCallers = new ArrayList<String>();
                if (aConfidence >= t_confidence) {
                    System.out.println("value over confidence");
                    for (Map.Entry<String, HashSet<Pair>> callersSet : callerPairs.entrySet()) {
                        /* Go through the functions and check where the pair is not present */
                        if (!(callersSet.getValue().contains(bugPair))) {
                            String callersName = callersSet.getKey();
                            /* For those where the pair isnt present, check if the func
                            exists in the caller at all.
                            If true, that caller is a bug spot that must be printed. */
                            if (callerContains.get(callersName).contains(a)) {
                                namesOfBuggedCallers.add(callersName);
                            }
                        }
                    }
                    for (String callerNameToPrint : namesOfBuggedCallers) {
                        System.out.println("bug: " + a + " in " + callerNameToPrint + 
                        ", pair: (" + a + ", " + b + ", support: " + pairSupp +
                        ", confidence: " + aConfidence + "\n");
                    }
                }
                namesOfBuggedCallers.clear();
                if (bConfidence > t_confidence) {
                    for (Map.Entry<String, HashSet<Pair>> callersSet : callerPairs.entrySet()) {
                        /* Go through the functions and check where the pair is not present */
                        if (!(callersSet.getValue().contains(bugPair))) {
                            String callersName = callersSet.getKey();
                            /* For those where the pair isnt present, check if the func
                            exists in the caller at all.
                            If true, that caller is a bug spot that must be printed. */
                            if (callerContains.get(callersName).contains(b)) {
                                //ArrayList<String> namesOfBuggedCallers = new ArrayList<String>();
                                namesOfBuggedCallers.add(callersName);
                            }
                        }
                    }
                    for (String callerNameToPrint : namesOfBuggedCallers)
                    System.out.println("bug: " + b + " in " + callerNameToPrint + 
                    ", pair: (" + a + ", " + b + ", support: " + pairSupp +
                    ", confidence: " + aConfidence + "\n");
                }
                namesOfBuggedCallers.clear();
            }
            
        }
    
    
    
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
        
        @Override
        public boolean equals(Object o) {
            if (o == this) {
                return true;
            }
            if (!(o instanceof Pair)) {
                System.out.println("its not a pair\n\n\n");
                return false;
            }
            Pair other = (Pair) o;
            if ((this.getA().compareTo(other.getA()) == 0) && (this.getB().compareTo(other.getB()) == 0)) {
                return true;
            }
            if ((this.getA().compareTo(other.getB()) == 0) && (this.getB().compareTo(other.getA()) == 0)) {
                return true;
            }
            return false;
        }
        @Override
        public String toString() {
            return "(" + a + ", " + b + ")";
        }
        @Override
        public int hashCode() {
            return this.a.hashCode() + this.b.hashCode();
        }
}