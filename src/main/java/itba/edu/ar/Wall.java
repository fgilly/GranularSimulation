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

		tangencialVersor = position2.minus(position1).getVersor();

		if (tangencialVersor.getX() == 0) {
			if (position1.getX() == 0) {
				normalVersor = tangencialVersor.rotateRadiants(-Math.PI / 2);
			} else {
				normalVersor = tangencialVersor.rotateRadiants(+Math.PI / 2);
			}
		} else if (tangencialVersor.getY() == 0) {
			if (position1.getY() == 5) {
				normalVersor = tangencialVersor.rotateRadiants(+Math.PI / 2);
			} else {
				normalVersor = tangencialVersor.rotateRadiants(-Math.PI / 2);
			}
		}
	}

	public FloatPoint getPositionOne() {
		return position1;
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

	public boolean isCollision(Particle particle, double overlap) {

		FloatPoint particlePosition = particle.getPosition();
		
		double tangencialParticlePosition = particlePosition.multiply(tangencialVersor);
		if(position1.getX()==0 &&position1.getY()==10){
		if (particlePosition.getX() >= 4.9 && particlePosition.getX() < 5 ) {
			System.out.println("TANG" + tangencialVersor);
			
			System.out.println(position1 +"-"+position2);
			System.out.println(particle);
			System.out.println("particle: " + tangencialParticlePosition);
			System.out.println("wall 1 " + position1.multiply(tangencialVersor));
			System.out.println("wall 2 " + position2.multiply(tangencialVersor));
			System.out.println("overlap: " + overlap);
		}}
		return overlap > 0 && position1.multiply(tangencialVersor) <= tangencialParticlePosition
				&& tangencialParticlePosition < position2.multiply(tangencialVersor);

	}

}
