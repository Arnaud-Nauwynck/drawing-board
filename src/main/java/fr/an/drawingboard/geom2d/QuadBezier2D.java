package fr.an.drawingboard.geom2d;

import lombok.AllArgsConstructor;

/**
 * cf https://github.com/Pomax/bezierinfo/blob/master/js/graphics-element/lib/bezierjs/bezier.js
 *
 */
@AllArgsConstructor
public class QuadBezier2D {
	public final Pt2D startPt;
	public final Pt2D controlPt;
	public final Pt2D endPt;
	
	public double eval_x(double s) {
		return evalB(s, startPt.x, controlPt.x, endPt.x);
	}
	
	public double eval_y(double s) {
		return evalB(s, startPt.y, controlPt.y, endPt.y);
	}
	
	public Pt2D eval(double s) {
		return new Pt2D(eval_x(s), eval_y(s));
	}

	public static double evalB(double s, double p0, double p1, double p2) {
		double _1s = 1.0-s;
		return _1s*_1s * p0 + 2*_1s*s * p1 + s*s * p2;
	}

}
