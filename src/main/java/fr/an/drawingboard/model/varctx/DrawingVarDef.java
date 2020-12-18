package fr.an.drawingboard.model.varctx;

import lombok.Getter;

@Getter
public class DrawingVarDef {

	private DrawingVarCtxNode ctxNode;
	
	public final String varName;

	private double currValue;

	public DrawingVarDef(DrawingVarCtxNode ctxNode, String varName, double currValue) {
		this.ctxNode = ctxNode;
		this.varName = varName;
		this.currValue = currValue;
	}
	
	
}
