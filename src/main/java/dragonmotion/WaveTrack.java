package dragonmotion;

import org.apache.log4j.Logger;

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

	Logger log=Logger.getLogger(WaveTrack.class);
	
	Label name=new Label("Samplefile");
	WaveService waveService;
	int steps;
	
	VBox rootNode = new VBox();
	
	private int canvassize=200;

	private int barwidth = 10;

	final GraphicsContext gc;
	private int looppoint = 0;
	
	
	public WaveTrack(final Stage stage,int steps) {
		
		waveService=WaveService.getInstance();     
		this.steps=steps;
		
		Canvas canvas = new Canvas(steps*barwidth, canvassize+20);
		log.info("Canvas width="+canvas.getWidth());
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
		gc.strokeLine(0, canvassize/2, gc.getCanvas().getWidth(), canvassize/2);
		gc.strokeLine(0, canvassize, gc.getCanvas().getWidth(), canvassize);
		
		
		gc.setStroke(Color.DARKGRAY);
		int samples[][]=waveService.getSample();
		int lowpasssamples[][]=waveService.getLowPass();
		if (samples != null)												// Als er geen sample is geladen wordt en niets getoond
		{
			int size         = waveService.sampleSize();
			
			int samstep      = size /(steps*barwidth);						// We vullen ook de barwidth voor een mooie sample
			double factor    = canvassize / waveService.getMaxvol();	
			
			// Draw the samples
			gc.setStroke(Color.DARKGRAY);						// The sample is darkgray
			int oldSampleVal = samples[0][0];					// Set the startpoint
			log.debug("factor is "+factor);
			for (int tel = 1; tel < steps*barwidth; tel++)					
			{
				int sampleVal = samples[0][tel * samstep];
				gc.strokeLine(tel-1, 20+oldSampleVal* factor, tel, 20 +sampleVal * factor);
				oldSampleVal=sampleVal;
			}
			
			// Draw the lowpass
			gc.setStroke(Color.GREEN);
			oldSampleVal = lowpasssamples[0][0];					// Set the startpoint
			log.debug("factor is "+factor);
			for (int tel = 1; tel < steps*barwidth; tel++)					
			{
				int sampleVal = lowpasssamples[0][tel * samstep];
				gc.strokeLine(tel-1, 20+oldSampleVal* factor, tel, 20 +sampleVal * factor);
				oldSampleVal=sampleVal;
			}
			
			
			gc.setStroke(Color.BLUE);										// Draw the grid lines
			gc.setFont(Font.getDefault());
			gc.strokeLine(0, 0, gc.getCanvas().getWidth(), 0);
			gc.strokeLine(0, canvassize/2, gc.getCanvas().getWidth(), canvassize/2);
			gc.strokeLine(0, canvassize, gc.getCanvas().getWidth(), canvassize);
			for (int tel = 0; tel < steps; tel = tel + 10) {				// Vertical grid lines
				gc.strokeLine(tel * barwidth, 0, tel * barwidth, canvassize);
				gc.fillText("" + tel * DragonMotion.interval*1000 + "ms", tel * barwidth, 110);
			}
		}
		
		gc.setFill(Color.RED);
		gc.setStroke(Color.RED);
		gc.setLineWidth(2.0);
		
		gc.strokeLine(looppoint * barwidth, 0, looppoint * barwidth, canvassize);
		log.debug("Redraw canvas of wavetrack with looppoint "+looppoint+" barwidth "+barwidth);
		
	}
	
	
	public Node getNode() {
		// TODO Auto-generated method stub
		return rootNode;
	}
	
	
	public void setLooppoint(int point) {
		this.looppoint = point;
		drawSample(gc);
	}
}
