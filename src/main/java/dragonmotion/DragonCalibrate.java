package dragonmotion;

import org.apache.log4j.Logger;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.input.InputMethodEvent;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.BorderWidths;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.text.TextAlignment;

public class DragonCalibrate {
	
	Logger log = Logger.getLogger(DragonCalibrate.class);
	
	int servo=1;
	int min=100;
	int max=450;
	int restpos=225;
	int sliderpos=0;
	
	GridPane fragment=new GridPane();
	
	Label servolbl=new Label("servo");
	Label minlbl=new Label("min");
	Label maxlbl=new Label("max");
	Label restlbl=new Label("rest position");
	Label sldlbl=new Label("Slider position");
	
	TextField servofld=new TextField();
	TextField minfld  =new TextField();
	TextField maxfld  =new TextField();
	TextField restfld =new TextField();
	Label     sldfld  =new Label();
	
	Slider sl=new Slider();
	
	DragonConnect conector=DragonUDP.getService();
	
	public DragonCalibrate()
	{
		
		servofld.setText(""+servo);
		minfld.setText(""+min);
		maxfld.setText(""+max);
		restfld.setText(""+restpos);
		sldfld.setTextAlignment(TextAlignment.CENTER);
		
		sl.setMin(min);
		sl.setMax(max);
		sl.valueProperty().set(restpos);
		
		servofld.textProperty().addListener(new ChangeListener<String>() {
			@Override
			public void changed(ObservableValue<? extends String> observable,
		            String oldValue, String newValue) {
				servo=Integer.parseInt(newValue);
			}
		});
		
		minfld.textProperty().addListener(new ChangeListener<String>() {
			@Override
			public void changed(ObservableValue<? extends String> observable,
		            String oldValue, String newValue) {
				min=Integer.parseInt(newValue);
				sl.setMin(min);
			}
		});
		
		maxfld.textProperty().addListener(new ChangeListener<String>() {
			@Override
			public void changed(ObservableValue<? extends String> observable,
		            String oldValue, String newValue) {
				max=Integer.parseInt(newValue);
				sl.setMax(max);
			}
		});
		
		restfld.textProperty().addListener(new ChangeListener<String>() {
			@Override
			public void changed(ObservableValue<? extends String> observable,
		            String oldValue, String newValue) {
				restpos=Integer.parseInt(newValue);
				sl.valueProperty().set(restpos);
			}
		});
		
		
		sl.valueProperty().addListener(new ChangeListener<Number>() {

			@Override
			public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
				log.info("Value is "+newValue.intValue());
				sldfld.setText(""+newValue.intValue());
				conector.setServo(servo, newValue.intValue());
				
			}
		});
		
		fragment.add(servolbl, 0, 0);
		fragment.add(minlbl, 0, 1);
		fragment.add(maxlbl, 0, 2);
		fragment.add(restlbl, 0, 3);
		fragment.add(sldlbl, 0, 4);
		fragment.add(servofld, 1, 0);
		fragment.add(minfld, 1, 1);
		fragment.add(maxfld, 1, 2);
		fragment.add(restfld, 1, 3);
		fragment.add(sldfld, 1, 4);
		
		fragment.add(sl,0,5,2,1);
		
		BorderStroke borderStroke = new BorderStroke(Color.DARKGRAY, BorderStrokeStyle.SOLID, CornerRadii.EMPTY,BorderWidths.DEFAULT);
		fragment.setBorder(new Border(borderStroke));
	}
	
	
	public Node getFragement()
	{
		return fragment;
	}

}
