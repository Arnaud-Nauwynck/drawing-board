package fr.an.drawingboard.geom2d.bezier;

import org.junit.Assert;
import org.junit.Test;

import fr.an.drawingboard.geom2d.BoundingRect2D.BoundingRect2DBuilder;
import fr.an.drawingboard.geom2d.CubicBezier2D;
import fr.an.drawingboard.geom2d.Pt2D;
import fr.an.drawingboard.geom2d.QuadBezier2D;
import lombok.val;

public class BezierEnclosingRect2DUtilTest {

	private static final double PREC = 1e-9;
	
	/**
	 * <PRE>
	 *        + p0
	 *         \
	 *          \
	 *         + + p1
	 *          /
	 *         /
	 *        + p2
	 * </PRE>
	 */
	@Test
	public void testBestEnclosing_QuadBezier_x() {
		// given
		BoundingRect2DBuilder res = new BoundingRect2DBuilder();
		val p0 = new Pt2D(0, 0), p1 = new Pt2D(1, 1), p2 = new Pt2D(0, 2);
		// when
		BezierEnclosingRect2DUtil.bestEnclosing_QuadBezier(res, p0, p1, p2);
		// then
		val r = res.build();
		assertEquals(0, r.minx);
		double maxX = QuadBezier2D.evalB(0.5, p0.x, p1.x, p2.x);
		assertEquals(0.5, maxX);
		assertEquals(maxX, r.maxx);
		assertEquals(0, r.miny);
		assertEquals(2, r.maxy);
	}

	@Test
	public void testBestEnclosing_QuadBezier_y() {
		// given
		BoundingRect2DBuilder res = new BoundingRect2DBuilder();
		val p0 = new Pt2D(0, 0), p1 = new Pt2D(1, 1), p2 = new Pt2D(2, 0);
		// when
		BezierEnclosingRect2DUtil.bestEnclosing_QuadBezier(res, p0, p1, p2);
		// then
		val r = res.build();
		assertEquals(0, r.minx);
		assertEquals(2, r.maxx);
		assertEquals(0, r.miny);
		assertEquals(QuadBezier2D.evalB(0.5, p0.y, p1.y, p2.y), r.maxy);
	}
	
	/**
	 * <PRE>
	 *        + p0
	 *         \
	 *          \
	 *           + p1
	 *           |
	 *          +|
	 *           |
	 *           +
	 *          /
	 *         /
	 *        + p2
	 * </PRE>
	 */
	@Test
	public void testBestEnclosing_CubicBezier_linear_x() {
		// given
		BoundingRect2DBuilder res = new BoundingRect2DBuilder();
		val p0 = new Pt2D(0, 0), p1 = new Pt2D(1, 1), p2 = new Pt2D(1, 2), p3 = new Pt2D(0, 3);
		// when
		BezierEnclosingRect2DUtil.bestEnclosing_CubicBezier(res, p0, p1, p2, p3);
		// then
		val r = res.build();
		assertEquals(0, r.minx);
		double maxX = CubicBezier2D.evalB(0.5, p0.x, p1.x, p2.x, p3.x);
		assertEquals(0.75, maxX);
		assertEquals(maxX, r.maxx);
		assertEquals(0, r.miny);
		assertEquals(3, r.maxy);
	}

	/**
	 * <PRE>
	 *        + p0
	 *         \
	 *          \
	 *           + p1
	 *           |
	 *          +|
	 *           |
	 *           +
	 *          /
	 *          |
	 *         /
	 *         |
	 *         /
	 *        + p2
	 * </PRE>
	 */
	
	@Test
	public void testBestEnclosing_CubicBezier_x() {
		// given
		BoundingRect2DBuilder res = new BoundingRect2DBuilder();
		val p0 = new Pt2D(0, 0), p1 = new Pt2D(1, 1), p2 = new Pt2D(1, 2), p3 = new Pt2D(.5, 5);
		// when
		BezierEnclosingRect2DUtil.bestEnclosing_CubicBezier(res, p0, p1, p2, p3);
		// then
		val r = res.build();
		assertEquals(0, r.minx);
		double maxX = CubicBezier2D.evalB(0.5, p0.x, p1.x, p2.x, p3.x);
//		assertEquals(0.8125, maxX);
		assertEquals(maxX, r.maxx);
		assertEquals(0, r.miny);
		assertEquals(3, r.maxy);
	}

	
	private static void assertEquals(double expected, double actual) {
		Assert.assertEquals(expected, actual, PREC);
	}

}
