import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

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

    public Automaton() {
    }

    public State getState(int id) {
        return states.get(id);
    }

    public State getNextState(State state, int key) {
        Map<Integer, State> stateTransitions = transitions.get(state);
        return stateTransitions.get(key);
    }

}

class d_9778515_9779141 {
    public static void main(String[] args) throws IOException {
        Scanner scanner = new Scanner(new FileReader(args[0]));
        int statesCount = scanner.nextInt();
        int alphabetRange = scanner.nextInt();
        int initialState = scanner.nextInt();

        scanner.nextLine();

        Automaton automaton = new Automaton();
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
    }
}