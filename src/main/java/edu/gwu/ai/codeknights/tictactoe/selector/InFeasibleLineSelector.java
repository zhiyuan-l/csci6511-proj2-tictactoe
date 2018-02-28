package edu.gwu.ai.codeknights.tictactoe.selector;

import java.util.ArrayList;
import java.util.List;

import edu.gwu.ai.codeknights.tictactoe.core.Game;

public class InFeasibleLineSelector implements CellSelector, CellFilter {

  @Override
  public List<Cell> selectCells(final Game game) {
    final List<Cell> cells = new ArrayList<>();
    final int dim = game.getDim();
    final int nextPlayer = game.getNextPlayer();
    // game.getAllLineCells();
    // TODO: need an iterable board (or game?) that knows about cells
    for (int i = 0; i < dim; i++) {
      for (int j = 0; j < dim; j++) {
        // TODO: finish
      }
    }
    return cells;
  }

  @Override
  public List<Cell> filterCells(final List<Cell> input, final Game game) {
    // TODO Auto-generated method stub
    return null;
  }
}