package fr.an.drawingboard.geom2d.bezier;

import org.junit.Assert;
import org.junit.Test;

import fr.an.drawingboard.geom2d.CubicBezier2D;
import fr.an.drawingboard.geom2d.Pt2D;

public class BezierMatrixSplitTest {
	private static final double PREC = 1e-9;
	
	@Test
	public void testMiddleSplitCubicBezier() {
		// given
		double start = 0.25, end = 0.75;
		Pt2D p1 = new Pt2D(0, 100);
		Pt2D p2 = new Pt2D(100, 0);
		Pt2D p3 = new Pt2D(0, 0);
		Pt2D p4 = new Pt2D(100, 200);
		
		CubicBezier2D res = new CubicBezier2D();
		// when
		BezierMatrixSplit.middleSplitCubicBezier(res, start, end, p1, p2, p3, p4);
		// then
		CubicBezier2D resCheck = new CubicBezier2D();
		BezierMatrixSplit.middleSplitCubicBezier_check(resCheck, start, end, p1, p2, p3, p4);
		assertEquals(res.startPt, resCheck.startPt);
		assertEquals(res.p1, resCheck.p1);
		assertEquals(res.p2, resCheck.p2);
		assertEquals(res.endPt, resCheck.endPt);
	}
	
	private static void assertEquals(Pt2D expected, Pt2D actual) {
		Assert.assertEquals(expected.x, actual.x, PREC);
		Assert.assertEquals(expected.y, actual.y, PREC);
	}
}
