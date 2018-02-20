package edu.gwu.ai.codeknights.tictactoe;

import java.util.*;
import java.util.stream.Collectors;

public abstract class MoveChooser {

    private boolean randomChoice;
    private final Map<Long, Long> hashScoreMap;

    public MoveChooser() {
        randomChoice = false;
        hashScoreMap = new HashMap<>();
    }

    public boolean isRandomChoice() {
        return randomChoice;
    }

    public void setRandomChoice(final boolean randomChoice) {
        this.randomChoice = randomChoice;
    }

    protected Map<Long, Long> getHashScoreMap() {
        return hashScoreMap;
    }

    protected List<Game.Move> findEmptyMoves(final Game game){
        final int dim = game.getDim();
        List<Game.Move> moves = new ArrayList<>();
        final int curPlayer = game.getNextPlayer();

        for (int rowIdx = 0; rowIdx < dim; rowIdx++) {
            for (int colIdx = 0; colIdx < dim; colIdx++) {
                final Integer value = game.getCellValue(rowIdx, colIdx);
                if (value == null) {
                    // This is a possible move (empty cell)
                    moves.add(new Game.Move(rowIdx, colIdx, curPlayer, null));
                }
            }
        }
        return moves;
    }

    protected List<Game.Move> findPossibleMoves(final Game game) {
        List<Game.Move> moves = new ArrayList<>();
        final int dim = game.getDim();
        final int prevPlayer = game.getPrevPlayer();
        final int curPlayer = game.getNextPlayer();
        for (int rowIdx = 0; rowIdx < dim; rowIdx++) {
            for (int colIdx = 0; colIdx < dim; colIdx++) {
                final Integer value = game.getCellValue(rowIdx, colIdx);
                if (value == null) {
                    // rule 1: if there's a chance to win, take it immediately
                    game.setCellValue(rowIdx, colIdx, curPlayer);
                    final boolean didWin = game.didPlayerWin(curPlayer);
                    game.setCellValue(rowIdx, colIdx, null);
                    if (didWin) {
                        return Collections.singletonList(new Game.Move(rowIdx, colIdx, curPlayer, null));
                    }

                    // rule 2: if the other player can win, block it immediately
                    game.setCellValue(rowIdx, colIdx, prevPlayer);
                    final boolean didLose = game.didPlayerWin(prevPlayer);
                    game.setCellValue(rowIdx, colIdx, null);
                    if (didLose) {
                        return Collections.singletonList(new Game.Move(rowIdx, colIdx, curPlayer, null));
                    }

                    // This is a possible move (empty cell)
                    moves.add(new Game.Move(rowIdx, colIdx, curPlayer, null));
                }
            }
        }

        // rule 3: don't consider the move if it is adjacent to none
        moves = moves.stream().filter(move -> hasNeighbors(game, move)).collect(Collectors.toList());
        // rule 4: don't consider the move if it is not adjacent to winning lines
        ArrayList<ArrayList<Game.Move>> sequences = getWinningSequences(game);
        moves = new ArrayList<>(findMovesAdjacentToWinningLine(game, moves, sequences));
        return moves;
    }

    /**
     * return set of moves that adjacent to the head and the tail of winning
     * lines
     * */
    private Set<Game.Move> findMovesAdjacentToWinningLine(Game game, List<Game.Move> moves,
                                                          ArrayList<ArrayList<Game.Move>> sequences) {
        Set<Game.Move> filterMoves = new HashSet<>();

        for (ArrayList<Game.Move> sequence : sequences) {
            Game.Move head = null;
            Game.Move tail = null;
            int size = sequence.size();
            if (size > 0) {
                head = sequence.get(0);
                tail = sequence.get(size - 1);
            }

            for (Game.Move move : moves) {
                if(isNeighbors(game, head, move) || isNeighbors(game, tail, move)){
                    filterMoves.add(move);
                }
            }
        }

        return filterMoves;
    }

    private boolean isNeighbors(final Game game, final Game.Move first, final
    Game.Move second) {

        if(first == null || second == null){
            return false;
        }

        int dim = game.getDim();

        boolean flag = false;
        for (int i = first.colIdx - 1; i < first.colIdx + 2; i++) {
            for (int j = first.rowIdx - 1; j < first.rowIdx + 2; j++) {
                // skip non-existent moves
                if (i < dim && j < dim && i > 0 && j > 0) {
                    if (second.colIdx == i && second.rowIdx == j) {
                        flag = true;
                        break;
                    }
                }
            }

            if (flag) {
                break;
            }
        }

        return flag;
    }

    /**
     * rule 3
     * determine if a move has occupied neighbors
     */
    private boolean hasNeighbors(final Game game, final Game.Move move) {
        int dim = game.getDim();
        // available signal
        boolean flag = false;
        for (int i = move.colIdx - 1; i < move.colIdx + 2; i++) {
            for (int j = move.rowIdx - 1; j < move.rowIdx + 2; j++) {
                // skip non-existent moves
                if (i < dim && j < dim && i > 0 && j > 0) {
                    if (game.getCellValue(j, i) != null) {
                        flag = true;
                        break;
                    }
                }
            }

            if (flag) {
                break;
            }
        }

        return flag;
    }

    private ArrayList<ArrayList<Game.Move>> getWinningSequences(Game game) {
        ArrayList<ArrayList<Game.Move>> sequences = new ArrayList<>();
        final Map<String, Game.Move[]> lines = game.getAllLinesOfMove(game.getWinLength());
        for (Game.Move[] line : lines.values()) {
            ArrayList<Game.Move> sequence = null;
            Integer prevPlayer = null;
            for (Game.Move move : line) {
                Integer player = move.player;

                // current move is empty
                if (player == null) {
                    prevPlayer = null;
                    continue;
                }

                if (prevPlayer == null || !prevPlayer.equals(player)) {
                    // different player
                    sequence = new ArrayList<>();
                    sequence.add(move);
                    prevPlayer = player;
                    // put sequence into list
                    sequences.add(sequence);
                    continue;
                }

                // same player
                if (prevPlayer.equals(player)) {
                    sequence.add(move);
                }
            }
        }

        ArrayList<ArrayList<Game.Move>> filterSequences = new ArrayList<>();
        int maxOfFirst = 0;
        int maxOfOther = 0;

        // findMovesAdjacentToWinningLine the max line length for each player
        for (ArrayList<Game.Move> line : sequences) {
            int size = line.size();
            if(size > 0){
                if(line.get(0).player == Game.FIRST_PLAYER_VALUE){
                    if(size > maxOfFirst){
                        maxOfFirst = size;
                    }
                }else{
                    if(line.get(0).player == Game.OTHER_PLAYER_VALUE){
                        if(size > maxOfOther){
                            maxOfOther = size;
                        }
                    }
                }
            }
        }

        // filter max length lines
        for (ArrayList<Game.Move> line : sequences) {
            int size = line.size();
            if(size > 0){
                if(line.get(0).player == Game.FIRST_PLAYER_VALUE){
                    if(size >= maxOfFirst){
                        filterSequences.add(line);
                    }
                }else{
                    if(line.get(0).player == Game.OTHER_PLAYER_VALUE){
                        if(size >= maxOfOther){
                            filterSequences.add(line);
                        }
                    }
                }
            }
        }

        return filterSequences;
    }

    protected Game.Move selectMove(final Game game, final List<Game.Move>
            moves) {
        if (moves.size() > 1 && isRandomChoice()) {
            // If many moves scored equally, choose randomly from among them
            return moves.get(new Random().nextInt(moves.size()));
        } else if (moves.size() > 0) {
            // Return best move
            return moves.get(0);
        } else {
            // No winning move found !!!
            List<Game.Move> emptyMoves = findEmptyMoves(game);
            Collections.shuffle(emptyMoves);
            return emptyMoves.get(0);
        }
    }

    public abstract Game.Move findBestMove(final Game game);
}
