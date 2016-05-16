package itba.edu.ar.simulation.test;

import java.io.IOException;

import itba.edu.ar.cellIndexMethod.data.particle.FloatPoint;
import itba.edu.ar.simulation.Simulation;
import itba.edu.ar.simulation.output.GranularSimulationPositions;
import itba.edu.ar.simulation.output.GranularSimulationEnergy;
import itba.edu.ar.simulation.output.GranularSimulationFlow;
import itba.edu.ar.ss.algorithm.Algorithm;
import itba.edu.ar.ss.algorithm.impl.Verlet;

public class Test {

	private static final int printAfterNFrames = 20;
	private static final double[] lengths = { 10, 15, 20 };
	private static final double width = 5;
	private static final double height = 5;
	private static final double diameter = 2;
	private static final double mass = 0.01;
	private static final String path = System.getProperty("user.dir") + "/";
	private static final double deltaTime = 0.5 * Math.pow(10, -4);
	private static final double finalTime = 3;

	public static void main(String[] args) throws IOException, InstantiationException, IllegalAccessException {

		for (int i = 0; i < lengths.length; i++) {

			double length = lengths[i];

			Simulation simulation = new Simulation(length, width, height, diameter, mass, path);

			GranularSimulationPositions gsp = new GranularSimulationPositions(path, length,width, printAfterNFrames,"lenght-"+length);
			GranularSimulationEnergy gse = new GranularSimulationEnergy(path, printAfterNFrames, "lenght-"+length);
			GranularSimulationFlow gsf = new GranularSimulationFlow(printAfterNFrames, path, height, length, width,
					diameter, deltaTime);

			simulation.subscribe(gsp);
			simulation.subscribe(gse);
			simulation.subscribe(gsf);

			Algorithm<FloatPoint> algorithm = new Verlet(deltaTime);

			simulation.simulate(algorithm, deltaTime, finalTime);

		}
	}

}
