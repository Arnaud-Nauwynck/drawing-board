package fr.an.drawingboard.model.shapedef;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import fr.an.drawingboard.math.expr.VarDef;
import fr.an.drawingboard.recognizer.initialParamEstimators.InitialParamForShapeEstimator;
import lombok.AllArgsConstructor;

public class ShapeDef extends ParametrizableEltDef {

	public final String name;
	
	public final List<GesturePathesDef> gestures = new ArrayList<>();

	// --------------------------------------------------------------------------------------------

	public ShapeDef(String name) {
		this.name = name;
		addVarDef("x");
		addVarDef("y");
		addVarDef("w");
		addVarDef("h");
	}

	// --------------------------------------------------------------------------------------------
	
	public GesturePathesDef addGesture(InitialParamForShapeEstimator initalParamEstimator) {
		GesturePathesDef res = new GesturePathesDef(this, initalParamEstimator);
		gestures.add(res);
		return res;
	}

	public GesturePathesDef addGesture_Segments(InitialParamForShapeEstimator initalParamEstimator, PtExpr... pts) {
		return addGesture_Segments(initalParamEstimator, Arrays.asList(pts));
	}
	
	public GesturePathesDef addGesture_Segments(InitialParamForShapeEstimator initalParamEstimator, List<PtExpr> pts) {
		GesturePathesDef res = addGesture(initalParamEstimator);
		PtExpr prevPt = pts.get(0);
		for(int i = 1; i < pts.size(); i++) {
			PtExpr pt = pts.get(i);
			res.addPath_Segment(prevPt, pt);
			prevPt = pt;
		}
		return res;
	}

	
	@AllArgsConstructor
	public static class CoordParams {
		public final VarDef x;
		public final VarDef y;
		public final VarDef w;
		public final VarDef h;
	}
	public CoordParams getCoordParams() {
		return new CoordParams(getParam("x"), getParam("y"), getParam("w"), getParam("h"));
	}
	
	public RectExpr getCoordRectExpr() {
		return RectExpr.fromXYHW(getParamExpr("x"), getParamExpr("y"), getParamExpr("w"), getParamExpr("h"));
	}

}
