package fr.an.drawingboard.model.drawingelt;

import fr.an.drawingboard.model.varctx.DrawingVarCtxNode;
import fr.an.drawingboard.ui.impl.GcRendererHelper;

public abstract class DrawingElement {

	private DrawingVarCtxNode ctxNode;
//	private String name;
	
	// ------------------------------------------------------------------------
	
	protected DrawingElement(DrawingVarCtxNode ctxNode) {
		this.ctxNode = ctxNode;
	}

	// ------------------------------------------------------------------------
	
	public DrawingVarCtxNode getCtxNode() {
		return ctxNode;
	}


	public void _setCtxNode(DrawingVarCtxNode ctxNode) {
		this.ctxNode = ctxNode;
	}

	public abstract void reevalWithVars();

	public abstract void draw(GcRendererHelper gc);

	// ------------------------------------------------------------------------

	
}
