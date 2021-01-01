package fr.an.drawingboard.model.shapedef;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import fr.an.drawingboard.model.shapedef.paramdef.ParamCategoryRegistry;
import fr.an.drawingboard.model.shapedef.paramdef.ParamDef;
import fr.an.drawingboard.model.shapedef.paramdef.ParametrizableEltDef;
import fr.an.drawingboard.recognizer.initialParamEstimators.InitialParamForShapeEstimator;
import lombok.AllArgsConstructor;

public class ShapeDef extends ParametrizableEltDef {

	public final String name;
	
	public final List<GestureDef> gestures = new ArrayList<>();

	// --------------------------------------------------------------------------------------------

	public ShapeDef(String name, ParamCategoryRegistry paramCategories) {
		this.name = name;
		addParamDef("x", paramCategories.STD_X);
		addParamDef("y", paramCategories.STD_Y);
		addParamDef("w", paramCategories.STD_W);
		addParamDef("h", paramCategories.STD_H);
	}

	// --------------------------------------------------------------------------------------------
	
	public GestureDef addGesture(InitialParamForShapeEstimator initalParamEstimator) {
		GestureDef res = new GestureDef(this, initalParamEstimator);
		gestures.add(res);
		return res;
	}

	public GestureDef addGesture_Segments(InitialParamForShapeEstimator initalParamEstimator, PtExpr... pts) {
		return addGesture_Segments(initalParamEstimator, Arrays.asList(pts));
	}
	
	public GestureDef addGesture_Segments(InitialParamForShapeEstimator initalParamEstimator, List<PtExpr> pts) {
		GestureDef res = addGesture(initalParamEstimator);
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
		public final ParamDef x;
		public final ParamDef y;
		public final ParamDef w;
		public final ParamDef h;
	}
	public CoordParams getCoordParams() {
		return new CoordParams(getParam("x"), getParam("y"), getParam("w"), getParam("h"));
	}
	
	public RectExpr getCoordRectExpr() {
		return RectExpr.fromXYHW(getParamExpr("x"), getParamExpr("y"), getParamExpr("w"), getParamExpr("h"));
	}

}
