package edu.gwu.ai.codeknights.tictactoe.gui.controller;

import java.io.IOException;

import edu.gwu.ai.codeknights.tictactoe.gui.TicTacToe;
import edu.gwu.ai.codeknights.tictactoe.gui.util.FXMLLoadResult;
import edu.gwu.ai.codeknights.tictactoe.gui.util.FXMLUtil;
import edu.gwu.ai.codeknights.tictactoe.util.Const;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;
import javafx.stage.Stage;

/**
 * @author zhiyuan
 */
public class StartController {

    @FXML
    private Button stPVE;

    @FXML
    private Button stEVE;

    @FXML
    private Button stOnline;

    @FXML
    private Button stStart;

    @FXML
    private TextField stDim;

    @FXML
    private TextField stWinLen;

    @FXML
    private TextField stTeamId;

    @FXML
    private TextField stOpId;

    @FXML
    private TextField stGameId;

    @FXML
    private Text stErr;

    @FXML
    void initialize() {

        stErr.setStyle("-fx-fill: red");

        stDim.textProperty().addListener(observable -> checkDimWinLen());
        stWinLen.textProperty().addListener(observable -> checkDimWinLen());

        stPVE.setOnAction(event -> {
            stPVE.disableProperty().set(true);
            stEVE.disableProperty().set(false);
            stOnline.disableProperty().set(false);
        });

        stEVE.setOnAction(event -> {
            stPVE.disableProperty().set(false);
            stEVE.disableProperty().set(true);
            stOnline.disableProperty().set(false);
        });

        stOnline.setOnAction(event -> {
            stPVE.disableProperty().set(false);
            stEVE.disableProperty().set(false);
            stOnline.disableProperty().set(true);
        });

        stPVE.setDisable(true);
    }

    private boolean checkDimWinLen(){
        try {
            Integer winLen = Integer.parseInt(stWinLen.getText());
            Integer dim = Integer.parseInt(stDim.getText());
            if (dim < winLen) {
                throw new NumberFormatException();
            }
            stErr.setText("");
        } catch (NumberFormatException ex) {
            stErr.setText("dimension must be equal to or larger than win length");
            return false;
        }
        return true;
    }

    @FXML
    void startHandler(ActionEvent event) {
        Integer dim = Integer.parseInt(stDim.getText());
        Integer winLen = Integer.parseInt(stWinLen.getText());

        GameMode mode = getMode();

        Stage primaryStage = TicTacToe.getPrimaryStage();
        try {
            Integer teamId = Integer.parseInt(stTeamId.getText());
            Integer opponentId = Integer.parseInt(stOpId.getText());
            Long gameId = Long.parseLong(stGameId.getText());
            // load the main scene, then current scene will dismiss
            FXMLLoadResult result = FXMLUtil.loadAsNode("fxml/main.fxml");
            FXMLUtil.addStylesheets(result.getNode(), Const.UNIVERSAL_STYLESHEET_URL);
            primaryStage.setScene(new Scene(result.getNode()));
            primaryStage.setResizable(true);
            MainController controller = (MainController) result.getController();
            controller.setup(gameId, dim, winLen, mode, teamId, opponentId);
            stStart.setDisable(true);
        } catch (IOException e) {
            e.printStackTrace();
        }catch (NumberFormatException e){
            stErr.setText("Invalid Number");
        }
    }

    private GameMode getMode(){
        if(stPVE.isDisabled()){
            // PVE
            return GameMode.PVE;
        }else{
            if (stEVE.isDisabled()){
                // EVE
                return GameMode.EVE;
            }else{
                // EVE Online
                return GameMode.EVE_ONLINE;
            }
        }
    }
}
