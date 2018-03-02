package application;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import netscape.javascript.JSObject;

public class controller implements Initializable {

	@FXML
	private WebView WebView;
	@FXML
	private Label bsVal, twsVal, twaVal, rdaVal, hdgVal, twdVal, awaVal, awsVal, freqval;

	@FXML
	private TextField frequence;

	@FXML
	private Button btnShow;

	@FXML
	private Slider slider;

	private WebEngine webEngine;
	private File jsonFile;
	private int freq = 250;
	private int sliderVal = 100;
	private boolean firstShow = true;
	private JSObject jsobj;

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		// TODO Auto-generated method stub

		webEngine = WebView.getEngine();
		final URL urlLeafletMaps = getClass().getResource("index.html");
		webEngine.load(urlLeafletMaps.toExternalForm());

		webEngine.setJavaScriptEnabled(true);
		btnShow.setDisable(true);
		jsobj = (JSObject) webEngine.executeScript("window");

	}

	public void ShowJson() {
		try {
			showOnMap(freq, sliderVal);
			jsobj.setMember("dataToSend", new Bridge());

		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
	}

	@FXML
	private void updateSlider() throws InterruptedException {
		this.webEngine.executeScript("clearMap();");
		sliderVal = (int) slider.getValue();
		showOnMap(freq, sliderVal);
		jsobj.setMember("dataToSend", new Bridge());

	}

	public void updateFrequence() {
		try {

			this.webEngine.executeScript("clearMap();");
			freq = Integer.parseInt(frequence.getText());
			showOnMap(freq, sliderVal);
			jsobj.setMember("dataToSend", new Bridge());

		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
	}

	public class Bridge {
		public void receveData(double BS, double TWS, double TWA, double RDA, double HDG, double TWD, double AWA,
				double AWS) {
			bsVal.setText("" + BS);
			twsVal.setText("" + TWS);
			twaVal.setText("" + TWA);
			rdaVal.setText("" + RDA);
			hdgVal.setText("" + HDG);
			twdVal.setText("" + TWD);
			awaVal.setText("" + AWA);
			awsVal.setText("" + AWS);
			jsobj.setMember("dataToSend", new Bridge());

		}

	}

	private File fileChooser(String title, String absolutePath, String nameEx, String extension) {
		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle(title);
		if (absolutePath != null) {
			File userDirectory = new File(absolutePath);
			fileChooser.setInitialDirectory(userDirectory);
		}
		fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter(nameEx, extension));
		return fileChooser.showOpenDialog(new Stage());
	}

	@FXML
	void chooseFile(ActionEvent event) {

		jsonFile = fileChooser("Select the Json File", null, "Json file", "*.json*");
		btnShow.setDisable(false);

	}

	private void showOnMap(int frequence, int sliderVal) {
		if (firstShow == false) {
			this.webEngine.executeScript("clearMap();");

		}

		try {
			JSONParser jsonParser = new JSONParser();
			String jsonStr = jsonParser.parse(new FileReader(jsonFile)).toString();
			this.webEngine.executeScript("jsonStr =" + jsonStr + ";");
			this.webEngine.executeScript("freq =" + frequence + ";");
			this.webEngine.executeScript("valSlider =" + sliderVal + ";");
			this.webEngine.executeScript("showOnMap(jsonStr, freq, valSlider);");
			this.frequence.setText("" + freq);
			freqval.setText("" + freq);
			jsobj.setMember("dataToSend", new Bridge());

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
