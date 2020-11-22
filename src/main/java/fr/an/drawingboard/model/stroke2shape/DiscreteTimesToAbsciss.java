package fr.an.drawingboard.model.stroke2shape;

import fr.an.drawingboard.model.expr.Expr.VariableExpr;
import fr.an.drawingboard.model.trace.TraceStrokePathElement;
import fr.an.drawingboard.model.trace.TraceStrokePathElement.DiscretePointsTraceStrokePathElement;

public class DiscreteTimesToAbsciss {

	public final int count;
	public final double[] values;
	
	public VariableExpr[] valueExprs;

	public DiscreteTimesToAbsciss(int count) {
		this.count = count;
		this.values = new double[count];
	}
	
	public static int countPathElementPoint(TraceStrokePathElement pathElement) {
		switch(pathElement.getType()) {
		case Segment: 
			return 1;
		case DiscretePoints: 
			return ((DiscretePointsTraceStrokePathElement) pathElement).tracePtCount() - 1;
		case QuadBezier: 
			return 1;
		case CubicBezier: 
			return 1;
		default: 
			return 0; // should not occur
		}
	}

}
