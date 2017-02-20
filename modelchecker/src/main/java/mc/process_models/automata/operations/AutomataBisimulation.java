package mc.process_models.automata.operations;

import mc.exceptions.CompilationException;
import mc.process_models.automata.Automaton;
import mc.process_models.automata.AutomatonNode;

import java.util.*;

/**
 * Created by sheriddavi on 26/01/17.
 */
public class AutomataBisimulation {

    private final int BASE_COLOUR = 1;
    private final int STOP_COLOUR = 0;
    private final int ERROR_COLOUR = -1;

    private int nextColourId;

    public Automaton performSimplification(Automaton automaton) throws CompilationException {
        reset();

        Map<Integer, List<Colour>> colourMap = new HashMap<Integer, List<Colour>>();
        Map<Integer, List<AutomatonNode>> nodeColours = performColouring(automaton, colourMap);

        // merge nodes that have the same colouring
        for(int colourId : nodeColours.keySet()){
            List<AutomatonNode> nodes = nodeColours.get(colourId);

            // no need to combine nodes if there is only one
            if(nodes.size() < 2){
                continue;
            }

            // merge the nodes to form one node
            AutomatonNode mergedNode = nodes.get(0);
            for(int i = 1; i < nodes.size(); i++){
                mergedNode = automaton.combineNodes(mergedNode, nodes.get(i));
            }

            // remove the nodes that were merged
            nodes.forEach(automaton::removeNode);
        }

        return automaton;
    }

    public boolean areBisimular(List<Automaton> automata){
        reset();

        Map<Integer, List<Colour>> colourMap = new HashMap<Integer, List<Colour>>();

        int rootColour = Integer.MIN_VALUE;

        for(Automaton automaton : automata){
            performColouring(automaton, colourMap);

            AutomatonNode root = automaton.getRoot();
            int colour = (int)root.getMetaData("colour");

            if(rootColour == Integer.MIN_VALUE){
                rootColour = colour;
            }
            else if(rootColour != colour){
                return false;
            }
        }

        return true;
    }

    private Map<Integer, List<AutomatonNode>> performColouring(Automaton automaton, Map<Integer, List<Colour>> colourMap){
        int lastColourCount = 0;
        int colourCount = 1;
        int colourAmount = 0;

        perfromInitialColouring(automaton);
        Map<Integer, List<AutomatonNode>> nodeColours = null;

        while(true){
            nodeColours = new HashMap<Integer, List<AutomatonNode>>();
            Set<String> visited = new HashSet<String>();

            Queue<AutomatonNode> fringe = new LinkedList<AutomatonNode>();
            fringe.offer(automaton.getRoot());

            while(!fringe.isEmpty()){
                AutomatonNode current = fringe.poll();

                // check if the current node has been visited
                if(visited.contains(current.getId())){
                    continue;
                }

                // check if the current node is a terminal
                if(current.hasMetaData("isTerminal")){
                    String terminal = (String)current.getMetaData("isTerminal");
                    if(terminal.equals("STOP")){
                        if(!nodeColours.containsKey(STOP_COLOUR)){
                            nodeColours.put(STOP_COLOUR, new ArrayList<AutomatonNode>());
                        }
                        nodeColours.get(STOP_COLOUR).add(current);
                    }
                    else if(terminal.equals("ERROR")){
                        if(!nodeColours.containsKey(ERROR_COLOUR)){
                            nodeColours.put(ERROR_COLOUR, new ArrayList<AutomatonNode>());
                        }
                        nodeColours.get(ERROR_COLOUR).add(current);
                    }

                    visited.add(current.getId());
                    continue;
                }

                // construct a colouring for the current node
                List<Colour> colouring = constructColouring(current);

                // check if this colouring already exists
                int colourId = Integer.MIN_VALUE;

                for(int id : colourMap.keySet()){
                    List<Colour> oldColouring = colourMap.get(id);
                    if(colouring.equals(oldColouring)){
                        colourId = id;
                        break;
                    }
                }

                if(colourId == Integer.MIN_VALUE){
                    colourId = getNextColourId();
                    colourMap.put(colourId, colouring);
                    colourCount++;
                }

                if(!nodeColours.containsKey(colourId)){
                    nodeColours.put(colourId, new ArrayList<AutomatonNode>());
                }
                nodeColours.get(colourId).add(current);

                current.getOutgoingEdges().stream()
                    .map(edge -> edge.getTo())
                    .filter(node -> !visited.contains(node.getId()))
                    .forEach(node -> fringe.offer(node));

                visited.add(current.getId());
            }

            // apply colours to the nodes
            for(int colourId : nodeColours.keySet()){
                nodeColours.get(colourId).forEach(node -> node.addMetaData("colour", colourId));
            }

            // break if no new colours were added
            if(colourCount - lastColourCount == colourAmount){
                break;
            }

            colourAmount = colourCount - lastColourCount;
            lastColourCount = colourCount;
        }

        return nodeColours;
    }

    private void perfromInitialColouring(Automaton automaton){
        List<AutomatonNode> nodes = automaton.getNodes();
        for(AutomatonNode node : nodes){
            node.addMetaData("colour", BASE_COLOUR);
        }
    }
    private List<Colour> constructColouring(AutomatonNode node){
        Set<Colour> colouringSet = new HashSet<>();
        int from = (int)node.getMetaData("colour");
        node.getOutgoingEdges()
            .forEach(edge -> colouringSet.add(new Colour(from, (int)edge.getTo().getMetaData("colour"),edge.getLabel())));
        List<Colour> colouring = new ArrayList<>(colouringSet);
        Collections.sort(colouring);
        return colouring;
    }

    private int getNextColourId(){
        return nextColourId++;
    }

    private void reset(){
        nextColourId = 1;
    }

    private class Colour implements Comparable<Colour> {

        public int from;
        public int to;
        public String action;

        public Colour(int from, int to, String action){
            this.from = from;
            this.to = to;
            this.action = action;
        }

        public boolean equals(Object obj){
            if(obj == this){
                return true;
            }

            if(obj instanceof Colour) {
                Colour col = (Colour) obj;
                return to == col.to && action.equals(col.action);
            }

            return false;
        }

        @Override
        public int hashCode() {
            int result = from;
            result = 31 * result + to;
            result = 31 * result + action.hashCode();
            return result;
        }

        public int compareTo(Colour col){
            if(to < col.to){
                return -1;
            }

            if(to > col.to){
                return 1;
            }

            return action.compareTo(col.action);
        }

    }
}
