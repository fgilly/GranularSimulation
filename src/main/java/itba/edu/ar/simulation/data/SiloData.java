package itba.edu.ar.simulation.data;

import itba.edu.ar.cellIndexMethod.data.particle.FloatPoint;
import itba.edu.ar.input.file.data.Data;

public class SiloData extends Data {

	private double heigth;
	private double length;
	private double width;

	public SiloData(int particleQuantity, double mass, double radio, double length, double height, double width) {
		super(particleQuantity, mass, radio);
		this.length = length;
		this.heigth = height;
		this.width = width;
	}

	@Override
	public FloatPoint getPosition() {
		double x = Math.random() * width;
		double y = (length - heigth) * Math.random() + heigth;
		return new FloatPoint(x, y);
	}

	public double getHeigth() {
		return heigth;
	}

	public double getLength() {
		return length;
	}

	public double getWidth() {
		return width;
	}
	
	

}
