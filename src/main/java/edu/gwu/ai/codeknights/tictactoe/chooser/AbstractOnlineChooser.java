package edu.gwu.ai.codeknights.tictactoe.chooser;

import edu.gwu.ai.codeknights.tictactoe.core.Game;
import edu.gwu.ai.codeknights.tictactoe.core.Player;
import edu.gwu.ai.codeknights.tictactoe.util.API;
import edu.gwu.ai.codeknights.tictactoe.util.res.GetMovesRes;
import edu.gwu.ai.codeknights.tictactoe.util.res.GetMovesRes.Move;
import org.pmw.tinylog.Logger;
import retrofit2.Call;
import retrofit2.Response;

import java.io.IOException;
import java.util.List;

public abstract class AbstractOnlineChooser extends AbstractCellChooser {

  /**
   * build the game board according to the online board
   * */
  public static void tryFastForward(final Game game) {
    List<Move> moves = getMoves(game);
    if(moves != null){
      for(Move move: moves){
        Player player = game.getPlayer(move.getTeamId());
        if(player != null){
          game.getBoard().getCell(move.getMoveX(), move.getMoveY())
              .setPlayer(player);
        }
      }
    }
  }

  static List<Move> getMoves(Game game){
    final long gameId = game.getGameId();
    final int dim = game.getDim();
    final int numCells = dim * dim;
    final Call<GetMovesRes> call = API.getApiService().getMoves(String.valueOf(gameId), numCells);
    try {
      final Response<GetMovesRes> response = call.execute();
      final GetMovesRes body = response.body();
      if(body == null){
        return null;
      }
      String code = body.getCode();
      if (code.equals(API.API_CODE_SUCCESS)) {
        List<Move> moves = body.getMoves();
        if(moves != null){
          return moves;
        }
      }
    }
    catch (final IOException e) {
      Logger.error(e, "error while fetching moves from server to fast-forward game");
    }
    return null;
  }
}
