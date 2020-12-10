package fr.an.drawingboard.geom2d.bezier;

import fr.an.drawingboard.geom2d.Pt2D;
import fr.an.drawingboard.geom2d.QuadBezier2D;
import fr.an.drawingboard.math.algo.numerical.DoublePolynomialRootSolver;
import fr.an.drawingboard.math.algo.numerical.DoublePolynomialRootSolver.DoublePolynomialRoots;
import fr.an.drawingboard.math.numeric.polynomial.DoublePolynom;
import lombok.val;

public class PtToBezierDistanceMinSolver {

	public static class PtToCurveDistanceMinSolverResult {
		public Pt2D projPt;
		public double curveParam;
		public double squareDist;
	}
	
	// solver for min distance projection of Pt to Quadratic Bezier
	// ------------------------------------------------------------------------

	/**
	 * min distance projection of pt M(m_x,m_y) to point B(s) on QuadBezier in [0,1]
	 */
	public static void projPtToQuadBezier(PtToCurveDistanceMinSolverResult result, //
			Pt2D m, QuadBezier2D quadBezier, //
			double minS, double maxS) {
		projPtToQuadBezier(result, m, quadBezier.startPt, quadBezier.controlPt, quadBezier.endPt, minS, maxS);
	}

	public static void projPtToQuadBezier(PtToCurveDistanceMinSolverResult result, //
			Pt2D m, Pt2D startPt, Pt2D controlPt, Pt2D endPt, //
			double minS, double maxS) {
		projPtToQuadBezier_polynom4_deriv_roots(result, m.x, m.y, startPt.x, startPt.y, controlPt.x, controlPt.y, endPt.x, endPt.y, minS, maxS);
	}

	/**
	 * algorithm:
	 * compute square dist as a polynom over s of degree 4
	 * => derivative of polynom is degree 3
	 * => solve roots of polynom, eval for each root which one give the minimum value
	 */
	public static void projPtToQuadBezier_polynom4_deriv_roots(PtToCurveDistanceMinSolverResult result, //
			double m_x, double m_y, //
			double p0_x, double p0_y, double p1_x, double p1_y, double p2_x, double p2_y,
			double minS, double maxS) {
		DoublePolynom squareDist_poly4 = PtToBezierDistanceEval.squareDistPtToQuadBezier_asPolynomOnS(
				m_x, m_y, p0_x, p0_y, p1_x, p1_y, p2_x, p2_y);
		DoublePolynom derive_square_dist_poly3 = squareDist_poly4.derive();
		DoublePolynomialRoots derive_squareDist_roots = DoublePolynomialRootSolver.solveCubic(derive_square_dist_poly3);
		double bestSquareDist = Double.MAX_VALUE;
		double bestParam = minS;
		val paramRoots = derive_squareDist_roots.roots;
		if (paramRoots != null && paramRoots.length > 0) {
			for(val paramRoot: paramRoots) {
				if (minS <= paramRoot && paramRoot <= maxS) {
					// only eval param for root in [minS, maxS]
					double squareDist = PtToBezierDistanceEval.squareDistPtToQuadBezier(paramRoot, m_x, m_y, p0_x, p0_y, p1_x, p1_y, p2_x, p2_y);
					if (squareDist < bestSquareDist) {
						bestSquareDist = squareDist;
						bestParam = paramRoot;
					}
				}
			}
		} else {
			// should not occur.. no root for polynom of degree 3?
		}
		// also eval bounded to minS and maxS (if bestSquareDist == Double.MAX_VALUE ?)
		// eval for minS
		val distMinS = PtToBezierDistanceEval.squareDistPtToQuadBezier(minS, m_x, m_y, p0_x, p0_y, p1_x, p1_y, p2_x, p2_y);
		if (distMinS < bestSquareDist) {
			bestSquareDist = distMinS;
			bestParam = minS;
		}
		val distMaxS = PtToBezierDistanceEval.squareDistPtToQuadBezier(maxS, m_x, m_y, p0_x, p0_y, p1_x, p1_y, p2_x, p2_y);
		if (distMaxS < bestSquareDist) {
			bestSquareDist = distMaxS;
			bestParam = maxS;
		}
		
		// eval pt for bestParamSoFar
		if (result.projPt == null) {
			result.projPt = new Pt2D(0, 0);
		}
		// QuadBezier2D.evalB(result.projPt, p0, p1, p2)
		result.curveParam = bestParam;
		result.squareDist = bestSquareDist;
		QuadBezier2D.eval(result.projPt, bestParam, p0_x, p0_y, p1_x, p1_y, p2_x, p2_y);
	}

}
