package fr.an.drawingboard.recognizer.trace;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.google.common.collect.ImmutableList;

import fr.an.drawingboard.geom2d.Pt2D;
import fr.an.drawingboard.geom2d.WeightedPt2D;

public class WeightedPtsBuilderTest {

	private static final double PREC = 1e-9;
	
	@Test
	public void testPtsToWeightedPts_polygonalDistance_simple() {
		// given
		Pt2D p0 = new Pt2D(0, 0), p1 = new Pt2D(1, 0), p2 = new Pt2D(2, 0), p3 = new Pt2D(3, 0);
		List<Pt2D> pts = ImmutableList.of(p0, p1, p2, p3);
		// when
		List<WeightedPt2D> res = WeightedPtsBuilder.ptsToWeightedPts_polygonalDistance(pts);
		// then
		Assert.assertEquals(4, res.size());
		WeightedPt2D wp0 = res.get(0), wp1 = res.get(1), wp2 = res.get(2), wp3 = res.get(3);
		Assert.assertSame(p0, wp0.pt);
		Assert.assertSame(p1, wp1.pt);
		Assert.assertSame(p2, wp2.pt);
		Assert.assertSame(p3, wp3.pt);
		Assert.assertEquals(1.0/3, wp0.weight, PREC);
		Assert.assertEquals(1.0/6, wp1.weight, PREC);
		Assert.assertEquals(1.0/6, wp2.weight, PREC);
		Assert.assertEquals(1.0/3, wp3.weight, PREC);
	}
	
	@Test
	public void testPtsToWeightedPts_polygonalDistance() {
		// given
		Pt2D p0 = new Pt2D(0, 0), p1 = new Pt2D(1, 0), p2 = new Pt2D(9, 0), p3 = new Pt2D(10, 0);
		List<Pt2D> pts = ImmutableList.of(p0, p1, p2, p3);
		// when
		List<WeightedPt2D> res = WeightedPtsBuilder.ptsToWeightedPts_polygonalDistance(pts);
		// then
		Assert.assertEquals(4, res.size());
		WeightedPt2D wp0 = res.get(0), wp1 = res.get(1), wp2 = res.get(2), wp3 = res.get(3);
		Assert.assertSame(p0, wp0.pt);
		Assert.assertSame(p1, wp1.pt);
		Assert.assertSame(p2, wp2.pt);
		Assert.assertSame(p3, wp3.pt);
		Assert.assertEquals(1.0/10, wp0.weight, PREC);
		Assert.assertEquals(4.0/10, wp1.weight, PREC);
		Assert.assertEquals(4.0/10, wp2.weight, PREC);
		Assert.assertEquals(1.0/10, wp3.weight, PREC);
	}
}
