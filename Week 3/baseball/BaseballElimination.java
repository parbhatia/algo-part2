import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

import edu.princeton.cs.algs4.FlowEdge;
import edu.princeton.cs.algs4.FlowNetwork;
import edu.princeton.cs.algs4.FordFulkerson;
import edu.princeton.cs.algs4.In;
import edu.princeton.cs.algs4.StdOut;

public class BaseballElimination {
    private class Team {
        private String name;
        private int wins;
        private int losses;
        private int remaining;
        private boolean eliminated;
        HashMap<Team, Integer> versus;
        ArrayList<String> eliminated_subset;

        public Team(String name, int wins, int losses, int remaining) {
            this.name = name;
            this.wins = wins;
            this.losses = losses;
            this.remaining = remaining;
            this.eliminated = false;
            versus = new HashMap<Team, Integer>();
            eliminated_subset = new ArrayList<String>();
        }

        public String name() {
            return name;
        }

        public int wins() {
            return wins;
        }

        public int losses() {
            return losses;
        }

        public int remaining() {
            return remaining;
        }

        public void insert_matches(Team team, int matches) {
            versus.put(team, matches);
        }

        public Integer versus(Team team) {
            return versus.get(team);
        }

        public void eliminate() {
            this.eliminated = true;
        }

        public void addToEliminatedSubset(String team) {
            this.eliminated_subset.add(team);
        }

        public Iterable<String> eliminatedSubset() {
            return eliminated_subset;
        }

        public boolean isEliminated() {
            return this.eliminated;
        }

    }

    private class CustomFlowOutput {
        public final FlowNetwork fn;
        public final int source;
        public final int sink;
        public final int num_of_game_vertices;
        public final int num_of_team_vertices;

        public CustomFlowOutput(FlowNetwork fn, int source, int sink, int num_of_game_vertices,
                int num_of_team_vertices) {
            this.fn = fn;
            this.source = source;
            this.sink = sink;
            this.num_of_team_vertices = num_of_team_vertices;
            this.num_of_game_vertices = num_of_game_vertices;
        }
    }

    private final int num_of_teams;
    private LinkedHashMap<String, Team> teams = new LinkedHashMap<String, Team>();

    public BaseballElimination(String filename) {
        checkNullArg(filename);
        In in = new In(filename);
        num_of_teams = Integer.parseInt(in.readLine());
        teams = new LinkedHashMap<String, Team>();
        Team[] teams_temp = new Team[num_of_teams];
        HashMap<Integer, int[]> team_vs_team = new HashMap<Integer, int[]>();
        int t = 0;
        while (in.hasNextLine()) {
            String line = in.readLine().trim();
            String[] split = line.split("\\s+");
            String name = split[0];
            Integer wins = Integer.parseInt(split[1]);
            Integer losses = Integer.parseInt(split[2]);
            Integer remaining = Integer.parseInt(split[3]);
            teams_temp[t] = new Team(name, wins, losses, remaining);
            int[] schedule = new int[split.length - 4];
            for (int i = 0; i < num_of_teams; ++i) {
                schedule[i] = Integer.parseInt(split[4 + i]);
            }
            team_vs_team.put(t, schedule);
            ++t;
        }
        for (Integer teamIdx : team_vs_team.keySet()) {
            int[] team_schedule = team_vs_team.get(teamIdx);
            for (int teamIdx2 = 0; teamIdx2 < num_of_teams; ++teamIdx2) {
                teams_temp[teamIdx].insert_matches(teams_temp[teamIdx2], team_schedule[teamIdx2]);
            }
        }
        for (Team team : teams_temp) {
            teams.put(team.name(), team);
        }
        checkTrivialElimination();
        runFF();
    }

    private void validateTeam(String team) {
        if (teams.get(team) == null) {
            throw new IllegalArgumentException("Invalid team");
        }
    }

    private void checkNullArg(String arg) {
        if (arg == null) {
            throw new IllegalArgumentException("Null string");
        }
    }

    private void validateTeams(String team1, String team2) {
        if (teams.get(team1) == null || teams.get(team2) == null) {
            throw new IllegalArgumentException("Invalid teams");
        }
    }

    private void checkNullArgs(String arg1, String arg2) {
        if (arg1 == null || arg2 == null) {
            throw new IllegalArgumentException("Null string");
        }
    }

    // number of teams
    public int numberOfTeams() {
        return num_of_teams;
    }

    // all teams
    public Iterable<String> teams() {
        return teams.keySet();
    }

    // remaining teams, when team is removed
    private ArrayList<String> remainingTeams(String team) {
        ArrayList<String> all_teams = new ArrayList<>(teams.keySet());
        all_teams.remove(team);
        return all_teams;
    }

    // number of wins for given team
    public int wins(String team) {
        checkNullArg(team);
        validateTeam(team);
        return teams.get(team).wins();
    }

    // number of losses for given team
    public int losses(String team) {
        checkNullArg(team);
        validateTeam(team);
        return teams.get(team).losses();
    }

    // number of remaining games for given team
    public int remaining(String team) {
        checkNullArg(team);
        validateTeam(team);
        return teams.get(team).remaining();
    }

    // number of remaining games between team1 and team2
    public int against(String team1, String team2) {
        checkNullArgs(team1, team2);
        validateTeams(team1, team2);
        return teams.get(team1).versus(teams.get(team2));
    }

    private CustomFlowOutput createFlowNetwork(String team) {
        ArrayList<String> remaining_teams = remainingTeams(team);
        int num_of_team_vertices = remaining_teams.size();
        int num_of_game_vertices = 0;

        // store edge weights for game vertices
        ArrayList<Integer> game_vertices_weights_to = new ArrayList<Integer>();

        // count how many game_vertices there are, and store games remaining between two
        // teams
        for (int i = 0; i < num_of_team_vertices; ++i) {
            for (int j = i + 1; j < num_of_team_vertices; ++j) {
                String team1 = remaining_teams.get(i);
                String team2 = remaining_teams.get(j);
                game_vertices_weights_to.add(against(team1, team2));
                num_of_game_vertices++;
            }
        }
        // we'll have vertices like: 0 ... game_vertices, game_vertices + 1 ...
        // team_vertices, source, sink
        int total_vertices = num_of_game_vertices + num_of_team_vertices + 2;
        int source = total_vertices - 2;
        int sink = total_vertices - 1;
        FlowNetwork fn = new FlowNetwork(total_vertices);

        // source to game_vertices edges
        for (int gameIdx = 0; gameIdx < num_of_game_vertices; ++gameIdx) {
            int weight = game_vertices_weights_to.get(gameIdx);
            FlowEdge e = new FlowEdge(source, gameIdx, weight);
            fn.addEdge(e);
        }

        // game_vertices to team_vertices
        int game_idx_start = 0;
        int game_idx_end = num_of_game_vertices;
        for (int i = 0; i < num_of_team_vertices; ++i) {
            for (int j = i + 1; j < num_of_team_vertices; ++j) {
                FlowEdge e = new FlowEdge(game_idx_start, game_idx_end + i, Double.POSITIVE_INFINITY);
                FlowEdge e2 = new FlowEdge(game_idx_start, game_idx_end + j, Double.POSITIVE_INFINITY);
                fn.addEdge(e);
                fn.addEdge(e2);
                ++game_idx_start;
            }
        }

        double upperbound_team_edge_weight = wins(team) + remaining(team);

        // team_vertices to sink edges
        for (int teamIdxStart = 0; teamIdxStart < num_of_team_vertices; ++teamIdxStart) {
            String local_team = remaining_teams.get(teamIdxStart);
            int teamIdx = teamIdxStart + game_idx_end;
            int wins = wins(local_team);
            double weight = upperbound_team_edge_weight - wins;
            FlowEdge e = new FlowEdge(teamIdx, sink, weight);
            fn.addEdge(e);
        }

        return new CustomFlowOutput(fn, source, sink, num_of_game_vertices, num_of_team_vertices);
    }

    private void checkTrivialElimination() {
        // determine max score of any team
        String maxWinningTeam = "";
        int maxWinScore = 0;
        for (String team : teams()) {
            if (teams.get(team).wins() >= maxWinScore) {
                maxWinScore = teams.get(team).wins();
                maxWinningTeam = team;
            }
        }
        // eliminate any team that cannot reach max winning score
        for (String team : teams()) {
            Team t = teams.get(team);
            if (t.wins() + t.remaining() < maxWinScore && team != maxWinningTeam) {
                t.eliminate();
                t.addToEliminatedSubset(maxWinningTeam);
            }
        }
    }

    private void runFF() {
        for (String team : teams()) {
            if (teams.get(team).isEliminated() == true) {
                continue;
            }
            ArrayList<String> remainingTeams = remainingTeams(team);
            CustomFlowOutput output = createFlowNetwork(team);
            FlowNetwork fn = output.fn;
            int source = output.source;
            int sink = output.sink;

            FordFulkerson ff = new FordFulkerson(fn, source, sink);
            // StdOut.println(team);
            // StdOut.println(fn.toString());

            for (int teamIdxStart = 0; teamIdxStart < output.num_of_team_vertices; ++teamIdxStart) {
                int teamIdx = teamIdxStart + output.num_of_game_vertices;
                // we get the set of vertices connected to source by undirected path with no
                // full forward or empty backward edges
                // this set gives us the min-cut
                // we check if any team vertices are part of the min-cut, if so, then this
                // indicates that there is no scenario in which the team in question can win
                if (ff.inCut(teamIdx)) {
                    teams.get(team).eliminate();
                    teams.get(team).addToEliminatedSubset(remainingTeams.get(teamIdxStart));
                }
            }
        }
    }

    // is given team eliminated?
    public boolean isEliminated(String team) {
        checkNullArg(team);
        validateTeam(team);
        return teams.get(team).isEliminated();
    }

    // subset R of teams that eliminates given team; null if not eliminated
    public Iterable<String> certificateOfElimination(String team) {
        checkNullArg(team);
        validateTeam(team);
        if (!teams.get(team).isEliminated()) {
            return null;
        }
        return teams.get(team).eliminatedSubset();
    }

    public static void main(String[] args) {
        BaseballElimination division = new BaseballElimination(args[0]);
        for (String team : division.teams()) {
            if (division.isEliminated(team)) {
                StdOut.print(team + " is eliminated by the subset R = { ");
                for (String t : division.certificateOfElimination(team)) {
                    StdOut.print(t + " ");
                }
                StdOut.println("}");
            } else {
                StdOut.println(team + " is not eliminated");
            }
        }
    }
}