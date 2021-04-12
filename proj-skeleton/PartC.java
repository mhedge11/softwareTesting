import java.io.IOException;
import java.util.*;
import java.io.*;

public class PartC {
    public static void main(String[] args) throws IOException {
        /* declare variables for t_support and confidence */
        int t_support;
        int t_confidence;
        int cmdLineArgExpand = 0;
        /* get them from command line */
        if (args.length > 1) {
            t_support = Integer.parseInt(args[1]);
            t_confidence = Integer.parseInt(args[2]);
        } else {
            /* default to 3 and 65 */
            t_support = 3;
            t_confidence = 65;
        }
        if (args.length >= 4) {
            cmdLineArgExpand = Integer.parseInt(args[3]);
        } else {
            cmdLineArgExpand = 0;
        }
        
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
        
        /* callerPairs will set up a hashmap of the unique pairs of
        callee string function names within a caller */
        //HashMap<String,HashSet<Pair>> callerPairs2 = new HashMap<String,HashSet<Pair>>();

        /* callerContains is hashmap <caller name, set of functions it calls>
        will add each individual function to the hashmap */
        HashMap<String,HashSet<String>> callerContains = new HashMap<String,HashSet<String>>();

        /* callerContains is hashmap <caller name, set of functions it calls>
        will add each individual function to the hashmap */
        //HashMap<String,HashSet<String>> callerContains2 = new HashMap<String,HashSet<String>>();

        /* pairSupport<Pairs, # of appearances in all scopes>
        can only be incremented once per call block - for support of the pair */
        HashMap<Pair,Integer> pairSupport = new HashMap<Pair,Integer>();

        /* pairSupport<Pairs, # of appearances in all scopes>
        can only be incremented once per call block - for support of the pair */
        //HashMap<Pair,Integer> pairSupport2 = new HashMap<Pair,Integer>();

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
                int uses = 0;
                try {
                    uses = Integer.parseInt(line.substring(useIndex));
                } catch (NumberFormatException e) {
                    in.close();
                    System.out.println("uses error");
                    return;
                }
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
                // if (!callerContainsSet.isEmpty()) {
                //     callerContains.put(callerName, callerContainsSet);
                // }

            } /* end if */
        } /* end while hasNextLine */
        /* close the scanner */
        in.close();

        // System.out.println("orig callerContains:" + callerContains);
        // System.out.println("orig callerPairs:" + callerPairs);

        for (int i = 0; i < cmdLineArgExpand; i++) {
            
            /* Iterate through callerContains, getting the set of each
            callers children. Get the functions each child contains, and add it back
            to be that callers children. After iterating, set callerContains to
            callerContains2.
            */
            //callerContains2.clear();

            /* callerContainsSet is a HashSet<String> that will be the set of
            String function names that are in the caller function.
            This set is the value for the callerContains hashmap. */
            HashSet<String> callerContainsSet2 = new HashSet<String>();


            //System.out.println("cc:" + callerContains);
            HashMap<String,HashSet<String>> callerContains2 = new HashMap<String,HashSet<String>>();
            for (Map.Entry<String,HashSet<String>> entry : callerContains.entrySet()) {
                String callName = entry.getKey();
                HashSet<String> children = entry.getValue();
                HashSet<String> expandChildren = new HashSet<String>();
                // New test to go with callerContainsSet2 to make printing work correctly
                // callerContainsSet2.addAll(children);
                //expandChildren.add(children);
                //System.out.println("callname:" + callName + " children:" + children);
                
                for (String child : children) {
                    if (callerContains.containsKey(child) && callerContains.get(child).size() == 0) {
                        expandChildren.add(child);
                    }
                   // System.out.println("inside callcontains for: "+ child);
                    else if (callerContains.containsKey(child)) {
                        expandChildren.addAll(callerContains.get(child));
                    } 
                    else {
                        expandChildren.add(child);
                        
                    }
                    
                }
                // New test to go with callerContainsSet2 to make printing work correctly
               //callerContainsSet2.add(callName);
                callerContains2.put(callName, expandChildren);
                //callerContains2.put(callName, callerContainsSet2);
                // New test
                // if (!callerContainsSet2.isEmpty()) {
                //     callerContains2.put(callName, callerContainsSet2);
                // }
            }
            callerContains = callerContains2;
            // System.out.println("----------Caller contains-------------");
            // System.out.println(callerContains.entrySet());
            //pairSupport2.clear();




            /* soloSupport will hold function names and the number
            of uses for the function as a hasmap <String,int> */
            HashMap<String,Integer> soloSupport2 = new HashMap<String,Integer>();
            // /* arr is the list of unique callee functions within a scope block */
            // ArrayList<String> arr = new ArrayList<String>();
            // for (Map.Entry<String,HashSet<String>> entry : callerContains.entrySet()) {
            //     String callName = entry.getKey();
            //     HashSet<String> children = entry.getValue();

            //     for (String child : children) {

            //     }

            // }

            // /*
            //  * if this is the first time this function occurs in this scope, add it to the
            //  * list of unique functions. Also increase the uses in soloSupport.
            //  */
            // if (!arr.contains(func1)) {
            //     boolean soloLoopItsNotNew = soloSupport.containsKey(func1);
            //     arr.add(func1);
            //     if (soloLoopItsNotNew) {
            //         int soloVal = soloSupport.get(func1);
            //         soloSupport.put(func1, soloVal + 1);
            //     } else {
            //         soloSupport.put(func1, 1);
            //     }
            // }






            HashMap<Pair,Integer> pairSupport2 = new HashMap<Pair,Integer>();


            
            for (Map.Entry<String, HashSet<String>> entry : callerContains.entrySet()) {
                HashSet<Pair> insideScopePairs = new HashSet<Pair>();
                String callerNameAgain2 = entry.getKey();
                HashSet<String> children = entry.getValue();
                /* arr is the list of unique callee functions within a scope block */
                ArrayList<String> arr = new ArrayList<String>();

                
                //System.out.println("-------in " + callerNameAgain2 + "-------");
                //pairsSet2.clear();
                //HashSet<Pair> insideScopePairs = new HashSet<Pair>();
                //insideScopePairs.clear();
                for (String child1 : children) {
                    for (String child2 : children) {

                        /*
                         * if this is the first time this function occurs in this scope, add it to the
                         * list of unique functions. Also increase the uses in soloSupport.
                         */
                        if (!arr.contains(child1)) {
                            boolean soloLoopItsNotNew = soloSupport2.containsKey(child1);
                            arr.add(child1);
                            if (soloLoopItsNotNew) {
                                int soloVal = soloSupport2.get(child1);
                                soloSupport2.put(child1, soloVal + 1);
                            } else {
                                soloSupport2.put(child1, 1);
                            }
                        }
                        if (!arr.contains(child2)) {
                            boolean soloLoopItsNotNew = soloSupport2.containsKey(child2);
                            arr.add(child2);
                            if (soloLoopItsNotNew) {
                                int soloVal = soloSupport2.get(child2);
                                soloSupport2.put(child2, soloVal + 1);
                            } else {
                                soloSupport2.put(child2, 1);
                            }
                        }


                        if (!child1.equals(child2)) {
                            // System.out.println("pair " + child1 + ", " + child2);
                            Pair newPair2 = new Pair(child1, child2);
                            // insideScopePairs.add(newPair2);
                            Boolean itsNotNew = pairSupport2.containsKey(newPair2);
                            /* if it exists, increase the value. If not, set it to 1 */
                            // if (itsNotNew && !insideScopePairs.contains(newPair2)) {
                            if (insideScopePairs.contains(newPair2)) {
                                continue;
                            }
                            insideScopePairs.add(newPair2);
                            if (itsNotNew) {
                                int val = pairSupport2.get(newPair2);
                                val++;
                                pairSupport2.put(newPair2, val);
                                // System.out.println("pair " + child1 + ", " + child2 + " new val: " + (val));
                            } else {
                                pairSupport2.put(newPair2, 1);
                            }
                        }
                        // else {
                        // int soloVal2 = soloSupport.get(child1);
                        // soloSupport.put(child1, soloVal2 - 1);
                        // }
                        
                    }
                }
            }
            pairSupport = pairSupport2;
            // System.out.println("----------pair support-----------");
            // System.out.println(pairSupport.entrySet());

            soloSupport = soloSupport2;



                // HashSet<String> childrenFunc1 = callerContains.get(entry.getKey().getA());
                // HashSet<String> childrenFunc2 = callerContains.get(entry.getKey().getB());
                // for (String child1 : childrenFunc1) {
                //     for (String child2 : childrenFunc2) {
                //         if (!child1.equals(child2)) {
                //             System.out.println("new pair: " + child1 + ", " + child2);
                //             Pair newPair2 = new Pair(child1, child2);
                //             //pairsSet.add(newPair2);
                //             /* check if the pair already exists */
                //             Boolean itsNotNew = pairSupport2.containsKey(newPair2);
                //             /* if it exists, increase the value. If not, set it to 1 */
                //             if (itsNotNew) {
                //                 int val = pairSupport2.get(newPair2);
                //                 pairSupport2.put(newPair2, val + 1);
                //             } else {
                //                 pairSupport2.put(newPair2, 1);
                //             }
                //         } else {
                //             int soloVal2 = soloSupport.get(child1);
                //             soloSupport.put(child1, soloVal2 - 1);
                //         }
                //     }
                // }
            

            HashMap<String,HashSet<Pair>> callerPairs2 = new HashMap<String,HashSet<Pair>>();
            //callerPairs2.clear();
            
            for (Map.Entry<String, HashSet<String>> entry : callerContains.entrySet()) {
                /**
                 * Had to make new pairSet2 each time - I think this is because if using clear
                 * then would map to same hashset value and override previous callerNames with the new data each time
                 */
                HashSet<Pair> pairsSet2 = new HashSet<Pair>();
                String callerNameAgain = entry.getKey();
                HashSet<String> children = entry.getValue();
                // System.out.println("callerNameAgain:" + callerNameAgain);
                // System.out.println("children:" + children);
                if (children.size() == 1) {
                    continue;
                }
                
                for (String child1 : children) {
                    for (String child2 : children) {
                        if (!child1.equals(child2)) {
                            Pair newPair2 = new Pair(child1, child2);
                            if (pairsSet2.contains(newPair2)) {
                                continue;
                            }
                            pairsSet2.add(newPair2);
                            //System.out.println("making new pair:" + newPair2);
                        }
                    }
                }
                // System.out.println("out of for loop");
                // System.out.println("CallerNameAgain:" + callerNameAgain);
                // System.out.println("in loop pairsSet2:" + pairsSet2);
                callerPairs2.put(callerNameAgain, pairsSet2);
                //pairsSet2.clear();
                // System.out.println("putting into callerPairs2");
                //System.out.println("in loop callerPairs2:" + callerPairs2);
                
                
            }
            callerPairs = callerPairs2;
        //    System.out.println("------------Caller Pairs------------");
        //    System.out.println(callerPairs.entrySet());
        }

        //System.out.println(pairSupport);

        for (Map.Entry<Pair,Integer> entry : pairSupport.entrySet()) {

            /* If the support threshold is met for possible bug */
            if (entry.getValue() >= t_support) {

                /* Then get the information about that pair */
                int pairSupp = entry.getValue();
                Pair bugPair = entry.getKey();
                String a = bugPair.getA();
                String b = bugPair.getB();
                //System.out.println("bugPair:" + bugPair);

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
                if (aConfidence >= t_confidence) {
                    for (Map.Entry<String, HashSet<Pair>> callersSet : callerPairs.entrySet()) {
                        /* Go through the functions and check where the pair is not present */
                        if (!(callersSet.getValue().contains(bugPair))) {
                            String callersName = callersSet.getKey();
                            /* If it does have function a by itself, add it to the bugged
                            callers array */
                            if (callerContains.get(callersName).contains(a)) {
                                namesOfBuggedCallers.add(callersName);
                                // System.out.println("callersName:" + callersName);
                                // System.out.println("bugPair:" + bugPair);
                            }
                        }
                    }
                    /* print them out */
                    for (String callerNameToPrint : namesOfBuggedCallers) {
                        System.out.println("bug: " + a + " in " + callerNameToPrint + 
                        ", pair: (" + a + ", " + b + "), support: " + pairSupp +
                        ", confidence: " + String.format("%.2f", aConfidence) + "%");
                    }
                }
                /* clear the array and do the same process again for function b */
                namesOfBuggedCallers.clear();
                if (bConfidence >= t_confidence) {
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
                    for (String callerNameToPrint : namesOfBuggedCallers)
                    System.out.println("bug: " + b + " in " + callerNameToPrint + 
                    ", pair: (" + a + ", " + b + "), support: " + pairSupp +
                    ", confidence: " + String.format("%.2f", bConfidence)  + "%");
                }
                namesOfBuggedCallers.clear();
            }
            
        }
    }
}

