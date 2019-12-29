package dragonmotion;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import dragonmotion.services.DragonEvent;
import dragonmotion.services.RunMotion;
import dragonmotion.services.WaveService;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Scene;

import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;

@SuppressWarnings("restriction")
public class DragonMotion extends Application {

	Logger log = Logger.getLogger(this.getClass().getCanonicalName());

	// Defaults
	static String ipadres = "192.168.178.29";
	static int port = 1080;
	public static float interval = 0.050f; // Step interval 50 ms
	public int steps = 200;
	static int timerInterval = 200; // disble timer interval to prevent servo
									// jitter

	RunMotion runMotion;
	List<SingleTrack> TrackList = new ArrayList<>();

	Label msgst = new Label("Run sequence <S>");
	Button start = new Button("Start");

	Label msgwave = new Label("Play wave");
	Button wave = new Button("Wave");

	Label msgsv = new Label("Save sequence");
	Button save = new Button("Save");

	Label msgrest = new Label("Drago to Rest pos");
	Button torest = new Button("To Rest");

	Label msgtonull = new Label("All servos to null");
	Button tonull = new Button("To Null");

	private Label msgseqname = new Label("Name sequence");
	private TextField trackName = new TextField("noname");

	private Label msgsip = new Label("IP Address sequence");
	private TextField ipaddress = new TextField(ipadres);

	private Label messages = new Label("messages");
	
	private DragonCalibrate calibrate=new DragonCalibrate();

	private WaveService waveService = WaveService.getInstance();

	private boolean threadFlag = false;

	private ScrollPane rightScrollPane;
	private GridPane buttonPane=new GridPane();;
	private FlowPane msgPane=new FlowPane();
	private Scene mainScene;

	private BorderPane root;
	private Stage primaryStage;
	private WaveTrack waveTrack;

	public static void main(String[] args) {
		launch();
	}

	@Override
	public void start(Stage primaryStage) throws Exception {

		this.primaryStage = primaryStage;
		
		primaryStage.setTitle("Dragon Controller");
		primaryStage.setOnCloseRequest(e -> HandleExit());

		// ***********************  Zet de services en variabelen
		waveService.loadFile(new File("d:\\erwin\\alive_16.wav"));	// Laadt een standaar file
		steps=waveService.getSteps();								// Bepaald de steps
		log.info("Number of steps is "+steps);
		this.resetTracklist();										// Bouw de nieuwe tracklist 
		runMotion = new RunMotion(steps);							// Maak het runmption object;
		

	
		//************************* maak de grafische shell ***********************
		root = new BorderPane();				// Maak de basis GUI
		mainScene=new Scene(root, 1200, 800);
		msgPane.getChildren().add(messages);

		buttonPane.setPrefWidth(300);
		buttonPane.setHgap(10);
		buttonPane.setVgap(10);

		buttonPane.add(msgst, 0, 0);
		buttonPane.add(start, 1, 0);
		buttonPane.add(msgwave, 0, 1);
		buttonPane.add(wave, 1, 1);
		buttonPane.add(msgrest, 0, 2);
		buttonPane.add(torest, 1, 2);
		buttonPane.add(msgtonull, 0, 3);
		buttonPane.add(tonull, 1, 3);
		buttonPane.add(calibrate.getFragement(), 0, 4,2,1);
		
		root.setLeft(buttonPane);
		root.setCenter(buildCenter(steps));			
		root.setBottom(msgPane);
		root.setTop(buildMenu());
		

		// ****************************** Zet alle events ************************
		start.setOnMouseClicked(new EventHandler<MouseEvent>() {	// Start the current motion thread
			public void handle(MouseEvent event) {
				Thread thread = new Thread(runMotion);
				if (threadFlag == false) {
					thread.start();
					threadFlag = true;
					start.setText("Stop");							// Switch the button text
				} else {
					runMotion.stopThread();
					threadFlag = false;
					start.setText("Start");
				}

			}
		});

		torest.setOnMouseClicked(new EventHandler<MouseEvent>() {	// Reset dragon to rest position
			public void handle(MouseEvent event) {
				TrackList.forEach(track -> track.toRest());
			}
		});

		tonull.setOnMouseClicked(new EventHandler<MouseEvent>() {	// Reset dragon to zero
			@Override
			public void handle(MouseEvent arg0) {
				TrackList.forEach(track -> track.toNull());
			}
		});

		wave.setOnMouseClicked(new EventHandler<MouseEvent>() {		// Play the wavefile when clicked
			@Override
			public void handle(MouseEvent event) {
				waveService.playWave();
			}
		});

		
		/**
		 * Callback van de runMortion Track
		 */
		runMotion.setOnActionEvent(new DragonEvent(){
			@Override
			public void handle(String msg,int val1,int val2) {
				log.debug("Event is "+msg);
				if(val1==0)												// Het eidne van een loop door de tracks is gedaan
					{
					  Platform.runLater(() -> start.setText("Start"));	// Update the start butten
					  threadFlag = false;								// Thread is finish, also for the GUI
					}
				if(val1==2)												// Handle for updating the GUI of the tracks
				{
					Platform.runLater(()->
						{
							waveTrack.setLooppoint(val2);
							TrackList.forEach(track -> track.setLooppoint(val2));
						});
				}
				
			}});
		
		mainScene.addEventHandler(KeyEvent.KEY_PRESSED, (key) -> {
			  log.info("keypressed "+key.getText());
		      if(key.getText().equals("s")) {
		          log.info("Start the runmotion");
		          Thread thread = new Thread(runMotion);
					if (threadFlag == false) {
						thread.start();
						threadFlag = true;
						start.setText("Stop");							// Switch the button text
					} else {
						runMotion.stopThread();
						threadFlag = false;
						start.setText("Start");
					}
		      }
		});
		
		
		// **************************** laad de 
		ScanForDragon();	// Zoek de draak
		

		primaryStage.setScene(mainScene);
		primaryStage.show();
	}

	/**
	 * Exit function
	 * @return
	 */
	private Object HandleExit() {
		
		runMotion.stopThread();
		DragonConnect tempConnect = DragonUDP.getService();
		tempConnect.close();
		log.info("Everthing is closed now");
		return null;
	}

	
	// Build the graphical shell
	public Node buildCenter(int steps) {

		log.info("Make a sequence of " + steps + " steps");
		VBox rightPane = new VBox();

		// Add the track pointers to the runMotion
		runMotion.clearTrack();
		TrackList.forEach(track -> runMotion.addTrack(track));

		// Add the tracks to the right pane
		rightPane.setPadding(new Insets(5));
		TrackList.forEach(track -> rightPane.getChildren().add(track.getNode()));

		// Add the wave track
		
		rightPane.getChildren().add(waveTrack.getNode());
		runMotion.setWaveTrack(waveTrack);

		rightScrollPane = new ScrollPane();		// Set the whole rightpane in a scrollpane
		rightScrollPane.setContent(rightPane);
		

		return rightScrollPane;
	}

	// Build the menu bar
	public MenuBar buildMenu() {
		MenuBar menuBar = new MenuBar();

		MenuItem save = new MenuItem("Save");
		MenuItem load = new MenuItem("Load");
		MenuItem exit = new MenuItem("Exit");
		Menu file = new Menu("File");
		file.getItems().addAll(save, load, exit);

		MenuItem settings = new MenuItem("Settings");
		MenuItem loadSample = new MenuItem("Load sample");
		MenuItem scanForDragon = new MenuItem("Scan for dragon");
		Menu edit = new Menu("edit");
		edit.getItems().addAll(settings, loadSample, scanForDragon);

		settings.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				log.info("Show dialog");
				Dialog<DialogValues> dialog = buildDialog();
				Optional<DialogValues> result = dialog.showAndWait();

				log.info("-> " + result.get().getSteps());
				int newSteps = result.get().getSteps();
				if (steps != newSteps) {
					steps = newSteps;
					RebuildCenter();
				}
			}
		});

		save.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				log.info("Save sequence");
				FileChooser fileChooser = new FileChooser();
				fileChooser.setTitle("Open Sequence File");
				fileChooser.getExtensionFilters().addAll(new ExtensionFilter("Sequencer Files", "*.seq"),
						new ExtensionFilter("All Files", "*.*"));
				File selectedFile = fileChooser.showSaveDialog(primaryStage);
				if (selectedFile != null) {
					saveSequencerFile(selectedFile);
					String base=selectedFile.getAbsolutePath();
					base=base.substring(0,base.length()-4)+".wav";
					File audioFile=new File(base);
					waveService.saveFile(audioFile);
				}
			}

		});

		load.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				log.info("Load sequence");
				FileChooser fileChooser = new FileChooser();
				fileChooser.setTitle("Open Sequence File");
				fileChooser.getExtensionFilters().addAll(new ExtensionFilter("Sequencer Files", "*.seq"),
						new ExtensionFilter("All Files", "*.*"));
				File selectedFile = fileChooser.showOpenDialog(primaryStage);
				if (selectedFile != null) {
					loadSequencerFile(selectedFile);
					String base=selectedFile.getAbsolutePath();
					base=base.substring(0,base.length()-4)+".wav";
					waveService.loadFile(new File(base));
					primaryStage.setTitle("Dragon Controller "+selectedFile.getAbsolutePath());
				}
			}

		});

		scanForDragon.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent arg0) {
				ScanForDragon();
			}
		});

		loadSample.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				log.info("Load sample");
				FileChooser fileChooser = new FileChooser();
				fileChooser.setTitle("Open Wave File");
				fileChooser.getExtensionFilters().addAll(new ExtensionFilter("Wave Files", "*.wav"),
						new ExtensionFilter("All Files", "*.*"));
				File selectedFile = fileChooser.showOpenDialog(primaryStage);
				if (selectedFile != null) {
					if (waveService.loadFile(selectedFile)) {
						steps = waveService.getSteps();
						runMotion=new RunMotion(steps);
						RebuildCenter();
					}
				}
			}
		});
		
		
		menuBar.getMenus().addAll(file, edit);
		return menuBar;
	}

	
	/**
	 * Clear the TrackList and build a new initial one
	 */
	public void resetTracklist() {
		TrackList.clear();
		// name servonumber min max restpos steps
		TrackList.add(new SingleTrack("Head turn", 8, 200, 500, 350,  steps,1));
		TrackList.add(new SingleTrack("Tail", 9, 50, 400, 225,  steps,1));
		TrackList.add(new SingleTrack("Neck muscle right", 10, 50, 350, 225,  steps,1));
		TrackList.add(new SingleTrack("Neck muscle left", 11, 50, 400, 225,  steps,1));
		TrackList.add(new SingleTrack("Hips", 12, 300, 410, 370,  steps,1));
		TrackList.add(new SingleTrack("wing right", 13, 310, 500, 490,  steps,1));
		TrackList.add(new SingleTrack("Wing left", 14, 110, 300, 120,  steps,1));
		TrackList.add(new SingleTrack("Eye green", 0, 0, 100, 0,  steps,40));
		TrackList.add(new SingleTrack("Eye red", 1, 0, 100, 0,  steps,40));
		TrackList.add(new SingleTrack("Eye blue", 2, 0, 100, 0,  steps,40));
		TrackList.add(new SingleTrack("Jaw", 7, 270, 330, 330,  steps,1));
		
		waveTrack = new WaveTrack(primaryStage,steps);
	}

	
	private void RebuildCenter() {
		resetTracklist();
		root.setCenter(null);
		root.setCenter(buildCenter(steps));

	};

	
	public void loadSequencerFile(File seqFile) {
		FileReader inFile = null;
		TrackList.clear();
		try {
			inFile = new FileReader(seqFile);

			BufferedReader bufReader = new BufferedReader(inFile);
			String line = bufReader.readLine();
			while (line != null) {
				if (line.equals("<BEGINOFFILE>")) {
					steps = Integer.parseInt(bufReader.readLine());
					log.info("new numbers of steps is " + steps);
					resetTracklist();
				}
				if (line.equals("<BEGINOFTRACK>")) {
					String name 	= bufReader.readLine();
					int min 		= Integer.parseInt(bufReader.readLine());
					int max 		= Integer.parseInt(bufReader.readLine());
					int rest 		= Integer.parseInt(bufReader.readLine());
					int servo 		= Integer.parseInt(bufReader.readLine());
					int factor		= Integer.parseInt(bufReader.readLine());
					
					String valueLine = bufReader.readLine();

					String endTrack = bufReader.readLine();
					;
					if (!endTrack.equals("<ENDOFTRACK>")) {
						log.info("Error in the file");
						bufReader.close();
						return;
					}

					SingleTrack newTrack=new SingleTrack(name,servo,min,max,rest,steps,factor);
					newTrack.fillTrack(valueLine);
					TrackList.add(newTrack);

				}
				line = bufReader.readLine();
			}
			bufReader.close();

		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
		root.setCenter(null);
		root.setCenter(buildCenter(steps));

	}

	
	
	public void saveSequencerFile(File seqFile) {
		FileWriter outFile = null;
		try {
			outFile = new FileWriter(seqFile);
		} catch (IOException e) {

			e.printStackTrace();
			return;
		}

		StringBuilder outline = new StringBuilder();
		outline.append("<BEGINOFFILE>\n");
		outline.append(steps).append("\n");
		for (SingleTrack track : TrackList) {
			outline.append("<BEGINOFTRACK>\n");
			outline.append(track.getName()).append("\n"); 		// name
			outline.append(track.getMin()).append("\n"); 		// min
			outline.append(track.getMax()).append("\n"); 		// max
			outline.append(track.getRestpos()).append("\n"); 	// rest
			outline.append(track.getServo()).append("\n"); 		// servo
			outline.append(track.getFactor()).append("\n"); 	// factor

			int[] values = track.getValueFields();

			for (int tel = 0; tel < values.length; tel++) {
				outline.append(values[tel]).append(" ");
				// if((tel+1) % 50 == 0)outline.append("\n");
			}
			outline.append("\n<ENDOFTRACK>\n");
		}
		outline.append("<ENDOFFILE>\n");
		try {
			outFile.write(outline.toString());
			outFile.flush();
			outFile.close();
		} catch (IOException e) {

			e.printStackTrace();
			return;
		}
		log.info("File saved");
		messages.setText("File saved");
	}

	
	
	public Dialog<DialogValues> buildDialog() {
		Label ipadresLabel = new Label("IP Address");
		TextField ipadresTf = new TextField(ipadres);
		Label portLabel = new Label("IP port");
		TextField portTf = new TextField("" + port);
		Label stepsLabel = new Label("Steps");
		TextField stepsTf = new TextField("" + steps);

		Dialog<DialogValues> dialog = new Dialog<>();

		GridPane grid = new GridPane();
		grid.add(ipadresLabel, 0, 0);
		grid.add(ipadresTf, 1, 0);
		grid.add(portLabel, 0, 1);
		grid.add(portTf, 1, 1);
		grid.add(stepsLabel, 0, 2);
		grid.add(stepsTf, 1, 2);

		ButtonType buttonTypeOk = new ButtonType("Ok", ButtonData.OK_DONE);
		dialog.getDialogPane().getButtonTypes().add(buttonTypeOk);

		dialog.getDialogPane().setContent(grid);

		dialog.setResultConverter(dialogButton -> {
			if (dialogButton == buttonTypeOk) {
				return new DialogValues(ipadresTf.getText(), Integer.parseInt(portTf.getText()),
						Integer.parseInt(stepsTf.getText()));
			} else
				return null;
		});

		return dialog;

	}

	
	
	public void ScanForDragon() {
		// Zoek de drgaonIP adres
		DragonConnect tempConnect = DragonUDP.getService();
		String localAddress = "192.168.1.2";
		try {
			localAddress = InetAddress.getLocalHost().getHostAddress();
		} catch (UnknownHostException e) {

			e.printStackTrace();
		}
		String netnums[] = localAddress.split(Pattern.quote("."));
		localAddress = netnums[0] + "." + netnums[1] + "." + netnums[2];
		ipadres = tempConnect.scanForDragon(port, localAddress);
		if (ipadres != null) {
			log.info("Dragon is at " + ipadres);
			messages.setText("Dragon is at " + ipadres);
			tempConnect.setIpadress(ipadres);
		} else {
			log.info("Cannot find dragon, set on own IP address");
			messages.setText("Cannot find dragon, set on own IP address");
			try {
				ipadres = InetAddress.getLocalHost().getHostAddress();
			} catch (UnknownHostException e) {

				e.printStackTrace();
			}
		}
	}

}
