package fr.an.drawingboard.geom2d.bezier;

import fr.an.drawingboard.geom2d.CubicBezier2D;
import fr.an.drawingboard.geom2d.Pt2D;
import fr.an.drawingboard.geom2d.QuadBezier2D;
import fr.an.drawingboard.math.algo.numerical.DoublePolynomialRootSolver;
import fr.an.drawingboard.math.algo.numerical.DoublePolynomialRootSolver.DoublePolynomialRoots;
import fr.an.drawingboard.math.numeric.polynomial.DoublePolynom;
import lombok.val;

public class PtToBezierDistanceMinSolver {

	private static final double PREC = 1e-10;
	
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
			Pt2D m, QuadBezier2D quadBezier) {
		projPtToQuadBezier(result, m, quadBezier.startPt, quadBezier.controlPt, quadBezier.endPt);
	}

	public static void projPtToQuadBezier(PtToCurveDistanceMinSolverResult result, //
			Pt2D m, Pt2D startPt, Pt2D controlPt, Pt2D endPt) {
		projPtToQuadBezier_polynom4_deriv_roots(result, m.x, m.y, startPt.x, startPt.y, controlPt.x, controlPt.y, endPt.x, endPt.y);
	}

	/**
	 * algorithm:
	 * compute square dist as a polynom over s of degree 4
	 * => derivative of polynom is degree 3
	 * => solve roots of polynom, eval for each root which one give the minimum value
	 */
	public static void projPtToQuadBezier_polynom4_deriv_roots(PtToCurveDistanceMinSolverResult result, //
			double m_x, double m_y, //
			double p0_x, double p0_y, double p1_x, double p1_y, double p2_x, double p2_y) {
		val squareDistStartPt = Pt2D.squareDist(p0_x, p0_y, m_x, m_y);
		val squareDistControlPt  = Pt2D.squareDist(p1_x, p1_y, m_x, m_y);
		val squareDistEndPt  = Pt2D.squareDist(p2_x, p2_y, m_x, m_y);
		if (result.projPt == null) {
			result.projPt = new Pt2D(0, 0);
		}
		if (squareDistStartPt < squareDistControlPt && squareDistStartPt < squareDistEndPt) {
			// avoid computation of degree 3 only if pt is "before" startPt
			//  P    startPt
			//  +----+               vect(P,startPt).vect(startPt,ctrlPt) >= 0
			//        \           && vect(P,startPt).vect(startPt,endPt) >= 0
			//        + ctrlPt
			//        |
			//        + endPt
			if ( ((p0_x-m_x)*(p1_x-p0_x) + (p0_y-m_y)*(p1_y-p1_x)) >= 0
					&& ((p0_x-m_x)*(p2_x-p0_x) + (p0_y-m_y)*(p2_y-p0_y)) >= 0
					) {
				//  startPt is the min dist
				result.curveParam = 0.0;
				result.squareDist = squareDistStartPt;
				result.projPt.set(p0_x, p0_y);
				return;
			}
		}
		if (squareDistEndPt < squareDistStartPt && squareDistEndPt < squareDistControlPt) {
			// avoid computation of degree 3 only if pt is "after" endPt
			//      startPt
			//         +               vect(P,endPt).vect(endPt,ctrlPt) >= 0
			//         |           && vect(P,endPt).vect(endPt,startPt) >= 0
			//         + ctrlPt
			//         /
			//  P+----+ endPt
			if ( ((p2_x-m_x)*(p1_x-p2_x) + (p2_y-m_y)*(p1_y-p2_y)) >= 0
					&& ((p2_x-m_x)*(p0_x-p2_x) + (p2_y-m_y)*(p0_y-p2_y)) >= 0
					) {
				result.curveParam = 1.0;
				result.squareDist = squareDistEndPt;
				result.projPt.set(p2_x, p2_y);
				return;
			}
		}

		// algrebric compute squareDist as polynom of degree 4
		DoublePolynom squareDist_poly4 = PtToBezierDistanceEval.squareDistPtToQuadBezier_asPolynomOnS(
				m_x, m_y, p0_x, p0_y, p1_x, p1_y, p2_x, p2_y);
		// algebric derivative of squareDist.. polynom of degree 3
		DoublePolynom derive_square_dist_poly3 = squareDist_poly4.derive();
		// solve roots polynom of degree 3, using cardan formula => 1,2,3 roots
		DoublePolynomialRoots derive_squareDist_roots = DoublePolynomialRootSolver.solveCubic(derive_square_dist_poly3);
		// retain only best root
		val minS = 0.0, maxS = 1.0;
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
		if (squareDistStartPt < bestSquareDist) {
			bestSquareDist = squareDistStartPt;
			bestParam = minS;
		}
		if (squareDistEndPt < bestSquareDist) {
			bestSquareDist = squareDistEndPt;
			bestParam = maxS;
		}
		// eval pt for bestParamSoFar
		result.curveParam = bestParam;
		result.squareDist = bestSquareDist;
		QuadBezier2D.eval(result.projPt, bestParam, p0_x, p0_y, p1_x, p1_y, p2_x, p2_y);
	}

	
	// solver for min distance projection of Pt to Cubic Bezier
	// ------------------------------------------------------------------------

	/**
	 * min distance projection of pt M(m_x,m_y) to point B(s) on CubicBezier in [0,1]
	 */
	public static void projPtToCubicBezier(PtToCurveDistanceMinSolverResult result, //
			Pt2D m, CubicBezier2D quadBezier) {
		projPtToCubicBezier(result, m, quadBezier.startPt, quadBezier.p1, quadBezier.p2, quadBezier.endPt);
	}

	public static void projPtToCubicBezier(PtToCurveDistanceMinSolverResult result, //
			Pt2D m, Pt2D startPt, Pt2D control1Pt, Pt2D control2Pt, Pt2D endPt) {
		projPtToCubicBezier_polynom6_deriv_roots(result, m.x, m.y, startPt.x, startPt.y, control1Pt.x, control1Pt.y, control2Pt.x, control2Pt.y, endPt.x, endPt.y);
	}

	/**
	 * algorithm:
	 * compute square dist as a polynom over s of degree 6
	 * => derivative of polynom is degree 5
	 * => discretize to N steps (N=10?) .. find nearest points => restrict search in interval neightboor
	 */
	public static void projPtToCubicBezier_polynom6_deriv_roots(PtToCurveDistanceMinSolverResult result, //
			double m_x, double m_y, //
			double p0_x, double p0_y, double p1_x, double p1_y, double p2_x, double p2_y, double p3_x, double p3_y) {
		val squareDistStartPt = Pt2D.squareDist(p0_x, p0_y, m_x, m_y);
		val squareDistCtrl1Pt  = Pt2D.squareDist(p1_x, p1_y, m_x, m_y);
		val squareDistCtrl2Pt  = Pt2D.squareDist(p2_x, p2_y, m_x, m_y);
		val squareDistEndPt  = Pt2D.squareDist(p3_x, p3_y, m_x, m_y);
		if (result.projPt == null) {
			result.projPt = new Pt2D(0, 0);
		}
		double p0_m_x = p0_x-m_x, p0_m_y = p0_y-m_y;
		double p3_m_x = p3_x-m_x, p3_m_y = p3_y-m_y;
		if (squareDistStartPt < squareDistCtrl1Pt && squareDistStartPt < squareDistCtrl2Pt && squareDistStartPt < squareDistEndPt) {
			// avoid computation only if pt is "before" startPt
			//  P    startPt
			//  +----+               vect(P,startPt).vect(startPt,ctrl1Pt) >= 0
			//        \           && vect(P,startPt).vect(startPt,ctrl2Pt) >= 0
			//        + ctrl1Pt   && vect(P,startPt).vect(startPt,endPt  ) >= 0
			//        + ctrl2Pt
			//        |
			//        + endPt
			if ( (p0_m_x*(p1_x-p0_x) + p0_m_y*(p1_y-p0_y)) >= -PREC
					&& (p0_m_x*(p2_x-p0_x) + p0_m_y*(p2_y-p0_y)) >= -PREC
					&& (p0_m_x*(p3_x-p0_x) + p0_m_y*(p3_y-p0_y)) >= -PREC
					) {
				//  startPt is the min dist
				result.curveParam = 0.0;
				result.squareDist = squareDistStartPt;
				result.projPt.set(p0_x, p0_y);
				return;
			}
		}
		if (squareDistEndPt < squareDistStartPt && squareDistEndPt < squareDistCtrl1Pt && squareDistEndPt < squareDistCtrl2Pt) {
			// avoid computation only if pt is "after" endPt
			//      startPt
			//         +              vect(P,endPt).vect(endPt,ctrl2Pt) >= 0
			//         |           && vect(P,endPt).vect(endPt,ctrl1Pt) >= 0
			//         + ctrlPt1   && vect(P,endPt).vect(endPt,startPt) >= 0
			//         + ctrlPt2
			//         /
			//  P+----+ endPt
			if ( (p3_m_x*(p2_x-p3_x) + p3_m_y*(p2_y-p3_y)) >= -PREC
					&& (p3_m_x*(p1_x-p3_x) + p3_m_y*(p1_y-p3_y)) >= -PREC
					&& (p3_m_x*(p0_x-p3_x) + p3_m_y*(p0_y-p3_y)) >= -PREC
					) {
				result.curveParam = 1.0;
				result.squareDist = squareDistEndPt;
				result.projPt.set(p3_x, p3_y);
				return;
			}
		}

		// algrebric compute squareDist as polynom of degree 6
		DoublePolynom squareDist_poly6 = PtToBezierDistanceEval.squareDistPtToCubicBezier_asPolynomOnS(
				m_x, m_y, p0_x, p0_y, p1_x, p1_y, p2_x, p2_y, p3_x, p3_y);
		// algebric derivative of squareDist.. polynom of degree 5
		DoublePolynom derive_square_dist_poly5 = squareDist_poly6.derive();
		DoublePolynom derive2_square_dist = derive_square_dist_poly5.derive();
		DoublePolynom derive3_square_dist = derive2_square_dist.derive();
		
		// discretize to N points to find nearest ... in each range [x,x+step] there should be at most 1 solution
		int N = 10;
		double step = 1.0 / N;
		double bestSquareDist = Double.MAX_VALUE;
		double bestStepIndex = 0;
		double bestStepParam = 0;
		double currStepParam = 0.0;
		for(int i = 0; i <= N; i++) {
			// double currSquareDist = // maybe numerical rounding error ?? squareDist_poly6.eval(currStepParam);
			double bs_x = CubicBezier2D.evalB(currStepParam, p0_x, p1_x, p2_x, p3_x);
			double bs_y = CubicBezier2D.evalB(currStepParam, p0_y, p1_y, p2_y, p3_y);
			double currSquareDist = Pt2D.squareDist(bs_x, bs_y, m_x, m_y);
			if (currSquareDist < bestSquareDist) {
				bestSquareDist = currSquareDist;
				bestStepParam = currStepParam;
				bestStepIndex = i;
			}
			currStepParam += step;
		}
		if ((bestStepIndex == 0
				// || bestStepIndex == 1 // ??
				)
				&& (p0_m_x*(p1_x-p0_x) + p0_m_y*(p1_y-p0_y)) >= -PREC
				) {
			result.curveParam = 0.0;
			result.squareDist = squareDistStartPt;
			result.projPt.set(p0_x, p0_y);
		} else if ((bestStepIndex == N 
					// || bestStepIndex == N-1 // ???
					)
				&& (p3_m_x*(p2_x-p3_x) + p3_m_y*(p2_y-p3_y)) >= -PREC
				) {
			result.curveParam = 1.0;
			result.squareDist = squareDistEndPt;
			result.projPt.set(p3_x, p3_y);
		} else {
			double searchParamPrecision = 1e-5;
			int maxIter = 10;
			double searchRootMin = Math.max(0.0, bestStepParam-step);
			double searchRootMax = Math.min(1.0, bestStepParam+step);
			
			double curveParam = findRootFuncInRange(x -> derive_square_dist_poly5.eval(x), x -> derive2_square_dist.eval(x), x -> derive3_square_dist.eval(x),
					searchRootMin, searchRootMax, searchParamPrecision, maxIter
					);
			
			result.curveParam = curveParam;
			result.squareDist = squareDist_poly6.eval(curveParam);
			CubicBezier2D.eval(result.projPt, curveParam, p0_x, p0_y, p1_x, p1_y, p2_x, p2_y, p3_x, p3_y);
		}
	}

	
	@FunctionalInterface
	public static interface DoubleFunc {
		public double eval(double x);
	}
	public static double findRootFuncInRange(final DoubleFunc f, final DoubleFunc derF, final DoubleFunc der2F, 
			final double initXmin, final double initXmax, //
			final double xPrecision, final int maxIter) {
		double xmin = initXmin, xmax = initXmax;
		double currFLeft = f.eval(xmin), currFRight = f.eval(xmax);
		if (currFLeft > 0 && currFRight > 0) { // maybe multiple solutions within range...
			double x = 0.5 * (xmin + xmax);
			double fx = f.eval(x);
			if (fx == 0.0) {
				return x;
			}
			if (fx < 0) {
				// ok..
				xmin = x;
				currFLeft = fx;
			}
		}
		if (currFLeft < 0 && currFRight < 0) { // maybe multiple solutions within range...
			double x = 0.5 * (xmin + xmax);
			double fx = f.eval(x);
			if (fx == 0.0) {
				return x;
			}
			if (fx > 0) {
				// ok..
				xmax = x;
				currFRight = fx;
			}
		}
		
		
		if ((currFLeft > 0) == (currFRight > 0)) {
			System.out.println("? not crossing");
			// throw new IllegalArgumentException();
		}
		//double currDerivFLeft = derF.eval(currLeft), currDerivFRight = derF.eval(currRight);
		// if (!currDerivFLeft)
		// boolean currLeftBelow = currFLeft < 0.0;
		int iter = 0;
		while(xmax - xmin > xPrecision) {
			if (iter++ > maxIter) {
				break; // STOP!!
			}
			// newton step:
			// f(xmin+dx) ~=  f(xmin) + f'(xmin) dx + o(dx)
			// f()=0 <=> dx= -f / f'
			double derFLeft = derF.eval(xmin);
			double x;
			if (derFLeft != 0.0) {
				x = -currFLeft / derFLeft;
				if (x < xmin || x > xmax) {
					// out of bound! .. use other split
					x = 0.5 * (xmin + xmax);
				}
			} else {
				x = 0.5 * (xmin + xmax);
			}
			double fx = f.eval(x);
			if (fx == 0.0) {
				return x;
			} else if (fx < 0.0) {
				if (xmin == x) {
					break; // same!!
				}
				xmin = x;
				currFLeft = fx;
			} else {
				if (xmax == x) {
					break; // same!!
				}
				xmax = x;
				currFRight = fx;
			}
			
//			// order 2 step?:
//			//f(xmin+dx) ~=  f(xmin) + f'(xmin) dx + 1/2 f''(xmin) dx^2  + o(dx^2)
//			//               c + b dx + a dx^2 + o(dx^2)
//			// f(x)=0 <=>  dx= (-b +- sqrt(b^2-4ac))/(2a)
//			double a_mult2 = der2F.eval(currLeft);
//			double a = 0.5 * a_mult2;
//			double b = derF.eval(currLeft);
//			double c = currFLeft;
//			if (a_mult2 != 0.0) {
//				double delta = b*b - 2*a_mult2*c;
//				if (delta > 0) {
//					double sqrt_delta = Math.sqrt(delta);
//					double inv_2a = 1.0/a_mult2;
//					double x1 = -(b-sqrt_delta)*inv_2a;
//					if (currLeft <= x1 && x1 <= currRight) {
//						// recurse split x1
//						double fx1 = f.eval(x1);
//						if (fx1 == 0.0) {
//							return x1;
//						}
//						boolean fx1Below = fx1 < 0.0;
//						if (fx1Below == currLeftBelow)  { // TOCHECK
//							currLeft = x1;
//							currFLeft = fx1;
//						} else {
//							currRight = x1;
//							currFRight = fx1;
//						}
//					} else { // should not occur?
//						double x2 = -(b+sqrt_delta)*inv_2a;
//						if (currLeft <= x2 && x2 <= currRight) {
//							// recurse split x2
//							double fx2 = f.eval(x2);
//							if (fx2 == 0.0) {
//								return x2;
//							}
//							if ((fx2 > 0.0) == currLeftBelow)  {
//								currRight = x2;
//								currFRight = fx2;
//							} else {
//								currLeft = x2;
//								currFLeft = fx2;
//							}
//						}
//					}
//				}
//			} else {
//				// not quadratic!
//				if (b != 0.0) {
//					double x = -c/b;
//					
//				} else {
//					//const?.. stop iteration?
//					break;
//				}
//			}
			
		}// while xmax - xmin > xPrecision
		return 0.5*(xmin+xmax);
	}
	
}
