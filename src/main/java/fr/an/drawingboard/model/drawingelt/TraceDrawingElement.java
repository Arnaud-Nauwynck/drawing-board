package fr.an.drawingboard.model.drawingelt;

import fr.an.drawingboard.model.trace.TraceShape;
import fr.an.drawingboard.model.varctx.DrawingCtxTreeNode;
import fr.an.drawingboard.ui.impl.GcRendererHelper;
import lombok.Getter;

public class TraceDrawingElement extends DrawingElement {
	
	@Getter
	private final TraceShape trace;

	public TraceDrawingElement(DrawingCtxTreeNode ctxNode, TraceShape trace) {
		super(ctxNode);
		this.trace = trace;
	}

	@Override
	public void reevalWithVars() {
		// TODO use boundingBox vars?
	}

	@Override
	public void draw(GcRendererHelper gc) {
		gc.draw(trace);
	}
	
}