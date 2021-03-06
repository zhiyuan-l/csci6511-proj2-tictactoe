package edu.gwu.ai.codeknights.tictactoe.filter;

import edu.gwu.ai.codeknights.tictactoe.core.Cell;
import edu.gwu.ai.codeknights.tictactoe.core.Game;
import edu.gwu.ai.codeknights.tictactoe.core.Player;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class BestOpenSublineFilter extends EmptyCellFilter {

  @Override
  public Stream<Cell> filterCells(final Stream<Cell> input, final Game game) {
    final List<Cell> inputCells = input.collect(Collectors.toList());
    final int winLength = game.getWinLength();
    final Player player = game.getNextPlayer();
    final Set<Cell> candidates = new HashSet<>();
    for (final Cell inputCell : inputCells) {
      final List<List<Cell>> lines = game.getBoard().findLinesThrough(inputCell, winLength);
      int maxLength = 0;
      List<Cell> maxSubline = null;
      for (final List<Cell> line : lines) {
        final List<Cell> subline = game.getLongestOpenSublineForPlayer(line, player);
        final int len = subline.size();
        if (subline.size() > maxLength) {
          maxLength = len;
          maxSubline = subline;
        }
      }
      if (maxSubline != null) {
        candidates.addAll(maxSubline);
      }
    }
    return super.filterCells(inputCells.stream(), game)
      .filter(candidates::contains);
  }
}
