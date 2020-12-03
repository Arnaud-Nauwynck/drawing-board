package fr.an.drawingboard.math.numeric;

import java.util.HashMap;
import java.util.Map;

import fr.an.drawingboard.geom2d.Pt2D;
import fr.an.drawingboard.math.expr.Expr;
import fr.an.drawingboard.math.expr.Expr.VariableExpr;
import fr.an.drawingboard.math.expr.VarDef;
import fr.an.drawingboard.model.shapedef.PtExpr;
import lombok.val;

public class NumericEvalCtx {

	/** for VariableExpr */
	public final Map<VarDef, Double> varValues = new HashMap<>();

	// --------------------------------------------------------------------------------------------

	public void put(VarDef varDef, double value) {
		varValues.put(varDef, value);
	}

	public void put(VariableExpr varExpr, double value) {
		put(varExpr.varDef, value);
	}

	public double get(VarDef varDef) {
		Double found = varValues.get(varDef);
		if (null == found) {
			throw new IllegalStateException("no numerical value for var '" + varDef + "'");
		}
		return found.doubleValue();
	}

	// @Deprecated?
	public VarDef findVarByName(String name) {
		for(val varDef : varValues.keySet()) {
			if (varDef.name.equals(name)) {
				return varDef; 
			}
		}
		return null;
	}

	public double evalExpr(Expr expr) {
		// TODO.. may gather stats, and lazily generate faster code..
		return SimpleExprDoubleCtxFunc.evalExpr(expr, this);
	}

	public Pt2D evalPtExpr(PtExpr ptExpr) {
		double x = evalExpr(ptExpr.x);
		double y = evalExpr(ptExpr.y);
		return new Pt2D(x, y);
	}


}
