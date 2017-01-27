package mc.compiler;

import mc.compiler.ast.OperationNode;
import mc.process_models.ProcessModel;
import mc.process_models.automata.Automaton;
import mc.process_models.automata.operations.AutomataOperations;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class OperationEvaluator {

    private int operationId;

    private AutomataOperations automataOperations;

    public OperationEvaluator(){
        this.automataOperations = new AutomataOperations();
    }

    public List<OperationResult> evaluateOperations(List<OperationNode> operations, Map<String, ProcessModel> processMap, Interpreter interpreter){
        reset();
        List<OperationResult> results = new ArrayList<OperationResult>();

        for(OperationNode operation : operations){
            List<Automaton> automata = new ArrayList<Automaton>();
            automata.add((Automaton)interpreter.interpret("automata", operation.getFirstProcess(), getNextOperationId(), processMap));
            automata.add((Automaton)interpreter.interpret("automata", operation.getSecondProcess(), getNextOperationId(), processMap));


            boolean result = automataOperations.bisimulation(automata);

            if(operation.isNegated()){
                result = !result;
            }

            results.add(new OperationResult(operation.getFirstProcess(),operation.getSecondProcess(),operation.getOperation(),result));
        }
        return results;
    }

    private String getNextOperationId(){
        return "op" + operationId++;
    }

    private void reset(){
        operationId = 0;
    }
}