package siani.javafmi;


import static org.javafmi.framework.FmiContainer.Causality.input;
import static org.javafmi.framework.FmiContainer.Causality.output;
import static org.javafmi.framework.FmiContainer.Variability.discrete;

import org.javafmi.framework.FmiSimulation;
import org.javafmi.framework.FmiSimulation.Status;
public class PDDLaw extends FmiSimulation  {
	private static final String ModelName = "PDDLaw";
	private static final String Consigne = "PDDLaw.consigne";
	private static final String KC = "PDDLaw.kc";
	private static final String KCap = "PDDLaw.kCap";
	private static final String KLacet = "PDDLaw.klacet";
	private static final String KRoulis = "PDDLaw.kRoulis";
	private static final String Amure = "PDDLaw.amure";
	private static final String Theta = "PDDLaw.theta";
	private static final String Hdg = "PDDLaw.hdg";
	private static final String Dhdg = "PDDLaw.dhdg";
	private static final String DHeel = "PDDLaw.dHeel";
	//private static final String Hdgpred = "PDDLaw.hdgpred";
	
	public double consigne = 20;
	public double kc = 0.1;
	public double kCap = 10;
	public double kLacet = 2;
	public double kRoulis = 0;
	public double amure = 1;
	public double dHeel = 0;

	public double theta = 0.;
	public double hdg = 0;
	public double dhdg = 0;
	public double hdgpred = 0;
	
	

	@Override
	public Model define() {
		return model(ModelName).canGetAndSetFMUstate(true)
				.add(variable(Consigne).asReal().causality(output).variability(discrete))
				.add(variable(KC).asReal().causality(output).variability(discrete))
				.add(variable(KCap).asReal().causality(output).variability(discrete))
				.add(variable(KLacet).asReal().causality(output).variability(discrete))
				.add(variable(KRoulis).asReal().causality(output).variability(discrete))
				.add(variable(Amure).asReal().causality(output).variability(discrete))
				.add(variable(Theta).asReal().causality(output).variability(discrete))
				//.add(variable(Hdgpred).asReal().causality(output).variability(discrete))
				.add(variable(Hdg).asReal().causality(input).variability(discrete))
				.add(variable(DHeel).asReal().causality(input).variability(discrete))
				.add(variable(Dhdg).asReal().causality(input).variability(discrete));

	}

	@Override
	public Status doStep(double stepSize) {
		dhdg= (hdgpred-hdg)/stepSize;
		hdgpred=hdg;
		theta = -kc * (kCap * (hdg - consigne) + kLacet * dhdg
				+ amure * kRoulis * dHeel);

		if (theta > 15) {
			theta = 15;
		} else if (theta < -15) {
			theta = -15;
		}
		return Status.OK;
	}

	@Override
	public Status init() {
		logger().info("doing init");
		registerReal(Consigne, () -> consigne, value -> consigne = value);
		registerReal(KC, () -> kc, value -> kc = value);
		registerReal(KCap, () -> kCap, value -> kCap = value);
		registerReal(KLacet, () -> kLacet, value -> kLacet = value);
		registerReal(KRoulis, () -> kRoulis, value -> kRoulis = value);
		registerReal(Amure, () -> amure, value -> amure = value);
		registerReal(Theta, () -> theta, value -> theta = value);
		registerReal(Hdg, () -> hdg, value -> hdg = value);
		registerReal(DHeel, () -> dHeel, value -> dHeel = value);
		registerReal(Dhdg, () -> dhdg, value -> dhdg = value);
		//registerReal(Hdgpred, () -> dhdg, value -> dhdg = value);
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
