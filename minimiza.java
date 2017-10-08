import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.Map.Entry;

class State {
    int id;
    boolean isAcceptance;

    @Override
    public String toString() {
        return "<id=" + this.id + " isAcceptance=" + this.isAcceptance + ">";
    }
}

class Automaton {

    List<State> states = new ArrayList<State>();
    Map<State, Map<Integer, State>> transitions = new HashMap<>();
    State initialState;
    int alphabetRange;

    public Automaton() {
    }

    public State getState(int id) {
        return states.get(id);
    }

    public Map<Integer, State> getTransitionsFor(int id) {
        return this.getTransitionsFor(this.getState(id));
    }

    public Map<Integer, State> getTransitionsFor(State s) {
        return transitions.get(s);
    }

    public State getNextState(State state, int key) {
        Map<Integer, State> stateTransitions = transitions.get(state);
        return stateTransitions.get(key);
    }

    public void remove(State state) {
        this.states.remove(state);
        this.transitions.remove(state);
        for (Map<Integer, State> localTransitions : this.transitions.values()) {
            Set<Integer> keysToRemove = new HashSet<Integer>();
            if (localTransitions == null)
                continue;

            for (Entry<Integer, State> e : localTransitions.entrySet()) {
                if (e.getValue() == state)
                    keysToRemove.add(e.getKey());
            }
            for (int key : keysToRemove)
                localTransitions.remove(key);
        }
    }

}

class AutomatonExplorer {
    public static void reduceAutomaton(Automaton automaton) {
        removeUnreachableStates(automaton);
    }

    private static void removeUnreachableStates(Automaton automaton) {
        Set<State> visited = new HashSet<State>();
        State currentState = automaton.initialState;
        visitNodes(automaton, currentState, visited);

        Set<State> statesToRemove = new HashSet<State>(automaton.states);
        statesToRemove.removeAll(visited);

        for (State s : statesToRemove){
            System.out.println("Removing " + s);
            automaton.remove(s);
        }

    }

    private static void visitNodes(Automaton automaton, State currentState, Set<State> visited) {
        visited.add(currentState);
        for (State state : automaton.getTransitionsFor(currentState).values()) {
            if (state != null && !visited.contains(state))
                visitNodes(automaton, state, visited);
        }

    }

}

class AutomatonLoader {

    public static Automaton loadFromFile(FileReader fileReader) {
        Scanner scanner = new Scanner(fileReader);
        int statesCount = scanner.nextInt();
        int alphabetRange = scanner.nextInt();
        int initialState = scanner.nextInt();

        scanner.nextLine();

        Automaton automaton = new Automaton();
        automaton.alphabetRange = alphabetRange;

        for (int i = 0; i < statesCount; i++) {
            State state = new State();
            state.id = i;
            state.isAcceptance = scanner.nextInt() == 1;
            automaton.states.add(i, state);
            System.out.println(state);
        }

        automaton.initialState = automaton.getState(initialState);

        scanner.nextLine();

        for (int i = 0; i < statesCount; i++) {
            State state = automaton.getState(i);
            automaton.transitions.put(state, new HashMap<>());
            for (int symbol = 0; symbol < alphabetRange; symbol++) {
                int nextStateId = scanner.nextInt();
                State nextState = nextStateId == -1 ? null : automaton.getState(nextStateId);
                automaton.transitions.get(state).put(symbol, nextState);
                System.out.print(nextStateId + " ");
            }
            System.out.println();
        }

        scanner.close();
        return automaton;
    }

    public static void dumpIntoFile(Automaton automaton, FileWriter writer) {
        PrintWriter printer = new PrintWriter(writer);
        printer.println(automaton.states.size() + " " + // State Count
                automaton.alphabetRange + " " + // Symbols in alphabet
                automaton.initialState.id); // Initial State 

        //Acceptance states
        for (int i = 0; i < automaton.states.size(); i++) {
            if (i > 0)
                printer.print(" ");
            printer.print(automaton.getState(i).isAcceptance ? 1 : 0);
        }
        printer.println();

        // Transition matrix
        for (int i = 0; i < automaton.states.size(); i++) {
            Map<Integer, State> transitions = automaton.getTransitionsFor(i);
            for (int j = 0; j < automaton.alphabetRange; j++) {
                if (j > 0)
                    printer.print(" ");
                State nextState = transitions.get(j);
                printer.print(nextState != null ? nextState.id : -1);
            }
            printer.println();
        }
    }
}

public class minimiza {
    public static void main(String[] args) throws IOException {
        FileReader reader = new FileReader(args[0]);
        Automaton automaton = AutomatonLoader.loadFromFile(reader);
        reader.close();

        AutomatonExplorer.reduceAutomaton(automaton);

        FileWriter writer = new FileWriter(args[1]);
        AutomatonLoader.dumpIntoFile(automaton, writer);
        writer.close();
    }
}