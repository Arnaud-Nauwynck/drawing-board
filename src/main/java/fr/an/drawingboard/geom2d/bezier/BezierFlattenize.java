package fr.an.drawingboard.geom2d.bezier;

import java.util.List;

import fr.an.drawingboard.geom2d.CubicBezier2D;
import fr.an.drawingboard.geom2d.Pt2D;
import fr.an.drawingboard.geom2d.QuadBezier2D;
import fr.an.drawingboard.geom2d.utils.DistinctPt2DListBuilder;
import lombok.val;

public class BezierFlattenize {

	// Quad Bezier Flattenize
	// ------------------------------------------------------------------------
	
	public static void flattenizeQuadBezier(DistinctPt2DListBuilder res,
			QuadBezier2D bezier, double prec) {
		// TODO naive impl: split in N steps
		flattenizeQuadBezier_fixedN(res, bezier, 20);
	}
	
	/** simple fixed split */
	public static List<Pt2D> flattenizeQuadBezier_fixedN(QuadBezier2D bezier, int N) {
		val res = new DistinctPt2DListBuilder();
		flattenizeQuadBezier_fixedN(bezier, N);
		return res.build();
	}
	
	/** simple fixed split */
	public static void flattenizeQuadBezier_fixedN(DistinctPt2DListBuilder res,
			QuadBezier2D bezier, int N) {
		res.add(bezier.startPt);
		final double ds = 1.0 / (N-1);
		double s = ds;
		for(int i = 0; i < N; i++) {
			Pt2D pt = bezier.eval(s);
			res.add(pt);
			s += ds;
		}
		res.add(bezier.endPt);
	}

	// Cubic Bezier Flattenize
	// ------------------------------------------------------------------------
	
	public static void flattenizeCubicBezier(DistinctPt2DListBuilder res,
			CubicBezier2D bezier, double prec) {
		// TODO naive impl: split in N steps
		flattenizeCubicBezier_fixedN(res, bezier, 20);
	}

	/** simple fixed split */
	public static List<Pt2D> flattenizeCubicBezier_fixedN(CubicBezier2D bezier, int N) {
		val res = new DistinctPt2DListBuilder();
		flattenizeCubicBezier_fixedN(bezier, N);
		return res.build();
	}
	
	/** simple fixed split */
	public static void flattenizeCubicBezier_fixedN(DistinctPt2DListBuilder res,
			CubicBezier2D bezier, int N) {
		res.add(bezier.startPt);
		final double ds = 1.0 / (N-1);
		double s = ds;
		for(int i = 0; i < N; i++) {
			Pt2D pt = bezier.eval(s);
			res.add(pt);
			s += ds;
		}
		res.add(bezier.endPt);
	}
	

}
