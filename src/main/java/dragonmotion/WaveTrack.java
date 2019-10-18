package dragonmotion;

import dragonmotion.services.WaveService;
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;



public class WaveTrack {

	
	
	Label name=new Label("Samplefile");
	WaveService waveService;
	int steps;
	
	VBox rootNode = new VBox();
	int canvaswidth=1000;
	int barwidth = 10;
	double maxvol = 1;
	final GraphicsContext gc;
	
	public WaveTrack(final Stage stage) {
		
		waveService=WaveService.getInstance();     
		steps=waveService.getSteps();
		
		Canvas canvas = new Canvas(steps*10, 100);
		gc = canvas.getGraphicsContext2D();
		drawSample(gc);
		
		rootNode.getChildren().add(name);
		rootNode.getChildren().add(canvas);
	}
	

	
	
	
	
	
	private void drawSample(GraphicsContext gc) {
		if (gc == null)
			return;
		gc.clearRect(0, 0, gc.getCanvas().getWidth(), gc.getCanvas().getHeight());
		gc.setStroke(Color.LIGHTBLUE);
		gc.setFont(Font.getDefault());
		gc.strokeLine(0, 0, gc.getCanvas().getWidth(), 0);
		gc.strokeLine(0, 50, gc.getCanvas().getWidth(), 50);
		gc.strokeLine(0, 100, gc.getCanvas().getWidth(), 100);
		
		int samples[][]=waveService.getSample();
		int size=waveService.sampleSize();
		
		if (samples != null)
		{
			int oldSampleVal=samples[0][0];
			int samstep = size /(steps*barwidth);		// We vullen ook de barwidth voor een mooie sample
			double factor = 100.0 / waveService.getMaxvol();
			for (int tel = 1; tel < steps*barwidth; tel++)
			{
				int sampleVal = samples[0][tel * samstep];
				System.out.printf("%d %f \n", tel,  factor);
				gc.strokeLine(tel-1, oldSampleVal* factor, tel, sampleVal * factor);
				oldSampleVal=sampleVal;
			}
			gc.setStroke(Color.BLUE);
			gc.setFont(Font.getDefault());
			gc.strokeLine(0, 0, gc.getCanvas().getWidth(), 0);
			gc.strokeLine(0, 50, gc.getCanvas().getWidth(), 50);
			gc.strokeLine(0, 100, gc.getCanvas().getWidth(), 100);
			for (int tel = 0; tel < steps; tel = tel + 10) {
				gc.strokeLine(tel * barwidth, 0, tel * barwidth, 100);
				gc.fillText("" + tel * DragonMotion.interval + "ms", tel * barwidth, 110);
			}
		}
		
	}
	
	public Node getNode() {
		// TODO Auto-generated method stub
		return rootNode;
	}
}
