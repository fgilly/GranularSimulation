package itba.edu.ar;

import itba.edu.ar.cellIndexMethod.data.particle.FloatPoint;
import itba.edu.ar.cellIndexMethod.data.particle.Particle;

public class Wall {
	public enum Orientation {
		Horizontal, Vertical
	};

	private FloatPoint position1;
	private FloatPoint position2;

	private FloatPoint normalVersor;
	private FloatPoint tangencialVersor;

	private double length;
	
	public Wall(FloatPoint position1, FloatPoint position2) {
		super();
		this.position1 = position1;
		this.position2 = position2;
		
		tangencialVersor=position2.minus(position1);
		length=tangencialVersor.abs();
		tangencialVersor=tangencialVersor.divide(tangencialVersor.abs());
		
		normalVersor=new FloatPoint(tangencialVersor.getY(),-tangencialVersor.getX());
		
		
	}

	public FloatPoint getNormalVersor() {
		return normalVersor;
	}

	public FloatPoint getTangencialVersor() {
		return tangencialVersor;
	}

	public double getLength() {
		return length;
	}

	
	public boolean isCollision(FloatPoint particlePosition, double overlap){
		
		double tangencialParticlePosition=particlePosition.multiply(tangencialVersor);

		return overlap > 0 && position1.multiply(tangencialVersor) <= tangencialParticlePosition && tangencialParticlePosition < position2.multiply(tangencialVersor);

	}

}
