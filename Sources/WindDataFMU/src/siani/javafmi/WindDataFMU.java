package siani.javafmi;

import static org.javafmi.framework.FmiContainer.Causality.output;
import static org.javafmi.framework.FmiContainer.Variability.discrete;

import java.util.ArrayList;
import java.util.List;

import org.javafmi.framework.FmiSimulation;

public class WindDataFMU extends FmiSimulation {

	private static final String ModelName = "WindDataFMU";
	private static final String Tws = "WindDataFMU.tws";
	private static final String Twd = "WindDataFMU.twd";

	public double tws = 0.;
	public double twd = 0;
	List<double[]> windList = new ArrayList<>();

	@Override
	public Model define() {
		return model(ModelName).canGetAndSetFMUstate(true)
				.add(variable(Tws).asReal().causality(output).variability(discrete))
				.add(variable(Twd).asReal().causality(output).variability(discrete));

	}

	@Override
	public Status doStep(double stepSize) {

		twd = windList.get((int) stepSize)[0];
		tws = windList.get((int) stepSize)[1];
		return Status.OK;

	}

	@Override
	public Status init() {
		ReadCSV read = new ReadCSV();
		try {
			windList = read.ReadCsV();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		logger().info("doing init");
		registerReal(Tws, () -> tws, value -> tws = value);
		registerReal(Twd, () -> twd, value -> twd = value);
		return Status.OK;
	}

	@Override
	public Status reset() {
		return Status.OK;
	}

	@Override
	public Status terminate() {
		return Status.OK;
	}

	public Status getState(String stateId) {
		logger().info("doing get state");
		return super.getState(stateId);
	}

}
