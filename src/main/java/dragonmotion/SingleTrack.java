package dragonmotion;

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

@SuppressWarnings("restriction")
public class SingleTrack {

	private float stepwidth = 3;
	private float interval = DragonMotion.interval; // 100ms interval
	private int steps ;
	private int valueFields[];
	private int looppoint = 0;
	private int barwidth = 10;

	//int canvaswidth = steps * barwidth;

	private int min;
	private int max;
	private int restpos;
	private int servo = 1;

	private int counter = 0;

	private double oldx = -1;
	private double oldy = -1;
	private double newx, newy;

	private VBox rootNode = new VBox();
	private Canvas canvas;
	
	private Label namelabel;
	private TextField maxField = new TextField();
	private TextField minField = new TextField();
	private TextField restposField = new TextField();
	private TextField servoField = new TextField("" + servo);
	final GraphicsContext gc;
	private Slider sl = new Slider();
	private Button smooth = new Button("smooth");
	private Button maximize = new Button("maximize");
	private CheckBox record=new CheckBox("Record");
	private DragonConnect connect = DragonUDP.getService();

	private final String name;

	float realStep;
	private boolean jitterFlag = false; // if true jitter control is enabled

	public SingleTrack(final String name, int servoval, int minimum, int maximum, int restpos, boolean jitterflag, int steps) {
		this.steps=steps;
		this.min = minimum;
		this.max = maximum;
		this.restpos = restpos;
		realStep = ((float) (max - min)) / 100;
		this.name = name;
		// this.servo=servo;
		servoField.setText("" + servoval);
		this.servo = Integer.parseInt(servoField.getText());
		valueFields = new int[steps];

		this.jitterFlag = jitterflag;

		//rootNode.setVgap(10);
		//rootNode.setHgap(10);
		BorderStroke borderStroke=new BorderStroke(Color.DARKGRAY, BorderStrokeStyle.SOLID, CornerRadii.EMPTY,BorderWidths.DEFAULT);
		rootNode.setBorder(new Border(borderStroke));

		System.out.printf(" Default value for %s is %d\n", name, (int) ((restpos - minimum) / realStep));
		for (int tel = 0; tel < steps; tel++) {
			valueFields[tel] = (int) ((restpos - minimum) / realStep);

		}

		canvas = new Canvas(steps * barwidth, 120); // 100 voor het canvas en
														// 20 voor de tekst er
														// onder
		gc = canvas.getGraphicsContext2D();

		drawMotionLine(gc);
		/*
		 * canvas.setOnMouseClicked(new EventHandler<MouseEvent>() { public void
		 * handle(MouseEvent ev) {
		 * System.out.printf("mouse %f %f\n",ev.getX(),ev.getY()); int subx =
		 * (int) (ev.getX() / barwidth); int suby = (int) (ev.getY());
		 * if(suby>100)suby=100; // Correctie omdat het veld groter van 100 is
		 * 
		 * int oldvaldiff=suby-valueFields[subx]; valueFields[subx] = suby;
		 * for(int tel=1;tel<5;tel++) { oldvaldiff=oldvaldiff/2; if(subx-tel>=0)
		 * {valueFields[subx-tel]=valueFields[subx-tel]+oldvaldiff;
		 * if(valueFields[subx-tel]>100)valueFields[subx-tel]=100; }
		 * if(subx+tel<steps)
		 * {valueFields[subx+tel]=valueFields[subx+tel]+oldvaldiff;
		 * if(valueFields[subx-tel]>100)valueFields[subx-tel]=100; } }
		 * 
		 * drawMotionLine(gc); } });
		 */

		namelabel = new Label(name);

		canvas.setOnMousePressed(new EventHandler<MouseEvent>() {

			@Override
			public void handle(MouseEvent ev) {
				System.out.println("Event is " + ev.getEventType());
				oldx = ev.getX();
				oldy = ev.getY();

			}
		});

		canvas.setOnMouseDragged(new EventHandler<MouseEvent>() {

			@Override
			public void handle(MouseEvent ev) {
				// System.out.println("Event is "+ ev.getEventType());
				newx = ev.getX();
				newy = ev.getY();
				drawMotionLine(gc);
			}
		});

		canvas.setOnMouseReleased(new EventHandler<MouseEvent>() {

			@Override
			public void handle(MouseEvent ev) {
				System.out.println("Event is " + ev.getEventType());
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

		sl.valueProperty().set((restpos - minimum) / realStep);
		sl.valueProperty().addListener(new ChangeListener<Number>() {

			@Override
			public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {

				// realStep=((float)(max-min))/100;
				int newval = min + (int) (realStep * newValue.floatValue());
				System.out.println("Slider changed " + newval + " slider at " + newValue + " steep is " + realStep);
				connect.setServo(servo, newval);
				if (jitterFlag)
					DragonMotion.jitterTimer.ResetTimer(servo);

			}
		});

		maxField.prefWidth(100);
		maxField.maxWidth(100);
		maxField.setPrefColumnCount(5);
		maxField.setText("" + max);
		maxField.textProperty().addListener(new ChangeListener<String>() {
			@Override
			public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
				System.out.println("Text changed to " + newValue);
				max = Integer.parseInt(newValue);
				realStep = ((float) (max - min)) / 100;
			}
		});

		minField.prefWidth(100);
		minField.maxWidth(100);
		minField.setPrefColumnCount(5);
		minField.setText("" + min);
		minField.textProperty().addListener(new ChangeListener<String>() {
			@Override
			public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
				System.out.println("Text changed to " + newValue);
				try {
					min = Integer.parseInt(newValue);
					realStep = ((float) (max - min)) / 100;
				} catch (NumberFormatException e) {

				}

			}
		});

		restposField.prefWidth(100);
		restposField.maxWidth(100);
		restposField.setPrefColumnCount(5);
		restposField.setText("" + restpos);

		servoField.prefWidth(100);
		servoField.maxWidth(100);
		servoField.setPrefColumnCount(3);
		servoField.setText("" + servo);
		servoField.textProperty().addListener(new ChangeListener<String>() {
			@Override
			public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
				System.out.println("Servo changed to " + newValue);
				try {
					servo = Integer.parseInt(newValue);

				} catch (NumberFormatException e) {

				}

			}
		});

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

		FlowPane topPane=new FlowPane();
		
		topPane.getChildren().add(namelabel);
		topPane.getChildren().add(servoField);
		topPane.getChildren().add(minField);
		topPane.getChildren().add(maxField);
		topPane.getChildren().add(restposField);
		topPane.getChildren().add(sl);
		topPane.getChildren().add(smooth);
		topPane.getChildren().add(maximize);
		topPane.getChildren().add(record);
		
		rootNode.getChildren().add(topPane);
		rootNode.getChildren().add(canvas);

		connect.setServo(servo, restpos);

	}

	private void drawMotionLine(GraphicsContext gc) {
		if (gc == null)
			return;
		gc.clearRect(0, 0, gc.getCanvas().getWidth(), gc.getCanvas().getHeight());
		gc.setStroke(Color.LIGHTBLUE);
		gc.setFont(Font.getDefault());
		gc.strokeLine(0, 0, gc.getCanvas().getWidth(), 0);
		gc.strokeLine(0, 50, gc.getCanvas().getWidth(), 50);
		gc.strokeLine(0, 100, gc.getCanvas().getWidth(), 100);
		for (int tel = 0; tel < steps; tel = tel + 10) {
			gc.strokeLine(tel * barwidth, 0, tel * barwidth, 100);
			gc.fillText("" + tel * interval*1000 + "ms", tel * barwidth, 110);
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

		if(oldx>=0)
        {
                        gc.strokeLine(oldx, oldy, newx,newy);
        }
		
		gc.setFill(Color.RED);
		gc.setStroke(Color.RED);
		gc.setLineWidth(2.0);
		gc.strokeLine(looppoint * barwidth, 0, looppoint * barwidth, 100);
		
		

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
			if (valueFields[tel] > 100)
				valueFields[tel] = 100;
			if (valueFields[tel] < 0)
				valueFields[tel] = 0;
		}
		drawMotionLine(gc);
	}

	public void maximize() {
		int min = 50;
		int max = 50;

		double factor = 1.1;
		for (int tel = 0; tel < steps; tel++) {
			if (valueFields[tel] < 50)
				valueFields[tel] = (int) (valueFields[tel] / factor);
			if (valueFields[tel] > 50)
				valueFields[tel] = (int) (valueFields[tel] * factor);

			if (valueFields[tel] > 100)
				valueFields[tel] = 100;
			if (valueFields[tel] < 0)
				valueFields[tel] = 0;
		}
		drawMotionLine(gc);
	}
	
	
	public void straiten(double startx,double starty,double endx,double endy)
    {
                  
                   int intBegin=(int)(startx/10);
                   int intEnd=(int)(endx/10);                           // bepaal de stappen
                   int size=intEnd-intBegin;
                  
                   System.out.println("Begin "+intBegin+"  end "+intEnd+" size "+size);            
                   System.out.println("BeginY "+starty+"  endY "+endy);
                  
                   double arc=(endy-starty)/size;  // maak de helling
                   System.out.println("Arc is "+arc);
                                  
                   for(int tel=0;tel<size;tel++)
                   {
                       //System.out.print("# "+tel+" oldval "+valueFields[tel+intBegin]);
                	   if(tel+intBegin<steps & tel+intBegin>=0)valueFields[tel+intBegin]=(int)(starty+(int)(tel*arc));
                       //System.out.print("newval "+valueFields[tel+intBegin]+"\n");
                   }
                                
    }

	public void redraw() {
		gc.clearRect(0, 0, gc.getCanvas().getWidth(), gc.getCanvas().getHeight());
		drawMotionLine(gc);

	}

	public Node getNode() {
		// TODO Auto-generated method stub
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
		return (int) ((valueFields[counter - 1] * realStep) + min);
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

	public boolean jitterFlag() {
		return jitterFlag;
	}
	
	public void toNull()
	{
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

	public void fillTrack(String string) {
		
		String valueStringList[]=string.split(" ");
		int size=valueStringList.length;
		
		if(size!=steps)
		{
			System.out.println("Step size problem");
			return;
		}
		
		valueFields = new int[size];
		for(int tel=0;tel<size;tel++)
		{
			valueFields[tel]=Integer.parseInt(valueStringList[tel]);
		}
		redraw();
	}


	
	

}
