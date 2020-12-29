package fr.an.drawingboard.model.drawingelt;

import java.util.LinkedHashMap;
import java.util.Map;

import fr.an.drawingboard.math.numeric.NumericEvalCtx;
import fr.an.drawingboard.model.shapedef.ShapeDef;
import fr.an.drawingboard.model.shapedef.obj.ShapeObj;
import fr.an.drawingboard.model.shapedef.paramdef.ParamDef;
import fr.an.drawingboard.model.varctx.DrawingCtxTreeNode;
import fr.an.drawingboard.model.varctx.DrawingVarDef;
import fr.an.drawingboard.ui.impl.GcRendererHelper;
import lombok.Getter;
import lombok.val;

public class ShapeDrawingElement extends DrawingElement {
	
	@Getter
	private final ShapeDef shapeDef;
	@Getter
	private final ShapeObj shapeCtxEval;

	private final Map<ParamDef,DrawingVarDef> paramBindings;
	
	// ------------------------------------------------------------------------
	
	public ShapeDrawingElement(DrawingCtxTreeNode ctxNode, ShapeDef shapeDef, Map<ParamDef,DrawingVarDef> paramBindings) {
		super(ctxNode);
		this.shapeDef = shapeDef;
		this.shapeCtxEval = new ShapeObj(shapeDef);
		// this.evalCtx = new NumericEvalCtx();
		this.paramBindings = new LinkedHashMap<ParamDef,DrawingVarDef>(paramBindings); 
		// check params are bound, and no extra params 
		for(ParamDef paramDef: shapeDef.getParams().values()) {
			val binding = paramBindings.get(paramDef);
			if (null == binding) {
				throw new IllegalArgumentException("missing param:" + paramDef);
			}
		}
		if (paramBindings.size() != shapeDef.getParams().size()) {
			throw new IllegalArgumentException("extra param for shape");
		}
		this.reevalWithVars();
	}

	// ------------------------------------------------------------------------

	public void updateParamBinding(ParamDef paramDef, DrawingVarDef binding) {
		if (paramDef != shapeDef.getParam(paramDef.name)) {
			throw new IllegalArgumentException("unknown param for shapeDef");
		}
		paramBindings.put(paramDef, binding);
		reevalWithVars();
	}

	@Override
	public void reevalWithVars() {
		NumericEvalCtx evalCtx = new NumericEvalCtx();
		for(val e : paramBindings.entrySet()) {
			ParamDef param = e.getKey();
			DrawingVarDef varDef = e.getValue();
			evalCtx.put(param.varDef, varDef.getCurrValue());
		}
		shapeCtxEval.eval(evalCtx);
	}

	@Override
	public void draw(GcRendererHelper gc) {
		gc.draw(shapeCtxEval);
	}
	
	
}
