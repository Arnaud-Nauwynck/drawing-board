package fr.an.drawingboard.recognizer.initialParamEstimators;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import fr.an.drawingboard.geom2d.Pt2D;
import fr.an.drawingboard.math.expr.Expr;
import fr.an.drawingboard.math.numeric.NumericEvalCtx;
import fr.an.drawingboard.model.shapedef.PtExpr;
import fr.an.drawingboard.model.shapedef.paramdef.ParamDef;
import lombok.val;

public class ParamEvalCtx {

	private final Map<String,ParamDef> paramsByName = new LinkedHashMap<>();
	
	public final NumericEvalCtx evalCtx = new NumericEvalCtx();
	// => public final Map<VarDef, Expr> varValues = new HashMap<>();

	// --------------------------------------------------------------------------------------------

	public Map<ParamDef,Double> getParamValuesCopy() {
		Map<ParamDef,Double> res = new HashMap<>();
		for(val param: paramsByName.values()) {
			double value = get(param);
			res.put(param, value);
		}
		return res;
	}

	public ParamDef paramByName(String name) {
		return paramsByName.get(name);
	}

	public double get(ParamDef param) {
		return evalCtx.evalExpr(param.expr);
	}

	
	public void put(ParamDef paramDef, double value) {
		paramsByName.put(paramDef.name, paramDef);
		evalCtx.put(paramDef.varDef, value);
	}

	
	public Pt2D evalPtExpr(PtExpr ptExpr) {
		return evalCtx.evalPtExpr(ptExpr);
	}

	public double evalExpr(Expr expr) {
		return evalCtx.evalExpr(expr);
	}
	
}
