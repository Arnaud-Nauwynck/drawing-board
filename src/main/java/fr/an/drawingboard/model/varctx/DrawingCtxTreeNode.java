package fr.an.drawingboard.model.varctx;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import fr.an.drawingboard.model.drawingelt.DrawingElement;
import fr.an.drawingboard.model.drawingelt.ShapeDrawingElement;
import fr.an.drawingboard.model.drawingelt.TraceDrawingElement;
import fr.an.drawingboard.model.shapedef.ShapeDef;
import fr.an.drawingboard.model.shapedef.paramdef.ParamCategory;
import fr.an.drawingboard.model.shapedef.paramdef.ParamDef;
import fr.an.drawingboard.model.trace.TraceShape;
import fr.an.drawingboard.ui.impl.GcRendererHelper;
import lombok.Getter;
import lombok.val;

public class DrawingCtxTreeNode {

	@Getter
	private final DrawingCtxTreeNode parent;
	public final String name;
	
//	private final Map<String,DrawingCtxTreeNode> childCtxByName = new LinkedHashMap<String,DrawingCtxTreeNode>();
	private List<DrawingCtxTreeNode> childList = new ArrayList<>();
	
	private final Map<String,DrawingVarDef> varDefByName = new LinkedHashMap<String,DrawingVarDef>();

	private final List<DrawingElement> drawingElements = new ArrayList<>();
	
	// ------------------------------------------------------------------------
	
	private DrawingCtxTreeNode(DrawingCtxTreeNode parent, String name) {
		this.parent = parent;
		this.name = name;
	}
	
	public static DrawingCtxTreeNode createRootNode() {
		return new DrawingCtxTreeNode(null, "");
	}
	
	// child DrawingCtxTreeNode
	// ------------------------------------------------------------------------
	
	public ImmutableList<DrawingCtxTreeNode> getChildList() {
		return ImmutableList.copyOf(childList);
	}

	public DrawingCtxTreeNode lastChildNode() {
		int childCount = childList.size();
		if (childCount == 0) {
			return null;
		}
		return childList.get(childCount-1);
	}

	public DrawingCtxTreeNode addChildCtx_GenerateNameFor(String childNamePrefix) {
//		if (null == childCtxByName.get(childNamePrefix)) {
//			return addChildCtx(childNamePrefix);
//		}
//		for(int i = 2; ; i++) {
//			String name = childNamePrefix + i;
//			if (null == childCtxByName.get(name)) {
//				return addChildCtx(name);
//			}
//		}
		return addChildCtx(name);
	}
	
	public DrawingCtxTreeNode addChildCtx(String childName) {
//		if (childCtxByName.get(childName) != null) {
//			throw new IllegalArgumentException();
//		}
		DrawingCtxTreeNode res = new DrawingCtxTreeNode(this, childName);
		this.childList.add(res);
//		this.childCtxByName.put(childName, res);
		return res;
	}

	public void removeChild(DrawingCtxTreeNode child) {
		this.childList.remove(child);
		
	}

	// DrawingVarDef
	// ------------------------------------------------------------------------
	 
	public ImmutableMap<String,DrawingVarDef> getVarDefByName() {
		return ImmutableMap.copyOf(varDefByName);
	}

	public DrawingVarDef addVarDef_GenerateNameFor(String namePrefix, ParamCategory paramCategory, double currValue) {
		if (null == varDefByName.get(namePrefix)) {
			return addVarDef(namePrefix, paramCategory, currValue);
		}
		for(int i = 2; ; i++) {
			String name = namePrefix + i;
			if (null == varDefByName.get(name)) {
				return addVarDef(name, paramCategory, currValue);
			}
		}
	}
	
	public DrawingVarDef addVarDef(String name, ParamCategory paramCategory, double currValue) {
		if (varDefByName.get(name) != null) {
			throw new IllegalArgumentException();
		}
		DrawingVarDef res = new DrawingVarDef(this, name, paramCategory, currValue);
		this.varDefByName.put(name, res);
		return res;
	}

	public ImmutableList<DrawingElement> getDrawingElements() {
		return ImmutableList.copyOf(drawingElements);
	}

	public DrawingElement lastDrawingElement() {
		int count = drawingElements.size();
		return (count != 0)? drawingElements.get(count-1) : null;
	}

	public TraceDrawingElement addDrawingElementTrace(TraceShape trace) {
		TraceDrawingElement res = new TraceDrawingElement(this, trace);
		addDrawingElement(res);
		return res;
	}

	public DrawingCtxTreeNode[] ancestorNodes() {
		int ancestorCount = 0;
		for(DrawingCtxTreeNode p = this; p != null; p = p.parent) {
			ancestorCount++;
		}
		val res = new DrawingCtxTreeNode[ancestorCount];
		int pos = ancestorCount-1;
		for(DrawingCtxTreeNode p = this; p != null; p = p.parent,pos--) {
			res[pos] = p;
		}
		return res;
	}
	
	public interface SimilarVarCostFunction {
		public double costForSimilarVar(double value, ParamCategory category, int ancestorDepth, DrawingCtxTreeNode node, DrawingVarDef varDef);
	}
	public static final SimilarVarCostFunction DEFAULT_SimilarVarCostEvaluator = (double value, ParamCategory category, int ancestorDepth, DrawingCtxTreeNode node, DrawingVarDef varDef) -> {
		double varValue = varDef.getCurrValue();
		double diff = Math.abs(value - varValue);
		if (diff == 0.0) {
			return 0.0;
		}
		return ancestorDepth*10 + diff;
	};

	public Map<ParamDef,DrawingVarDef> defineVarExpr(Map<ParamDef, Double> paramValues) {
		Map<ParamDef,DrawingVarDef> res = new HashMap<>();
		for(val e : paramValues.entrySet()) {
			val paramDef = e.getKey();
			double value = e.getValue();
			val binding = addVarDef_GenerateNameFor(paramDef.name, paramDef.paramCategory, value);
			res.put(paramDef, binding);
		}
		return res;
	}

	public Map<ParamDef,DrawingVarDef> resolveSimilarOrDefineVarExpr(Map<ParamDef, Double> paramValues, 
			SimilarVarCostFunction costFunc, double maxCostOrDefine, double maxDiffOrDefine) {
		Map<ParamDef,DrawingVarDef> res = new HashMap<>();
		val ancestorNodes = ancestorNodes();
		for(val e : paramValues.entrySet()) {
			val paramDef = e.getKey();
			double value = e.getValue();
			val binding = resolveSimilarOrDefineVar(value, paramDef, ancestorNodes, costFunc, maxCostOrDefine, maxDiffOrDefine);
			res.put(paramDef, binding);
		}
		return res;
	}

	public DrawingVarDef resolveSimilarOrDefineVar(double value, ParamDef paramDef, DrawingCtxTreeNode[] ancestorNodes, 
			SimilarVarCostFunction costFunc, double maxCostOrDefine, double maxDiffOrDefine) {
		val category = paramDef.paramCategory;
		double bestCost = Double.MAX_VALUE;
		DrawingVarDef bestVarDef = null;
		int nodeDepth = 0;
		// find similar value, starting from root node, down to this node
		for(val node: ancestorNodes) {
			DrawingVarDef bestNodeVar = node.findCurrNodeSimilarVarDef(value, category, nodeDepth, bestCost, costFunc);
			if (bestNodeVar != null) {
				bestCost = Math.abs(value - bestNodeVar.getCurrValue());
				bestVarDef = bestNodeVar;
			}
			nodeDepth++;
		}
		if (bestVarDef != null && (bestCost > maxCostOrDefine || Math.abs(value - bestVarDef.getCurrValue()) > maxDiffOrDefine)) {
			bestVarDef = null; // force define new
		}
		DrawingVarDef res;
		if (bestVarDef == null) {
			// define new VarDef
			// TODO define on parent??? (promote var to be reusable) 
			val target = (parent != null)? parent : this;
			res = target.addVarDef_GenerateNameFor(paramDef.name, category, value);
		} else {
			// reuse existing similar
			res = bestVarDef;
		}
		return res;
	}

	private DrawingVarDef findCurrNodeSimilarVarDef(double value, ParamCategory category, int currDepth, double maxCost, SimilarVarCostFunction costFunc) {
		DrawingVarDef bestVar = null;
		double bestCost = maxCost;
		for (val varDef: varDefByName.values()) {
			if (varDef.paramCategory == category) {
				val varValue = varDef.getCurrValue();
				if (varValue == value) {
					return varDef;
				}
				double cost = costFunc.costForSimilarVar(value, category, currDepth, this, varDef);
				if (cost < bestCost) {
					bestCost = cost;
					bestVar = varDef;
				}
			}
		}
		return bestVar;
	}
	
	// ShapeDrawingElement
	// ------------------------------------------------------------------------
	
	public ShapeDrawingElement addDrawingElementShape(ShapeDef shapeDef, Map<ParamDef,DrawingVarDef> paramBindings) {
		ShapeDrawingElement res = new ShapeDrawingElement(this, shapeDef, paramBindings);
		addDrawingElement(res);
		return res;
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
		for(val childCtx: childList) {
			childCtx.recursiveReevalDrawingElements();
		}
	}

	public void recursiveDraw(GcRendererHelper gc) {
		for(val e: drawingElements) {
			e.draw(gc);
		}
		if (! childList.isEmpty()) {
			for(val childCtx: childList) {
				childCtx.recursiveDraw(gc);
			}
		}
	}

}
