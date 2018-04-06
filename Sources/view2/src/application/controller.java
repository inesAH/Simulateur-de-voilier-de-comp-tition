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
	/*méthode d'initialisation */
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		// TODO Auto-generated method stub

		webEngine = WebView.getEngine();/*instance de web webview qui va contenir la page html */
		final URL urlLeafletMaps = getClass().getResource("index.html");// chargement de l'url page html a afficher
		webEngine.load(urlLeafletMaps.toExternalForm());//chargement de la page dans le webview

		webEngine.setJavaScriptEnabled(true);//activation de l'éxécution du javascript
		/*boutton d'affichage de la carte est désactivé par défaut avant le chargement du fichier json à afficher 
		*/
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
	/*
	*méthode de mise à jour de l'affichage avec modification de la valeur du slider 
	*/
	@FXML
	private void updateSlider() throws InterruptedException {
		this.webEngine.executeScript("clearMap();");//vider la map 
		sliderVal = (int) slider.getValue();//récuperer la valeur du slider
		showOnMap(freq, sliderVal);//appel de la methode d'affichage avec la nouvelle valeur 
		jsobj.setMember("dataToSend", new Bridge());//insertion d'un de la class Bridge utilisé pour la communication entre le coté java et le coté web 

	}
	/*
	*méthode de mise à jour de l'affichage avec modéfication de la fréquence d'affichage de points sur la map
	*/
	public void updateFrequence() {
		try {

			this.webEngine.executeScript("clearMap();");//vider la carte
			freq = Integer.parseInt(frequence.getText());//récuperer la nouvelle valeur de la fréquence 
			showOnMap(freq, sliderVal);//appel de la méthode d'affichage avec la nouvelle valeur
			jsobj.setMember("dataToSend", new Bridge());//insertionde l'objet de la classe Bridge 

		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
	}	
	/*
	*La classe bridge est une classe intemédiaire qui pertmet de faire le lien entre le coté java 
	*et le coté web (javaScript), instancier un objet de la classe et injecter dans le javaScript 
	*donc dans le javaScript on peut utilisé cet objet ainsi que les méthode de la classe 
	*/
	public class Bridge {
		/*
		*Méthode de mise à jour des valeur affichées sur l'ihm 
		*/
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
	//méthiode de chargement du fichier json à afficher
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
	/*action du boutton load Boat Trace qui appel la méthode fileChooser*/
	@FXML
	void chooseFile(ActionEvent event) {

		jsonFile = fileChooser("Select the Json File", null, "Json file", "*.json*");
		btnShow.setDisable(false);

	}
	/*
	*méthode d'affichage du fichier json dans la map
	*/
	private void showOnMap(int frequence, int sliderVal) {
		if (firstShow == false) {
			this.webEngine.executeScript("clearMap();");//si la map contient déja un fichier on va la vider 

		}

		try {
			JSONParser jsonParser = new JSONParser();
			String jsonStr = jsonParser.parse(new FileReader(jsonFile)).toString();//parser le json on string 
			this.webEngine.executeScript("jsonStr =" + jsonStr + ";");//créer la variable jsonStr avec la valeur de jsonString dans le javaScript
			this.webEngine.executeScript("freq =" + frequence + ";");//créer la variable freq avec la valeur de la frequence  dans le javaScript
			this.webEngine.executeScript("valSlider =" + sliderVal + ";");//créer une variable valSlider avec  la valeur du sliderVal
			this.webEngine.executeScript("showOnMap(jsonStr, freq, valSlider);");//executer la methode javaScript d'affichage avec les paramètres jsonStr, freq, valSlider 
			this.frequence.setText("" + freq);// affichage de la valeur de la freq dans l'ihm
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
