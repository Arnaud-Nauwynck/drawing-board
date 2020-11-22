package fr.an.drawingboard.model.shapedef;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import fr.an.drawingboard.model.var.ParamDef;
import fr.an.drawingboard.model.var.ParametrizableEltDef;
import lombok.AllArgsConstructor;

public class ShapeDef extends ParametrizableEltDef {

	public final String name;
	
	public final List<MultiStrokeDef> gestures = new ArrayList<>();

	// --------------------------------------------------------------------------------------------

	public ShapeDef(String name) {
		this.name = name;
		addParamDef("x");
		addParamDef("y");
		addParamDef("w");
		addParamDef("h");
	}

	// --------------------------------------------------------------------------------------------
	
	public MultiStrokeDef addGesture() {
		MultiStrokeDef res = new MultiStrokeDef(this);
		gestures.add(res);
		return res;
	}

	public MultiStrokeDef addGesture_Segments(PtExpr... pts) {
		return addGesture_Segments(Arrays.asList(pts));
	}
	
	public MultiStrokeDef addGesture_Segments(List<PtExpr> pts) {
		MultiStrokeDef res = addGesture();
		PtExpr prevPt = pts.get(0);
		for(int i = 1; i < pts.size(); i++) {
			PtExpr pt = pts.get(i);
			res.addStroke_Segment(prevPt, pt);
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
