package fr.an.drawingboard.math.numeric;

import java.util.HashMap;
import java.util.Map;

import fr.an.drawingboard.geom2d.Pt2D;
import fr.an.drawingboard.math.expr.Expr;
import fr.an.drawingboard.math.expr.Expr.LiteralDoubleExpr;
import fr.an.drawingboard.math.expr.Expr.VariableExpr;
import fr.an.drawingboard.math.expr.VarDef;
import fr.an.drawingboard.model.shapedef.PtExpr;
import lombok.val;

public class NumericEvalCtx {

	public final Map<VarDef, Expr> varValues = new HashMap<>();

	// --------------------------------------------------------------------------------------------

	public void put(VarDef varDef, Expr expr) {
		varValues.put(varDef, expr);
	}

	public void put(VarDef varDef, double value) {
		put(varDef, new LiteralDoubleExpr(value));
	}

	public void put(VariableExpr varExpr, double value) {
		put(varExpr.varDef, value);
	}

	public Expr get(VarDef varDef) {
		Expr found = varValues.get(varDef);
		if (null == found) {
			throw new IllegalStateException("no expr for var '" + varDef + "'");
		}
		return found;
	}
	
	public double getEval(VarDef varDef) {
		Expr found = get(varDef);
		if (found instanceof LiteralDoubleExpr) {
			return ((LiteralDoubleExpr) found).value;
		}
		return SimpleExprDoubleCtxFunc.evalExpr(found, this);
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

	public void evalPtExpr(Pt2D res, PtExpr ptExpr) {
		double x = evalExpr(ptExpr.x);
		double y = evalExpr(ptExpr.y);
		res.set(x, y);
	}
	

}
