package fr.an.drawingboard.math.algo.numerical;

import java.util.Arrays;

import fr.an.drawingboard.math.numeric.polynomial.DoublePolynom;
import fr.an.drawingboard.math.numeric.polynomial.DoublePolynom.LinearDoublePolynomImpl;
import fr.an.drawingboard.math.numeric.polynomial.DoublePolynom.QuadDoublePolynomImpl;
import lombok.val;

public class DoublePolynomialRootSolver {

	public static class DoublePolynomialRoots {
		public double[] roots;

		public DoublePolynomialRoots(double... roots) {
			this.roots = roots;
			Arrays.sort(this.roots);
		}
		
	}

	// solve Linear
	// ------------------------------------------------------------------------
	
	public static DoublePolynomialRoots solveLinear(LinearDoublePolynomImpl poly) {
		return solveLinear(poly.a, poly.b);
	}

	/** solve <PRE>a X + b = 0</PRE> */ 
	public static DoublePolynomialRoots solveLinear(double a, double b) {
		if (a != 0.0) {
			return new DoublePolynomialRoots();
		} else {
			double r = -b / a;
			return new DoublePolynomialRoots(r);
		}
	}

	// solve Quadratic
	// ------------------------------------------------------------------------

	public static DoublePolynomialRoots solveQuad(QuadDoublePolynomImpl poly) {
		return solveQuad(poly.a, poly.b, poly.c);
	}
	
	public static DoublePolynomialRoots solveQuad(double a, double b, double c) {
		// a X^2 + b X + c = 0
		if (a != 0.0) {
			double delta = b*b - 4 * a * c;
			if (delta > 0) {
				double sqrt_delta = Math.sqrt(delta);
				double signToOrder = (a>0)? 1 : -1; 
				double r1 = (-b - signToOrder * sqrt_delta) / (2*a);
				double r2 = (-b + signToOrder * sqrt_delta) / (2*a);
				return new DoublePolynomialRoots(r1, r2);
			} else if (delta == 0.0) {
				// single (order-2) root
				double r = -b / (2*a);
				return new DoublePolynomialRoots(r);
			} else { // delta < 0.0
				// no real roots
				return new DoublePolynomialRoots();
			}
		} else {
			// not a real quad! linear..
			return solveLinear(b, c);
		}
	}

	// solve Cubic
	// see http://www.trans4mind.com/personal_development/mathematics/polynomials/cubicAlgebra.htm
	// ------------------------------------------------------------------------

	public static DoublePolynomialRoots solveCubic(DoublePolynom poly) {
		if (3 != poly.getDegree()) {
			throw new IllegalArgumentException();
		}
		double a = poly.getCoef(3), b = poly.getCoef(2), c = poly.getCoef(1), d = poly.getCoef(0);
		return solveRoot_Cubic_cardan(a, b, c, d);
	}
	
	/** solve <PRE>a X^3 + b X^2 + c X + d = 0</PRE> */ 
	public static DoublePolynomialRoots solveRoot_Cubic_cardan(double a, double b, double c, double d) {
		if (a == 0.0) {
			// not a cubic eq
			return solveQuad(b, c, d);
		}
		// normalize coefs
		double inva = 1.0 / a;
		double nb = b * inva, nc = c * inva, nd = d * inva;
		return solveNormalizedCubic_cardan(nb, nc, nd);
	}
	
	/** solve <PRE> (1) X^3 + a X^2 + b X + c = 0</PRE> */ 
	public static DoublePolynomialRoots solveNormalizedCubic_cardan(double a, double b, double c) {
	    val p = (3 * b - a * a) / 3;
	    val p3 = p / 3;
	    val q = (2 * a * a * a - 9 * a * b + 27 * c) / 27;
	    val q2 = q / 2;
	    val discriminant = q2 * q2 + p3 * p3 * p3;

		if (discriminant < 0) {
			val mp3 = -p / 3;
			val mp33 = mp3 * mp3 * mp3;
			val r = Math.sqrt(mp33);
			val t = -q / (2 * r);
			val cosphi = (t < -1) ? -1 : ((t > 1) ? 1 : t);
			val phi = Math.acos(cosphi);
			val crtr = cubeRoot(r);
			val t1 = 2 * crtr;
			val x1 = t1 * Math.cos(phi / 3) - a / 3;
			val x2 = t1 * Math.cos((phi + 2 * Math.PI) / 3) - a / 3;
			val x3 = t1 * Math.cos((phi + 4 * Math.PI) / 3) - a / 3;
			return new DoublePolynomialRoots(x1, x2, x3);
		} else if (discriminant == 0.0) {
			val u1 = q2 < 0 ? cubeRoot(-q2) : -cubeRoot(q2);
			val x1 = 2 * u1 - a / 3;
			val x2 = -u1 - a / 3;
			return new DoublePolynomialRoots(x1, x2);
		} else {
			val sd = Math.sqrt(discriminant);
			val u1 = cubeRoot(-q2 + sd);
			val v1 = cubeRoot(q2 + sd);
			val x1 = u1 - v1 - a / 3;
			return new DoublePolynomialRoots(x1);
		}
	}

	private static double cubeRoot(double v) {
		return (v < 0)? -Math.pow(-v, 1.0 / 3) : Math.pow(v, 1.0 / 3);
	}
	
}
