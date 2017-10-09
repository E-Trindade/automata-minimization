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

    public void normalizeIds() {
        for (int i = 0; i < this.states.size(); i++) {
            this.states.get(i).id = i;
        }
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

class AutomatonReducer {
    public static void reduceAutomaton(Automaton automaton) {
        removeUnreachableStates(automaton);
        removeUselessStates(automaton);
        mergeEquivalentStates(automaton);
    }

    private static void removeUnreachableStates(Automaton automaton) {
        Set<State> visited = new HashSet<State>();
        State currentState = automaton.initialState;
        visitNodes(automaton, currentState, visited);

        Set<State> statesToRemove = new HashSet<State>(automaton.states);
        statesToRemove.removeAll(visited);

        for (State s : statesToRemove) {
            System.out.println("Unreachable: Removing " + s);
            automaton.remove(s);
        }
        automaton.normalizeIds();
    }

    private static void visitNodes(Automaton automaton, State currentState, Set<State> visited) {
        visited.add(currentState);
        for (State state : automaton.getTransitionsFor(currentState).values()) {
            if (state != null && !visited.contains(state))
                visitNodes(automaton, state, visited);
        }
    }

    private static void removeUselessStates(Automaton automaton) {
        Set<State> toRemove = new HashSet<State>();
        checkIfIsUseless(automaton, automaton.initialState, new HashSet<State>(), toRemove);

        for (State s : toRemove) {
            System.out.println("Useless: Removing " + s);
            automaton.remove(s);
        }
        automaton.normalizeIds();
    }

    private static void checkIfIsUseless(Automaton automaton, State currentState, Set<State> superVisited,
            Set<State> toRemove) {
        superVisited.add(currentState);
        Set<State> visited = new HashSet<State>();

        visitNodes(automaton, currentState, visited);
        boolean canGoToAcceptanceFromHere = false;
        for (State s : visited)
            if (s.isAcceptance)
                canGoToAcceptanceFromHere = true;
        if (!canGoToAcceptanceFromHere)
            toRemove.add(currentState);

        for (State state : automaton.getTransitionsFor(currentState).values()) {
            if (state != null && !superVisited.contains(state))
                checkIfIsUseless(automaton, state, superVisited, toRemove);
        }

    }

    private static void mergeEquivalentStates(Automaton automaton) {
        for (int i = 0; i < automaton.states.size() - 1; i++) {
            for (int j = i + 1; j < automaton.states.size(); j++) {
                boolean equivalent = checkStatesForEquivalence(automaton, automaton.getState(i), automaton.getState(j), new HashSet<State>(), new HashSet<State>());
                if(equivalent)
                    System.out.println("Equivalent: " + automaton.getState(i) + " and " + automaton.getState(j));
            }
        }
    }

    private static boolean checkStatesForEquivalence(Automaton automaton, State state1, State state2, Set<State> visited1, Set<State> visited2) {
        if (state1 != null)
        visited1.add(state1);
        if (state2 != null)
        visited2.add(state2);
        
        if (state1 == state2) {
            return true;
        }

        if (state1 == null || state2 == null)
            return false;
        if (state1.isAcceptance != state2.isAcceptance)
            return false;
            
        for (int key = 0; key < automaton.alphabetRange; key++) {
            State newState1 = automaton.getNextState(state1, key);
            State newState2 = automaton.getNextState(state2, key);

            if (visited1.contains(newState1) || visited2.contains(newState2))
                continue;

            boolean childrenAreEquivalent = checkStatesForEquivalence(automaton, newState1, newState2, visited1, visited2);
            if (!childrenAreEquivalent)
                return false;
        }

        return true;

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

        AutomatonReducer.reduceAutomaton(automaton);

        FileWriter writer = new FileWriter(args[1]);
        AutomatonLoader.dumpIntoFile(automaton, writer);
        writer.close();
    }
}