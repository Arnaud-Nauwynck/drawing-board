package fr.an.drawingboard.geom2d.bezier;

import fr.an.drawingboard.geom2d.CubicBezier2D;
import fr.an.drawingboard.geom2d.Pt2D;
import fr.an.drawingboard.geom2d.QuadBezier2D;

public class BezierLenUtils {

	public static double len(QuadBezier2D bezier) {
		// TOCHANGE naive implementation using N=30..
		return len_fixedN(bezier, 30);
	}

	public static double len_fixedN(QuadBezier2D bezier, int N) {
		final double ds = 1.0 / (N-1);
		double s = ds;
		double res = 0.0;
		Pt2D prevPt = new Pt2D();
		prevPt.set(bezier.startPt);
		Pt2D pt = new Pt2D();
		for(int i = 1; i < N; i++) {
			bezier.eval(pt, s);
			res += prevPt.distTo(pt);
			Pt2D tmp = pt; pt = prevPt; prevPt = tmp;
			s += ds;
		}
		return res;
	}

	public static double len(CubicBezier2D bezier) {
		// TOCHANGE naive implementation using N=30..
		return len_fixedN(bezier, 30);
	}

	public static double len_fixedN(CubicBezier2D bezier, int N) {
		final double ds = 1.0 / (N-1);
		double s = ds;
		double res = 0.0;
		Pt2D prevPt = new Pt2D();
		prevPt.set(bezier.startPt);
		Pt2D pt = new Pt2D();
		for(int i = 1; i < N; i++) {
			bezier.eval(pt, s);
			res += prevPt.distTo(pt);
			Pt2D tmp = pt; pt = prevPt; prevPt = tmp;
			s += ds;
		}
		return res;
	}
	
}
