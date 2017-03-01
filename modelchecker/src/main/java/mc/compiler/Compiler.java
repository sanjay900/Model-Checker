package mc.compiler;

import lombok.AllArgsConstructor;
import lombok.Getter;
import mc.compiler.ast.AbstractSyntaxTree;
import mc.compiler.ast.ProcessNode;
import mc.exceptions.CompilationException;
import mc.process_models.ProcessModel;
import mc.process_models.automata.Automaton;
import mc.process_models.automata.generator.AutomatonGenerator;
import mc.process_models.automata.operations.AutomataOperations;
import mc.webserver.Context;
import mc.webserver.LogMessage;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Compiler {
    // fields
    private Lexer lexer;
    private Expander expander;
    private ReferenceReplacer replacer;
    private Interpreter interpreter;
    private OperationEvaluator evaluator;
    private EquationEvaluator eqEvaluator;
    private Parser parser;

    public Compiler(){
        this.lexer = new Lexer();
        parser = new Parser();
        this.expander = new Expander();
        this.replacer = new ReferenceReplacer();
        this.interpreter = new Interpreter();
        this.eqEvaluator = new EquationEvaluator();
        this.evaluator = new OperationEvaluator();
    }

    public CompilationObject compile(String code, Context context) throws CompilationException{
        if (code.startsWith("random")) {
            new LogMessage("Generating random models").send();
            boolean alphabet = Boolean.parseBoolean(code.split(",")[1]);
            int nodeCount = Integer.parseInt(code.split(",")[2]);
            int alphabetCount = Integer.parseInt(code.split(",")[3]);
            int maxTransitionCount = Integer.parseInt(code.split(",")[4]);
            Map<String,ProcessModel> models = new HashMap<>();
            int i = 0;
            List<ProcessModel> nodes = new AutomatonGenerator().generateAutomaton(alphabetCount,nodeCount,maxTransitionCount,"A",new AutomataOperations(),alphabet);
            for (ProcessModel model:nodes) {
                Automaton a = (Automaton) model;
                models.put(a.getId()+(i++),a);
            }
            return new CompilationObject(models,Collections.emptyList(),Collections.emptyList());
        }
        return compile(parser.parse(lexer.tokenise(code)), code, context);
    }
    private CompilationObject compile(AbstractSyntaxTree ast, String code, Context context) throws CompilationException {
        HashMap<String,ProcessNode> processNodeMap = new HashMap<>();
        for (ProcessNode node: ast.getProcesses()) {
            processNodeMap.put(node.getIdentifier(), (ProcessNode) node.copy());
        }
        ast = expander.expand(ast);
        ast = replacer.replaceReferences(ast);
        Map<String, ProcessModel> processMap = interpreter.interpret(ast, new LocalCompiler(processNodeMap, expander, replacer));
        List<OperationResult> results = evaluator.evaluateOperations(ast.getOperations(), processMap, interpreter, code);
        List<OperationResult> eqResults = eqEvaluator.evaluateEquations(ast.getEquations(), code, context);
        return new CompilationObject(processMap, results, eqResults);
    }
    @AllArgsConstructor
    public static class LocalCompiler {
        @Getter
        private HashMap<String,ProcessNode> processNodeMap;
        private Expander expander;
        private ReferenceReplacer replacer;

        public ProcessNode compile(ProcessNode node) throws CompilationException {
            node = expander.expand(node);
            node = replacer.replaceReferences(node);
            return node;
        }
    }
}
