package fr.an.drawingboard.model.varctx;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import fr.an.drawingboard.math.expr.Expr;
import fr.an.drawingboard.math.expr.VarDef;
import fr.an.drawingboard.model.drawingelt.DrawingElement;
import fr.an.drawingboard.model.drawingelt.ShapeDrawingElement;
import fr.an.drawingboard.model.drawingelt.TraceDrawingElement;
import fr.an.drawingboard.model.shapedef.ShapeDef;
import fr.an.drawingboard.model.trace.TraceShape;
import fr.an.drawingboard.ui.impl.GcRendererHelper;
import lombok.Getter;
import lombok.val;

public class DrawingVarCtxNode {

	@Getter
	private final DrawingVarCtxNode parent;
	public final String name;
	
	private final Map<String,DrawingVarCtxNode> childCtxByName = new LinkedHashMap<String,DrawingVarCtxNode>();
	
	private final Map<String,DrawingVarDef> varDefByName = new LinkedHashMap<String,DrawingVarDef>();

	private final List<DrawingElement> drawingElements = new ArrayList<>();
	
	// ------------------------------------------------------------------------
	
	private DrawingVarCtxNode(DrawingVarCtxNode parent, String name) {
		this.parent = parent;
		this.name = name;
	}
	
	public static DrawingVarCtxNode createRootNode() {
		return new DrawingVarCtxNode(null, "");
	}
	
	// ------------------------------------------------------------------------
	
	public ImmutableMap<String,DrawingVarCtxNode> getChildCtxByName() {
		return ImmutableMap.copyOf(childCtxByName);
	}
	
	public DrawingVarCtxNode addChildCtx(String childName) {
		if (childCtxByName.get(childName) != null) {
			throw new IllegalArgumentException();
		}
		DrawingVarCtxNode res = new DrawingVarCtxNode(this, childName);
		this.childCtxByName.put(childName, res);
		return res;
	}

	public ImmutableMap<String,DrawingVarDef> getVarDefByName() {
		return ImmutableMap.copyOf(varDefByName);
	}

	public DrawingVarDef addVarDef(String name, double currValue) {
		if (varDefByName.get(name) != null) {
			throw new IllegalArgumentException();
		}
		DrawingVarDef res = new DrawingVarDef(this, name, currValue);
		this.varDefByName.put(name, res);
		return res;
	}

	public ImmutableList<DrawingElement> getDrawingElements() {
		return ImmutableList.copyOf(drawingElements);
	}

	public void addDrawingElementTrace(TraceShape trace) {
		addDrawingElement(new TraceDrawingElement(this, trace));
	}

	public void addDrawingElementShape(ShapeDef shapeDef, Map<VarDef,Expr> shapeDefVarExpr) {
		addDrawingElement(new ShapeDrawingElement(this, shapeDef, shapeDefVarExpr));
	}

	protected void addDrawingElement(DrawingElement drawingElement) {
		this.drawingElements.add(drawingElement);
		drawingElement._setCtxNode(this);
	}

	public void removeDrawingElement(DrawingElement drawingElement) {
		if (drawingElement.getCtxNode() != this) {
			throw new IllegalArgumentException();
		}
		this.drawingElements.remove(drawingElement);
		drawingElement._setCtxNode(null);
	}

	public void recursiveReevalDrawingElements() {
		for(val e: drawingElements) {
			e.reevalWithVars();
		}
		if (! childCtxByName.isEmpty()) {
			for(val childCtx: childCtxByName.values()) {
				childCtx.recursiveReevalDrawingElements();
			}
		}
	}

	public void recursiveDraw(GcRendererHelper gc) {
		for(val e: drawingElements) {
			e.draw(gc);
		}
		if (! childCtxByName.isEmpty()) {
			for(val childCtx: childCtxByName.values()) {
				childCtx.recursiveDraw(gc);
			}
		}
	}
}
