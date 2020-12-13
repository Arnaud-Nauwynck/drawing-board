package fr.an.drawingboard.geom2d.bezier;

import org.junit.Assert;
import org.junit.Test;

import fr.an.drawingboard.geom2d.CubicBezier2D;
import fr.an.drawingboard.geom2d.Pt2D;
import fr.an.drawingboard.geom2d.QuadBezier2D;
import fr.an.drawingboard.math.numeric.polynomial.DoublePolynom;

public class PtToBezierDistanceEvalTest {
	
	private static final double PREC = 1e-9;

	@Test
	public void testSquareDistPtToQuadBezier_asPolynomOnS() {
		// given
		QuadBezier2D bezier = new QuadBezier2D(new Pt2D(100, 0), new Pt2D(200, 100), new Pt2D(100, 300));
		Pt2D pt = new Pt2D(300, 200);
		// when
		DoublePolynom squareDistS = PtToBezierDistanceEval.squareDistPtToQuadBezier_asPolynomOnS(pt, bezier);
		// then
		for(double s = 0.0; s <= 1.0; s+= 0.1) {
			double squareDist = squareDistS.eval(s);
			Pt2D bezierPs = bezier.eval(s);
			double checkSquareDist = bezierPs.squareDistTo(pt);
			Assert.assertEquals(checkSquareDist, squareDist, PREC);
		}
	}

	@Test
	public void testSquareDistPtToCubicBezier_asPolynomOnS() {
		// given
		CubicBezier2D bezier = new CubicBezier2D(new Pt2D(100, 0), new Pt2D(200, 100), new Pt2D(200, 200), new Pt2D(100, 300));
		Pt2D pt = new Pt2D(300, 200);
		// when
		DoublePolynom squareDistS = PtToBezierDistanceEval.squareDistPtToCubicBezier_asPolynomOnS(pt, bezier);
		// then
		for(double s = 0.0; s <= 1.0; s+= 0.1) {
			double squareDist = squareDistS.eval(s);
			Pt2D bezierPs = bezier.eval(s);
			double checkSquareDist = bezierPs.squareDistTo(pt);
			Assert.assertEquals(checkSquareDist, squareDist, PREC);
		}
	}
}
