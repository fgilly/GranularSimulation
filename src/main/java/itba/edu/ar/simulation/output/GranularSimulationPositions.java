package itba.edu.ar.simulation.output;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import itba.edu.ar.cellIndexMethod.data.particle.FloatPoint;
import itba.edu.ar.cellIndexMethod.data.particle.Particle;
import itba.edu.ar.simulation.SimulationObserver;
import itba.edu.ar.ss.algorithm.AlgorithmObserver;
import itba.edu.ar.ss.model.entity.Entity;
import itba.edu.ar.ss.model.entity.impl.AstronomicalObject;
import itba.edu.ar.ss.system.data.SolarSystemData;

public class GranularSimulationPositions implements SimulationObserver {

	private static final String COLUMNS_FILE = "Properties=id:I:1:pos:R:2:velocity:R:2:color:R:3";
	private static final String _SEPARTOR_ = " ";
	int printAfterNFrames;
	private String path;
	private double length;
	private String tag;

	public GranularSimulationPositions(String path, double length,int printAfterNFrames,String tag) throws IOException {
		this.path = path;
		this.length=length;
		this.printAfterNFrames=printAfterNFrames;
		this.tag=tag;
		
		Files.write(Paths.get(path + "GranularSimulationPositions_"+tag), new LinkedList<String>(), Charset.forName("UTF-8"),
				StandardOpenOption.TRUNCATE_EXISTING,StandardOpenOption.CREATE);

	}
	
	int frame=0;
	public void stepEnded(List<Particle> particles, double time) {
		if(frame++ % printAfterNFrames != 0)
			return;
		
		System.out.println(frame);
		List<String> fileContent = new ArrayList<String>();

		fileContent.add(particles.size() + "");
		fileContent.add("Time=" + frame + " " + sizeBox(length) + " " + COLUMNS_FILE);

		StringBuilder sb = new StringBuilder();
		int id = 1;
		for (Particle particle : particles) {
			sb.append(id).append(_SEPARTOR_).append(particle.getPosition().getX()).append(_SEPARTOR_)
					.append(particle.getPosition().getY()).append(_SEPARTOR_).append(particle.getRadio());
			fileContent.add(sb.toString());
			sb= new StringBuilder();
			id++;
		}
		

		try {
			Files.write(Paths.get(path + "GranularSimulationPositions_"+tag), fileContent, Charset.forName("UTF-8"),
					StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.APPEND);
		} catch (IOException e) {
			throw new IllegalAccessError();
		}

	}

	
	public void simulationEnded() {
	}

	private String sizeBox(double length) {
		String sizeX = length + " 0.00000000 0.00000000";
		String sizeY = "0.00000000 " + length + " 0.00000000";
		String sizeZ = "0.00000000 0.00000000 0.000000000000000001"; // sizeZ!=(0,0,0)
																		// for
																		// Ovito
																		// recognize
																		// the
																		// box
																		// size
		String sizeBox = "Lattice=\"" + sizeX + " " + sizeY + " " + sizeZ + "\"";
		return sizeBox;
	}


}
