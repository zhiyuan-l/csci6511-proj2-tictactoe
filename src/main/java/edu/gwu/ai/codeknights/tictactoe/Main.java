package edu.gwu.ai.codeknights.tictactoe;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.pmw.tinylog.Logger;

public class Main {

  /*
   * TODO: Some things to consider:
   *   - minimax algorithm?
   *   - alpha beta pruning algorithm?
   *   - graph-based search by hashing and storing visited already board states?
   *   - pre-compute some lookup tables for various board sizes?
   *   - how to make best-effort choice if we are running out of time?
   *   - is there a general heuristic (for any board size) to choose the BEST FIRST MOVE? i.e., always play close to center?
   *   - what is the API to interface w/ the REST/JSON game server?
   */

  public static void main(final String[] args) throws DimensionException, StateException {
    // Default values
    int dim = 3;
    int winLength = 3;
    String[] stateArgs = null;

    // Command-line options
    final Option helpOpt = Option.builder("h").longOpt("help").desc("print this usage information").build();
    final Option dimOpt = Option.builder("d").longOpt("dim").hasArg().argName("DIM")
      .desc("board dimension (default is " + String.valueOf(dim) + ")").build();
    final Option winLengthOpt = Option.builder("l").longOpt("win-length").hasArg().argName("LEN")
      .desc("length of sequence required to win (default is " + String.valueOf(winLength) + ")").build();
    final Option stateOpt = Option.builder("s").longOpt("state").hasArgs().argName("CELLS")
      .desc("initial state of board (default is an empty board); moves of the first player given by '"
        + String.valueOf(Game.FIRST_PLAYER_CHAR) + "' or '" + String.valueOf(Game.FIRST_PLAYER_VALUE)
        + "'; moves of the other player given by '" + String.valueOf(Game.OTHER_PLAYER_CHAR) + "' or '"
        + String.valueOf(Game.OTHER_PLAYER_VALUE) + "'; empty spaces given by '" + String.valueOf(Game.BLANK_SPACE_CHAR)
        + "'")
      .build();

    final Options options = new Options();
    options.addOption(helpOpt);
    options.addOption(dimOpt);
    options.addOption(winLengthOpt);
    options.addOption(stateOpt);

    // Parse command-line options
    final CommandLineParser parser = new DefaultParser();
    CommandLine line = null;
    Logger.trace("parsing command-line options");
    try {
      line = parser.parse(options, args);
    }
    catch (final ParseException e) {
      Logger.error(e, "error while parsing command-line options");
    }
    if (line != null) {
      if (line.hasOption(dimOpt.getLongOpt())) {
        final String dimVal = line.getOptionValue(dimOpt.getLongOpt());
        try {
          dim = Integer.parseInt(dimVal);
        }
        catch (final NumberFormatException e) {
          Logger.error(e, "could not parse dim: " + dimVal);
        }
      }
      if (line.hasOption(winLengthOpt.getLongOpt())) {
        final String winLengthVal = line.getOptionValue(winLengthOpt.getLongOpt());
        try {
          winLength = Integer.parseInt(winLengthVal);
        }
        catch (final NumberFormatException e) {
          Logger.error(e, "could not parse winLength: " + winLengthVal);
        }
      }
      if (line.hasOption(stateOpt.getLongOpt())) {
        stateArgs = line.getOptionValues(stateOpt.getLongOpt());
      }
    }
    if (line == null || line.hasOption(helpOpt.getLongOpt())) {
      // Print usage information
      final HelpFormatter formatter = new HelpFormatter();
      formatter.printHelp(" ", options);
      if (line == null) {
        System.exit(1);
      }
    }
    else {
      // Create the game
      final Integer[][] board = new Integer[dim][dim];
      if (stateArgs != null) {
        for (int i = 0; i < dim; i++) {
          int idx = i * dim;
          if (idx >= stateArgs.length) {
            break;
          }
          for (int j = 0; j < dim; j++) {
            idx = i * dim + j;
            if (idx >= stateArgs.length) {
              break;
            }
            final String curArg = stateArgs[idx].trim();
            try {
              board[i][j] = Integer.parseInt(curArg);
            }
            catch (final NumberFormatException e) {
              if (curArg.equalsIgnoreCase(String.valueOf(Game.FIRST_PLAYER_CHAR))) {
                board[i][j] = Game.FIRST_PLAYER_VALUE;
              }
              else if (curArg.equalsIgnoreCase(String.valueOf(Game.OTHER_PLAYER_CHAR))) {
                board[i][j] = Game.OTHER_PLAYER_VALUE;
              }
              else {
                board[i][j] = null;
              }
            }
          }
        }
      }
      final Game game = new Game(dim, winLength, board);

      // TODO: actually play the game; create Solver class that selects the best next move
      Logger.info("All lines on board:\n{}\n", game.toStringAllLines(" * "));
      Logger.info("Game board state:\n{}\n", game.toString());
      Logger.info("# spaces:       {}={}, {}={}, {}={}", Game.FIRST_PLAYER_CHAR, game.countFirstPlayer(),
        Game.OTHER_PLAYER_CHAR, game.countOtherPlayer(), Game.BLANK_SPACE_CHAR, game.countEmpty());
      Logger.info("Is game over?   {}", game.isGameOver());
      Logger.info("Did anyone win? {}", game.didAnyPlayerWin());
      Logger.info("Who won?        {}={}, {}={}", Game.FIRST_PLAYER_CHAR, game.didFirstPlayerWin(),
        Game.OTHER_PLAYER_CHAR, game.didOtherPlayerWin());
    }
  }
}
