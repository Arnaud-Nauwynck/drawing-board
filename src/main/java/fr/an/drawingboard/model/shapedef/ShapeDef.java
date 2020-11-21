package fr.an.drawingboard.model.shapedef;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import fr.an.drawingboard.model.expr.Expr;
import fr.an.drawingboard.model.expr.Expr.LiteralDoubleExpr;
import fr.an.drawingboard.model.expr.Expr.MultExpr;
import fr.an.drawingboard.model.expr.Expr.SumExpr;
import fr.an.drawingboard.model.var.ParamDef;
import fr.an.drawingboard.model.var.ParametrizableEltDef;
import lombok.AllArgsConstructor;

public class ShapeDef extends ParametrizableEltDef {

	public final String name;
	
	public final List<MultiStrokeDef> gestures = new ArrayList<>();

	public ShapeDef(String name) {
		this.name = name;
		addParamDef("x");
		addParamDef("y");
		addParamDef("w");
		addParamDef("h");
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
	
	/**
	 * <PRE>
	 *           x
     *           |
     *    PtUL ------ PtUR   /\
     *     |            |     |
     *  y- |            |     h
     *     |            |     |
     *    PtDL ------ PtDR   \/
     * 
     *     <-----w----->
	 * </PRE>
	 */
	@AllArgsConstructor
	public static class CoordRectExpr {
		public final PtExpr ptUL;
		public final PtExpr ptUR;
		public final PtExpr ptDR;
		public final PtExpr ptDL;
	}

	public CoordRectExpr getCoordRectExpr() {
		Expr x = getParam("x").expr; 
		Expr y = getParam("y").expr;
		Expr w = getParam("w").expr;
		Expr h = getParam("h").expr;
		
		Expr val05 = LiteralDoubleExpr.VAL_05;
		Expr valMinus05 = LiteralDoubleExpr.VAL_minus05;
		Expr xmin = new SumExpr(x, new MultExpr(valMinus05, w));
		Expr xmax = new SumExpr(x, new MultExpr(val05, w));
		Expr ymin = new SumExpr(y, new MultExpr(valMinus05, h));
		Expr ymax = new SumExpr(y, new MultExpr(val05, h));

		PtExpr ptUL = new PtExpr(xmin, ymin);
		PtExpr ptUR = new PtExpr(xmax, ymin);
		PtExpr ptDR = new PtExpr(xmax, ymax);
		PtExpr ptDL = new PtExpr(xmin, ymax);
		return new CoordRectExpr(ptUL, ptUR, ptDR, ptDL);
	}
	
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

}
