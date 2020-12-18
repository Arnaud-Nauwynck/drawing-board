package fr.an.drawingboard.model.drawingelt;

import java.util.Map;

import fr.an.drawingboard.math.expr.Expr;
import fr.an.drawingboard.math.expr.VarDef;
import fr.an.drawingboard.math.numeric.NumericEvalCtx;
import fr.an.drawingboard.model.shapedef.ShapeDef;
import fr.an.drawingboard.model.shapedef.ctxeval.ShapeCtxEval;
import fr.an.drawingboard.model.varctx.DrawingVarCtxNode;
import fr.an.drawingboard.ui.impl.GcRendererHelper;
import lombok.Getter;
import lombok.val;

public class ShapeDrawingElement extends DrawingElement {
	
	@Getter
	private final ShapeDef shapeDef;
	@Getter
	private final ShapeCtxEval shapeCtxEval;

	private final NumericEvalCtx shapeVarsCtx;
	
	// ------------------------------------------------------------------------
	
	public ShapeDrawingElement(DrawingVarCtxNode ctxNode, ShapeDef shapeDef, Map<VarDef,Expr> shapeDefVarExpr) {
		super(ctxNode);
		this.shapeDef = shapeDef;
		this.shapeCtxEval = new ShapeCtxEval(shapeDef);
		this.shapeVarsCtx = new NumericEvalCtx();
		for(val varDef: shapeDef.getParams().values()) {
			Expr expr = shapeDefVarExpr.get(varDef);
			if (null == expr) {
				throw new IllegalArgumentException("missing expr for var:" + varDef);
			}
			shapeDefVarExpr.put(varDef, expr);
		}
		if (shapeDefVarExpr.size() != shapeDef.getParams().size()) {
			throw new IllegalArgumentException("extra unknown var for shapeDef");
		}
	}

	// ------------------------------------------------------------------------

	public void updateShapeVarExpr(VarDef varDef, Expr expr) {
		if (varDef != shapeDef.getParam(varDef.name)) {
			throw new IllegalArgumentException("unknown var for shapeDef");
		}
		shapeVarsCtx.put(varDef, expr);
		reevalWithVars();
	}

	@Override
	public void reevalWithVars() {
		shapeCtxEval.eval(shapeVarsCtx);
	}

	@Override
	public void draw(GcRendererHelper gc) {
		gc.draw(shapeCtxEval);
	}
	
	
}
