package edu.gwu.ai.codeknights.tictactoe.selector;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.pmw.tinylog.Logger;

public class RuleBasedChooser extends AbstractCellChooser {

  @Override
  public Cell chooseCell(final Stream<Cell> input, final TicTacToeGame game) {
    final List<Cell> cells = input.collect(Collectors.toList());
    final int dim = game.getDim();
    final int winLength = game.getWinLength();
    // final Player player = game.getNextPlayer(); // TODO: should need nextPlayer for most rule-based decisions
    final Board board = game.getBoard();

    // Rule 1: first move goes near the center
    if (board.isEmpty()) {
      final int center = (int) (dim / 2);
      final Cell centerCell = cells.parallelStream()
        .filter(cell -> Math.abs(cell.getRowIdx() - center) <= 1 && Math.abs(cell.getColIdx() - center) <= 1)
        .findAny().orElse(null);
      if (centerCell != null) {
        return centerCell;
      }
    }

    // Rule 2: win if immediately possible
    board.getLinesAtLeastLength(winLength).parallelStream()
      .filter(line -> true)
      .findFirst()
      .orElse(null);
    // TODO: finish this one with a correct implementation

    // Rule 3: block opponent wins at both ends of lines
    // TODO

    // Rule 4: create a winnable fork (win in two ways)
    // TODO

    // Rule 5: prevent opponent fork
    // TODO

    // Rule 6: maximize win potential
    // TODO

    // TODO: finish
    Logger.debug("TODO: finish RuleBasedChooser.choosePlay()");
    return null;
  }
}
