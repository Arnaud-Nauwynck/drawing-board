package fr.an.drawingboard.model.varctx;

import fr.an.drawingboard.model.shapedef.paramdef.ParamCategory;
import lombok.Getter;

@Getter
public class DrawingVarDef {

	private DrawingCtxTreeNode ctxNode;
	
	public final String varName;

	public final ParamCategory paramCategory;

	private double currValue;

	public DrawingVarDef(DrawingCtxTreeNode ctxNode, String varName, ParamCategory paramCategory, double currValue) {
		this.ctxNode = ctxNode;
		this.varName = varName;
		this.paramCategory = paramCategory;
		this.currValue = currValue;
	}
	
	
}
