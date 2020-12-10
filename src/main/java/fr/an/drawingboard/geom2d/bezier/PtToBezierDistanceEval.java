package fr.an.drawingboard.geom2d.bezier;

import fr.an.drawingboard.geom2d.CubicBezier2D;
import fr.an.drawingboard.geom2d.Pt2D;
import fr.an.drawingboard.geom2d.QuadBezier2D;
import fr.an.drawingboard.math.numeric.polynomial.DoublePolynom;

/**
 *
 */
public class PtToBezierDistanceEval {

	// Quadratic Bezier to Pt distance
	// ------------------------------------------------------------------------
	
	public static double squareDistPtToQuadBezier(double s, //
			double m_x, double m_y, //
			double p0_x, double p0_y, double p1_x, double p1_y, double p2_x, double p2_y) {
		double dx = m_x - QuadBezier2D.evalB(s, p0_x, p1_x, p2_x);
		double dy = m_y - QuadBezier2D.evalB(s, p0_y, p1_y, p2_y);
		return dx*dx + dy*dy;
	}

	/**
	 * square distance between point M(m_x,m_y) to point B(s) on QuadBezier at param 's' in [0,1]
	 */
	public static DoublePolynom squareDistPtToQuadBezier_asPolynomOnS(
			Pt2D m, QuadBezier2D quadBezier) {
		return squareDistPtToQuadBezier_asPolynomOnS(m, quadBezier.startPt, quadBezier.controlPt, quadBezier.endPt);
	}

	/**
	 * square distance between point M(m_x,m_y) to point B(s) on QuadBezier(p0_x,p0_y.. p2_x,p2_y) at param 's' in [0,1]
	 */
	public static DoublePolynom squareDistPtToQuadBezier_asPolynomOnS(
			Pt2D m, Pt2D startPt, Pt2D controlPt, Pt2D endPt) {
		return squareDistPtToQuadBezier_asPolynomOnS(m.x, m.y, startPt.x, startPt.y, controlPt.x, controlPt.y, endPt.x, endPt.y);
	}
	
	/**
	 * alg computation using sagemath:
	 * compute E=square distance between point M(m_x,m_y) to point B(s) on QuadBezier(p0_x,p0_y.. p2_x,p2_y) at param 's' in [0,1]
	 * => explain as a polynom in degree 4 over 's', with parameters depending on pts + bezier control points
	 * <PRE>
	 * s, m_x, m_y, p0_x, p0_y, p1_x, p1_y, p2_x, p2_y = var('s m_x m_y p0_x p0_y p1_x p1_y p2_x p2_y')
	 * QuadBezier(s, a, b, c) = (1-s)^2 *a + 2*(1-s)*s *b + s^2 *c
	 * E = (m_x-QuadBezier(s, p0_x, p1_x, p2_x))^2 + (m_y-QuadBezier(s, p0_y, p1_y, p2_y))^2
	 * expandE = expand(E).simplify_full()
	 * expandE.coefficients(s)
	 * 
	 * => 
	 * (p0_x^2 + p0_y^2 - 4*p0_x*p1_x + 4*p1_x^2 - 4*p0_y*p1_y + 4*p1_y^2 + 2*(p0_x - 2*p1_x)*p2_x + p2_x^2 + 2*(p0_y - 2*p1_y)*p2_y + p2_y^2)
	 * 		*s^4
	 * - 4*(p0_x^2 + p0_y^2 - 3*p0_x*p1_x + 2*p1_x^2 - 3*p0_y*p1_y + 2*p1_y^2 + (p0_x - p1_x)*p2_x + (p0_y - p1_y)*p2_y)
	 * 		*s^3
	 * - 2*(m_x*p0_x - 3*p0_x^2 + m_y*p0_y - 3*p0_y^2 - 2*(m_x - 3*p0_x)*p1_x - 2*p1_x^2 - 2*(m_y - 3*p0_y)*p1_y - 2*p1_y^2 + (m_x - p0_x)*p2_x + (m_y - p0_y)*p2_y)
	 * 		*s^2
	 *  + 4*(m_x*p0_x - p0_x^2 + m_y*p0_y - p0_y^2 - (m_x - p0_x)*p1_x - (m_y - p0_y)*p1_y)
	 *  	*s
	 *  + m_x^2 + m_y^2 - 2*m_x*p0_x + p0_x^2 - 2*m_y*p0_y + p0_y^2 
	 * 
	 * 
	 * .. no code gen in java, but some in C
	 * from sympy.utilities.codegen import codegen
	 * codegen(("f", expandE), "C")[0][1]
	 * ...
	 * </PRE>
	 */
	public static DoublePolynom squareDistPtToQuadBezier_asPolynomOnS(
			double m_x, double m_y, //
			double p0_x, double p0_y, double p1_x, double p1_y, double p2_x, double p2_y
			) {
		double coef4 = p0_x*p0_x + p0_y*p0_y  //
				- 4*p0_x*p1_x + 4*p1_x*p1_x //
				- 4*p0_y*p1_y + 4*p1_y*p1_y //
				+ 2*(p0_x - 2*p1_x)*p2_x + p2_x*p2_x + 2*(p0_y - 2*p1_y)*p2_y + p2_y*p2_y;
		double coef3 = - 4*( //
				p0_x*p0_x //
				+ p0_y*p0_y //
				- 3*p0_x*p1_x + 2*p1_x*p1_x //
				- 3*p0_y*p1_y + 2*p1_y*p1_y //
				+ (p0_x - p1_x)*p2_x //
				+ (p0_y - p1_y)*p2_y
				);
		double coef2 = - 2*( //
				m_x*p0_x - 3*p0_x*p0_x //
				+ m_y*p0_y - 3*p0_y*p0_y //
				- 2*(m_x - 3*p0_x)*p1_x - 2*p1_x*p1_x //
				- 2*(m_y - 3*p0_y)*p1_y - 2*p1_y*p1_y //
				+ (m_x - p0_x)*p2_x //
				+ (m_y - p0_y)*p2_y)
				;
		double coef1 = 4*(m_x*p0_x - p0_x*p0_x + m_y*p0_y - p0_y*p0_y - (m_x - p0_x)*p1_x - (m_y - p0_y)*p1_y);
		double coef0 = m_x*m_x + m_y*m_y - 2*m_x*p0_x + p0_x*p0_x - 2*m_y*p0_y + p0_y*p0_y;
		return DoublePolynom.create(new double[] { coef0, coef1, coef2, coef3, coef4 });
	}

	// ------------------------------------------------------------------------

	public static double squareDistPtToCubicBezier(double s, //
			double m_x, double m_y, //
			double p0_x, double p0_y, double p1_x, double p1_y, double p2_x, double p2_y, double p3_x, double p3_y) {
		double dx = m_x - CubicBezier2D.evalB(s, p0_x, p1_x, p2_x, p3_x);
		double dy = m_y - CubicBezier2D.evalB(s, p0_y, p1_y, p2_y, p3_y);
		return dx*dx + dy*dy;
	}
	
	/**
	 * square distance between point M(m_x,m_y) to point B(s) on CubicBezier at param 's' in [0,1]
	 */
	public static DoublePolynom squareDistPtToCubicBezier_asPolynomOnS(
			Pt2D m, CubicBezier2D to) {
		return squareDistPtToCubicBezier_asPolynomOnS(m, to.startPt, to.p1, to.p2, to.endPt);
	}

	public static DoublePolynom squareDistPtToCubicBezier_asPolynomOnS(
			Pt2D m, Pt2D startPt, Pt2D p1, Pt2D p2, Pt2D endPt) {
		return squareDistPtToCubicBezier_asPolynomOnS(m.x, m.y, startPt.x, startPt.y, p1.x, p1.y, p2.x, p2.y, endPt.x, endPt.y);
	}
	
	/**
	 * square distance between point M(m_x,m_y) to point B(s) on CubicBezier(p0_x,p0_y.. p3_x,p3_y) at param 's' in [0,1]
	 * => explain as a polynom in degree 6 over 's', with parameters depending on pts + bezier control points
	 * 
	 * alg computation using sagemath:
	 * <PRE>
	 * s, m_x, m_y, p0_x, p0_y, p1_x, p1_y, p2_x, p2_y, p3_x, p3_y = var('s m_x m_y p0_x p0_y p1_x p1_y p2_x p2_y p3_x p3_y ')
	 * CubicBezier(s, a, b, c, d) = (1-s)^3 *a + 3*(1-s)^2*s *b + 3*(1-s)*s^2 *c + s^3 * d
	 * E3 = (m_x-CubicBezier(s, p0_x, p1_x, p2_x, p3_x))^2 + (m_y-CubicBezier(s, p0_y, p1_y, p2_y, p3_y))^2
	 * expandE3 = expand(E3).simplify_full()
	 * expandE3.coefficients(s)
	 * => 
	 * (p0_x^2 + p0_y^2 - 6*p0_x*p1_x + 9*p1_x^2 - 6*p0_y*p1_y + 9*p1_y^2 + 6*(p0_x - 3*p1_x)*p2_x + 9*p2_x^2 + 6*(p0_y - 3*p1_y)*p2_y + 9*p2_y^2 - 2*(p0_x - 3*p1_x + 3*p2_x)*p3_x + p3_x^2 - 2*(p0_y - 3*p1_y + 3*p2_y)*p3_y + p3_y^2
	 * )*s^6 
	 * - 6*(p0_x^2 + p0_y^2 - 5*p0_x*p1_x + 6*p1_x^2 - 5*p0_y*p1_y + 6*p1_y^2 + (4*p0_x - 9*p1_x)*p2_x + 3*p2_x^2 + (4*p0_y - 9*p1_y)*p2_y + 3*p2_y^2 - (p0_x - 2*p1_x + p2_x)*p3_x - (p0_y - 2*p1_y + p2_y)*p3_y
	 * 		)*s^5
	 *  + 3*(5*p0_x^2 + 5*p0_y^2 - 20*p0_x*p1_x + 18*p1_x^2 - 20*p0_y*p1_y + 18*p1_y^2 + 6*(2*p0_x - 3*p1_x)*p2_x + 3*p2_x^2 + 6*(2*p0_y - 3*p1_y)*p2_y + 3*p2_y^2 - 2*(p0_x - p1_x)*p3_x - 2*(p0_y - p1_y)*p3_y
	 *  	)*s^4 
	 *  + 2*(m_x*p0_x - 10*p0_x^2 + m_y*p0_y - 10*p0_y^2 - 3*(m_x - 10*p0_x)*p1_x - 18*p1_x^2 - 3*(m_y - 10*p0_y)*p1_y - 18*p1_y^2 + 3*(m_x - 4*p0_x + 3*p1_x)*p2_x + 3*(m_y - 4*p0_y + 3*p1_y)*p2_y - (m_x - p0_x)*p3_x - (m_y - p0_y)*p3_y
	 *  	)*s^3
	 *  - 3*(2*m_x*p0_x - 5*p0_x^2 + 2*m_y*p0_y - 5*p0_y^2 - 2*(2*m_x - 5*p0_x)*p1_x - 3*p1_x^2 - 2*(2*m_y - 5*p0_y)*p1_y - 3*p1_y^2 + 2*(m_x - p0_x)*p2_x + 2*(m_y - p0_y)*p2_y
	 *  	)*s^2
	 *  + 6*(m_x*p0_x - p0_x^2 + m_y*p0_y - p0_y^2 - (m_x - p0_x)*p1_x - (m_y - p0_y)*p1_y
	 *  	)*s
	 *  + m_x^2 + m_y^2 - 2*m_x*p0_x + p0_x^2 - 2*m_y*p0_y + p0_y^2
	 */
	public static DoublePolynom squareDistPtToCubicBezier_asPolynomOnS(
			double m_x, double m_y, //
			double p0_x, double p0_y, double p1_x, double p1_y, double p2_x, double p2_y, double p3_x, double p3_y
			) {	
		double p0_x2 = p0_x*p0_x, p0_y2 = p0_y*p0_y;
		double p1_x2 = p1_x*p1_x, p1_y2 = p1_y*p1_y;
		double p2_x2 = p2_x*p2_x, p2_y2 = p2_y*p2_y;
		double p3_x2 = p3_x*p3_x, p3_y2 = p3_y*p3_y;
		double m_x2 = m_x*m_x, m_y2 = m_y*m_y;
		double coef6 = (p0_x2 + p0_y2 //
				- 6*p0_x*p1_x + 9*p1_x*p1_x //
				- 6*p0_y*p1_y + 9*p1_y*p1_y //
				+ 6*(p0_x - 3*p1_x)*p2_x + 9*p2_x2 //
				+ 6*(p0_y - 3*p1_y)*p2_y + 9*p2_y2 //
				- 2*(p0_x - 3*p1_x + 3*p2_x)*p3_x + p3_x2 //
				- 2*(p0_y - 3*p1_y + 3*p2_y)*p3_y + p3_y2 //
				 );
		double coef5 = - 6*( //
				p0_x2 + p0_y2 //
				- 5*p0_x*p1_x + 6*p1_x2 //
				- 5*p0_y*p1_y + 6*p1_y2 //
				+ (4*p0_x - 9*p1_x)*p2_x + 3*p2_x2 //
				+ (4*p0_y - 9*p1_y)*p2_y + 3*p2_y2 //
				- (p0_x - 2*p1_x + p2_x)*p3_x //
				- (p0_y - 2*p1_y + p2_y)*p3_y
				);
		double coef4 = 3*(//
				5*p0_x2 + 5*p0_y2 //
				- 20*p0_x*p1_x + 18*p1_x2 //
				- 20*p0_y*p1_y + 18*p1_y2 //
				+ 6*(2*p0_x - 3*p1_x)*p2_x + 3*p2_x2 //
				+ 6*(2*p0_y - 3*p1_y)*p2_y + 3*p2_y2 //
				- 2*(p0_x - p1_x)*p3_x //
				- 2*(p0_y - p1_y)*p3_y
				 );
		double coef3 = 2*( //
				  m_x*p0_x - 10*p0_x2 //
				+ m_y*p0_y - 10*p0_y2 //
				- 3*(m_x - 10*p0_x)*p1_x //
				- 3*(m_y - 10*p0_y)*p1_y //
				- 18*p1_x2 //
				- 18*p1_y2 //
				+ 3*(m_x - 4*p0_x + 3*p1_x)*p2_x //
				+ 3*(m_y - 4*p0_y + 3*p1_y)*p2_y //
				- (m_x - p0_x)*p3_x //
				- (m_y - p0_y)*p3_y //
				 );
		double coef2 = - 3*(//
				  2*m_x*p0_x //
				+ 2*m_y*p0_y //
				- 5*p0_x2 //
				- 5*p0_y2 //
				- 2*(2*m_x - 5*p0_x)*p1_x //
				- 2*(2*m_y - 5*p0_y)*p1_y //
				- 3*p1_x2 //
				- 3*p1_y2 //
				+ 2*(m_x - p0_x)*p2_x //
				+ 2*(m_y - p0_y)*p2_y
				);
		double coef1 = 6*(//
				m_x*p0_x //
				- p0_x2 //
				+ m_y*p0_y //
				- p0_y2 //
				- (m_x - p0_x)*p1_x //
				- (m_y - p0_y)*p1_y
				 );
		double coef0 = m_x2 + m_y2 //
				- 2*m_x*p0_x //
				- 2*m_y*p0_y //
				+ p0_x2 //
				+ p0_y2;
		return DoublePolynom.create(new double[] { coef0, coef1, coef2, coef3, coef4, coef5, coef6 });
	}

	public static class IterStep {
		public double nextS;
		public double nextSquareDist;
	}

	/**
	 * Newtown iterative step optimization s0 -> s=s0+ds .. for optimizing "a ds + b + o(ds)"
	 * using Taylor developmeent at order 1 around s=s0+ds
	 */
	public static boolean iterativeStep_minSquareDistPtToCubicBezier_Newtown_onS0(
			IterStep stepResult,
			double s0, double prevSquareDist,
			double m_x, double m_y, //
			double p0_x, double p0_y, double p1_x, double p1_y, double p2_x, double p2_y, double p3_x, double p3_y
			) {
		double s02 = s0*s0, s03 = s02*s0, s04 = s03*s0, s05 = s04*s0, s06 = s05*s0;
		double p0_x2 = p0_x*p0_x, p0_y2 = p0_y*p0_y;
		double p1_x2 = p1_x*p1_x, p1_y2 = p1_y*p1_y;
		double p2_x2 = p2_x*p2_x, p2_y2 = p2_y*p2_y;
		double p3_x2 = p3_x*p3_x, p3_y2 = p3_y*p3_y;
		double m_x2 = m_x*m_x, m_y2 = m_y*m_y;

		double coef1 = 6*p0_x2*s05 + 6*p0_y2*s05 - 36*p0_x*p1_x*s05 + 54*p1_x2*s05 - 36*p0_y*p1_y*s05 + 54*p1_y2*s05 + 36*p0_x*p2_x*s05 - 108*p1_x*p2_x*s05 + 54*p2_x2*s05 + 36*p0_y*p2_y*s05 - 108*p1_y*p2_y*s05 + 54*p2_y2*s05 - 12*p0_x*p3_x*s05 + 36*p1_x*p3_x*s05 - 36*p2_x*p3_x*s05 + 6*p3_x2*s05 - 12*p0_y*p3_y*s05 + 36*p1_y*p3_y*s05 - 36*p2_y*p3_y*s05 + 6*p3_y2*s05 - 30*p0_x2*s04 - 30*p0_y2*s04 + 150*p0_x*p1_x*s04 - 180*p1_x2*s04 + 150*p0_y*p1_y*s04 - 180*p1_y2*s04 - 120*p0_x*p2_x*s04 + 270*p1_x*p2_x*s04 - 90*p2_x2*s04 - 120*p0_y*p2_y*s04 + 270*p1_y*p2_y*s04 - 90*p2_y2*s04 + 30*p0_x*p3_x*s04 - 60*p1_x*p3_x*s04 + 30*p2_x*p3_x*s04 + 30*p0_y*p3_y*s04 - 60*p1_y*p3_y*s04 + 30*p2_y*p3_y*s04 - 180*p0_x2*s03 - 180*p0_y2*s03 + 1200*p0_x*p1_x*s03 - 1944*p1_x2*s03 + 1200*p0_y*p1_y*s03 - 1944*p1_y2*s03 - 1296*p0_x*p2_x*s03 + 4104*p1_x*p2_x*s03 - 2124*p2_x2*s03 - 1296*p0_y*p2_y*s03 + 4104*p1_y*p2_y*s03 - 2124*p2_y2*s03 + 456*p0_x*p3_x*s03 - 1416*p1_x*p3_x*s03 + 1440*p2_x*p3_x*s03 - 240*p3_x2*s03 + 456*p0_y*p3_y*s03 - 1416*p1_y*p3_y*s03 + 1440*p2_y*p3_y*s03 - 240*p3_y2*s03 + 6*m_x*p0_x*s02 - 300*p0_x2*s02 + 6*m_y*p0_y*s02 - 300*p0_y2*s02 - 18*m_x*p1_x*s02 + 2340*p0_x*p1_x*s02 - 4428*p1_x2*s02 - 18*m_y*p1_y*s02 + 2340*p0_y*p1_y*s02 - 4428*p1_y2*s02 + 18*m_x*p2_x*s02 - 2952*p0_x*p2_x*s02 + 10854*p1_x*p2_x*s02 - 6480*p2_x2*s02 + 18*m_y*p2_y*s02 - 2952*p0_y*p2_y*s02 + 10854*p1_y*p2_y*s02 - 6480*p2_y2*s02 - 6*m_x*p3_x*s02 + 1206*p0_x*p3_x*s02 - 4320*p1_x*p3_x*s02 + 5040*p2_x*p3_x*s02 - 960*p3_x2*s02 - 6*m_y*p3_y*s02 + 1206*p0_y*p3_y*s02 - 4320*p1_y*p3_y*s02 + 5040*p2_y*p3_y*s02 - 960*p3_y2*s02 - 12*m_x*p0_x*s0 - 210*p0_x2*s0 - 12*m_y*p0_y*s0 - 210*p0_y2*s0 + 24*m_x*p1_x*s0 + 1860*p0_x*p1_x*s0 - 4014*p1_x2*s0 + 24*m_y*p1_y*s0 + 1860*p0_y*p1_y*s0 - 4014*p1_y2*s0 - 12*m_x*p2_x*s0 - 2676*p0_x*p2_x*s0 + 11232*p1_x*p2_x*s0 - 7632*p2_x2*s0 - 12*m_y*p2_y*s0 - 2676*p0_y*p2_y*s0 + 11232*p1_y*p2_y*s0 - 7632*p2_y2*s0 + 1248*p0_x*p3_x*s0 - 5088*p1_x*p3_x*s0 + 6720*p2_x*p3_x*s0 - 1440*p3_x2*s0 + 1248*p0_y*p3_y*s0 - 5088*p1_y*p3_y*s0 + 6720*p2_y*p3_y*s0 - 1440*p3_y2*s0 - 18*m_x*p0_x - 54*p0_x2 - 18*m_y*p0_y - 54*p0_y2 + 66*m_x*p1_x + 534*p0_x*p1_x - 1296*p1_x2 + 66*m_y*p1_y + 534*p0_y*p1_y - 1296*p1_y2 - 72*m_x*p2_x - 864*p0_x*p2_x + 4104*p1_x*p2_x - 3168*p2_x2 - 72*m_y*p2_y - 864*p0_y*p2_y + 4104*p1_y*p2_y - 3168*p2_y2 + 24*m_x*p3_x + 456*p0_x*p3_x - 2112*p1_x*p3_x + 3168*p2_x*p3_x - 768*p3_x2 + 24*m_y*p3_y + 456*p0_y*p3_y - 2112*p1_y*p3_y + 3168*p2_y*p3_y - 768*p3_y2;
		if (coef1 != 0.0) {
			double coef0 = -6*p0_x2*s05 - 6*p0_y2*s05 + 30*p0_x*p1_x*s05 - 36*p1_x2*s05 + 30*p0_y*p1_y*s05 - 36*p1_y2*s05 - 24*p0_x*p2_x*s05 + 54*p1_x*p2_x*s05 - 18*p2_x2*s05 - 24*p0_y*p2_y*s05 + 54*p1_y*p2_y*s05 - 18*p2_y2*s05 + 6*p0_x*p3_x*s05 - 12*p1_x*p3_x*s05 + 6*p2_x*p3_x*s05 + 6*p0_y*p3_y*s05 - 12*p1_y*p3_y*s05 + 6*p2_y*p3_y*s05 + (p0_x2 + p0_y2 - 6*p0_x*p1_x + 9*p1_x2 - 6*p0_y*p1_y + 9*p1_y2 + 6*(p0_x - 3*p1_x)*p2_x + 9*p2_x2 + 6*(p0_y - 3*p1_y)*p2_y + 9*p2_y2 - 2*(p0_x - 3*p1_x + 3*p2_x)*p3_x + p3_x2 - 2*(p0_y - 3*p1_y + 3*p2_y)*p3_y + p3_y2)*s06 + 15*p0_x2*s04 + 15*p0_y2*s04 - 60*p0_x*p1_x*s04 + 54*p1_x2*s04 - 60*p0_y*p1_y*s04 + 54*p1_y2*s04 + 36*p0_x*p2_x*s04 - 54*p1_x*p2_x*s04 + 9*p2_x2*s04 + 36*p0_y*p2_y*s04 - 54*p1_y*p2_y*s04 + 9*p2_y2*s04 - 6*p0_x*p3_x*s04 + 6*p1_x*p3_x*s04 - 6*p0_y*p3_y*s04 + 6*p1_y*p3_y*s04 + 2*m_x*p0_x*s03 + 140*p0_x2*s03 + 2*m_y*p0_y*s03 + 140*p0_y2*s03 - 6*m_x*p1_x*s03 - 900*p0_x*p1_x*s03 + 1404*p1_x2*s03 - 6*m_y*p1_y*s03 - 900*p0_y*p1_y*s03 + 1404*p1_y2*s03 + 6*m_x*p2_x*s03 + 936*p0_x*p2_x*s03 - 2862*p1_x*p2_x*s03 + 1440*p2_x2*s03 + 6*m_y*p2_y*s03 + 936*p0_y*p2_y*s03 - 2862*p1_y*p2_y*s03 + 1440*p2_y2*s03 - 2*m_x*p3_x*s03 - 318*p0_x*p3_x*s03 + 960*p1_x*p3_x*s03 - 960*p2_x*p3_x*s03 + 160*p3_x2*s03 - 2*m_y*p3_y*s03 - 318*p0_y*p3_y*s03 + 960*p1_y*p3_y*s03 - 960*p2_y*p3_y*s03 + 160*p3_y2*s03 - 6*m_x*p0_x*s02 + 255*p0_x2*s02 - 6*m_y*p0_y*s02 + 255*p0_y2*s02 + 12*m_x*p1_x*s02 - 1950*p0_x*p1_x*s02 + 3609*p1_x2*s02 + 12*m_y*p1_y*s02 - 1950*p0_y*p1_y*s02 + 3609*p1_y2*s02 - 6*m_x*p2_x*s02 + 2406*p0_x*p2_x*s02 - 8640*p1_x*p2_x*s02 + 5040*p2_x2*s02 - 6*m_y*p2_y*s02 + 2406*p0_y*p2_y*s02 - 8640*p1_y*p2_y*s02 + 5040*p2_y2*s02 - 960*p0_x*p3_x*s02 + 3360*p1_x*p3_x*s02 - 3840*p2_x*p3_x*s02 + 720*p3_x2*s02 - 960*p0_y*p3_y*s02 + 3360*p1_y*p3_y*s02 - 3840*p2_y*p3_y*s02 + 720*p3_y2*s02 + 6*m_x*p0_x*s0 + 186*p0_x2*s0 + 6*m_y*p0_y*s0 + 186*p0_y2*s0 - 6*m_x*p1_x*s0 - 1626*p0_x*p1_x*s0 + 3456*p1_x2*s0 - 6*m_y*p1_y*s0 - 1626*p0_y*p1_y*s0 + 3456*p1_y2*s0 + 2304*p0_x*p2_x*s0 - 9504*p1_x*p2_x*s0 + 6336*p2_x2*s0 + 2304*p0_y*p2_y*s0 - 9504*p1_y*p2_y*s0 + 6336*p2_y2*s0 - 1056*p0_x*p3_x*s0 + 4224*p1_x*p3_x*s0 - 5472*p2_x*p3_x*s0 + 1152*p3_x2*s0 - 1056*p0_y*p3_y*s0 + 4224*p1_y*p3_y*s0 - 5472*p2_y*p3_y*s0 + 1152*p3_y2*s0 + m_x2 + m_y2 + 14*m_x*p0_x + 49*p0_x2 + 14*m_y*p0_y + 49*p0_y2 - 48*m_x*p1_x - 480*p0_x*p1_x + 1152*p1_x2 - 48*m_y*p1_y - 480*p0_y*p1_y + 1152*p1_y2 + 48*m_x*p2_x + 768*p0_x*p2_x - 3600*p1_x*p2_x + 2736*p2_x2 + 48*m_y*p2_y + 768*p0_y*p2_y - 3600*p1_y*p2_y + 2736*p2_y2 - 16*m_x*p3_x - 400*p0_x*p3_x + 1824*p1_x*p3_x - 2688*p2_x*p3_x + 640*p3_x2 - 16*m_y*p3_y - 400*p0_y*p3_y + 1824*p1_y*p3_y - 2688*p2_y*p3_y + 640*p3_y2;
			double ds = - coef0 / coef1;
			double nextS = s0 + ds;
			double nextSquareDist = squareDistPtToCubicBezier(nextS, m_x, m_y, p0_x, p0_y, p1_x, p1_y, p2_x, p2_y, p3_x, p3_y);
			if (nextSquareDist < prevSquareDist) {
				stepResult.nextS = nextS;
				stepResult.nextSquareDist = nextSquareDist;
				return true;
			} else {
				// linear step, but maybe taylor 1 degree approximation is not precise enough around point s0 .. so optim step would be wrong
				return false;
			}
		} else {
			// b=0 .. taylor 1 approx => need coef2+coef3..
			return false;
		}
	}
	
	/**
	 * iterative step optimization s0 -> s=s0+ds .. for optimizing min "a ds^2 + b ds + c + o(ds^2)"
	 * using Taylor development at order 2 around s=s0+ds, 
	 * of square distance between point M(m_x,m_y) to point B(s0+ds) on CubicBezier(p0_x,p0_y.. p3_x,p3_y)
	 * => retain only degree 2 coefs over 'ds', with parameters depending on pts + bezier control points
	 * return next value for s for minimizing "a ds^2 + b ds + c", 
	 * 	so  if a!=0 => ds=-b/(2a)  else  ds=-c/b within range??
	 * in both cases, check if better dist is obtained after step
	 * 
	 * <PRE>
	 * s, m_x, m_y, p0_x, p0_y, p1_x, p1_y, p2_x, p2_y, p3_x, p3_y = var('s m_x m_y p0_x p0_y p1_x p1_y p2_x p2_y p3_x p3_y ')
	 * B3(s, a, b, c, d) = (1-s)^3 *a + 3*(1-s)^2*s *b + 3*(1-s)*s^2 *c + s^3 * d
	 * E3 = (m_x-B3(s, p0_x, p1_x, p2_x, p3_x))^2 + (m_y-B3(s, p0_y, p1_y, p2_y, p3_y))^2
	 * expandE3 = expand(E3).simplify_full()
	 * s0, ds = var('s0 ds') 
	 * s=s0+ds
	 * taylor(expandE3, ds, 2)
	 * 
	 * => 
	 * [[-6*p0_x^2*s0^5 - 6*p0_y^2*s0^5 + 30*p0_x*p1_x*s0^5 - 36*p1_x^2*s0^5 + 30*p0_y*p1_y*s0^5 - 36*p1_y^2*s0^5 - 24*p0_x*p2_x*s0^5 + 54*p1_x*p2_x*s0^5 - 18*p2_x^2*s0^5 - 24*p0_y*p2_y*s0^5 + 54*p1_y*p2_y*s0^5 - 18*p2_y^2*s0^5 + 6*p0_x*p3_x*s0^5 - 12*p1_x*p3_x*s0^5 + 6*p2_x*p3_x*s0^5 + 6*p0_y*p3_y*s0^5 - 12*p1_y*p3_y*s0^5 + 6*p2_y*p3_y*s0^5 + (p0_x^2 + p0_y^2 - 6*p0_x*p1_x + 9*p1_x^2 - 6*p0_y*p1_y + 9*p1_y^2 + 6*(p0_x - 3*p1_x)*p2_x + 9*p2_x^2 + 6*(p0_y - 3*p1_y)*p2_y + 9*p2_y^2 - 2*(p0_x - 3*p1_x + 3*p2_x)*p3_x + p3_x^2 - 2*(p0_y - 3*p1_y + 3*p2_y)*p3_y + p3_y^2)*s0^6 + 15*p0_x^2*s0^4 + 15*p0_y^2*s0^4 - 60*p0_x*p1_x*s0^4 + 54*p1_x^2*s0^4 - 60*p0_y*p1_y*s0^4 + 54*p1_y^2*s0^4 + 36*p0_x*p2_x*s0^4 - 54*p1_x*p2_x*s0^4 + 9*p2_x^2*s0^4 + 36*p0_y*p2_y*s0^4 - 54*p1_y*p2_y*s0^4 + 9*p2_y^2*s0^4 - 6*p0_x*p3_x*s0^4 + 6*p1_x*p3_x*s0^4 - 6*p0_y*p3_y*s0^4 + 6*p1_y*p3_y*s0^4 + 2*m_x*p0_x*s0^3 + 140*p0_x^2*s0^3 + 2*m_y*p0_y*s0^3 + 140*p0_y^2*s0^3 - 6*m_x*p1_x*s0^3 - 900*p0_x*p1_x*s0^3 + 1404*p1_x^2*s0^3 - 6*m_y*p1_y*s0^3 - 900*p0_y*p1_y*s0^3 + 1404*p1_y^2*s0^3 + 6*m_x*p2_x*s0^3 + 936*p0_x*p2_x*s0^3 - 2862*p1_x*p2_x*s0^3 + 1440*p2_x^2*s0^3 + 6*m_y*p2_y*s0^3 + 936*p0_y*p2_y*s0^3 - 2862*p1_y*p2_y*s0^3 + 1440*p2_y^2*s0^3 - 2*m_x*p3_x*s0^3 - 318*p0_x*p3_x*s0^3 + 960*p1_x*p3_x*s0^3 - 960*p2_x*p3_x*s0^3 + 160*p3_x^2*s0^3 - 2*m_y*p3_y*s0^3 - 318*p0_y*p3_y*s0^3 + 960*p1_y*p3_y*s0^3 - 960*p2_y*p3_y*s0^3 + 160*p3_y^2*s0^3 - 6*m_x*p0_x*s0^2 + 255*p0_x^2*s0^2 - 6*m_y*p0_y*s0^2 + 255*p0_y^2*s0^2 + 12*m_x*p1_x*s0^2 - 1950*p0_x*p1_x*s0^2 + 3609*p1_x^2*s0^2 + 12*m_y*p1_y*s0^2 - 1950*p0_y*p1_y*s0^2 + 3609*p1_y^2*s0^2 - 6*m_x*p2_x*s0^2 + 2406*p0_x*p2_x*s0^2 - 8640*p1_x*p2_x*s0^2 + 5040*p2_x^2*s0^2 - 6*m_y*p2_y*s0^2 + 2406*p0_y*p2_y*s0^2 - 8640*p1_y*p2_y*s0^2 + 5040*p2_y^2*s0^2 - 960*p0_x*p3_x*s0^2 + 3360*p1_x*p3_x*s0^2 - 3840*p2_x*p3_x*s0^2 + 720*p3_x^2*s0^2 - 960*p0_y*p3_y*s0^2 + 3360*p1_y*p3_y*s0^2 - 3840*p2_y*p3_y*s0^2 + 720*p3_y^2*s0^2 + 6*m_x*p0_x*s0 + 186*p0_x^2*s0 + 6*m_y*p0_y*s0 + 186*p0_y^2*s0 - 6*m_x*p1_x*s0 - 1626*p0_x*p1_x*s0 + 3456*p1_x^2*s0 - 6*m_y*p1_y*s0 - 1626*p0_y*p1_y*s0 + 3456*p1_y^2*s0 + 2304*p0_x*p2_x*s0 - 9504*p1_x*p2_x*s0 + 6336*p2_x^2*s0 + 2304*p0_y*p2_y*s0 - 9504*p1_y*p2_y*s0 + 6336*p2_y^2*s0 - 1056*p0_x*p3_x*s0 + 4224*p1_x*p3_x*s0 - 5472*p2_x*p3_x*s0 + 1152*p3_x^2*s0 - 1056*p0_y*p3_y*s0 + 4224*p1_y*p3_y*s0 - 5472*p2_y*p3_y*s0 + 1152*p3_y^2*s0 + m_x^2 + m_y^2 + 14*m_x*p0_x + 49*p0_x^2 + 14*m_y*p0_y + 49*p0_y^2 - 48*m_x*p1_x - 480*p0_x*p1_x + 1152*p1_x^2 - 48*m_y*p1_y - 480*p0_y*p1_y + 1152*p1_y^2 + 48*m_x*p2_x + 768*p0_x*p2_x - 3600*p1_x*p2_x + 2736*p2_x^2 + 48*m_y*p2_y + 768*p0_y*p2_y - 3600*p1_y*p2_y + 2736*p2_y^2 - 16*m_x*p3_x - 400*p0_x*p3_x + 1824*p1_x*p3_x - 2688*p2_x*p3_x + 640*p3_x^2 - 16*m_y*p3_y - 400*p0_y*p3_y + 1824*p1_y*p3_y - 2688*p2_y*p3_y + 640*p3_y^2,
  	 *   0],
 	 * [6*p0_x^2*s0^5 + 6*p0_y^2*s0^5 - 36*p0_x*p1_x*s0^5 + 54*p1_x^2*s0^5 - 36*p0_y*p1_y*s0^5 + 54*p1_y^2*s0^5 + 36*p0_x*p2_x*s0^5 - 108*p1_x*p2_x*s0^5 + 54*p2_x^2*s0^5 + 36*p0_y*p2_y*s0^5 - 108*p1_y*p2_y*s0^5 + 54*p2_y^2*s0^5 - 12*p0_x*p3_x*s0^5 + 36*p1_x*p3_x*s0^5 - 36*p2_x*p3_x*s0^5 + 6*p3_x^2*s0^5 - 12*p0_y*p3_y*s0^5 + 36*p1_y*p3_y*s0^5 - 36*p2_y*p3_y*s0^5 + 6*p3_y^2*s0^5 - 30*p0_x^2*s0^4 - 30*p0_y^2*s0^4 + 150*p0_x*p1_x*s0^4 - 180*p1_x^2*s0^4 + 150*p0_y*p1_y*s0^4 - 180*p1_y^2*s0^4 - 120*p0_x*p2_x*s0^4 + 270*p1_x*p2_x*s0^4 - 90*p2_x^2*s0^4 - 120*p0_y*p2_y*s0^4 + 270*p1_y*p2_y*s0^4 - 90*p2_y^2*s0^4 + 30*p0_x*p3_x*s0^4 - 60*p1_x*p3_x*s0^4 + 30*p2_x*p3_x*s0^4 + 30*p0_y*p3_y*s0^4 - 60*p1_y*p3_y*s0^4 + 30*p2_y*p3_y*s0^4 - 180*p0_x^2*s0^3 - 180*p0_y^2*s0^3 + 1200*p0_x*p1_x*s0^3 - 1944*p1_x^2*s0^3 + 1200*p0_y*p1_y*s0^3 - 1944*p1_y^2*s0^3 - 1296*p0_x*p2_x*s0^3 + 4104*p1_x*p2_x*s0^3 - 2124*p2_x^2*s0^3 - 1296*p0_y*p2_y*s0^3 + 4104*p1_y*p2_y*s0^3 - 2124*p2_y^2*s0^3 + 456*p0_x*p3_x*s0^3 - 1416*p1_x*p3_x*s0^3 + 1440*p2_x*p3_x*s0^3 - 240*p3_x^2*s0^3 + 456*p0_y*p3_y*s0^3 - 1416*p1_y*p3_y*s0^3 + 1440*p2_y*p3_y*s0^3 - 240*p3_y^2*s0^3 + 6*m_x*p0_x*s0^2 - 300*p0_x^2*s0^2 + 6*m_y*p0_y*s0^2 - 300*p0_y^2*s0^2 - 18*m_x*p1_x*s0^2 + 2340*p0_x*p1_x*s0^2 - 4428*p1_x^2*s0^2 - 18*m_y*p1_y*s0^2 + 2340*p0_y*p1_y*s0^2 - 4428*p1_y^2*s0^2 + 18*m_x*p2_x*s0^2 - 2952*p0_x*p2_x*s0^2 + 10854*p1_x*p2_x*s0^2 - 6480*p2_x^2*s0^2 + 18*m_y*p2_y*s0^2 - 2952*p0_y*p2_y*s0^2 + 10854*p1_y*p2_y*s0^2 - 6480*p2_y^2*s0^2 - 6*m_x*p3_x*s0^2 + 1206*p0_x*p3_x*s0^2 - 4320*p1_x*p3_x*s0^2 + 5040*p2_x*p3_x*s0^2 - 960*p3_x^2*s0^2 - 6*m_y*p3_y*s0^2 + 1206*p0_y*p3_y*s0^2 - 4320*p1_y*p3_y*s0^2 + 5040*p2_y*p3_y*s0^2 - 960*p3_y^2*s0^2 - 12*m_x*p0_x*s0 - 210*p0_x^2*s0 - 12*m_y*p0_y*s0 - 210*p0_y^2*s0 + 24*m_x*p1_x*s0 + 1860*p0_x*p1_x*s0 - 4014*p1_x^2*s0 + 24*m_y*p1_y*s0 + 1860*p0_y*p1_y*s0 - 4014*p1_y^2*s0 - 12*m_x*p2_x*s0 - 2676*p0_x*p2_x*s0 + 11232*p1_x*p2_x*s0 - 7632*p2_x^2*s0 - 12*m_y*p2_y*s0 - 2676*p0_y*p2_y*s0 + 11232*p1_y*p2_y*s0 - 7632*p2_y^2*s0 + 1248*p0_x*p3_x*s0 - 5088*p1_x*p3_x*s0 + 6720*p2_x*p3_x*s0 - 1440*p3_x^2*s0 + 1248*p0_y*p3_y*s0 - 5088*p1_y*p3_y*s0 + 6720*p2_y*p3_y*s0 - 1440*p3_y^2*s0 - 18*m_x*p0_x - 54*p0_x^2 - 18*m_y*p0_y - 54*p0_y^2 + 66*m_x*p1_x + 534*p0_x*p1_x - 1296*p1_x^2 + 66*m_y*p1_y + 534*p0_y*p1_y - 1296*p1_y^2 - 72*m_x*p2_x - 864*p0_x*p2_x + 4104*p1_x*p2_x - 3168*p2_x^2 - 72*m_y*p2_y - 864*p0_y*p2_y + 4104*p1_y*p2_y - 3168*p2_y^2 + 24*m_x*p3_x + 456*p0_x*p3_x - 2112*p1_x*p3_x + 3168*p2_x*p3_x - 768*p3_x^2 + 24*m_y*p3_y + 456*p0_y*p3_y - 2112*p1_y*p3_y + 3168*p2_y*p3_y - 768*p3_y^2,
  	 *   1],
     * [15*p0_x^2*s0^4 + 15*p0_y^2*s0^4 - 90*p0_x*p1_x*s0^4 + 135*p1_x^2*s0^4 - 90*p0_y*p1_y*s0^4 + 135*p1_y^2*s0^4 + 90*p0_x*p2_x*s0^4 - 270*p1_x*p2_x*s0^4 + 135*p2_x^2*s0^4 + 90*p0_y*p2_y*s0^4 - 270*p1_y*p2_y*s0^4 + 135*p2_y^2*s0^4 - 30*p0_x*p3_x*s0^4 + 90*p1_x*p3_x*s0^4 - 90*p2_x*p3_x*s0^4 + 15*p3_x^2*s0^4 - 30*p0_y*p3_y*s0^4 + 90*p1_y*p3_y*s0^4 - 90*p2_y*p3_y*s0^4 + 15*p3_y^2*s0^4 + 60*p0_x^2*s0^3 + 60*p0_y^2*s0^3 - 420*p0_x*p1_x*s0^3 + 720*p1_x^2*s0^3 - 420*p0_y*p1_y*s0^3 + 720*p1_y^2*s0^3 + 480*p0_x*p2_x*s0^3 - 1620*p1_x*p2_x*s0^3 + 900*p2_x^2*s0^3 + 480*p0_y*p2_y*s0^3 - 1620*p1_y*p2_y*s0^3 + 900*p2_y^2*s0^3 - 180*p0_x*p3_x*s0^3 + 600*p1_x*p3_x*s0^3 - 660*p2_x*p3_x*s0^3 + 120*p3_x^2*s0^3 - 180*p0_y*p3_y*s0^3 + 600*p1_y*p3_y*s0^3 - 660*p2_y*p3_y*s0^3 + 120*p3_y^2*s0^3 + 90*p0_x^2*s0^2 + 90*p0_y^2*s0^2 - 720*p0_x*p1_x*s0^2 + 1404*p1_x^2*s0^2 - 720*p0_y*p1_y*s0^2 + 1404*p1_y^2*s0^2 + 936*p0_x*p2_x*s0^2 - 3564*p1_x*p2_x*s0^2 + 2214*p2_x^2*s0^2 + 936*p0_y*p2_y*s0^2 - 3564*p1_y*p2_y*s0^2 + 2214*p2_y^2*s0^2 - 396*p0_x*p3_x*s0^2 + 1476*p1_x*p3_x*s0^2 - 1800*p2_x*p3_x*s0^2 + 360*p3_x^2*s0^2 - 396*p0_y*p3_y*s0^2 + 1476*p1_y*p3_y*s0^2 - 1800*p2_y*p3_y*s0^2 + 360*p3_y^2*s0^2 + 6*m_x*p0_x*s0 + 60*p0_x^2*s0 + 6*m_y*p0_y*s0 + 60*p0_y^2*s0 - 18*m_x*p1_x*s0 - 540*p0_x*p1_x*s0 + 1188*p1_x^2*s0 - 18*m_y*p1_y*s0 - 540*p0_y*p1_y*s0 + 1188*p1_y^2*s0 + 18*m_x*p2_x*s0 + 792*p0_x*p2_x*s0 - 3402*p1_x*p2_x*s0 + 2376*p2_x^2*s0 + 18*m_y*p2_y*s0 + 792*p0_y*p2_y*s0 - 3402*p1_y*p2_y*s0 + 2376*p2_y^2*s0 - 6*m_x*p3_x*s0 - 378*p0_x*p3_x*s0 + 1584*p1_x*p3_x*s0 - 2160*p2_x*p3_x*s0 + 480*p3_x^2*s0 - 6*m_y*p3_y*s0 - 378*p0_y*p3_y*s0 + 1584*p1_y*p3_y*s0 - 2160*p2_y*p3_y*s0 + 480*p3_y^2*s0 + 6*m_x*p0_x + 15*p0_x^2 + 6*m_y*p0_y + 15*p0_y^2 - 24*m_x*p1_x - 150*p0_x*p1_x + 369*p1_x^2 - 24*m_y*p1_y - 150*p0_y*p1_y + 369*p1_y^2 + 30*m_x*p2_x + 246*p0_x*p2_x - 1188*p1_x*p2_x + 936*p2_x^2 + 30*m_y*p2_y + 246*p0_y*p2_y - 1188*p1_y*p2_y + 936*p2_y^2 - 12*m_x*p3_x - 132*p0_x*p3_x + 624*p1_x*p3_x - 960*p2_x*p3_x + 240*p3_x^2 - 12*m_y*p3_y - 132*p0_y*p3_y + 624*p1_y*p3_y - 960*p2_y*p3_y + 240*p3_y^2,
  	 *   2]]
	 * </PRE>
	 */
	public static boolean iterativeStep_minSquareDistPtToCubicBezier_Taylor2_onS0(
			IterStep stepResult,
			double s0, double prevSquareDist,
			double m_x, double m_y, //
			double p0_x, double p0_y, double p1_x, double p1_y, double p2_x, double p2_y, double p3_x, double p3_y
			) {
		double s02 = s0*s0, s03 = s02*s0, s04 = s03*s0, s05 = s04*s0, s06 = s05*s0;
		double p0_x2 = p0_x*p0_x, p0_y2 = p0_y*p0_y;
		double p1_x2 = p1_x*p1_x, p1_y2 = p1_y*p1_y;
		double p2_x2 = p2_x*p2_x, p2_y2 = p2_y*p2_y;
		double p3_x2 = p3_x*p3_x, p3_y2 = p3_y*p3_y;
		double m_x2 = m_x*m_x, m_y2 = m_y*m_y;
		
		double coef2 = 15*p0_x2*s04 + 15*p0_y2*s04 - 90*p0_x*p1_x*s04 + 135*p1_x2*s04 - 90*p0_y*p1_y*s04 + 135*p1_y2*s04 + 90*p0_x*p2_x*s04 - 270*p1_x*p2_x*s04 + 135*p2_x2*s04 + 90*p0_y*p2_y*s04 - 270*p1_y*p2_y*s04 + 135*p2_y2*s04 - 30*p0_x*p3_x*s04 + 90*p1_x*p3_x*s04 - 90*p2_x*p3_x*s04 + 15*p3_x2*s04 - 30*p0_y*p3_y*s04 + 90*p1_y*p3_y*s04 - 90*p2_y*p3_y*s04 + 15*p3_y2*s04 + 60*p0_x2*s03 + 60*p0_y2*s03 - 420*p0_x*p1_x*s03 + 720*p1_x2*s03 - 420*p0_y*p1_y*s03 + 720*p1_y2*s03 + 480*p0_x*p2_x*s03 - 1620*p1_x*p2_x*s03 + 900*p2_x2*s03 + 480*p0_y*p2_y*s03 - 1620*p1_y*p2_y*s03 + 900*p2_y2*s03 - 180*p0_x*p3_x*s03 + 600*p1_x*p3_x*s03 - 660*p2_x*p3_x*s03 + 120*p3_x2*s03 - 180*p0_y*p3_y*s03 + 600*p1_y*p3_y*s03 - 660*p2_y*p3_y*s03 + 120*p3_y2*s03 + 90*p0_x2*s02 + 90*p0_y2*s02 - 720*p0_x*p1_x*s02 + 1404*p1_x2*s02 - 720*p0_y*p1_y*s02 + 1404*p1_y2*s02 + 936*p0_x*p2_x*s02 - 3564*p1_x*p2_x*s02 + 2214*p2_x2*s02 + 936*p0_y*p2_y*s02 - 3564*p1_y*p2_y*s02 + 2214*p2_y2*s02 - 396*p0_x*p3_x*s02 + 1476*p1_x*p3_x*s02 - 1800*p2_x*p3_x*s02 + 360*p3_x2*s02 - 396*p0_y*p3_y*s02 + 1476*p1_y*p3_y*s02 - 1800*p2_y*p3_y*s02 + 360*p3_y2*s02 + 6*m_x*p0_x*s0 + 60*p0_x2*s0 + 6*m_y*p0_y*s0 + 60*p0_y2*s0 - 18*m_x*p1_x*s0 - 540*p0_x*p1_x*s0 + 1188*p1_x2*s0 - 18*m_y*p1_y*s0 - 540*p0_y*p1_y*s0 + 1188*p1_y2*s0 + 18*m_x*p2_x*s0 + 792*p0_x*p2_x*s0 - 3402*p1_x*p2_x*s0 + 2376*p2_x2*s0 + 18*m_y*p2_y*s0 + 792*p0_y*p2_y*s0 - 3402*p1_y*p2_y*s0 + 2376*p2_y2*s0 - 6*m_x*p3_x*s0 - 378*p0_x*p3_x*s0 + 1584*p1_x*p3_x*s0 - 2160*p2_x*p3_x*s0 + 480*p3_x2*s0 - 6*m_y*p3_y*s0 - 378*p0_y*p3_y*s0 + 1584*p1_y*p3_y*s0 - 2160*p2_y*p3_y*s0 + 480*p3_y2*s0 + 6*m_x*p0_x + 15*p0_x2 + 6*m_y*p0_y + 15*p0_y2 - 24*m_x*p1_x - 150*p0_x*p1_x + 369*p1_x2 - 24*m_y*p1_y - 150*p0_y*p1_y + 369*p1_y2 + 30*m_x*p2_x + 246*p0_x*p2_x - 1188*p1_x*p2_x + 936*p2_x2 + 30*m_y*p2_y + 246*p0_y*p2_y - 1188*p1_y*p2_y + 936*p2_y2 - 12*m_x*p3_x - 132*p0_x*p3_x + 624*p1_x*p3_x - 960*p2_x*p3_x + 240*p3_x2 - 12*m_y*p3_y - 132*p0_y*p3_y + 624*p1_y*p3_y - 960*p2_y*p3_y + 240*p3_y2;
		double coef1 = 6*p0_x2*s05 + 6*p0_y2*s05 - 36*p0_x*p1_x*s05 + 54*p1_x2*s05 - 36*p0_y*p1_y*s05 + 54*p1_y2*s05 + 36*p0_x*p2_x*s05 - 108*p1_x*p2_x*s05 + 54*p2_x2*s05 + 36*p0_y*p2_y*s05 - 108*p1_y*p2_y*s05 + 54*p2_y2*s05 - 12*p0_x*p3_x*s05 + 36*p1_x*p3_x*s05 - 36*p2_x*p3_x*s05 + 6*p3_x2*s05 - 12*p0_y*p3_y*s05 + 36*p1_y*p3_y*s05 - 36*p2_y*p3_y*s05 + 6*p3_y2*s05 - 30*p0_x2*s04 - 30*p0_y2*s04 + 150*p0_x*p1_x*s04 - 180*p1_x2*s04 + 150*p0_y*p1_y*s04 - 180*p1_y2*s04 - 120*p0_x*p2_x*s04 + 270*p1_x*p2_x*s04 - 90*p2_x2*s04 - 120*p0_y*p2_y*s04 + 270*p1_y*p2_y*s04 - 90*p2_y2*s04 + 30*p0_x*p3_x*s04 - 60*p1_x*p3_x*s04 + 30*p2_x*p3_x*s04 + 30*p0_y*p3_y*s04 - 60*p1_y*p3_y*s04 + 30*p2_y*p3_y*s04 - 180*p0_x2*s03 - 180*p0_y2*s03 + 1200*p0_x*p1_x*s03 - 1944*p1_x2*s03 + 1200*p0_y*p1_y*s03 - 1944*p1_y2*s03 - 1296*p0_x*p2_x*s03 + 4104*p1_x*p2_x*s03 - 2124*p2_x2*s03 - 1296*p0_y*p2_y*s03 + 4104*p1_y*p2_y*s03 - 2124*p2_y2*s03 + 456*p0_x*p3_x*s03 - 1416*p1_x*p3_x*s03 + 1440*p2_x*p3_x*s03 - 240*p3_x2*s03 + 456*p0_y*p3_y*s03 - 1416*p1_y*p3_y*s03 + 1440*p2_y*p3_y*s03 - 240*p3_y2*s03 + 6*m_x*p0_x*s02 - 300*p0_x2*s02 + 6*m_y*p0_y*s02 - 300*p0_y2*s02 - 18*m_x*p1_x*s02 + 2340*p0_x*p1_x*s02 - 4428*p1_x2*s02 - 18*m_y*p1_y*s02 + 2340*p0_y*p1_y*s02 - 4428*p1_y2*s02 + 18*m_x*p2_x*s02 - 2952*p0_x*p2_x*s02 + 10854*p1_x*p2_x*s02 - 6480*p2_x2*s02 + 18*m_y*p2_y*s02 - 2952*p0_y*p2_y*s02 + 10854*p1_y*p2_y*s02 - 6480*p2_y2*s02 - 6*m_x*p3_x*s02 + 1206*p0_x*p3_x*s02 - 4320*p1_x*p3_x*s02 + 5040*p2_x*p3_x*s02 - 960*p3_x2*s02 - 6*m_y*p3_y*s02 + 1206*p0_y*p3_y*s02 - 4320*p1_y*p3_y*s02 + 5040*p2_y*p3_y*s02 - 960*p3_y2*s02 - 12*m_x*p0_x*s0 - 210*p0_x2*s0 - 12*m_y*p0_y*s0 - 210*p0_y2*s0 + 24*m_x*p1_x*s0 + 1860*p0_x*p1_x*s0 - 4014*p1_x2*s0 + 24*m_y*p1_y*s0 + 1860*p0_y*p1_y*s0 - 4014*p1_y2*s0 - 12*m_x*p2_x*s0 - 2676*p0_x*p2_x*s0 + 11232*p1_x*p2_x*s0 - 7632*p2_x2*s0 - 12*m_y*p2_y*s0 - 2676*p0_y*p2_y*s0 + 11232*p1_y*p2_y*s0 - 7632*p2_y2*s0 + 1248*p0_x*p3_x*s0 - 5088*p1_x*p3_x*s0 + 6720*p2_x*p3_x*s0 - 1440*p3_x2*s0 + 1248*p0_y*p3_y*s0 - 5088*p1_y*p3_y*s0 + 6720*p2_y*p3_y*s0 - 1440*p3_y2*s0 - 18*m_x*p0_x - 54*p0_x2 - 18*m_y*p0_y - 54*p0_y2 + 66*m_x*p1_x + 534*p0_x*p1_x - 1296*p1_x2 + 66*m_y*p1_y + 534*p0_y*p1_y - 1296*p1_y2 - 72*m_x*p2_x - 864*p0_x*p2_x + 4104*p1_x*p2_x - 3168*p2_x2 - 72*m_y*p2_y - 864*p0_y*p2_y + 4104*p1_y*p2_y - 3168*p2_y2 + 24*m_x*p3_x + 456*p0_x*p3_x - 2112*p1_x*p3_x + 3168*p2_x*p3_x - 768*p3_x2 + 24*m_y*p3_y + 456*p0_y*p3_y - 2112*p1_y*p3_y + 3168*p2_y*p3_y - 768*p3_y2;
		if (coef2 != 0.0) {
			double ds = - coef1 / (2*coef2);
			double nextS = s0 + ds;
			double nextSquareDist = squareDistPtToCubicBezier(nextS, m_x, m_y, p0_x, p0_y, p1_x, p1_y, p2_x, p2_y, p3_x, p3_y);
			if (nextSquareDist < prevSquareDist) {
				stepResult.nextS = nextS;
				stepResult.nextSquareDist = nextSquareDist;
				return true;
			} else {
				// quadratic step, but maybe taylor 2 degree approximation is not precise enough around point s0 .. so optim step would be wrong
				return false;
			}
		} else {
			if (coef1 != 0.0) {
				double coef0 = -6*p0_x2*s05 - 6*p0_y2*s05 + 30*p0_x*p1_x*s05 - 36*p1_x2*s05 + 30*p0_y*p1_y*s05 - 36*p1_y2*s05 - 24*p0_x*p2_x*s05 + 54*p1_x*p2_x*s05 - 18*p2_x2*s05 - 24*p0_y*p2_y*s05 + 54*p1_y*p2_y*s05 - 18*p2_y2*s05 + 6*p0_x*p3_x*s05 - 12*p1_x*p3_x*s05 + 6*p2_x*p3_x*s05 + 6*p0_y*p3_y*s05 - 12*p1_y*p3_y*s05 + 6*p2_y*p3_y*s05 + (p0_x2 + p0_y2 - 6*p0_x*p1_x + 9*p1_x2 - 6*p0_y*p1_y + 9*p1_y2 + 6*(p0_x - 3*p1_x)*p2_x + 9*p2_x2 + 6*(p0_y - 3*p1_y)*p2_y + 9*p2_y2 - 2*(p0_x - 3*p1_x + 3*p2_x)*p3_x + p3_x2 - 2*(p0_y - 3*p1_y + 3*p2_y)*p3_y + p3_y2)*s06 + 15*p0_x2*s04 + 15*p0_y2*s04 - 60*p0_x*p1_x*s04 + 54*p1_x2*s04 - 60*p0_y*p1_y*s04 + 54*p1_y2*s04 + 36*p0_x*p2_x*s04 - 54*p1_x*p2_x*s04 + 9*p2_x2*s04 + 36*p0_y*p2_y*s04 - 54*p1_y*p2_y*s04 + 9*p2_y2*s04 - 6*p0_x*p3_x*s04 + 6*p1_x*p3_x*s04 - 6*p0_y*p3_y*s04 + 6*p1_y*p3_y*s04 + 2*m_x*p0_x*s03 + 140*p0_x2*s03 + 2*m_y*p0_y*s03 + 140*p0_y2*s03 - 6*m_x*p1_x*s03 - 900*p0_x*p1_x*s03 + 1404*p1_x2*s03 - 6*m_y*p1_y*s03 - 900*p0_y*p1_y*s03 + 1404*p1_y2*s03 + 6*m_x*p2_x*s03 + 936*p0_x*p2_x*s03 - 2862*p1_x*p2_x*s03 + 1440*p2_x2*s03 + 6*m_y*p2_y*s03 + 936*p0_y*p2_y*s03 - 2862*p1_y*p2_y*s03 + 1440*p2_y2*s03 - 2*m_x*p3_x*s03 - 318*p0_x*p3_x*s03 + 960*p1_x*p3_x*s03 - 960*p2_x*p3_x*s03 + 160*p3_x2*s03 - 2*m_y*p3_y*s03 - 318*p0_y*p3_y*s03 + 960*p1_y*p3_y*s03 - 960*p2_y*p3_y*s03 + 160*p3_y2*s03 - 6*m_x*p0_x*s02 + 255*p0_x2*s02 - 6*m_y*p0_y*s02 + 255*p0_y2*s02 + 12*m_x*p1_x*s02 - 1950*p0_x*p1_x*s02 + 3609*p1_x2*s02 + 12*m_y*p1_y*s02 - 1950*p0_y*p1_y*s02 + 3609*p1_y2*s02 - 6*m_x*p2_x*s02 + 2406*p0_x*p2_x*s02 - 8640*p1_x*p2_x*s02 + 5040*p2_x2*s02 - 6*m_y*p2_y*s02 + 2406*p0_y*p2_y*s02 - 8640*p1_y*p2_y*s02 + 5040*p2_y2*s02 - 960*p0_x*p3_x*s02 + 3360*p1_x*p3_x*s02 - 3840*p2_x*p3_x*s02 + 720*p3_x2*s02 - 960*p0_y*p3_y*s02 + 3360*p1_y*p3_y*s02 - 3840*p2_y*p3_y*s02 + 720*p3_y2*s02 + 6*m_x*p0_x*s0 + 186*p0_x2*s0 + 6*m_y*p0_y*s0 + 186*p0_y2*s0 - 6*m_x*p1_x*s0 - 1626*p0_x*p1_x*s0 + 3456*p1_x2*s0 - 6*m_y*p1_y*s0 - 1626*p0_y*p1_y*s0 + 3456*p1_y2*s0 + 2304*p0_x*p2_x*s0 - 9504*p1_x*p2_x*s0 + 6336*p2_x2*s0 + 2304*p0_y*p2_y*s0 - 9504*p1_y*p2_y*s0 + 6336*p2_y2*s0 - 1056*p0_x*p3_x*s0 + 4224*p1_x*p3_x*s0 - 5472*p2_x*p3_x*s0 + 1152*p3_x2*s0 - 1056*p0_y*p3_y*s0 + 4224*p1_y*p3_y*s0 - 5472*p2_y*p3_y*s0 + 1152*p3_y2*s0 + m_x2 + m_y2 + 14*m_x*p0_x + 49*p0_x2 + 14*m_y*p0_y + 49*p0_y2 - 48*m_x*p1_x - 480*p0_x*p1_x + 1152*p1_x2 - 48*m_y*p1_y - 480*p0_y*p1_y + 1152*p1_y2 + 48*m_x*p2_x + 768*p0_x*p2_x - 3600*p1_x*p2_x + 2736*p2_x2 + 48*m_y*p2_y + 768*p0_y*p2_y - 3600*p1_y*p2_y + 2736*p2_y2 - 16*m_x*p3_x - 400*p0_x*p3_x + 1824*p1_x*p3_x - 2688*p2_x*p3_x + 640*p3_x2 - 16*m_y*p3_y - 400*p0_y*p3_y + 1824*p1_y*p3_y - 2688*p2_y*p3_y + 640*p3_y2;
				double ds = - coef0 / coef1;
				double nextS = s0 + ds;
				double nextSquareDist = squareDistPtToCubicBezier(nextS, m_x, m_y, p0_x, p0_y, p1_x, p1_y, p2_x, p2_y, p3_x, p3_y);
				if (nextSquareDist < prevSquareDist) {
					stepResult.nextS = nextS;
					stepResult.nextSquareDist = nextSquareDist;
					return true;
				} else {
					// linear step, but maybe taylor 2 degree approximation is not precise enough around point s0 .. so optim step would be wrong
					return false;
				}
			} else {
				// a=b=0 .. taylor 2 approx => need coef3..
				return false;
			}
		}
	}
	
}
