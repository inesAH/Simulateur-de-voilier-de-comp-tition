package siani.javafmi;

import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

import com.opencsv.CSVReader;

public class ReadCSV {

	@SuppressWarnings("deprecation")
	public List<double[]> ReadCsV() throws Exception {
		CSVReader reader = null;
		List<double[]> lignes = new ArrayList<>();
		double[] ligne = new double[2];
		List<String[]> ligneString = null;
		File f = new File(getClass().getClassLoader().getResource("resources/vent.csv").getFile());
		if (f.exists()) {

			FileReader fileReader = new FileReader(f);

			reader = new CSVReader(fileReader, ';');

			ligneString = reader.readAll();

			fileReader.close();

			for (String[] strings : ligneString) {
				try {
					ligne[0] = Double.parseDouble(strings[0]);
					ligne[1] = Double.parseDouble(strings[1]);
					lignes.add(ligne);
				} catch (Exception e) {

				}

			}

		}
		return lignes;
	}

}
