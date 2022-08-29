package syncit.application;

import java.io.File;
import java.util.Timer;
import java.util.TimerTask;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.StringProperty;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.BorderWidths;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Arc;
import javafx.scene.shape.ArcType;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.FileChooser;
import javafx.stage.Screen;
import javafx.stage.Stage;
import syncit.connection.vlc.VlcConnection;
import syncit.starting.vlc.StartingVlc;

public class ApplicationManager extends Application {

	private Stage primaryStage;
	private final String mainColor = "#00666a";
	private final String mainButtonColor = "#55aea2";
	private Font BakbakOner;
	private Font RobotoMedium_14;
//	private StackPane headerPane;
//	private VBox footerPane;
	private File selectedFile;
	private StartingVlc startingVlc;
	private VlcConnection vlcConnection;
	private Label currentStatus;
	private IntegerProperty vlcRunningStatus;
//	private DoubleProperty stageInsideWidthProp = new SimpleDoubleProperty(0);

	public static void main(String[] args) {
		launch();
	}

	public Button getStyledButton(String text, int fSize, String bColor, int paddingV, int paddingH) {
		Button designedButton = new Button(text);
		designedButton.setFont(RobotoMedium_14);
		designedButton.setTextFill(Color.WHITE);
		designedButton.setStyle("-fx-background-color: " + bColor + "; -fx-padding: " + paddingV + "px " + paddingH
				+ "px; -fx-font-size: " + fSize + "; -fx-background-radius: 10px; -fx-font-weight: bold;");
		return designedButton;
	}

	// Remove primary stage
	private Arc getStyledArc(Stage primaryStage, double startAngle, ReadOnlyDoubleProperty bindWidthProperty) {
		Arc styledArc = new Arc();
		styledArc.centerXProperty().bind(bindWidthProperty.divide(2));
		styledArc.setCenterY(0);
		styledArc.radiusXProperty().bind(bindWidthProperty.divide(2));

//		styledArc.setStyle("-fx-width: 100%;");
		styledArc.setType(ArcType.CHORD);
		styledArc.setStartAngle(startAngle);
		styledArc.setLength(180);
		styledArc.setFill(Color.web(mainColor));
		styledArc.setSmooth(true);

		return styledArc;
	}
	
	private Node getHeaderPane(ReadOnlyDoubleProperty bindWidthProperty) {
		
		// Background Rectangle Of the Header
		Rectangle headerRectangle = new Rectangle();
		headerRectangle.widthProperty().bind(bindWidthProperty);
		headerRectangle.setHeight(80);
		headerRectangle.setFill(Color.web(mainColor));

		// Arc of the Header
		Arc headerArc = getStyledArc(primaryStage, -180, bindWidthProperty);
		headerArc.setRadiusY(30);
		
		// ImageView of logo
		ImageView logoImage = new ImageView("file:res/syncit_logo_Membra_Regular.png");
		logoImage.setFitHeight(50);
		logoImage.setPreserveRatio(true);
		logoImage.setSmooth(true);
		
		// StcakPane for the header
		StackPane headerPane = new StackPane();
		headerPane.getChildren().addAll(headerRectangle, headerArc, logoImage);
		StackPane.setAlignment(headerRectangle, Pos.TOP_CENTER);
		StackPane.setAlignment(headerArc, Pos.BOTTOM_CENTER);
		StackPane.setAlignment(logoImage, Pos.CENTER);
		headerPane.setPrefHeight(headerRectangle.getHeight() + headerArc.getRadiusY());

		//Binding width
//		headerRectangle.widthProperty().bind(headerPane.widthProperty());
//		headerArc.radiusXProperty().bind(headerPane.widthProperty().divide(2));
//		headerArc.scaleXProperty().bind(headerPane.widthProperty().divide(2));

		return headerPane;
	}
	
	private Node getFooterPane(ReadOnlyDoubleProperty bindWidthProperty) {
		
		Rectangle footerRectangle = new Rectangle();
		
		footerRectangle.widthProperty().bind(bindWidthProperty);
		footerRectangle.setHeight(20);
		footerRectangle.setFill(Color.web(mainColor));

		Arc footerArc = getStyledArc(primaryStage, 0, bindWidthProperty);
		footerArc.setRadiusY(10);
		
		// VBox for footer
		VBox footerPane = new VBox();
		footerPane.getChildren().addAll(footerArc, footerRectangle);
		
		return footerPane;
	}

	public Scene getHomeScene() {
		//Setting title of the primary stage
		primaryStage.setTitle("SyncIt - Select file to play in vlc");
		
		// GridPane and Nodes for selecting files
		Text textToSelect = new Text("Select File to Play");
		textToSelect.setFont(BakbakOner);

		TextField fileLocation = new TextField();
		fileLocation.setFont(RobotoMedium_14);
		fileLocation.setPromptText("Enter location or Select file");
		fileLocation.setStyle("-fx-focus-color: #8e0039; -fx-faint-focus-color: #8e003922; -fx-padding: 10px 10px; -fx-border-color: #a86d8b;");
		fileLocation.setPrefWidth(265);

		Button selectFileB = getStyledButton("Select", 18, "#8e0039", 5, 10);
		
		Button playButton = getStyledButton("PLAY", 24, mainButtonColor, 8, 40);
		playButton.setFont(BakbakOner);
		
		Label whenFileSelected = new Label("");
		whenFileSelected.setFont(RobotoMedium_14);
		Label ifFileNotSelected = new Label("");
		ifFileNotSelected.setFont(RobotoMedium_14);
		ifFileNotSelected.setTextFill(Color.WHITE);
		ifFileNotSelected.setWrapText(true);
		
		GridPane fileSelectionPane = new GridPane();
		fileSelectionPane.setAlignment(Pos.CENTER);
		fileSelectionPane.add(textToSelect, 0, 0, 2, 1);
		fileSelectionPane.add(fileLocation, 0, 1);
		fileSelectionPane.add(selectFileB, 1, 1);
		fileSelectionPane.add(whenFileSelected, 0, 2, 2, 1);
		fileSelectionPane.add(playButton, 0, 3, 2, 1);
		fileSelectionPane.add(ifFileNotSelected, 0, 4, 2, 1);
		GridPane.setHalignment(textToSelect, HPos.CENTER);
		GridPane.setHalignment(playButton, HPos.CENTER);
		GridPane.setHalignment(whenFileSelected, HPos.RIGHT);
		GridPane.setHalignment(ifFileNotSelected, HPos.CENTER);
		GridPane.setHalignment(fileLocation, HPos.CENTER);
		GridPane.setHalignment(selectFileB, HPos.CENTER);
		fileSelectionPane.setPadding(new Insets(30));
		GridPane.setMargin(textToSelect, new Insets(0, 0, 15, 0));
		GridPane.setMargin(whenFileSelected, new Insets(5, 0, 10, 0));
		GridPane.setMargin(playButton, new Insets(10, 0, 20, 0));
		
		//FileChooser to select file and event
		FileChooser selectFile = new FileChooser();
		selectFile.setTitle("Select File To Play");
		selectFile.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("All Files", "*.*"),
            new FileChooser.ExtensionFilter("MP4", "*.mp4"),
            new FileChooser.ExtensionFilter("MKV", "*.mkv")
        );
		selectFileB.setOnAction(e -> {
			File nowChoosenFile = selectFile.showOpenDialog(primaryStage);
			if(nowChoosenFile != null) {
				selectedFile = nowChoosenFile;
				whenFileSelected.setText("Selected");
				whenFileSelected.setStyle("-fx-background-color: #ffa2bd; -fx-padding: 3px 6px; -fx-font-weight: bold; -fx-font-style: italic;");
				fileLocation.setText(selectedFile.getAbsolutePath());
				ifFileNotSelected.setText("");
				ifFileNotSelected.setStyle("");
			}
		});
		
		//Key typed event for TextField
		fileLocation.setOnKeyTyped(e -> {
			whenFileSelected.setText("");
			ifFileNotSelected.setText("");
			
			whenFileSelected.setStyle("");
			ifFileNotSelected.setStyle("");
			
			selectedFile = null;
		});
		
		//Action event for play button
		playButton.setOnAction(e -> {
			checkFile(ifFileNotSelected, fileLocation);
		});
		
		// BorderPane for handling all panes
		BorderPane homePane = new BorderPane();
		homePane.setTop(getHeaderPane(homePane.widthProperty()));
		homePane.setCenter(fileSelectionPane);
		homePane.setBottom(getFooterPane(homePane.widthProperty()));
		homePane.setStyle("-fx-background-color: #ffffff;");
		
		return new Scene(homePane);
	}
	
	public Scene getPlayingScene() {
		//Knowing vlc status
		vlcRunningStatus = vlcConnection.getVlcRunningStatus();
		
		//Getting current playing and set it as title of stage
		String currentPlaying = vlcConnection.getPlayingTitle();
//		primaryStage.setTitle("SyncIt - " + currentPlaying.substring(0, currentPlaying.lastIndexOf('.')));
		primaryStage.setTitle("SyncIt - " + currentPlaying);
		
		//Now Playing File Title
		Text textNowPlaying = new Text();
		textNowPlaying.setFont(BakbakOner);
		textNowPlaying.setText("Now Playing: " + currentPlaying);
		textNowPlaying.setTextAlignment(TextAlignment.CENTER);
		
		//Play or Paused Status
//		Text t1 = new Text("Playing");
//		Label currentStatus = new Label();
//		currentStatus.textProperty().bind(t1.textProperty());
//		currentStatus.setFont(loadRobotoFont("Roboto-BoldItalic", 15));
//		currentStatus.textProperty().bind(vlcConnection.getcurrStatusProp());
//		currentStatus.styleProperty().bind(vlcConnection.getcurrStatusStryleProp());
//		currentStatus.setStyle("-fx-background-color: #9de18b; -fx-padding: 3px 6px; -fx-font-weight: bold; -fx-font-style: italic;");
//		vlcConnection.forUpdatingStatus(t1);
//		Platform.setImplicitExit(false);
//		currentStatus.setStyle("-fx-background-color: #ff8278; -fx-padding: 3px 6px; -fx-font-weight: bold; -fx-font-style: italic;");

		
		
		//Sync again button and notify after syncing
		Button syncButton = getStyledButton("Sync", 24, mainButtonColor, 8, 40);
		Text syncedSuccess = new Text();
		syncedSuccess.prefHeight(7);
		syncedSuccess.setFont(loadRobotoFont("Roboto-Bold", 18));
		syncButton.setOnAction(e -> {
			vlcConnection.syncAgain();
			syncedSuccess.setText("Synced");
			Timer clearSynced = new Timer();
			clearSynced.schedule(new TimerTask() {
				
				@Override
				public void run() {
					syncedSuccess.setText("");
					clearSynced.cancel();
					clearSynced.purge();
				}
			}, 2000);
		});
		
		//Play another and quit buttons with events
		Button playAnotherBtn = getStyledButton("Play Another", 16, "#8e0039", 8, 8);
		Button quitButton = getStyledButton("QUIT", 16, "#e73827", 8, 40);
		playAnotherBtn.setOnAction(e -> {
			int currStatusValue = vlcRunningStatus.getValue().intValue();
			if (currStatusValue == 0) {
				vlcConnection.quitAll();
			} else if (currStatusValue == 1) {
				vlcConnection.quitFirst();
			} else if (currStatusValue == 2) {
				vlcConnection.quitSecond();
			} else if (currStatusValue == 3) {
				vlcConnection.closeEverything();
			}
			
			vlcConnection = null;
			primaryStage.setScene(getHomeScene());
		});
		
		quitButton.setOnAction(e -> {
			System.out.println("Quit Request");
			int currStatusValue = vlcRunningStatus.getValue().intValue();
			if (currStatusValue == 0) {
				vlcConnection.quitAll();
			} else if (currStatusValue == 1) {
				vlcConnection.quitFirst();
				System.out.println("Quiting first");
			} else if (currStatusValue == 2) {
				vlcConnection.quitSecond();
			} else if (currStatusValue == 3) {
				vlcConnection.closeEverything();
			}
			
			vlcConnection = null;			
			primaryStage.close();
		});
		
		
		/*
		GridPane middlePane = new GridPane();
		middlePane.add(textNowPlaying, 0, 0, 2, 1);
//		middlePane.add(currentStatus, 1, 1, 2, 1);
		middlePane.add(syncButton, 0, 1, 2, 1);
		middlePane.add(syncedSuccess, 0, 2, 2, 1);
		middlePane.add(playAnotherBtn, 0, 3);
		middlePane.add(quitButton, 1, 3);
		middlePane.setPadding(new Insets(30));
		middlePane.setAlignment(Pos.CENTER);
		GridPane.setHalignment(textNowPlaying, HPos.CENTER);
		GridPane.setHalignment(syncButton, HPos.CENTER);
		GridPane.setHalignment(syncedSuccess, HPos.CENTER);
		GridPane.setHalignment(quitButton, HPos.CENTER);
//		GridPane.setMargin(currentStatus, new Insets(5, 0, 40, 0));
		GridPane.setMargin(syncButton, new Insets(20, 0, 5, 0));
		GridPane.setMargin(syncedSuccess, new Insets(0, 0, 60, 0));
		GridPane.setMargin(playAnotherBtn, new Insets(0, 5, 0, 0));
		GridPane.setMargin(quitButton, new Insets(0, 0, 0, 5));
		*/
		
		//Button for test delete it
//		Button chekW = getStyledButton("Tell Width", 16, "#e73827", 8, 40);
		
		
		GridPane syncPane = new GridPane();
		syncPane.add(syncButton, 0, 0);
		syncPane.add(syncedSuccess, 0, 1);
		GridPane.setHalignment(syncButton, HPos.CENTER);
		GridPane.setHalignment(syncedSuccess, HPos.CENTER);
		GridPane.setMargin(syncButton, new Insets(0, 0, 5, 0));
		syncPane.setAlignment(Pos.CENTER);
		syncPane.setStyle("-fx-background-color: #33FFCC;");
		
		//Delete it
//		syncPane.add(chekW, 0, 2);
		
		GridPane anotherQuitButtonsPane = new GridPane();
		anotherQuitButtonsPane.add(playAnotherBtn, 0, 0);
		anotherQuitButtonsPane.add(quitButton, 1, 0);
		GridPane.setMargin(playAnotherBtn, new Insets(0, 5, 0, 0));
		GridPane.setMargin(quitButton, new Insets(0, 0, 0, 5));
		anotherQuitButtonsPane.setAlignment(Pos.CENTER);
		
		VBox middlePane = new VBox(40);
		middlePane.getChildren().addAll(textNowPlaying, syncPane, anotherQuitButtonsPane);
		middlePane.setPadding(new Insets(10));
		middlePane.setAlignment(Pos.CENTER);
		middlePane.setFillWidth(false);
//		middlePane.setStyle("-fx-background-color: #A9A9A9;");
//		middlePane.setBorder(new Border(new BorderStroke(Color.BLUE, BorderStrokeStyle.DASHED, null, new BorderWidths(2))));
		
		BorderPane playingPane = new BorderPane();
		playingPane.setTop(getHeaderPane(playingPane.widthProperty()));
		playingPane.setCenter(middlePane);
		playingPane.setBottom(getFooterPane(playingPane.widthProperty()));
		playingPane.setStyle("-fx-background-color: #ffffff;");
//		playingPane.setStyle("-fx-background-color: #000000;");
//		playingPane.setBorder(new Border(new BorderStroke(Color.WHITE, BorderStrokeStyle.DASHED, null, new BorderWidths(4))));
//		playingPane.maxWidthProperty().bind(primaryStage.widthProperty());
		
		//Binding wrap width
//		textNowPlaying.wrappingWidthProperty().bind(playingPane.widthProperty().multiply(0.75));
		
		
		vlcRunningStatus.addListener((obs, oldValue, newValue) -> {
			if(newValue.intValue() == 1) {
				Label secondNotLabel = new Label("Seems like second VLC is not running. Wanna start second again ?");
				secondNotLabel.setFont(loadRobotoFont("Roboto-Bold", 20));
				secondNotLabel.setTextFill(Color.WHITE);
				secondNotLabel.setStyle("-fx-background-color: #ff6e7f; -fx-padding: 5px 10px;");
				secondNotLabel.setAlignment(Pos.CENTER);
				secondNotLabel.setWrapText(true);
				//Bind Width for wrapping
				secondNotLabel.maxWidthProperty().bind(middlePane.widthProperty().multiply(0.8));
				
				Button playSecondAgain = getStyledButton("Start Second", 20, mainButtonColor, 8, 30);
				playSecondAgain.setOnAction(e -> {
					startingVlc.startSecondVlcAgain();
					vlcConnection.startSecondConnectAgain();
					Platform.runLater(() -> middlePane.getChildren().set(1, syncPane));
				});
				
				GridPane secondNotPane = new GridPane();
				secondNotPane.add(secondNotLabel, 0, 0);
				secondNotPane.add(playSecondAgain, 0, 1);
				GridPane.setMargin(secondNotLabel, new Insets(0, 0, 10, 0));				
				GridPane.setHalignment(secondNotLabel, HPos.CENTER);
				GridPane.setHalignment(playSecondAgain, HPos.CENTER);
				secondNotPane.setAlignment(Pos.CENTER);
				secondNotPane.setStyle("-fx-background-color: #33FFCC;");
				
				
				
				Platform.runLater(() -> middlePane.getChildren().set(1, secondNotPane));
			} else if (newValue.intValue() == 3) {
				Label secondNotLabel = new Label("Seems like VLC is closed. Wanna play again ?");
				secondNotLabel.setFont(loadRobotoFont("Roboto-Bold", 20));
				secondNotLabel.setTextFill(Color.WHITE);
				secondNotLabel.setStyle("-fx-background-color: #ff6e7f; -fx-padding: 5px 10px;");
				secondNotLabel.setWrapText(true);
				secondNotLabel.maxWidthProperty().bind(middlePane.widthProperty().multiply(0.8));
				
				Platform.runLater(() ->  {
					middlePane.getChildren().set(0, new Text(""));
					middlePane.getChildren().set(1, secondNotLabel);
				});
			}
		});
		
		//Delete it
//		Scene generatedScene;
		
		/*
		chekW.setOnAction(e -> {
			System.out.println("- - - - - - - - - - - - - - - - - - - - - - - - ");
			System.out.println("Primary Stage Width: " + primaryStage.getWidth() );
			System.out.println("Scene Width: " + generatedScene.getWidth());
			System.out.println("Scene Window Width: " + generatedScene.getWindow().getWidth());
//			System.out.println("Header Pane Width: " + headerPane.getWidth());
//			System.out.println("Footer Pane Width: " + footerPane.getWidth());
			System.out.println("Playing Pane (BorderPane) Width: " + playingPane.getWidth());
			System.out.println("VBox Pane width: " + middlePane.getWidth());
			System.out.println("");
		});
		*/
		return new Scene(playingPane);
	}
	
	public static void updateStatus(int pauseOrPlay) {
		if(pauseOrPlay == 0) {
			
		}
	}
	
	public void checkFile(Label ifFileNotSelected, TextField fileLocation) {
		boolean  readyToStart = false;
		
		if(selectedFile == null && (fileLocation.getText() == null || fileLocation.getText().length() == 0) ) {
			ifFileNotSelected.setText("Please select file or enter location");
			ifFileNotSelected.setStyle("-fx-background-color: #ff6e7f; -fx-padding: 5px 10px; -fx-font-weight: bold; -fx-font-size: 20px");
		} else if (selectedFile != null){
			startingVlc = new StartingVlc(selectedFile.getAbsolutePath());
			readyToStart = true;
			startingVlc.startVlc();
			vlcConnection = new VlcConnection(startingVlc.getServerLocation(), startingVlc.getPortFirst(), startingVlc.getPortSecond());
			
		} else if (fileLocation.getText() != null || fileLocation.getText().length() != 0) {
			startingVlc = new StartingVlc(fileLocation.getText());
			readyToStart = true;
			startingVlc.startVlc();
			vlcConnection = new VlcConnection(startingVlc.getServerLocation(), startingVlc.getPortFirst(), startingVlc.getPortSecond());
		}
		
		if(readyToStart) {
			vlcConnection.makeConnection();
			primaryStage.setScene(getPlayingScene());
			vlcConnection.startReading();
		}
		
	}

	private Font loadRobotoFont(String robotoType, double size) {
		return Font.loadFont("file:res/fonts/Roboto/" + robotoType + ".ttf", size);
	}

	@Override
	public void start(Stage primaryStage) throws Exception {

		this.primaryStage = primaryStage;

		// Font loading
		BakbakOner = Font.loadFont("file:res/fonts/BakbakOne-Regular.ttf", 25);
		RobotoMedium_14 = loadRobotoFont("Roboto-Medium", 14);

		/*
		// StcakPane for the header
		headerPane = new StackPane();
		headerPane.getChildren().addAll(headerRectangle, headerArc, logoImage);
		StackPane.setAlignment(headerRectangle, Pos.TOP_CENTER);
		StackPane.setAlignment(headerArc, Pos.BOTTOM_CENTER);
		StackPane.setAlignment(logoImage, Pos.CENTER);
		headerPane.setPrefHeight(headerRectangle.getHeight() + headerArc.getRadiusY());

		// VBox for footer
		footerPane = new VBox();
		Rectangle footerRectangle = new Rectangle();
		footerRectangle.widthProperty().bind(primaryStage.widthProperty());
		footerRectangle.setHeight(20);
		footerRectangle.setFill(Color.web(mainColor));

		Arc footerArc = getStyledArc(primaryStage, 0);
		footerArc.setRadiusY(10);

		footerPane.getChildren().addAll(footerArc, footerRectangle);
*/
		primaryStage.setScene(getHomeScene());
		primaryStage.getIcons().add(new Image("file:res/syncit_icon_Membra_Regular.png"));
		primaryStage.setWidth(600);
		primaryStage.setHeight(800);
		primaryStage.show();
		
		/*
		stageInsideWidthProp.set(primaryStage.getScene().getWidth());
		
		primaryStage.widthProperty().addListener((obs, oldValue, newValue) -> {
			stageInsideWidthProp.set( stageInsideWidthProp.doubleValue() + (newValue.doubleValue() - oldValue.doubleValue()));
		});
		*/
		 
		/*
			
		});
		
		*/
		
//		System.out.println(Screen.getPrimary().getVisualBounds());
//		System.out.println(Screen.getPrimary().getBounds());

	}
}
