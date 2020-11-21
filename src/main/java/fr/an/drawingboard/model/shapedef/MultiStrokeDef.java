package fr.an.drawingboard.model.shapedef;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import fr.an.drawingboard.model.shapedef.StrokePathElementDef.CubicBezierStrokePathElementDef;
import fr.an.drawingboard.model.shapedef.StrokePathElementDef.DiscretePointsStrokePathElementDef;
import fr.an.drawingboard.model.shapedef.StrokePathElementDef.QuadBezierStrokePathElementDef;
import fr.an.drawingboard.model.shapedef.StrokePathElementDef.SegmentStrokePathElementDef;
import fr.an.drawingboard.model.var.ParametrizableEltDef;

public class MultiStrokeDef extends ParametrizableEltDef {

	public List<StrokeDef> strokes = new ArrayList<>();

	public MultiStrokeDef(ParametrizableEltDef parent) {
		super(parent);
	}

	public StrokeDef addStroke(List<StrokePathElementDef> pathElements) {
		StrokeDef res = new StrokeDef(pathElements);
		strokes.add(res);
		return res;
	}

	public StrokeDef addStroke(StrokePathElementDef... pathElements) {
		return addStroke(Arrays.asList(pathElements));
	}

	public StrokeDef addStroke(StrokePathElementDef pathElement) {
		return addStroke(Arrays.asList(pathElement));
	}

	public StrokeDef addStroke_Segment(PtExpr startPt, PtExpr endPt) {
		return addStroke(new SegmentStrokePathElementDef(startPt, endPt));
	}

	public StrokeDef addStroke_DiscreteLine(List<PtExpr> pts) {
		return addStroke(new DiscretePointsStrokePathElementDef(pts));
	}
	
	public StrokeDef addStroke_QuadBezier(PtExpr startPt, PtExpr controlPt1, PtExpr endPt) {
		return addStroke(new QuadBezierStrokePathElementDef(startPt, controlPt1, endPt));
	}

	public StrokeDef addStroke_CubicBezier(PtExpr startPt, PtExpr controlPt1, PtExpr controlPt2, PtExpr endPt) {
		return addStroke(new CubicBezierStrokePathElementDef(startPt, controlPt1, controlPt2, endPt));
	}

}
