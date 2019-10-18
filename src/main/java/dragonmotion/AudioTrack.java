package dragonmotion;

import java.io.File;
import java.io.IOException;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.FlowPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;

public class AudioTrack {
	FlowPane rootNode = new FlowPane();
	
	final GraphicsContext gc;

	int barwidth = 10;
	//float stepwidth = 3;
	//int interval = 100; // 100ms interval
	int steps = 100;
	int valueFields[];

	double oldx = -1;
	double oldy = -1;
	double newx, newy;
	double time=10;

	int samplelength = 1;
	double maxvol = 1;

	int[][] samples;

	public AudioTrack(final Stage stage,int steps) {

		this.steps=steps;
		valueFields = new int[steps];
		for (int tel = 0; tel < steps; tel++)
			valueFields[tel] = 50;
		valueFields[20] = 30;
		valueFields[60] = 50;

		// Canvas canvas = new Canvas(1000,100);

		Canvas canvas = new Canvas();
		canvas.setHeight(120);
		canvas.setWidth(steps * barwidth);
		gc = canvas.getGraphicsContext2D();
		drawMotionLine(gc);
		
		
		
		rootNode.getChildren().add(canvas);

	}

	private void drawMotionLine(GraphicsContext gc)
	{
		gc.clearRect(0, 0, gc.getCanvas().getWidth(), gc.getCanvas().getHeight());
		gc.setFill(Color.BLACK);
		gc.setLineWidth(2.0);
		
		
		if (samples != null)
		{
			int oldSampleVal=samples[0][0];
			int samstep = samplelength /(steps*barwidth);		// We vullen ook de barwidth voor een mooie sample
			double factor = 100.0 / maxvol;
			for (int tel = 1; tel < steps*barwidth; tel++)
			{
				int sampleVal = samples[0][tel * samstep];
				//System.out.printf("%d %d %f \n", tel, spike, factor);
				gc.strokeLine(tel-1, oldSampleVal* factor, tel, sampleVal * factor);
				oldSampleVal=sampleVal;
			}
			gc.setStroke(Color.LIGHTBLUE);
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

	public void processFile(File audioFile)
	{
		try {
			AudioInputStream ais = AudioSystem.getAudioInputStream(audioFile);
			System.out.println("SampleBits " + ais.getFormat().getSampleSizeInBits());
			System.out.println("Channels " + ais.getFormat().getChannels());
			System.out.println("Samplerate " + ais.getFormat().getSampleRate());
			System.out.println("FrameSize " + ais.getFormat().getFrameSize());
			System.out.println("FrameLength " + ais.getFrameLength());
			byte[] eightBitByteArray = new byte[(int) (ais.getFrameLength() * ais.getFormat().getFrameSize())];
			int result = ais.read(eightBitByteArray);
			samplelength = (int) ais.getFrameLength();
			samples = new int[ais.getFormat().getChannels()][(int) ais.getFrameLength()];
			maxvol = 1;
			time=ais.getFrameLength()/ais.getFormat().getSampleRate();
			System.out.println("Time is " + time);
			steps=(int) ((time*1000.0)/DragonMotion.interval);
			System.out.println("Servo steps is " + steps);
			
			int sampleIndex = 0;
			for (int t = 0; t < eightBitByteArray.length;) {
				for (int channel = 0; channel < ais.getFormat().getChannels(); channel++) {
					int low = (int) eightBitByteArray[t];
					t++;
					int high = (int) eightBitByteArray[t];
					t++;
					int sample = getSixteenBitSample(high, low) + 16000;
					if (sample > maxvol)
					maxvol = sample;
					samples[channel][sampleIndex] = sample;
					// System.out.printf("S:%d M:%f\t",sample,maxvol);
				}

				sampleIndex++;
			}
		} catch (UnsupportedAudioFileException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		drawMotionLine(gc);
	}

	protected int getSixteenBitSample(int high, int low) {
		return (high << 8) + (low & 0x00ff);
	}

	public Node getNode() {
		// TODO Auto-generated method stub
		return rootNode;
	}

}
