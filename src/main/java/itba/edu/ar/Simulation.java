package itba.edu.ar;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import itba.edu.ar.cellIndexMethod.CellIndexMethod;
import itba.edu.ar.cellIndexMethod.IndexMatrix;
import itba.edu.ar.cellIndexMethod.IndexMatrixBuilder;
import itba.edu.ar.cellIndexMethod.data.particle.FloatPoint;
import itba.edu.ar.cellIndexMethod.data.particle.Particle;
import itba.edu.ar.cellIndexMethod.route.Route;
import itba.edu.ar.cellIndexMethod.route.routeImpl.OptimizedRoute;
import itba.edu.ar.input.file.CellIndexMethodFileGenerator;
import itba.edu.ar.simulation.data.ParticleForce;
import itba.edu.ar.simulation.data.SiloData;
import itba.edu.ar.ss.algorithm.Algorithm;
import itba.edu.ar.ss.model.entity.Entity;
import itba.edu.ar.ss.model.force.Force;

public class Simulation {

	private static final double CONSTANT_g = 9.8;
	
	private List<Wall> walls=new ArrayList<Wall>();
	private List<SiloData> datas = new LinkedList<SiloData>();
	private String path;
	
	public Simulation(double length,double width, double height, double diameter,double mass,String path){
		
		this.path=path;
		
		double bottomLength=(length-diameter)/2;
		
		walls.add(new Wall(new FloatPoint(0,height),new FloatPoint(0,height+length)));
		walls.add(new Wall(new FloatPoint(0,length),new FloatPoint(width,length)));
		walls.add(new Wall(new FloatPoint(width,height),new FloatPoint(width,length)));

		walls.add(new Wall(new FloatPoint(0,height),new FloatPoint(bottomLength,height)));
		walls.add(new Wall(new FloatPoint(length-bottomLength,height),new FloatPoint(width,height)));
		
		
		double particleRadio = diameter/20;
		double spawningSurface = (length - height)*width;
		int particleQuantity = (int) Math.floor(spawningSurface/Math.pow(particleRadio*2,2));
		
		datas.add(new SiloData(particleQuantity, mass, particleRadio,length,height,width));

	}
	
	
	
	
	public void simulate(Algorithm<FloatPoint> algorithm,double deltaTime,double finalTime){
	

		List<String> staticPath = new ArrayList<String>();
		List<String> dynamicPath = new ArrayList<String>();

		CellIndexMethodFileGenerator.generate(staticPath, dynamicPath, datas, path);

		IndexMatrix indexMatrix = IndexMatrixBuilder.getIndexMatrix(staticPath.get(0), dynamicPath.get(0),
				getCellQuantity((SiloData)datas.get(0)), deltaTime);

		List<Particle> particles = indexMatrix.getParticles();
		
		for (double time = 0; time < finalTime; time += deltaTime) {

			indexMatrix.clear();
			indexMatrix.addParticles(particles);

			CellIndexMethod cellIndexMethod = new CellIndexMethod(indexMatrix, getRoute(datas.get(0)),
						2*datas.get(0).getRadio());

			cellIndexMethod.execute();
			
			List<Entity<FloatPoint>> granularParticles = getGranularParticles(particles);

			algorithm.evolveSystem(granularParticles, getForces(indexMatrix.getParticles()), deltaTime);

			removeOutsiders(particles);
			
			notifyStepEnded(particles, time);

		}

		notifySimulationEnded();

	}

	private List<Entity<FloatPoint>> getGranularParticles(List<Particle> particles) {
		private List<Entity<FloatPoint>> granularParticles = new LinkedList<>();
		for(Particle particle : particles){
			granularParticles.add(new GranularParticle());
		}
		return granularParticles;
	}




	private void removeOutsiders(List<Particle> particles) {
		for(Particle particle : particles){
			
		}
	}




	private List<Force<FloatPoint>> getForces(List<Particle> particles) {
		List<Force<FloatPoint>> forces=new LinkedList<Force<FloatPoint>>();

		for(Particle particle:particles){
			FloatPoint totalForce=new FloatPoint(0,particle.getMass()*CONSTANT_g);
			for(Particle neighbour:particle.getNeightbours()){
				double overlap=getOverlap(particle,neighbour);
				FloatPoint normalVersor=getNormalVersor(particle,neighbour);
				FloatPoint tangencialVersor=new FloatPoint(-normalVersor.getY(),normalVersor.getX());
				
				double relativeVelocity=particle.getVelocity().minus(neighbour.getVelocity()).multiply(tangencialVersor);
				FloatPoint force=getForce(normalVersor,tangencialVersor,relativeVelocity,overlap);
				
				totalForce.plus(force);
			}
			totalForce = wallCollision(particle,totalForce);
			forces.add(new ParticleForce(totalForce));
		}
		return forces;
	}

	public FloatPoint getForce(FloatPoint normalVersor, FloatPoint tangencialVersor,double relativeVelocity,double overlap){
		FloatPoint normalForce=normalVersor.multiply(-1*getConstantNormal()*overlap);
		FloatPoint tangencialForce=tangencialVersor.multiply(-1*getConstantTangencial()*overlap*relativeVelocity);
		FloatPoint force=normalForce.plus(tangencialForce);
		return force;

	}

	private FloatPoint wallCollision(Particle particle, FloatPoint totalForce) {
		for(Wall wall:walls){
			FloatPoint tangencialVersor=wall.getTangencialVersor();
			FloatPoint normalVersor=wall.getNormalVersor();
			
			double overlap=particle.getRadio() - Math.abs(particle.getPosition().multiply(normalVersor));
			
			if( wall.isCollision(particle.getPosition(), overlap) ){
				FloatPoint force=getForce(normalVersor,tangencialVersor,particle.getVelocity().multiply(tangencialVersor),overlap);
				totalForce=totalForce.plus(force);
			}
			
		}
		
		return totalForce;
	}

	private double getConstantTangencial() {
		return 2*getConstantNormal();
	}

	private double getConstantNormal() {
		return Math.pow(10,5);
	}

	private double getOverlap(Particle particle, Particle neighbour) {
		
		return particle.getRadio()+neighbour.getRadio()-neighbour.getPosition().minus(particle.getPosition()).abs();
	}

	private FloatPoint getNormalVersor(Particle particle, Particle neighbour) {
		FloatPoint vector = neighbour.getPosition().minus(particle.getPosition());
		return vector.divide(vector.abs());
	}

	private Route getRoute(SiloData data) {
		return new OptimizedRoute(getCellQuantity(data), false, getLength());
	}

	private int getCellQuantity(SiloData data) {
		return (int) Math.ceil((data.getLength()-data.getHeigth()) / (data.getRadio()*2)) - 1;
	}
}
