import java.io.IOException;
import java.util.*;
import java.io.*;

public class PartD {
    public static void main(String[] args) throws IOException {
        System.out.println("part D running");
        /* declare variables for t_support and confidence */
        // int t_support;
        // int t_confidence;
        // /* get them from command line */
        // if (args.length > 1) {
        //     t_support = Integer.parseInt(args[1]);
        //     t_confidence = Integer.parseInt(args[2]);
        // } else {
        //     /* default to 3 and 65 */
        //     t_support = 3;
        //     t_confidence = 65;
        // }
        /* Get the name of the callgraph */
        String callGraphFile = args[0] + ".callgraph";

        /* Create file and scanner to read file */
        File file = new File(callGraphFile);
        Scanner in = new Scanner(file);
        
        String line = in.nextLine();

        /* soloSupport will hold function names and the number
        of uses for the function as a hasmap <String,int> */
        HashMap<String,Integer> soloSupport = new HashMap<>();

        /* callerPairs will set up a hashmap of the unique pairs of
        callee string function names within a caller */
        HashMap<String,HashSet<Pair>> callerPairs = new HashMap<String,HashSet<Pair>>();

        /* callerContains is hashmap <caller name, set of functions it calls>
        will add each individual function to the hashmap */
        HashMap<String,HashSet<String>> callerContains = new HashMap<String,HashSet<String>>();

        /* pairSupport<Pairs, # of appearances in all scopes>
        can only be incremented once per call block - for support of the pair */
        HashMap<Pair,Integer> pairSupport = new HashMap<Pair,Integer>();

        /* List of zStatandBug
        saves the values of the zStat calculation to be sorted */
        List<zStatandBug> zStatSortedBugs = new ArrayList<zStatandBug>();

        while (in.hasNextLine()) {
            /* pairsSet gets the set of string pairs of callees within a
            caller function - will be added to callerPairs for the caller name */
            HashSet<Pair> pairsSet = new HashSet<Pair>();

            /* callerContainsSet is a HashSet<String> that will be the set of
            String function names that are in the caller function.
            This set is the value for the callerContains hashmap. */
            HashSet<String> callerContainsSet = new HashSet<String>();

            /* ignore lines that are not important */
            if (line.contains("null function")) {
                line = in.nextLine();
            }
            while (!line.contains("Call graph node")) {
                line = in.nextLine();
            }
            /* if we hit a caller title line, begin our algorithm */
            if (line.indexOf("Call graph node") != -1) {
                int useIndex = line.indexOf("uses=");
                useIndex = useIndex + 5;
                int uses = Integer.parseInt(line.substring(useIndex));
                uses--;
                /* Get the name of the caller */
                int firstOfName = line.indexOf("\'") + 1;
                int lastOfName = line.lastIndexOf("\'");
                String callerName = line.substring(firstOfName,lastOfName);

                /* arr is the list of unique callee functions 
                within a scope block */
                ArrayList<String> arr = new ArrayList<String>();

                /* make sure the data structures are clear */
                pairsSet.clear();
                callerContainsSet.clear();
                line = in.nextLine();
                
                /* Iterating through the lines of callee functions */
                while (line.contains("CS<") && (in.hasNextLine())) {
                    /* If the line is to external node, ignore it */
                    if (line.indexOf("external node") != -1) {
                        line = in.nextLine();
                        continue;
                    }
                    /* If line is empty or doesnt have what we want */
                    if (line.indexOf("\'") == -1) {
                        line = in.nextLine();
                        break;
                    }
                    /* get the function name */
                    firstOfName = line.indexOf("\'") + 1;
                    lastOfName = line.lastIndexOf("\'");
                    String func1 = line.substring(firstOfName,lastOfName);
 
                    /* add the function to callerContainsSet */
                    callerContainsSet.add(func1);
                    
                    /* if this is the first time this function occurs in this scope,
                    add it to the list of unique functions. Also increase the uses in soloSupport. */
                    if (!arr.contains(func1)) {
                        boolean soloLoopItsNotNew = soloSupport.containsKey(func1);
                        arr.add(func1);
                        if (soloLoopItsNotNew) {
                            int soloVal = soloSupport.get(func1);
                            soloSupport.put(func1, soloVal + 1);
                        } else {
                            soloSupport.put(func1, 1);
                        }
                    }
                    line = in.nextLine();
                } /* end while CS< */
                /* Iterate through arr, creating pairs for each element with 
                the other elements. */
                for (int i = 0; i < arr.size(); i++) {
                    for (int j = i+1; j < arr.size(); j++) {
                        Pair newPair2 = new Pair(arr.get(i), arr.get(j));
                        pairsSet.add(newPair2);
                        /* check if the pair already exists */
                        Boolean itsNotNew = pairSupport.containsKey(newPair2);
                        /* if it exists, increase the value. If not, set it to 1 */
                        if (itsNotNew) {
                            int val = pairSupport.get(newPair2);
                            pairSupport.put(newPair2, val + 1);
                        } else {
                            pairSupport.put(newPair2, 1);
                        }

                    }
                }
                /* finally, put the sets into the the data structs
                that need them */
                callerPairs.put(callerName, pairsSet);
                callerContains.put(callerName, callerContainsSet);

            } /* end if */
        } /* end while hasNextLine */
        /* close the scanner */
        in.close();

        for (Map.Entry<Pair,Integer> entry : pairSupport.entrySet()) {

            /* If the support threshold is met for possible bug */
            //if (entry.getValue() >= t_support) {

                /* Then get the information about that pair */
                int pairSupp = entry.getValue();
                Pair bugPair = entry.getKey();
                String a = bugPair.getA();
                String b = bugPair.getB();

                /* Get each soloSupport to calculate confidence */ 
                int aSoloSupp = soloSupport.get(a);
                int bSoloSupp = soloSupport.get(b);

                /* Get the confidence for each member of the pair */
                double aConfidence = 100*(((double)pairSupp)/(aSoloSupp));
                double bConfidence = 100*(((double)pairSupp)/(bSoloSupp));

                /* namesOfBuggedCallers will be all caller functions where the
                potential bug is located */
                ArrayList<String> namesOfBuggedCallers = new ArrayList<String>();

                /* If above threshold, it is a bug and needs to be printed */
                    for (Map.Entry<String, HashSet<Pair>> callersSet : callerPairs.entrySet()) {
                        /* Go through the functions and check where the pair is not present */
                        if (!(callersSet.getValue().contains(bugPair))) {
                            String callersName = callersSet.getKey();
                            /* If it does have function a by itself, add it to the bugged
                            callers array */
                            if (callerContains.get(callersName).contains(a)) {
                                namesOfBuggedCallers.add(callersName);
                            }
                        }
                    }
                    /* print them out */
                    for (String callerNameToPrint : namesOfBuggedCallers) {
                        double zStata = (((double)pairSupp/aSoloSupp)-0.9)/(Math.sqrt(0.9*(1-0.9)/(double)aSoloSupp));
                        String bugOutput = "" + a + " in " + callerNameToPrint + 
                        ", pair: (" + a + ", " + b + "), support: " + pairSupp +
                        ", confidence: " + String.format("%.2f", aConfidence) + "%";
                        zStatandBug newEntry = new zStatandBug(zStata, bugOutput);
                        zStatSortedBugs.add(newEntry);

                        // System.out.println("bug: " + a + " in " + callerNameToPrint + 
                        // ", pair: (" + a + ", " + b + "), support: " + pairSupp +
                        // ", confidence: " + String.format("%.2f", aConfidence) + "%");
                    }
                /* clear the array and do the same process again for function b */
                namesOfBuggedCallers.clear();
                    for (Map.Entry<String, HashSet<Pair>> callersSet : callerPairs.entrySet()) {
                        /* Go through the functions and check where the pair is not present */
                        if (!(callersSet.getValue().contains(bugPair))) {
                            String callersName = callersSet.getKey();
                            /* For those where the pair isnt present, check if the func
                            exists in the caller at all.
                            If true, that caller is a bug spot that must be printed. */
                            if (callerContains.get(callersName).contains(b)) {
                                namesOfBuggedCallers.add(callersName);
                            }
                        }
                    }
                    for (String callerNameToPrint : namesOfBuggedCallers) {
                        double zStatb = (((double)pairSupp/bSoloSupp)-0.9)/(Math.sqrt(0.9*(1-0.9)/(double)bSoloSupp));
                        String bugOutput = "" + a + " in " + callerNameToPrint + 
                        ", pair: (" + a + ", " + b + "), support: " + pairSupp +
                        ", confidence: " + String.format("%.2f", bConfidence) + "%";
                        zStatandBug newEntry = new zStatandBug(zStatb, bugOutput);
                        zStatSortedBugs.add(newEntry);
                    // System.out.println("bug: " + b + " in " + callerNameToPrint + 
                    // ", pair: (" + a + ", " + b + "), support: " + pairSupp +
                    // ", confidence: " + String.format("%.2f", bConfidence)  + "%");
                    }
                namesOfBuggedCallers.clear();
            //} 
        }
        Collections.sort(zStatSortedBugs, new zCompare());
        for (int i = 0; i < zStatSortedBugs.size(); i++)
            System.out.println(zStatSortedBugs.get(i));
    }
}

/*class Pair {
        String a;
        String b;
        public Pair(String a, String b) {
            /* Pairs are sorted into alphebetical upon creation */
            /*if (a.compareTo(b) < 0) {
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
            /* Pairs are equal no matter what position the elements are,
            though they should be alphebetical */
            /*if ((this.getA().compareTo(other.getA()) == 0) && (this.getB().compareTo(other.getB()) == 0)) {
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
}*/

class zStatandBug {
    double zStat;
    String bug;
    public zStatandBug(double z, String s) {
        this.zStat = z;
        this.bug = s;
    }
    public double getZStat () {
        return zStat;
    }
    public String getBug() {
        return bug;
    }
    @Override
    public String toString() {
        return "zStat: " + String.format("%.3f", this.zStat) + " |  " + this.bug;
    }
}
class zCompare implements Comparator<zStatandBug> {
    public int compare(zStatandBug a, zStatandBug b)
    {
        return (int)(1000*b.zStat) - (int)(1000*a.zStat);
    }
}
