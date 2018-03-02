package siani.javafmi;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.javafmi.proxy.FmuFile;
import org.javafmi.wrapper.Simulation;

import org.json.simple.JSONObject;

import com.sun.javafx.geom.Vec2d;

public class masterFMI {
	int startTime = 0;
	// int stopTime = 2193;
	double stopTime = 0;
	int stepSize = 1;
	double hdg = 0.0, dhdg = 0.0;
	double heel = 0.0, boatSpeed = 0.0, y2 = 0.0;
	int freq = 1;
	double dheel = 0.0;
	double heelpred = 0.0;
	double previousLon = Math.toRadians(-12.301267);
	double previousLat = Math.toRadians(49.148695);
	double lat;
	double lng;
	double x[] = { 0.0, 0.0, 0.0, 0.0, 0.0 };// initialisation du vecteur x
	double y[] = { 0.0, 0.0 };

	double x2[] = { 0.0, 0.0, 0.0, 0.0, 0.0 };// initialisation du vecteur x

	public masterFMI() {
		Simulation simulationPDD = null;
		Simulation simulationWind = null;
		Simulation simulationMODEL3 = null;
		FileWriter file = null;
		JSONObject obj = new JSONObject();
		double moybs = 0;

		String os = System.getProperty("os.name").toLowerCase();
		if (os.contains("win")) {
			// Creation des simulations
			simulationPDD = new Simulation("fmuWin/PDDLaw.fmu");
			simulationWind = new Simulation("fmuWin/WindDataFMU.fmu");
			simulationMODEL3 = new Simulation("fmuWin/BoatModeleComplete.fmu");
		} else if (os.contains("nux")) {
			simulationPDD = new Simulation("fmuLin/PDDLaw.fmu");
			simulationWind = new Simulation("fmuLin/WindDataFMU.fmu");
			simulationMODEL3 = new Simulation("fmuLin/BoatModeleComplete.fmu");
		} else {
			System.out.println("vous devez avoir un OS windows ou linux pour pouvoir travailler avec FMI");
		}

		// initialisation des simulations
		simulationPDD.init(startTime, stopTime);
		simulationMODEL3.init(startTime, stopTime);
		simulationWind.init(startTime, stopTime);
		stopTime = simulationWind.read("WindDataFMU.length").asDouble();
		// Initialisation du fichier JSON a extraire
		try {
			String workingDir = System.getProperty("user.dir");
			// final File dir = new File(workingDir, "dataJson");
			Path path = Paths.get(workingDir + "/dataJson");
			if (!Files.exists(path)) {
				try {
					Files.createDirectories(path);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			// String userHomeFolder = System.getProperty("user.home");
			// File textFile = new File("dataJson", "FileConfig.json");
			File textFile = new File(path.toString(), "FileConfig.json");
			file = new FileWriter(textFile);
			file.write("[");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// Lacement de la simulation
		for (int i = startTime; i < stopTime; i++) {

			simulationPDD.write("PDDLaw.hdg").with(hdg);
			simulationPDD.write("PDDLaw.dHeel").with(dheel);
			simulationPDD.doStep(stepSize);
			double rudderAngle = simulationPDD.read("PDDLaw.theta").asDouble();

			// extraction du vent
			simulationWind.doStep(stepSize);
			double tws = simulationWind.read("WindDataFMU.tws").asDouble();
			double twd = simulationWind.read("WindDataFMU.twd").asDouble();

			// ecriture du vecreur x et des valeurs du vents

			/// sim3
			simulationMODEL3
					.write("TWS", "TWD", "rudderAngle", "xx1", "xy1", "xz1", "xk1", "xl1", "xx", "xy", "xz", "xk", "xl")
					.with(tws, twd, rudderAngle, x[0], x[1], x[2], x[3], x[4], x2[0], x2[1], x2[2], x2[3], x2[4]);

			// Pas d'executuion
			simulationMODEL3.doStep(stepSize);
			// lecture du x
			x = simulationMODEL3.read("X1i[1]", "X1i[2]", "X1i[3]", "X1i[4]", "X1i[5]").asDoubles();
			// lecture du y
			y = simulationMODEL3.read("Y1[1]", "Y1[2]").asDoubles();

			// eclater Y
			heel = y[0];
			boatSpeed = y[1];
			dheel = (heelpred - heel) / stepSize;
			heelpred = heel;
			// lecture du x

			x2 = simulationMODEL3.read("X2i[1]", "X2i[2]", "X2i[3]", "X2i[4]", "X2i[5]").asDoubles();
			// lecture du y
			y2 = simulationMODEL3.read("Y2").asDouble();
			// maj des val
			hdg = y2;
			System.out.println("-------------" + i + " ----------------");
			System.out.println("------------- *** ----------------");
			// Calcul de latitude et longitude
			double sog = boatSpeed * 1852 / (3600 * freq) / 6378000;
			double cog = hdg * Math.PI / 180;
			previousLon = previousLon + sog * Math.sin(cog) / Math.cos(previousLat);
			previousLat = previousLat + sog * Math.cos(cog);
			lat = previousLat * 180 / Math.PI;
			lng = previousLon * 180 / Math.PI;
			moybs = moybs + boatSpeed;
			double twa = twd - hdg;
			if (twa > 180) {
				twa = twa - 360;
			} else if (twa < -180) {
				twa = twa + 360;
			}
			// formule a verifier
			// double AWA = Math.atan((Math.sin(twa) * tws) / (boatSpeed +
			// Math.cos(twa) * tws));
			// double AWS = Math.sqrt(Math.pow((Math.sin(twa) * tws), 2) +
			// Math.pow((boatSpeed + Math.cos(twa) * tws), 2));
			
			//Calcul du vent apparent
			Vec2d vTW = new Vec2d(); // vecteur vent reel
			Vec2d vB = new Vec2d(); // vecteur bateau
			vTW.x = tws * Math.cos(Math.toRadians(twd));
			vTW.y = tws * Math.sin(Math.toRadians(twd));
			vB.x = boatSpeed * Math.cos(Math.toRadians(hdg));
			vB.y = boatSpeed * Math.sin(Math.toRadians(hdg));
			Vec2d vA = new Vec2d(); // vecteur vent apparent
			vA.y = vTW.y - vB.y;
			vA.x = vTW.x - vB.x;
			double aws2 = Math.sqrt(Math.pow(vA.x, 2) + Math.pow(vA.y, 2));
			double awa = Math.atan2(vA.y, vA.x); // donne du radians
			double A = Math.atan2(vA.y, vA.x);
			double awa2 = Math.toDegrees(awa);
			if (awa2 < 0) {
				awa2 = Math.toDegrees(awa) + 90;
			} else if (awa2 > 180) {
				awa2 = Math.toDegrees(awa) - 90;
			}
			// Creation des element du fichiers JSON

			obj.put("pas", i);
			obj.put("lat", lat);
			obj.put("lng", lng);
			obj.put("RDA", rudderAngle);
			obj.put("TWS", tws);
			obj.put("TWD", twd);
			obj.put("BS", boatSpeed);
			obj.put("hdg", hdg);
			obj.put("TWA", twa);
			obj.put("AWA", awa2);
			obj.put("AWS", aws2);
			// ecriture dans la liste
			if (i != stopTime - 1) {
				try {

					file.write(obj.toJSONString());
					file.write(",");
					System.out.println("fichier bien copie");
					System.out.println("\nJSON Objet: " + obj);

				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			} else {
				try {

					file.write(obj.toJSONString());
					file.write("]");
					System.out.println("fichier bien copie");
					System.out.println("\nJSON Objet: " + obj);

				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}
			// ecriture dans le fichier

		}

		try {
			file.flush();
			file.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		double moyenne = moybs / stopTime;
		System.out.println("moyenne = " + moyenne);
		// simulation termine
		simulationMODEL3.terminate();
		simulationPDD.terminate();
		simulationWind.terminate();

	}

	public static void main(String[] args) {
		System.out.println("--- MasterFMI  --- ");
		new masterFMI();

	}
}
