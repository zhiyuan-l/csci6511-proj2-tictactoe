package edu.gwu.ai.codeknights.tictactoe.chooser;

import edu.gwu.ai.codeknights.tictactoe.core.Game;
import edu.gwu.ai.codeknights.tictactoe.core.Move;

import java.util.*;
import java.util.stream.Collectors;

public abstract class AIMoveChooser extends AbstractMoveChooser {

  private boolean randomChoice;
  private final Map<Long, Long> hashScoreMap;

  AIMoveChooser() {
    randomChoice = false;
    hashScoreMap = new HashMap<>();
  }

  private boolean isRandomChoice() {
    return randomChoice;
  }

  public void setRandomChoice(final boolean randomChoice) {
    this.randomChoice = randomChoice;
  }

  Map<Long, Long> getHashScoreMap() {
    return hashScoreMap;
  }

  @Override
  public abstract Move findNextMove(final Game game);

  static List<Move> findEmptyMoves(final Game game) {
    final int dim = game.getDim();
    List<Move> moves = new ArrayList<>();

    for (int rowIdx = 0; rowIdx < dim; rowIdx++) {
      for (int colIdx = 0; colIdx < dim; colIdx++) {
        final Integer value = game.getCellValue(rowIdx, colIdx);
        if (value == null) {
          // This is a possible move (empty cell)
          moves.add(new Move(rowIdx, colIdx, game.getNextPlayer(), null));
        }
      }
    }
    return moves;
  }

  static List<Move> findPossibleMoves(final Game game) {
    List<Move> moves;
    // check instant win move
    Move winMove = getInstantWinMove(game);
    if (winMove != null) {
      return Collections.singletonList(winMove);
    }

    moves = findEmptyMoves(game);
    moves = moves.stream().filter(move -> hasNeighbor(game, move))
        .collect(Collectors.toList());
    List<List<Move>> sequences = getLongestLines(game);
    moves = new ArrayList<>(findMovesAdjacentToLines(game, moves, sequences));
    Integer player =  game.getNextPlayer();
    moves.forEach(move -> move.player = player);
    return moves;
  }

  /**
   * find all the moves on winning lines
   * */
  static List<Move> yetAnotherMoveFinder(final Game game) {
    List<Move> moves;
    // check instant win move
    Move winMove = getInstantWinMove(game);
    if (winMove != null) {
      return Collections.singletonList(winMove);
    }

    moves = new ArrayList<>(findWinningMoves(game));
//    moves = moves.stream().filter(move -> hasNeighbor(game, move)).collect(Collectors.toList());
    Integer player =  game.getNextPlayer();
    moves.forEach(move -> move.player = player);
    return moves;
  }

  /**
   * find a move that will lead to instant win
   */
  private static Move getInstantWinMove(Game game) {
    List<Move> moves;
    moves = findEmptyMoves(game);
    final int prevPlayer = game.getPrevPlayer();
    final int curPlayer = game.getNextPlayer();
    for (Move move : moves) {
      int rowIdx = move.rowIdx;
      int colIdx = move.colIdx;
      // rule 1: if there's a chance for anyone to win, take it immediately
      game.setCellValue(rowIdx, colIdx, curPlayer);
      final boolean didWin = game.didPlayerWin(curPlayer);
      game.setCellValue(rowIdx, colIdx, null);
      if (didWin) {
        return new Move(rowIdx, colIdx, curPlayer, null);
      }

      // rule 2: if the other player can win, block it immediately
      game.setCellValue(rowIdx, colIdx, prevPlayer);
      final boolean didLose = game.didPlayerWin(prevPlayer);
      game.setCellValue(rowIdx, colIdx, null);
      if (didLose) {
        return new Move(rowIdx, colIdx, prevPlayer, null);
      }
    }
    return null;
  }

  /**
   * find moves that are closest to winning
   */
  private static Set<Move> findWinningMoves(Game game) {
    Set<Move> filterMoves = new HashSet<>();
    List<List<Move>> sequences = getWinningLines(game);
    // extract all empty moves on winning lines
    sequences.forEach(sequence -> filterMoves.addAll(sequence.stream().filter(move -> move.player == null).collect(Collectors.toSet())));
    return filterMoves;
  }

  /**
   * find winning lines for both players
   * a winning line can be either closest to win length or have size of opponent
   * moves short to win length by 2
   */
  private static List<List<Move>> getWinningLines(final Game game) {
    final int winLen = game.getWinLength();
    final int opId = game.getFirstPlayerId();
    final List<List<Move>> completeLines = new ArrayList<>();
    List<List<Move>> filteredLines = new ArrayList<>();
    // the all lines on the board
    final Map<String, Move[]> rawLines = game.getAllLinesOfMove(game.getWinLength());
    // extract all the complete lines from raw lines
    for (Move[] line : rawLines.values()) {
      completeLines.addAll(extractCompleteLines(Arrays.asList(line)));
    }

    // filter out lines longer than or equal to win length
    List<List<Move>> winningCompleteLines = completeLines.stream()
        .filter(wl -> wl.size() >= game.getWinLength()).collect(Collectors.toList());
    List<List<Move>> winningLines = new ArrayList<>();
    // extract the longest winning line
    winningCompleteLines.forEach(line -> winningLines.addAll(extractWinningLine(line)));

    boolean flag = false;
    for (List<Move> line : winningLines) {
      Integer player = getPlayerOfSequence(line);
      // if a opponent sequence is two moves from winning
      // and the two moves are located at head and tail
      // block it
      if (player == opId && line.size() >= winLen) {
        if(getNonEmptyCount(line) >= winLen - 2){
          flag = true;
          filteredLines.add(line);
        }
      }
    }

    if(flag){
      return filteredLines;
    }

    // lines grouped by player
    Map<Integer, List<List<Move>>> groupedByPlayer = winningLines.stream().collect
        (Collectors.groupingByConcurrent(AIMoveChooser::getPlayerOfSequence));

    // put lines into groups grouping by line length
    // find a group with max line length on each player, then put
    // them into filteredLines
    groupedByPlayer.values().forEach(lines -> {
      Map<Integer, List<List<Move>>> groupedByCount = lines.stream().collect
          (Collectors.groupingByConcurrent(List::size));
      Integer maxSize = groupedByCount.keySet().stream().max(Comparator
          .comparingInt(Integer::intValue)).get();

      filteredLines.addAll(groupedByCount.get(maxSize));
    });
    return filteredLines;
  }

  private static Integer getPlayerOfSequence(List<Move> line) {
    Integer player = null;
    for (Move move : line) {
      if (move.player != null) {
        player = move.player;
        break;
      }
    }

    return player;
  }

  /**
   * get count of non-empty moves in a line
   */
  private static int getNonEmptyCount(List<Move> line) {
    int count = 0;

    for (Move move : line) {
      if (move.player != null) {
        count++;
      }
    }

    return count;
  }

  /**
   * extract the longest lines from a single-player line
   * <p>
   * a longest line can be the concatenation of at most two non-empty lines, and
   * there can be only one empty move between them. This line will also have
   * at most one empty move on each side.
   * <p>
   * the input line is a line contains all empty moves and non-empty moves
   * of same player value
   */
  private static List<List<Move>> extractWinningLine(final List<Move> line) {

    List<List<Move>> lines = extractSinglePlayerLines(line);
    List<List<Move>> newLines = new ArrayList<>();
    for (int i = 0; i < lines.size(); i++) {
      List<Move> newLine = new ArrayList<>();
      List<Move> curLine = lines.get(i);
      // skip empty lines
      if (curLine.get(0).player == null) {
        continue;
      }

      newLines.add(newLine);

      if (i - 1 >= 0) {
        // add the empty move before current line
        List<Move> prev = lines.get(i - 1);
        newLine.add(prev.get(prev.size() - 1));
      }

      newLine.addAll(curLine);

      if (i + 1 < lines.size()) {
        // if there is next line
        // add the first empty move of next line
        List<Move> nextNull = lines.get(i + 1);
        newLine.add(nextNull.get(0));
      }

      if (i + 2 < lines.size()) {
        // another non-empty line exists
        // concatenate it to the previous line
        List<Move> nextNull = lines.get(i + 1);
        List<Move> nextLine = lines.get(i + 2);
        int nextNullLen = nextNull.size();
        if (nextNullLen == 1) {
          // only one empty move exists between two non-empty lines
          newLine.addAll(nextLine);
          if (i + 3 < lines.size()) {
            // another empty line exists
            List<Move> next = lines.get(i + 3);
            newLine.add(next.get(0));
          }
        }
      }
    }

    Map<Integer, List<List<Move>>> groupedByLength = newLines.stream().collect
        (Collectors.groupingByConcurrent(List::size));
    Integer maxSize = groupedByLength.keySet().stream().max(Comparator
        .comparingInt(Integer::intValue)).get();

    newLines = groupedByLength.get(maxSize);
    return newLines;
  }

  /**
   * extract all complete lines in a line
   * <p>
   * complete line means a line including all the empty moves on both sides
   * if the line is an empty line, it will not be returned
   * <p>
   * the input lines are extracted from a raw board row, column or diagonal.
   * <p>
   * each input line will be separated into single player value lines, and
   * then built into complete lines
   */
  private static List<List<Move>> extractCompleteLines(List<Move> line) {
    // player value of most recent sequence
    Integer prev = null;
    // non-null player value of the recent sequence
    Integer prevPlayer = null;
    List<List<Move>> completeLines = new ArrayList<>();
    List<Move> prevLine = new ArrayList<>();
    List<List<Move>> extractLines = extractSinglePlayerLines(line);
    List<Move> newLine = new ArrayList<>();
    for (int i = 0; i < extractLines.size(); i++) {
      int j = i;
      while (j < extractLines.size()) {
        List<Move> currentLine = extractLines.get(j);
        Integer current = currentLine.get(0).player;
        if (current == null || current.equals(prevPlayer)) {
          // concatenate to previous line
          newLine.addAll(currentLine);
        } else {
          if (prev == null) {
            // concatenate the previous empty line
            newLine = new ArrayList<>(prevLine);
          } else {
            // create a new line
            newLine = new ArrayList<>();
          }
          completeLines.add(newLine);
          newLine.addAll(currentLine);
          prevPlayer = newLine.get(0).player;
        }
        prev = current;
        prevLine = currentLine;
        j++;
      }
      i = j - 1;
    }

    return completeLines;
  }

  /**
   * break the mixed-player line into lines of single-player lines
   * <p>
   * the input line is a line contains non-empty moves of mixed player value
   * and all non-empty moves, it can be a row, a column or a diagonal in the
   * board
   * <p>
   * it will be separate into lines of single player value, which means this
   * method will return a list consists of lines, and each line can only have
   * moves of same player value, the player value can be null, firstPlayerId
   * or otherPlayerId
   */
  private static List<List<Move>> extractSinglePlayerLines(List<Move> line) {
    Integer prev = null;
    List<Move> newLine = null;
    List<List<Move>> tempLines = new ArrayList<>();
    for (int i = 0; i < line.size(); i++) {
      Move move = line.get(i);
      newLine = new ArrayList<>();
      tempLines.add(newLine);
      int j = i;
      if (move.player == null) {
        while (j < line.size() && line.get(j).player == null) {
          newLine.add(line.get(j));
          j++;
        }
      } else {
        prev = move.player;
        while (j < line.size() && line.get(j).player != null && line.get(j)
            .player.equals(prev)) {
          newLine.add(line.get(j));
          j++;
        }
      }
      i = j - 1;
    }

    return tempLines;
  }

  /**
   * find moves that are adjacent to the head or tail of input lines in
   * input moves
   */
  private static Set<Move> findMovesAdjacentToLines(Game game, List<Move> moves, List<List<Move>> lines) {
    Set<Move> filterMoves = new HashSet<>();
    for (List<Move> sequence : lines) {
      Move head = null;
      Move tail = null;
      int size = sequence.size();
      if (size > 0) {
        head = sequence.get(0);
        tail = sequence.get(size - 1);
      }

      for (Move move : moves) {
        if (isAdjacentMoves(game, head, move) || isAdjacentMoves(game, tail, move)) {
          filterMoves.add(move);
        }
      }
    }

    return filterMoves;
  }

  /**
   * get longest single player value lines
   * these lines does not contain empty moves
   */
  private static List<List<Move>> getLongestLines(Game game) {
    List<List<Move>> allLines = new ArrayList<>();
    final Map<String, Move[]> lines = game.getAllLinesOfMove(game.getWinLength());
    for (Move[] line : lines.values()) {
      List<Move> newLine = null;
      Integer prevPlayer = null;
      for (Move move : line) {
        Integer player = move.player;

        // current move is empty
        if (player == null) {
          prevPlayer = null;
          continue;
        }

        if (prevPlayer == null || !prevPlayer.equals(player)) {
          // different player
          newLine = new ArrayList<>();
          newLine.add(move);
          prevPlayer = player;
          // put line into list
          allLines.add(newLine);
          continue;
        }

        // same player
        if (prevPlayer.equals(player)) {
          newLine.add(move);
        }
      }
    }

    List<List<Move>> filteredLines = new ArrayList<>();
    int maxOfFirst = 0;
    int maxOfOther = 0;
    int masterId = game.getFirstPlayerId();
    int opId = game.getOtherPlayerId();

    // find the max line length for each player
    for (List<Move> line : allLines) {
      int size = line.size();
      if (size > 0) {
        if (line.get(0).player == masterId) {
          if (size > maxOfFirst) {
            maxOfFirst = size;
          }
        } else {
          if (line.get(0).player == opId) {
            if (size > maxOfOther) {
              maxOfOther = size;
            }
          }
        }
      }
    }
    int finalMaxOfFirst = maxOfFirst;
    int finalMaxOfOther = maxOfOther;
    // filter max length lines
    allLines.forEach(line -> {
      int size = line.size();
      if (size > 0) {
        if (line.get(0).player == masterId) {
          if (size >= finalMaxOfFirst) {
            filteredLines.add(line);
          }
        } else {
          if (line.get(0).player == masterId) {
            if (size >= finalMaxOfOther) {
              filteredLines.add(line);
            }
          }
        }
      }
    });

    return filteredLines;
  }

  Move selectMove(final Game game, final List<Move>
      moves) {
    if (moves.size() > 1 && isRandomChoice()) {
      // If many moves scored equally, choose randomly from among them
      return moves.get(new Random().nextInt(moves.size()));
    } else if (moves.size() > 0) {
      // Return best move
      return moves.get(0);
    } else {
      // No winning move found !!!
      List<Move> emptyMoves = findEmptyMoves(game);
      Collections.shuffle(emptyMoves);
      return emptyMoves.get(0);
    }
  }

  /**
   * determine if two moves are adjacent to each other
   */
  private static boolean isAdjacentMoves(final Game game, Move first, Move second) {
    if (first == null || second == null) {
      return false;
    }

    int dim = game.getDim();
    boolean isAdjacent = false;
    for (int i = first.colIdx - 1; i < first.colIdx + 2; i++) {
      for (int j = first.rowIdx - 1; j < first.rowIdx + 2; j++) {
        // skip non-existent moves
        if (i < dim && j < dim && i > 0 && j > 0) {
          if (second.colIdx == i && second.rowIdx == j) {
            isAdjacent = true;
            break;
          }
        }
      }

      if (isAdjacent) {
        break;
      }
    }

    return isAdjacent;
  }

  /**
   * determine if a move is adjacent to any non-empty move
   */
  private static boolean hasNeighbor(final Game game, Move move) {
    int dim = game.getDim();
    // available signal
    boolean hasNeighbor = false;
    for (int i = move.colIdx - 1; i < move.colIdx + 2; i++) {
      for (int j = move.rowIdx - 1; j < move.rowIdx + 2; j++) {
        // skip non-existent moves
        if (i < dim && j < dim && i > 0 && j > 0) {
          if (game.getCellValue(j, i) != null) {
            hasNeighbor = true;
            break;
          }
        }
      }

      if (hasNeighbor) {
        break;
      }
    }

    return hasNeighbor;
  }

}