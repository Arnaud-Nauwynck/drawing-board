package fr.an.drawingboard.geom2d.bezier;

import org.junit.Test;

import fr.an.drawingboard.geom2d.CubicBezier2D;
import fr.an.drawingboard.geom2d.Pt2D;
import fr.an.drawingboard.geom2d.bezier.PtToBezierDistanceMinSolver.PtToCurveDistanceMinSolverResult;

public class PtToBezierDistanceMinSolverTest {


	@Test
	public void testProjPtToCubicBezier() {
		// given
		CubicBezier2D bezier = new CubicBezier2D(new Pt2D(100, 0), new Pt2D(200, 100), new Pt2D(200, 200), new Pt2D(100, 300));
		Pt2D pt = new Pt2D(300, 200);
		PtToCurveDistanceMinSolverResult result = new PtToCurveDistanceMinSolverResult();
		// when
		PtToBezierDistanceMinSolver.projPtToCubicBezier(result, pt, bezier);
		// then
		// TODO
	}
	
}
