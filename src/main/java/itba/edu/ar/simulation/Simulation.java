package itba.edu.ar.simulation;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import itba.edu.ar.cellIndexMethod.CellIndexMethod;
import itba.edu.ar.cellIndexMethod.IndexMatrix;
import itba.edu.ar.cellIndexMethod.IndexMatrixBuilder;
import itba.edu.ar.cellIndexMethod.data.particle.FloatPoint;
import itba.edu.ar.cellIndexMethod.data.particle.Particle;
import itba.edu.ar.cellIndexMethod.route.Route;
import itba.edu.ar.cellIndexMethod.route.routeImpl.OptimizedRoute;
import itba.edu.ar.input.file.CellIndexMethodFileGenerator;
import itba.edu.ar.input.file.data.Data;
import itba.edu.ar.simulation.data.GranularParticle;
import itba.edu.ar.simulation.data.ParticleForce;
import itba.edu.ar.simulation.data.SiloData;
import itba.edu.ar.simulation.model.Wall;
import itba.edu.ar.ss.algorithm.Algorithm;
import itba.edu.ar.ss.model.entity.Entity;
import itba.edu.ar.ss.model.force.Force;

public class Simulation {

	private static final double CONSTANT_g = 9.8;
	private static final double distanceBetweenParticles = 0;

	private List<Wall> walls = new ArrayList<Wall>();
	private List<Data> datas = new LinkedList<Data>();
	private String path;
	private List<SimulationObserver> subscribers = new LinkedList<SimulationObserver>();

	public Simulation(double length, double width, double height, double diameter, double mass, String path) {

		this.path = path;

		double bottomLength = (width - diameter) / 2;

		walls.add(new Wall(new FloatPoint(0, height), new FloatPoint(0, height + length)));
		walls.add(new Wall(new FloatPoint(0, length), new FloatPoint(width, length)));
		walls.add(new Wall(new FloatPoint(width, height), new FloatPoint(width, length)));

		walls.add(new Wall(new FloatPoint(0, height), new FloatPoint(bottomLength, height)));
		walls.add(new Wall(new FloatPoint(width - bottomLength, height), new FloatPoint(width, height)));

		double particleRadio = diameter / 20;
		double spawningSurface = (length - height) * width;
		int particleQuantity = (int) Math.floor(spawningSurface / Math.pow(particleRadio * 2, 2));
		
		datas.add(new SiloData(particleQuantity, mass, particleRadio, length, height, width));

	}

	public void simulate(Algorithm<FloatPoint> algorithm, double deltaTime, double finalTime)
			throws InstantiationException, IllegalAccessException, IOException {

		List<String> staticPath = new ArrayList<String>();
		List<String> dynamicPath = new ArrayList<String>();

		CellIndexMethodFileGenerator.generate(staticPath, dynamicPath, datas, path,
				((SiloData) datas.get(0)).getLength());

		IndexMatrix indexMatrix = IndexMatrixBuilder.getIndexMatrix(staticPath.get(0), dynamicPath.get(0),
				getCellQuantity((SiloData) datas.get(0)), deltaTime);

		List<Particle> particles = indexMatrix.getParticles();
		System.out.println("Particle quantity: " + particles.size());
		
		for (double time = 0; time < finalTime; time += deltaTime) {

			indexMatrix.clear();
			indexMatrix.addParticles(particles);

			CellIndexMethod cellIndexMethod = new CellIndexMethod(indexMatrix, getRoute((SiloData) datas.get(0)),
					distanceBetweenParticles);

			cellIndexMethod.execute();

			List<Entity<FloatPoint>> granularParticles = getGranularParticles(particles, deltaTime);

			algorithm.evolveSystem(granularParticles, getForces(indexMatrix.getParticles()), deltaTime);

			removeOutsiders(particles);

			notifyStepEnded(particles, time);

		}

		notifySimulationEnded();

	}

	private void notifySimulationEnded() throws IOException {
		for (SimulationObserver so : subscribers)
			so.simulationEnded();
	}

	private void notifyStepEnded(List<Particle> particles, double time) throws IOException {
		for (SimulationObserver so : subscribers)
			so.stepEnded(particles, time);
	}

	private List<Entity<FloatPoint>> getGranularParticles(List<Particle> particles, double deltaTime) {
		List<Entity<FloatPoint>> granularParticles = new LinkedList<Entity<FloatPoint>>();
		for (Particle particle : particles) {
			granularParticles.add(new GranularParticle(particle, deltaTime));
		}
		return granularParticles;
	}

	private void removeOutsiders(List<Particle> particles) {
		Iterator<Particle> iter = particles.iterator();
		while (iter.hasNext()) {
			Particle particle = iter.next();
			FloatPoint position = particle.getPosition();
			
			if (outOfBorders(position))
			{
				iter.remove();
			}
		}
	}

	private boolean outOfBorders(FloatPoint position) {
		return position.getY() < 0 || position.getX() < 0 || position.getX() > ((SiloData) datas.get(0)).getWidth() || position.getY() > ((SiloData) datas.get(0)).getLength() ;
	}

	private List<Force<FloatPoint>> getForces(List<Particle> particles) {
		List<Force<FloatPoint>> forces = new LinkedList<Force<FloatPoint>>();

		for (Particle particle : particles) {
			FloatPoint totalForce = new FloatPoint(0, -particle.getMass() * CONSTANT_g);
			for (Particle neighbour : particle.getNeightbours()) {
				
				Double overlap = getOverlap(particle, neighbour);
								
				FloatPoint normalVersor = getNormalVersor(particle, neighbour);
				FloatPoint tangencialVersor = normalVersor.rotateRadiants(Math.PI/2);

				Double relativeVelocity = particle.getVelocity().minus(neighbour.getVelocity())
						.multiply(tangencialVersor);
				
				FloatPoint force = getForce(normalVersor, tangencialVersor, relativeVelocity, overlap);
				totalForce = totalForce.plus(force);
			}
			totalForce = wallCollision(particle, totalForce);
			forces.add(new ParticleForce(totalForce));
		}
		return forces;
	}

	public FloatPoint getForce(FloatPoint normalVersor, FloatPoint tangencialVersor, double relativeVelocity,
			double overlap) {

		FloatPoint normalForce = normalVersor.multiply(-1 * getConstantNormal() * overlap);
		FloatPoint tangencialForce = tangencialVersor
				.multiply(-1 * getConstantTangencial() * overlap * relativeVelocity);
		FloatPoint force = normalForce.plus(tangencialForce);
		return force;

	}

	private FloatPoint wallCollision(Particle particle, FloatPoint totalForce) {
		for (Wall wall : walls) {
			FloatPoint tangencialVersor = wall.getTangencialVersor();
			FloatPoint normalVersor = wall.getNormalVersor();
			
			double overlap = particle.getRadio() - Math.abs(Math.abs(particle.getPosition().multiply(normalVersor)) - Math.abs(wall.getPositionOne().multiply(normalVersor)));
			if (wall.isCollision(particle, overlap)) {
				FloatPoint force = getForce(normalVersor.multiply(-1), tangencialVersor,
						particle.getVelocity().multiply(tangencialVersor), overlap);
				totalForce = totalForce.plus(force);
			}

		}

		return totalForce;
	}

	private double getConstantTangencial() {
		return 2 * getConstantNormal();
	}

	private double getConstantNormal() {
		return Math.pow(10, 5);
	}

	private double getOverlap(Particle particle, Particle neighbour) {

		return particle.getRadio() + neighbour.getRadio() - neighbour.getPosition().minus(particle.getPosition()).abs();
	}

	private FloatPoint getNormalVersor(Particle particle, Particle neighbour) {
		FloatPoint vector = neighbour.getPosition().minus(particle.getPosition());
		return vector.divide(vector.abs());
	}

	private Route getRoute(SiloData data) {
		return new OptimizedRoute(getCellQuantity(data), false, ((SiloData) datas.get(0)).getLength());
	}

	private int getCellQuantity(SiloData data) {
		return (int) Math.ceil((data.getLength() - data.getHeigth()) / (data.getRadio() * 2)) - 1;
	}

	public void subscribe(SimulationObserver gsp) {
		subscribers.add(gsp);
	}
}
