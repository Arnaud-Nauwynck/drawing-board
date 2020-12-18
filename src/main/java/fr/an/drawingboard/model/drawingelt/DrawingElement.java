package fr.an.drawingboard.model.drawingelt;

import fr.an.drawingboard.model.varctx.DrawingCtxTreeNode;
import fr.an.drawingboard.ui.impl.GcRendererHelper;

public abstract class DrawingElement {

	private DrawingCtxTreeNode ctxNode;
//	private String name;
	
	// ------------------------------------------------------------------------
	
	protected DrawingElement(DrawingCtxTreeNode ctxNode) {
		this.ctxNode = ctxNode;
	}

	// ------------------------------------------------------------------------
	
	public DrawingCtxTreeNode getCtxNode() {
		return ctxNode;
	}


	public void _setCtxNode(DrawingCtxTreeNode ctxNode) {
		this.ctxNode = ctxNode;
	}

	public abstract void reevalWithVars();

	public abstract void draw(GcRendererHelper gc);

	// ------------------------------------------------------------------------

	
}
