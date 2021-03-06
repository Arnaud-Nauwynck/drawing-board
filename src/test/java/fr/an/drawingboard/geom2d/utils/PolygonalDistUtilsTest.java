package fr.an.drawingboard.geom2d.utils;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.google.common.collect.ImmutableList;

import fr.an.drawingboard.geom2d.ParamWeightedPt2D;
import fr.an.drawingboard.geom2d.Pt2D;
import fr.an.drawingboard.geom2d.WeightedPt2D;

public class PolygonalDistUtilsTest {

	private static final double PREC = 1e-9;
	
	@Test
	public void testPtsToWeightedPts_polygonalDistance_simple() {
		// given
		Pt2D p0 = new Pt2D(0, 0), p1 = new Pt2D(1, 0), p2 = new Pt2D(2, 0), p3 = new Pt2D(3, 0);
		List<Pt2D> pts = ImmutableList.of(p0, p1, p2, p3);
		// when
		List<ParamWeightedPt2D> res = PolygonalDistUtils.ptsParamWeightedPts_polygonalDistance(pts).pts;
		// then
		Assert.assertEquals(4, res.size());
		ParamWeightedPt2D wp0 = res.get(0), wp1 = res.get(1), wp2 = res.get(2), wp3 = res.get(3);
		Assert.assertSame(p0, wp0.pt);
		Assert.assertSame(p1, wp1.pt);
		Assert.assertSame(p2, wp2.pt);
		Assert.assertSame(p3, wp3.pt);
		Assert.assertEquals(1.0/4, wp0.weight, PREC);
		Assert.assertEquals(1.0/4, wp1.weight, PREC);
		Assert.assertEquals(1.0/4, wp2.weight, PREC);
		Assert.assertEquals(1.0/4, wp3.weight, PREC);
		
		Assert.assertEquals(0.0, wp0.distRatio, PREC);
		Assert.assertEquals(1.0/3, wp1.distRatio, PREC);
		Assert.assertEquals(2.0/3, wp2.distRatio, PREC);
		Assert.assertEquals(1.0, wp3.distRatio, PREC);
	}
	
	@Test
	public void testPtsToWeightedPts_polygonalDistance() {
		// given
		Pt2D p0 = new Pt2D(0, 0), p1 = new Pt2D(1, 0), p2 = new Pt2D(9, 0), p3 = new Pt2D(10, 0);
		List<Pt2D> pts = ImmutableList.of(p0, p1, p2, p3);
		// when
		List<ParamWeightedPt2D> res = PolygonalDistUtils.ptsParamWeightedPts_polygonalDistance(pts).pts;
		// then
		Assert.assertEquals(4, res.size());
		ParamWeightedPt2D wp0 = res.get(0), wp1 = res.get(1), wp2 = res.get(2), wp3 = res.get(3);
		Assert.assertSame(p0, wp0.pt);
		Assert.assertSame(p1, wp1.pt);
		Assert.assertSame(p2, wp2.pt);
		Assert.assertSame(p3, wp3.pt);
//		Assert.assertEquals(1.0/10, wp0.weight, PREC);
//		Assert.assertEquals(4.0/10, wp1.weight, PREC);
//		Assert.assertEquals(4.0/10, wp2.weight, PREC);
//		Assert.assertEquals(1.0/10, wp3.weight, PREC);
	}
}
