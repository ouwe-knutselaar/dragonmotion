package dragonmotion;

import org.apache.log4j.Logger;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.control.CheckBox;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.BorderWidths;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

public class SingleTrack {

	Logger log = Logger.getLogger(SingleTrack.class);


	private int steps;
	private int valueFields[];
	private int looppoint = 0;
	private int barwidth = 10;
	
	private int factor;						// Verhouding tussen waarde op scherm en de waarde op de servo
	private int min;						// Ondergrens
	private int max;						// Bovengrens
	private int restpos;					// Rustpositie
	private int servo = 1;					// Nummber servo
	private boolean recordingmode=false;	// Is it recording or not?
	private int counter = 0;				// Positie cursor tijdens een run

	private double oldx = -1;				// The movement line
	private double oldy = -1;				// old en new position
	private double newx, newy;				//
	
	private VBox rootNode = new VBox();								// De basis box met alle track objecten
	private Canvas canvas;

	private Label namelabel;
	private Label srvlbl=new Label("servo");
	private Label minlbl=new Label("min");
	private Label maxlbl=new Label("max");
	private Label restlbl=new Label("rest");
	private TextField maxField = new TextField();
	private TextField minField = new TextField();
	private TextField restposField = new TextField();
	private TextField servoField = new TextField("" + servo);
	private GraphicsContext gc=null;
	private Slider sl = new Slider();
	private Button smooth = new Button("smooth");
	private Button maximize = new Button("maximize");
	private CheckBox record = new CheckBox("Record");
	private DragonConnect connect = DragonUDP.getService();			// Get the network
	private boolean expand=false;									// Set the canvas on visible
	private FlowPane topPane;
	private final String name;										// Name of the servo
	private float canvasSize;
	
	


	public SingleTrack(final String newname, int servoval, int minimum, int maximum, int newrestpos, int newsteps,int factor)
	{
		
		steps		= newsteps;
		min			= minimum;									// Minimal servo value
		max			= maximum;									// Max servo value
		restpos		= newrestpos;									// restposition servo
		canvasSize	= ((float) (max - min));
		name	 	= newname;
		servo	 	= servoval;
		valueFields	= new int[steps];
		namelabel	= new Label(name);
		this.factor	= factor;
		
		log.info(String.format(" Default value for %s is %d", name, (int) (restpos - minimum)));
		log.info(String.format(" min is %d, max is %d, restpos is %d, factor is %d",min,max,restpos,factor));
		for (int tel = 0; tel < steps; tel++) {
			valueFields[tel] = (int) (restpos - minimum);
		}
		
		canvas	= buildCanvas(steps * barwidth, canvasSize+20); 	// Maak het canvas voor de grafiek
		topPane	= buildTopPane();									// Maak de buttonbar
		
		// Zet alles in elkaar tot 1 net geheel
		BorderStroke borderStroke = new BorderStroke(Color.DARKGRAY, BorderStrokeStyle.SOLID, CornerRadii.EMPTY,BorderWidths.DEFAULT);
		rootNode.setBorder(new Border(borderStroke));	
		rootNode.getChildren().add(topPane);
		if(expand)rootNode.getChildren().add(canvas);

		//
		connect.setServo(servo, restpos);

	}

	
	
	private FlowPane buildTopPane()
	{
		topPane		 = new FlowPane();						// bovenkant van de track, bevat de knoppen
		
		sl.valueProperty().set(restpos - min);
		sl.setPrefWidth(200);
		sl.setMin(min);
		sl.setMax(max);
		sl.valueProperty().addListener(new ChangeListener<Number>() {
			@Override
			public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
				if(recordingmode)
				{
					valueFields[counter]=(int)sl.getValue();
				}
				// realStep=((float)(max-min))/100;
				int newval = (int) newValue.intValue();
				log.debug("Slider changed " + newval + " slider at " + newValue + " canvassize is " + canvasSize+" with factor "+factor);
				connect.setServo(servo, newval*factor);

			}
		});
		

		maxField.prefWidth(100);
		maxField.maxWidth(100);
		maxField.setPrefColumnCount(5);
		maxField.setText("" + max);
		maxField.textProperty().addListener(new ChangeListener<String>() {
			@Override
			public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
				log.info("Text changed to " + newValue);
				max = Integer.parseInt(newValue);
				canvasSize = (float) (max - min);
				sl.valueProperty().set(restpos - min);
				sl.setPrefWidth(200);
				sl.setMin(min);
				sl.setMax(max);
				redraw();
			}
		});

		minField.prefWidth(100);
		minField.maxWidth(100);
		minField.setPrefColumnCount(5);
		minField.setText("" + min);
		minField.textProperty().addListener(new ChangeListener<String>() {
			@Override
			public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
				log.info("Text changed to " + newValue);
				min = Integer.parseInt(newValue);
				canvasSize = (float) (max - min);
				sl.valueProperty().set(restpos - min);
				sl.setPrefWidth(200);
				sl.setMin(min);
				sl.setMax(max);
				redraw();
			}
		});

		restposField.prefWidth(100);
		restposField.maxWidth(100);
		restposField.setPrefColumnCount(5);
		restposField.setText("" + restpos);
		restposField.textProperty().addListener(new ChangeListener<String>() {
			@Override
			public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
				log.info("Text changed to " + newValue);
				restpos = Integer.parseInt(newValue);
				canvasSize = (float) (max - min);
				sl.valueProperty().set(restpos - min);
				sl.setPrefWidth(200);
				sl.setMin(min);
				sl.setMax(max);
				redraw();
			}
		});
		

		servoField.prefWidth(100);
		servoField.maxWidth(100);
		servoField.setPrefColumnCount(3);
		servoField.setText("" + servo);
		servoField.textProperty().addListener(new ChangeListener<String>() {
			@Override
			public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
				log.info("Servo changed to " + newValue);
				try {
					servo = Integer.parseInt(newValue);

				} catch (NumberFormatException e) {

				}

			}
		});
		
		namelabel.setOnMouseClicked(new EventHandler<MouseEvent>(){
			@Override
			public void handle(MouseEvent event) {
				if(expand == true)
				{
					rootNode.getChildren().remove(canvas);
					expand=false;
					return;
				}
				if(expand == false)
				{
					rootNode.getChildren().add(canvas);
					expand=true;
					return;
				}
				
			}});

		smooth.setOnMouseClicked(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				smooth();
			}
		});

		maximize.setOnMouseClicked(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				maximize();
			}
		});
		
		
		record.setSelected(false);
		record.selectedProperty().addListener(new ChangeListener<Boolean>() {

			@Override
			public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
				recordingmode=newValue;
				if(recordingmode)log.info(name+" is set to record");
				else log.info(name+" has recording disabled");
			}
		});

		
		namelabel.setPrefWidth(200);	
		topPane.getChildren().add(namelabel);
		topPane.getChildren().add(srvlbl);
		topPane.getChildren().add(servoField);
		topPane.getChildren().add(minlbl);
		topPane.getChildren().add(minField);
		topPane.getChildren().add(maxlbl);
		topPane.getChildren().add(maxField);
		topPane.getChildren().add(restlbl);
		topPane.getChildren().add(restposField);
		topPane.getChildren().add(sl);
		topPane.getChildren().add(smooth);
		topPane.getChildren().add(maximize);
		topPane.getChildren().add(record);
		
		return topPane;
	}
	
	
	
	
	private Canvas buildCanvas(float length,float heigth)
	{
		log.debug("Make a canvas of "+length+" pixels width and "+heigth+" pixels high");
		
		Canvas canvas=new Canvas(length,heigth);
		
		// A mouse button bowb
		canvas.setOnMousePressed(new EventHandler<MouseEvent>() {			
			@Override
			public void handle(MouseEvent ev) {
				log.debug("Event is " + ev.getEventType());		// Possible start of a drag, store the current mouse position
				oldx = ev.getX();
				oldy = ev.getY();

			}
		});
		
		canvas.setOnMouseDragged(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent ev) {
				// System.out.println("Event is "+ ev.getEventType());
				newx = ev.getX();								// Store the latest mouse position, 
				newy = ev.getY();
				drawMotionLine(gc);								// Draw the line but we are still moving
			}
		});

		canvas.setOnMouseReleased(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent ev) {
				log.info("Event is " + ev.getEventType());
				// gc.strokeLine(oldx, oldy, ev.getX(), ev.getY());
				newx = ev.getX();
				newy = ev.getY();

				if (newx > oldx)
					straiten(oldx, oldy, newx, newy);
				else {
					straiten(newx, newy, oldx, oldy);
				}

				oldx = -1;
				oldy = -1;
				drawMotionLine(gc);
			}
		});
		
		gc	= canvas.getGraphicsContext2D();
		drawMotionLine(gc);
		
		return canvas;
	}
	
	
	
	
	/**
	 * Function that paint the whole canvas
	 * @param gc
	 */
	private void drawMotionLine(GraphicsContext gc) {
		if (gc == null)
			return;
		gc.setLineWidth(2.0);
		gc.clearRect(0, 0, gc.getCanvas().getWidth(), gc.getCanvas().getHeight());
		gc.setStroke(Color.LIGHTBLUE);
		gc.setFont(Font.getDefault());
		gc.strokeLine(0, 0, gc.getCanvas().getWidth(), 0);
		gc.strokeLine(0, canvasSize/2, gc.getCanvas().getWidth(), canvasSize/2);
		gc.strokeLine(0, canvasSize, gc.getCanvas().getWidth(), canvasSize);
		for (int tel = 0; tel < steps; tel = tel + 10) {
			gc.strokeLine(tel * barwidth, 0, tel * barwidth, canvasSize);
			//gc.fillText("" + tel * interval * 1000 + "ms", tel * barwidth, 110);
		}

		gc.setFill(Color.BLACK);
		gc.setStroke(Color.BLACK);
		gc.setLineWidth(1.0);
		for (int tel = 0; tel < steps; tel++) {
			float topx = tel * barwidth;
			float topy = 4.0f;
			float bottomx = 5;
			float bottomy = valueFields[tel];
			// System.out.printf("%d
			// %f,%f,%f,%f\n",tel,topx,topy,bottomx,bottomy);
			gc.strokeRect(topx, topy, bottomx, bottomy);
		}

		if (oldx >= 0) {
			gc.strokeLine(oldx, oldy, newx, newy);
		}

		gc.setFill(Color.RED);
		gc.setStroke(Color.RED);
		gc.strokeLine(looppoint * barwidth, 0, looppoint * barwidth, canvasSize);
		log.debug("Redraw canvas of "+this.name+" with looppoint "+looppoint+" barwidth "+barwidth);

	}
	
	

	public void smooth() {
		int total[] = new int[steps];
		total[0] = valueFields[0];
		total[steps - 1] = valueFields[steps - 1];

		for (int tel = 1; tel < steps - 1; tel++) {
			int sub = (50 - valueFields[tel - 1]) + (50 - valueFields[tel]) + (50 - valueFields[tel + 1]);
			sub = sub / 3;
			total[tel] = 50 - sub;
		}

		for (int tel = 0; tel < steps; tel++) {
			valueFields[tel] = total[tel];
			if (valueFields[tel] > canvasSize)
				valueFields[tel] = (int)canvasSize;
			if (valueFields[tel] < 0)
				valueFields[tel] = 0;
		}
		drawMotionLine(gc);
	}

	
	public void maximize() {
		double factor = 1.1;
		for (int tel = 0; tel < steps; tel++) {
			if (valueFields[tel] < (canvasSize/2))
				valueFields[tel] = (int) (valueFields[tel] / factor);
			if (valueFields[tel] > (canvasSize/2))
				valueFields[tel] = (int) (valueFields[tel] * factor);

			if (valueFields[tel] > canvasSize)
				valueFields[tel] = (int)canvasSize;
			if (valueFields[tel] < 0)
				valueFields[tel] = 0;
		}
		drawMotionLine(gc);
	}

	
	// Here we create the new line after the mouse button is released
	private void straiten(double startx, double starty, double endx, double endy) {

		int intBegin = (int) (startx / 10);										// Determine the start point as bar
		int intEnd = (int) (endx / 10); // bepaal de stappen					// And the end point	
		int size = intEnd - intBegin;											// The number of steps or size of the new part

		log.debug("Begin " + intBegin + "  end " + intEnd + " size " + size);	// A little logging
		log.debug("BeginY " + starty + "  endY " + endy);

		double arc = (endy - starty) / size; 									// Determine the arc of the line 		
		log.debug("Arc is " + arc);

		for (int tel = 0; tel < size; tel++) {									// Fill the fields that are selected
			if (tel + intBegin < steps & tel + intBegin >= 0)
				{
					int newvalue=(int) (starty + (int) (tel * arc));
					if(newvalue<0)newvalue=0;
					if(newvalue>canvasSize)newvalue=(int)canvasSize;
					valueFields[tel + intBegin] = newvalue;
				}
			// System.out.print("newval "+valueFields[tel+intBegin]+"\n");
		}

	}

	
	/**
	 * Redraw this graphic node
	 */
	public void redraw() {
		gc.clearRect(0, 0, gc.getCanvas().getWidth(), gc.getCanvas().getHeight());
		drawMotionLine(gc);

	}

	
	/**
	 * Return the nod to add it to the main screen
	 * @return
	 */
	public Node getNode() {
		return rootNode;
	}

	
	public int getNext() {
		if (counter < steps)
			counter++;
		return valueFields[counter - 1];
	}

	
	public int getNextReal() {
		
		if (counter < steps)
			counter++;
		return (int) ((valueFields[counter - 1]) + min);
	}

	
	public void toRest() {
		connect.setServo(servo, restpos);
	}

	
	public int getServo() {
		return servo;
	}

	
	public void setServo(int servo) {
		this.servo = servo;
	}

	
	public void reset() {
		counter = 0;
		looppoint = 0;
		drawMotionLine(gc);
	}

	
	public void setLooppoint(int point) {
		this.looppoint = point;
		drawMotionLine(gc);
	}

	
	public void toNull() {
		connect.setServo(servo, 0);
	}

	
	public int[] getValueFields() {
		return valueFields;
	}

	public int getMin() {
		return min;
	}

	public int getMax() {
		return max;
	}

	public int getRestpos() {
		return restpos;
	}

	public String getName() {
		return name;
	}

	
	public int getFactor()
	{
		return factor;
	}
	
	public void fillTrack(String string) {

		String valueStringList[] = string.split(" ");
		int size = valueStringList.length;

		if (size != steps) {
			log.info("Step size problem");
			return;
		}

		valueFields = new int[size];
		for (int tel = 0; tel < size; tel++) {
			valueFields[tel] = Integer.parseInt(valueStringList[tel]);
		}
		redraw();
	}

}
