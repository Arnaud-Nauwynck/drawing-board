package fr.an.drawingboard.model.trace2shape;

import fr.an.drawingboard.model.trace.TracePathElement;
import fr.an.drawingboard.model.trace.TracePathElement.DiscretePointsTracePathElement;

public class DiscreteTimesToAbsciss {

	public final int count;
	
	public final double[] ptToPathAbsciss;
	
//	private VariableExpr[] valueExprs;

	public DiscreteTimesToAbsciss(int count) {
		this.count = count;
		this.ptToPathAbsciss = new double[count];
	}

	public static int countPathElementPoint(TracePathElement pathElement) {
		switch(pathElement.getType()) {
		case Segment: 
			return 1;
		case DiscretePoints: 
			return ((DiscretePointsTracePathElement) pathElement).tracePtCount() - 1;
		case QuadBezier: 
			return 1;
		case CubicBezier: 
			return 1;
		default: 
			return 0; // should not occur
		}
	}
	
}
