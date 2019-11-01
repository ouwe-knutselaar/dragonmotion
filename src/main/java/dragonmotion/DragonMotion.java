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

import dragonmotion.services.RunMotion;
import dragonmotion.services.WaveService;
import javafx.application.Application;
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
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;

 

@SuppressWarnings("restriction")
public class DragonMotion extends Application{

	
		// Defaults
	static String ipadres="192.168.178.29";
	static int port=80;
	public static float interval=0.050f;						// Step interval 50 ms
	public static int steps=200;
	static int timerInterval=200;				// disble timer interval to prevent servo jitter
	
	   RunMotion runMotion;
	   List<SingleTrack> TrackList=new ArrayList<>(); 
	
	   Label msgst=new Label("Run sequence");
       Button start=new Button("Start");
       
       Label msgsv=new Label("Save sequence");
       Button save=new Button("Save");
       
       Label msgrest=new Label("Drago to Rest pos");
       Button torest=new Button("To Rest");
       
       Label msgtonull=new Label("All servos to null");
       Button tonull=new Button("To Null");
    		   
       
       Label msgseqname=new Label("Name sequence");
       TextField trackName=new TextField("noname");
      
       Label msgsip=new Label("IP Address sequence");
       TextField ipaddress=new TextField(ipadres);
       
       Label messages=new Label("messages");

       WaveService waveService=WaveService.getInstance();
       
       private boolean threadFlag=false;
       private Thread thread;
       
       public static JitterTimer jitterTimer;
       
       private Thread jitterThread;
       
       BorderPane  root;
       
       Stage primaryStage;
       
       WaveTrack wv1;
       
       public static void main(String[] args) {
             launch();
       }


      
       
       @Override
       public void start(Stage primaryStage) throws Exception {
            
    	    this.primaryStage=primaryStage;
             primaryStage.setTitle("Dragon Controller");
             
             // Zoek de draak
             ScanForDragon();
             
             
             
             root = new BorderPane();
            
             GridPane buttonPane=new GridPane();
             FlowPane msgPane=new FlowPane();
             msgPane.getChildren().add(messages);

             buttonPane.setPrefWidth(300);
             buttonPane.setHgap(10);
             buttonPane.setVgap(10);
             
             buttonPane.add(msgst, 0, 0);
             buttonPane.add(start,1,0); 
             buttonPane.add(msgrest,0,1);
             buttonPane.add(torest,1,1);             
             buttonPane.add(msgtonull,0,2);
             buttonPane.add(tonull,1,2);
 
             runMotion=new RunMotion(steps);
             jitterTimer=new JitterTimer();

             start.setOnMouseClicked(new EventHandler<MouseEvent>(){
				public void handle(MouseEvent event) {
					Thread thread=new Thread(runMotion);
					if(threadFlag==false)
						{
						 thread.start();
						 threadFlag=true;
						 start.setText("Stop");
						}
					else
					{
						runMotion.stopThread();
						threadFlag=false;
						start.setText("Start");
					}
					
				}});
             
             
             
             torest.setOnMouseClicked(new EventHandler<MouseEvent>(){
 				public void handle(MouseEvent event) {
 					TrackList.forEach(track -> track.toRest());
 				}
             });
             
             
             tonull.setOnMouseClicked(new EventHandler<MouseEvent>() {
				@Override
				public void handle(MouseEvent arg0) {
					TrackList.forEach(track -> track.toNull());
				}});
             
             primaryStage.setOnCloseRequest(e -> HandleExit());
             
             
             int tel=0;
             for(SingleTrack track: TrackList)
             {
            	 jitterTimer.setTimerFlag(tel,track.jitterFlag());
            	 tel++;
             }

             
             jitterThread=new Thread(jitterTimer);
             jitterThread.start();
             
             
             resetTracklist();						// Maak de tracks om ze straks te laten zien
             root.setLeft(buttonPane);
             root.setCenter(buildCenter(steps));
             root.setBottom(msgPane);
             root.setTop(buildMenu());
             
             primaryStage.setScene(new Scene(root, 1200, 800));
             primaryStage.show();
            
       }

       
      
       private Object HandleExit() {
    	   jitterThread.stop();
    	   runMotion.stopThread();
    	   DragonConnect tempConnect=DragonUDP.getService();
    	   tempConnect.close();
    	   System.out.println("Everthing is closed now");
		return null;
	}


    // Build the graphical shell
	public Node buildCenter(int steps)
       {
    	   
    	   System.out.println("Make a sequence of "+steps+" steps");
    	   VBox rightPane=new VBox();
           
    	   // Add the track pointers to the runMotion
    	   runMotion.clearTrack();
    	   TrackList.forEach(track -> runMotion.addTrack(track));
       
    	   // Add the tracks to the right pane
    	   rightPane.setPadding(new Insets(5));
           TrackList.forEach(track -> rightPane.getChildren().add(track.getNode()));
           
           // Add the wave track
           wv1=new WaveTrack(primaryStage);
           rightPane.getChildren().add(wv1.getNode());
    	   
    	   ScrollPane rightScrollPane=new ScrollPane();
           rightScrollPane.setContent(rightPane);
           
           return rightScrollPane;
       }
       
       
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
		edit.getItems().addAll(settings, loadSample,scanForDragon);

		settings.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				System.out.println("Show dialog");
				Dialog<DialogValues> dialog = buildDialog();
				Optional<DialogValues> result = dialog.showAndWait();

				System.out.println("-> " + result.get().getSteps());
				int newSteps=result.get().getSteps();
				if(steps!=newSteps)
				{
					steps=newSteps;
					RebuildCenter();
				}
			}
		});
		
		
		save.setOnAction(new EventHandler<ActionEvent>(){
			@Override
			public void handle(ActionEvent event) {
				System.out.println("Load sequence");
				FileChooser fileChooser = new FileChooser();
				fileChooser.setTitle("Open Sequence File");
				fileChooser.getExtensionFilters().addAll(new ExtensionFilter("Sequencer Files", "*.seq"),
						new ExtensionFilter("All Files", "*.*"));
				File selectedFile = fileChooser.showSaveDialog(primaryStage);
				if (selectedFile != null) {
					saveSequencerFile(selectedFile);
					}
				}
				
			});
		
		
		load.setOnAction(new EventHandler<ActionEvent>(){
			@Override
			public void handle(ActionEvent event) {
				System.out.println("Load sequence");
				FileChooser fileChooser = new FileChooser();
				fileChooser.setTitle("Open Sequence File");
				fileChooser.getExtensionFilters().addAll(new ExtensionFilter("Sequencer Files", "*.seq"),
						new ExtensionFilter("All Files", "*.*"));
				File selectedFile = fileChooser.showOpenDialog(primaryStage);
				if (selectedFile != null) {
					loadSequencerFile(selectedFile);
					}
				}
				
			});

		scanForDragon.setOnAction(new EventHandler<ActionEvent>(){			
			@Override
			public void handle(ActionEvent arg0) {
				ScanForDragon();
			}});
		
		loadSample.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				System.out.println("Load sample");
				FileChooser fileChooser = new FileChooser();
				fileChooser.setTitle("Open Wave File");
				fileChooser.getExtensionFilters().addAll(new ExtensionFilter("Wave Files", "*.wav"),
						new ExtensionFilter("All Files", "*.*"));
				File selectedFile = fileChooser.showOpenDialog(primaryStage);
				if (selectedFile != null) {
					if (waveService.loadFile(selectedFile)) {
						steps=waveService.getSteps();
						RebuildCenter();
					}
				}
			}
		});

		menuBar.getMenus().addAll(file, edit);
		return menuBar;
	}
       
	public void resetTracklist() {
		TrackList.clear();

		// Add all tracks
		// TrackList.add(new SingleTrack("none",3,0,4096,0,false,steps));
		// TrackList.add(new SingleTrack("none",4,0,4096,0,false,steps));
		// TrackList.add(new SingleTrack("none",5,0,4096,0,false,steps));
		// TrackList.add(new SingleTrack("none",6,0,4096,0,false,steps));
		TrackList.add(new SingleTrack("Head turn", 8, 200, 500, 350, true, steps));
		TrackList.add(new SingleTrack("Tail", 9, 50, 400, 225, true, steps));
		TrackList.add(new SingleTrack("Neck muscle right", 10, 50, 350, 225, true, steps));
		TrackList.add(new SingleTrack("Neck muscle left", 11, 50, 400, 225, true, steps));
		TrackList.add(new SingleTrack("Hips", 12, 300, 410, 370, false, steps));
		TrackList.add(new SingleTrack("wing right", 13, 310, 500, 490, false, steps));
		TrackList.add(new SingleTrack("Wing left", 14, 110, 300, 120, false, steps));
		TrackList.add(new SingleTrack("Eye green", 0, 0, 4080, 0, false, steps));
		TrackList.add(new SingleTrack("Eye red", 1, 0, 4080, 0, false, steps));
		TrackList.add(new SingleTrack("Eye blue", 2, 0, 4096, 0, false, steps));
		TrackList.add(new SingleTrack("Jaw", 7, 230, 320, 310, true, steps));
		// TrackList.add(new SingleTrack("none",15,150,450,200,false,steps));
	}
	
	private void RebuildCenter() {
		resetTracklist();

		root.setCenter(null);
		root.setCenter(buildCenter(steps));

	};
       
		public void loadSequencerFile(File seqFile)
		{
			FileReader inFile=null;
			
			try {
				inFile=new FileReader(seqFile);
			
				BufferedReader bufReader=new BufferedReader(inFile);
				String line=bufReader.readLine();
				while(line!=null)
				{
				if(line.equals("<BEGINOFFILE>"))
				{
					steps=Integer.parseInt(bufReader.readLine());
					System.out.println("new numbers of steps is "+steps);
					resetTracklist();
				}
				if(line.equals("<BEGINOFTRACK>"))
				  {
					String name=bufReader.readLine();
					int min=Integer.parseInt(bufReader.readLine());
					int max=Integer.parseInt(bufReader.readLine());
					int rest=Integer.parseInt(bufReader.readLine());
					int servo=Integer.parseInt(bufReader.readLine());
					String valueLine=bufReader.readLine();
					
					String endTrack=bufReader.readLine();;
					if(!endTrack.equals("<ENDOFTRACK>"))
						{
						 System.out.println("Error in the file");
						 return;
						}
					
					
					for(SingleTrack track:TrackList)
					{
						if(track.getName().equals(name))
						{
							track.fillTrack(valueLine);
						}
					}
					
				  }
				line=bufReader.readLine();
				}
				
			} catch (IOException e) {
				
				e.printStackTrace();
				return;
			}
			root.setCenter(null);
			root.setCenter(buildCenter(steps));
			
		}
		



		public void saveSequencerFile(File seqFile)
		{
			FileWriter outFile=null;
			try {
				outFile=new FileWriter(seqFile);
			} catch (IOException e) {
				
				e.printStackTrace();
				return;
			}
			
			StringBuilder outline=new StringBuilder();
			outline.append("<BEGINOFFILE>\n");
			outline.append(steps).append("\n");
			for(SingleTrack track: TrackList)
			{
				outline.append("<BEGINOFTRACK>\n");
				outline.append(track.getName()).append("\n");		// name
				outline.append(track.getMin()).append("\n");			// min
				outline.append(track.getMax()).append("\n");			// max
				outline.append(track.getRestpos()).append("\n");	// rest
				outline.append(track.getServo()).append("\n");		// servo
				
				int[] values=track.getValueFields();
				
				
				for(int tel=0;tel<values.length;tel++)
				{
					outline.append(values[tel]).append(" ");
					//if((tel+1) % 50 == 0)outline.append("\n");
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
			System.out.println("File saved");
			messages.setText("File saved");
		}

		
       public Dialog<DialogValues> buildDialog()
       {
    	   Label ipadresLabel=new Label("IP Address");
    	   TextField ipadresTf=new TextField(ipadres);
    	   Label portLabel=new Label("IP port");
    	   TextField portTf=new TextField(""+port);
    	   Label stepsLabel=new Label("Steps");
    	   TextField stepsTf=new TextField(""+steps);
    	   
    	   Dialog<DialogValues> dialog=new Dialog<>();
    	   
    	   GridPane grid = new GridPane();
    	   grid.add(ipadresLabel, 0,0);
    	   grid.add(ipadresTf, 1, 0);
    	   grid.add(portLabel, 0, 1);
    	   grid.add(portTf,1,1);
    	   grid.add(stepsLabel, 0, 2);
    	   grid.add(stepsTf,1,2);
    	   
    	   ButtonType buttonTypeOk = new ButtonType("Ok", ButtonData.OK_DONE);
    	   dialog.getDialogPane().getButtonTypes().add(buttonTypeOk);
    	   
    	   dialog.getDialogPane().setContent(grid);
    	   
    	   dialog.setResultConverter(dialogButton -> 	{if(dialogButton == buttonTypeOk)
    	   													{
    		   												 return new DialogValues(ipadresTf.getText(),Integer.parseInt(portTf.getText()),Integer.parseInt(stepsTf.getText()));
    	   													}
    	   												 else
    	   													 return null;
    	   												});
    	   
    	   return dialog;
    	   
       }
       
       
       public void ScanForDragon()
       {
    	// Zoek de drgaonIP adres
           DragonConnect tempConnect=DragonUDP.getService();
           String localAddress="192.168.1.2";
		try {
			localAddress = InetAddress.getLocalHost().getHostAddress();
		} catch (UnknownHostException e) {
			
			e.printStackTrace();
		}
           String netnums[]=localAddress.split(Pattern.quote("."));
           localAddress=netnums[0]+"."+netnums[1]+"."+netnums[2];
           ipadres=tempConnect.scanForDragon(80, localAddress);
           if(ipadres!=null)
           {
          	 System.out.println("Dragon is at "+ipadres);
          	 messages.setText("Dragon is at "+ipadres);
          	 tempConnect.setIpadress(ipadres);
           }
           else
           {
          	 System.out.println("Cannot find dragon, set on own IP address");
          	messages.setText("Cannot find dragon, set on own IP address");
          	 try {
				ipadres=InetAddress.getLocalHost().getHostAddress();
			} catch (UnknownHostException e) {
				
				e.printStackTrace();
			}
           }
       }
       
}

 