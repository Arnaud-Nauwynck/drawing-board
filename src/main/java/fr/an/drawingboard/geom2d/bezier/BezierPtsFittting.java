package fr.an.drawingboard.geom2d.bezier;

import java.util.List;

import fr.an.drawingboard.geom2d.CubicBezier2D;
import fr.an.drawingboard.geom2d.Pt2D;
import fr.an.drawingboard.geom2d.QuadBezier2D;
import fr.an.drawingboard.geom2d.WeightedPt2D;
import fr.an.drawingboard.recognizer.trace.PathDistLengthesUtils;
import fr.an.drawingboard.util.LsUtils;
import lombok.val;

/**
 * cf https://pomax.github.io/BezierInfo-2/#curvefitting
 * https://pomax.github.io/BezierInfo-2/chapters/curvefitting/curve-fitting.js
 */
public class BezierPtsFittting {

	/**
	 * fitting only the controlPt of a Bezier Quad
	 * with bezier.startPt= pts[0], bezier.endPt=pts[N]
	 *    bezier.controlPt = optimal least square fitting ..
	 * .. heuristic: s_i = len(start -> pt_i) / len(start -> pt_N)
	 *    
	 * <PRE>
	 * m_x,s, start, p1_x, p2_x = var('m_x s start p1_x p2_x')
	 * B(s, a, b, c) = (1-s)^2 *a + 2*(1-s)*s *b + s^2 *c
	 * expand((m_x-B(s, start, p1_x, end))^2).coefficients(p1_x)
	 * =>
	 * [[start^2*s^4 + 2*start*end*s^4 + end^2*s^4 - 4*start^2*s^3 - 4*start*end*s^3 - 2*m_x*start*s^2 + 6*start^2*s^2 - 2*m_x*end*s^2 + 2*start*end*s^2 + 4*m_x*start*s - 4*start^2*s + m_x^2 - 2*m_x*start + start^2,
     *    0],
     *  [-4*start*s^4 - 4*end*s^4 + 12*start*s^3 + 4*end*s^3 + 4*m_x*s^2 - 12*start*s^2 - 4*m_x*s + 4*start*s,
     *    1],
     *  [4*s^4 - 8*s^3 + 4*s^2, 
     *    2]]
	 * 
	 * 
	 * E = sum ( w_i A^_i X^2 + B_i X + C_i )
	 *   =  a_x p1_x^2 + b_x p1_x + c_x  
	 *    + a_y p1_y^2 + b_y p1_y + c_y  
	 * => p1_x = -b_x/(2 a_x)
	 *    p1_y = -b_y/(2 a_y)
	 * </PRE>
	 */
	public static void fitControlPt_QuadBezier(QuadBezier2D res, List<WeightedPt2D> wpts) {
		// startPt and endPt
		int ptCount = wpts.size();
		if (ptCount == 0) {
			return;
		}
		val start = wpts.get(0).pt;
		val end = wpts.get(ptCount-1).pt;
		res.startPt.set(start);
		res.endPt.set(end);
		
		// step 1: compute heuristic s_i for pt[i]
		double[] s_i = PathDistLengthesUtils.ptsToRatioDistLengthes(LsUtils.map(wpts, wpt -> wpt.pt));
		
		// compute quad form over parameters p1_x, and (symmetric)independent quad form over p1_y
		double a_div4 = 0.0; // = a_x = a_y
		double b_div4_x = 0.0;
		double b_div4_y = 0.0;
		double start_x = start.x, start_y = start.y;
		double end_x = end.x, end_y = end.y;
		for(int i = 1; i < ptCount; i++) {
			WeightedPt2D wpti = wpts.get(i);
			Pt2D pt_i = wpti.pt;
			val wi = wpti.weight;
			double pti_x = pt_i.x, pti_y = pt_i.y; 
			double s = s_i[i], s2 = s*s, s3=s2*s, s4=s3*s;

			a_div4 += wi*(s4 - 2*s3 + s2);
			b_div4_x += wi * (- start_x*s4 - end_x*s4 + 3*start_x*s3 + end_x*s3 + pti_x*s2 - 3*start_x*s2 - pti_x*s + start_x*s);
			b_div4_y += wi * (- start_y*s4 - end_y*s4 + 3*start_y*s3 + end_y*s3 + pti_y*s2 - 3*start_y*s2 - pti_y*s + start_y*s);
		}
		assert a_div4 > 0.0;
		double inv2A = 1.0 / ((a_div4 != 0.0)? 2*a_div4 : 1.0);
		double resP_x = - b_div4_x * inv2A;
		double resP_y = - b_div4_y * inv2A;
		res.controlPt.set(resP_x, resP_y);
	}

	
	/**
	 * fitting only the controlPt1,controlPt2 of a Cubic Bezier
	 * with bezier.startPt= pts[0], bezier.endPt=pts[N]
	 *    bezier.controlPts = optimal least square fitting ..
	 * .. heuristic: s_i = len(start -> pt_i) / len(start -> pt_N)
	 *    
	 * <PRE>
	 * m_x,s, start, p1, p2, end = var('m_x s start p1 p2 end')
     * B3(s, a, b, c, d) = (1-s)^3 *a + 3*(1-s)^2*s *b + 3*(1-s)*s^2 *c + s^3 *d
     * Q=expand((m_x-B3(s, start, p1, p2, end))^2)
     * =>
     * end^2*s^6 + 6*end*p1*s^6 + 9*p1^2*s^6 - 6*end*p2*s^6 - 18*p1*p2*s^6 + 9*p2^2*s^6 - 2*end*s^6*start - 6*p1*s^6*start + 6*p2*s^6*start + s^6*start^2 - 12*end*p1*s^5 - 36*p1^2*s^5 + 6*end*p2*s^5 + 54*p1*p2*s^5 - 18*p2^2*s^5 + 6*end*s^5*start + 30*p1*s^5*start - 24*p2*s^5*start - 6*s^5*start^2 + 6*end*p1*s^4 + 54*p1^2*s^4 - 54*p1*p2*s^4 + 9*p2^2*s^4 - 6*end*s^4*start - 60*p1*s^4*start + 36*p2*s^4*start + 15*s^4*start^2 - 2*end*m_x*s^3 - 6*m_x*p1*s^3 - 36*p1^2*s^3 + 6*m_x*p2*s^3 + 18*p1*p2*s^3 + 2*end*s^3*start + 2*m_x*s^3*start + 60*p1*s^3*start - 24*p2*s^3*start - 20*s^3*start^2 + 12*m_x*p1*s^2 + 9*p1^2*s^2 - 6*m_x*p2*s^2 - 6*m_x*s^2*start - 30*p1*s^2*start + 6*p2*s^2*start + 15*s^2*start^2 - 6*m_x*p1*s + 6*m_x*s*start + 6*p1*s*start - 6*s*start^2 + m_x^2 - 2*m_x*start + start^2
     * 
     * 
     * a_div9_p1p1 = Q.coefficients(p1)[2][0] /9
     * a_div9_p1p2 = Q.coefficients(p1)[1][0].coefficients(p2)[1][0] /9 /2
     * a_div9_p2p2 = Q.coefficients(p2)[2][0] /9
     * b_div6_p1 = Q.coefficients(p1)[1][0].coefficients(p2)[0][0] / 6
     * b_div6_p2 = Q.coefficients(p2)[1][0].coefficients(p1)[0][0] / 6
     *
     * checkQ = 9*(a_div9_p1p1 * p1 * p1 + 2 * a_div9_p1p2 * p1 * p2 + a_div9_p2p2 * p2 * p2) + 6*( b_div6_p1 * p1 + b_div6_p2 * p2)
     * checkConst=(checkQ.expand() - Q.expand()).simplify_full()
     * checkConst.coefficients(p1)[0][0]-checkConst.coefficients(p2)[0][0]
     * 
     * 
 	 * compute quad form over parameters [p1, p2] 
	 * for x: [p1_x, p2_x] and symmetric independent for y: [p1_y,p2_y]
	 * Q = [p1 p2] [ a_p1p1  a_p1p2 ] [p1]  + [ b_p1 b_p2] [p1] + c
	 *             [ a_p1p2  a_p2p2 ] [p2]                 [p2]
     * 
	 * a_div9_p1p1 = Q.coefficients(p1)[2][0] /9
	 * s^6 - 4*s^5 + 6*s^4 - 4*s^3 + s^2
	 * 
	 * a_div9_p1p2 = Q.coefficients(p1)[1][0].coefficients(p2)[1][0] /9 /2
	 * -s^6 + 3*s^5 - 3*s^4 + s^3
	 * 
	 * a_div9_p2p2 = Q.coefficients(p2)[2][0] /9
	 * s^6 - 2*s^5 + s^4
	 * 
     * 
     * b_div6_p1 = Q.coefficients(p1)[1][0].coefficients(p2)[0][0] / 6
     * end*s^6 - s^6*start - 2*end*s^5 + 5*s^5*start + end*s^4 - 10*s^4*start - m_x*s^3 + 10*s^3*start + 2*m_x*s^2 - 5*s^2*start - m_x*s + s*start
     * 
     * b_div6_p2 = Q.coefficients(p2)[1][0].coefficients(p1)[0][0] / 6
     * -end*s^6 + s^6*start + end*s^5 - 4*s^5*start + 6*s^4*start + m_x*s^3 - 4*s^3*start - m_x*s^2 + s^2*start
     * 
     * 
     * 
	 * </PRE>
	 */
	public static void fitControlPts_CubicBezier(CubicBezier2D res, List<WeightedPt2D> wpts) {
		// startPt and endPt
		int ptCount = wpts.size();
		if (ptCount == 0) {
			return;
		}
		val start = wpts.get(0).pt;
		val end = wpts.get(ptCount-1).pt;
		res.startPt.set(start);
		res.endPt.set(end);
		
		// step 1: compute heuristic s_i for pt[i]
		double[] s_i = PathDistLengthesUtils.ptsToRatioDistLengthes(LsUtils.map(wpts, wpt -> wpt.pt));
		
		double a_div9_p1p1 = 0.0, a_div9_p1p2 = 0.0, a_div9_p2p2 = 0.0;
		double b_div6_p1_x = 0.0, b_div6_p2_x = 0.0;
		double b_div6_p1_y = 0.0, b_div6_p2_y = 0.0;

		double start_x = start.x, start_y = start.y;
		double end_x = end.x, end_y = end.y;
		for(int i = 1; i < ptCount; i++) {
			WeightedPt2D wpti = wpts.get(i);
			Pt2D pt_i = wpti.pt;
			val wi = wpti.weight;
			double pti_x = pt_i.x, pti_y = pt_i.y; 
			double s = s_i[i], s2 = s*s, s3=s2*s, s4=s3*s, s5=s4*s, s6=s5*s;
		
			a_div9_p1p1 += wi * ( s6 - 4*s5 + 6*s4 - 4*s3 + s2 );
			a_div9_p1p2 += wi * ( -s6 + 3*s5 - 3*s4 + s3 );
			a_div9_p2p2 += wi * ( s6 - 2*s5 + s4 );
			
			b_div6_p1_x += wi * ( end_x*s6 - s6*start_x - 2*end_x*s5 + 5*s5*start_x + end_x*s4 - 10*s4*start_x - pti_x*s3 + 10*s3*start_x + 2*pti_x*s2 - 5*s2*start_x - pti_x*s + s*start_x );
			b_div6_p1_y += wi * ( end_y*s6 - s6*start_y - 2*end_y*s5 + 5*s5*start_y + end_y*s4 - 10*s4*start_y - pti_y*s3 + 10*s3*start_y + 2*pti_y*s2 - 5*s2*start_y - pti_y*s + s*start_y );

			b_div6_p2_x += wi * ( -end_x*s6 + s6*start_x + end_x*s5 - 4*s5*start_x + 6*s4*start_x + pti_x*s3 - 4*s3*start_x - pti_x*s2 + s2*start_x );
			b_div6_p2_y += wi * ( -end_y*s6 + s6*start_y + end_y*s5 - 4*s5*start_y + 6*s4*start_y + pti_y*s3 - 4*s3*start_y - pti_y*s2 + s2*start_y );
			
		}

		double a_p1p1 = 9*a_div9_p1p1, a_p1p2 = 9*a_div9_p1p2, a_p2p2 = 9*a_div9_p2p2;
		double b_p1_x = 6*b_div6_p1_x, b_p2_x = 6*b_div6_p2_x;
		double b_p1_y = 6*b_div6_p1_y, b_p2_y = 6*b_div6_p2_y;
		
		// step 2: inverse.. =- A^-1 B
		// reminder:
		// A = | a11 a12 |  => A^-1 =  1/(a11 a22 - a12 a21) | a22  -a12 |
		//     | a21 a22 |                                   | -a21 a11  |
		
		double detA = a_p1p1 * a_p2p2 - a_p1p2 * a_p1p2;
		if (Math.abs(detA) < 1e-9) {
			res.p1.set(start);
			res.p2.set(end);
			return;
		}
		double inv_DetA = 1.0 / ((detA != 0.0)? detA : 1.0);
		double coefA = -0.5 * inv_DetA;
		
		double p1_x = coefA * ( a_p2p2  * b_p1_x - a_p1p2 * b_p2_x );
		double p2_x = coefA * ( -a_p1p2 * b_p1_x + a_p1p1 * b_p2_x );
		
		double p1_y = coefA * ( a_p2p2  * b_p1_y - a_p1p2 * b_p2_y );
		double p2_y = coefA * ( -a_p1p2 * b_p1_y + a_p1p1 * b_p2_y );
		
		res.p1.set(p1_x, p1_y);
		res.p2.set(p2_x, p2_y);
	}

	
	// TODO fit startPt + controlPt + endPt
	// ------------------------------------------------------------------------
	

//	fitCurveToPoints(n) {
//		  // alright, let's do this thing:
//		  const tm = this.formTMatrix(tvalues, n),
//		        T = tm.T,
//		        Tt = tm.Tt,
//		        M = this.generateBasisMatrix(n),
//		        M1 = M.invert(),
//		        TtT1 = Tt.multiply(T).invert(),
//		        step1 = TtT1.multiply(Tt),
//		        step2 = M1.multiply(step1),
//		        // almost there...
//		        X = new Matrix(points.map((v) => [v.x])),
//		        Cx = step2.multiply(X),
//		        x = Cx.data,
//		        // almost...
//		        Y = new Matrix(points.map((v) => [v.y])),
//		        Cy = step2.multiply(Y),
//		        y = Cy.data,
//		        // last step!
//		        bpoints = x.map((r,i) => ({x: r[0], y: y[i][0]}));
//
//		  return new Bezier(this, bpoints);
//		}
//
//		formTMatrix(row, n) {
//		  // it's actually easier to create the transposed
//		  // version, and then (un)transpose that to get T!
//		  let data = [];
//		  for (var i = 0; i < n; i++) {
//		    data.push(row.map((v) => v ** i));
//		  }
//		  const Tt = new Matrix(n, n, data);
//		  const T = Tt.transpose();
//		  return { T, Tt };
//		}
//
//		generateBasisMatrix(n) {
//		  const M = new Matrix(n, n);
//
//		  // populate the main diagonal
//		  var k = n - 1;
//		  for (let i = 0; i < n; i++) {
//		    M.set(i, i, binomial(k, i));
//		  }
//
//		  // compute the remaining values
//		  for (var c = 0, r; c < n; c++) {
//		    for (r = c + 1; r < n; r++) {
//		      var sign = (r + c) % 2 === 0 ? 1 : -1;
//		      var value = binomial(r, c) * M.get(r, r);
//		      M.set(r, c, sign * value);
//		    }
//		  }
//
//		  return M;
//		}


	
//	https://sourceforge.net/p/lsbezier/code/HEAD/tree/LeastSquaresBezier/src/BezierFit.java#l884
//	
//	/**
//	 * Computes the best bezier fit of the supplied points using a simple RSS minimization.
//	 * Returns a list of 4 points, the control points
//	 * @param points
//	 * @return
//	 */
//	public Point[] bestFit(ArrayList<Point> points){
//		Matrix M = M();
//		Matrix Minv;
//		if(M.det() == 0)Minv = M.invSPD();
//		else Minv = M.inv();
//		Matrix U = U(points);
//		Matrix UT = U.transpose();
//		Matrix X = X(points);
//		Matrix Y = Y(points);
//		
//		Matrix A = UT.mtimes(U);
//		Matrix B;
//		if(A.det() == 0) B = A.invSPD();
//		else B = A.inv();
//		Matrix C = Minv.mtimes(B);
//		Matrix D = C.mtimes(UT);
//		Matrix E = D.mtimes(X);
//		Matrix F = D.mtimes(Y);
//		
//		Point[] P = new Point[4];
//		for(int i = 0; i < 4; i++){
//			double x = E.getAsDouble(i, 0);
//			double y = F.getAsDouble(i, 0);
//			
//			Point p = new Point(x, y);
//			P[i] = p;
//		}
//		
//		return P;
//	}
//	
//	private Matrix Y(List<Point> points){
//		Matrix Y = MatrixFactory.fill(0.0, points.size(), 1);
//		
//		for(int i = 0; i < points.size(); i++)
//			Y.setAsDouble(points.get(i).getY(), i, 0);
//		
//		return Y;
//	}
//	
//	private Matrix X(List<Point> points){
//		Matrix X = MatrixFactory.fill(0.0, points.size(), 1);
//		
//		for(int i = 0; i < points.size(); i++)
//			X.setAsDouble(points.get(i).getX(), i, 0);
//		
//		return X;
//	}
//	
//	private Matrix U(List<Point> points){
//		double[] npls = normalizedPathLengths(points);
//		
//		Matrix U = MatrixFactory.fill(0.0, npls.length, 4);
//		for(int i = 0; i < npls.length; i++){
//			U.setAsDouble(Math.pow(npls[i], 3), i, 0);
//			U.setAsDouble(Math.pow(npls[i], 2), i, 1);
//			U.setAsDouble(Math.pow(npls[i], 1), i, 2);
//			U.setAsDouble(Math.pow(npls[i], 0), i, 3);
//		}
//
//		return U;
//	}
//	
//	private Matrix M(){
//		Matrix M = MatrixFactory.fill(0.0, 4, 4);
//		M.setAsDouble(-1, 0, 0); M.setAsDouble( 3, 0, 1); M.setAsDouble(-3, 0, 2); M.setAsDouble( 1, 0, 3); 
//		M.setAsDouble( 3, 1, 0); M.setAsDouble(-6, 1, 1); M.setAsDouble( 3, 1, 2); M.setAsDouble( 0, 1, 3);
//		M.setAsDouble(-3, 2, 0); M.setAsDouble( 3, 2, 1); M.setAsDouble( 0, 2, 2); M.setAsDouble( 0, 2, 3);
//		M.setAsDouble( 1, 3, 0); M.setAsDouble( 0, 3, 1); M.setAsDouble( 0, 3, 2); M.setAsDouble( 0, 3, 3);
//		return M;
//	}
//

}
