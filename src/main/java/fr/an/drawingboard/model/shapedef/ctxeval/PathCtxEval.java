package fr.an.drawingboard.model.shapedef.ctxeval;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.ImmutableList;

import fr.an.drawingboard.geom2d.BoundingRect2D;
import fr.an.drawingboard.math.numeric.NumericEvalCtx;
import fr.an.drawingboard.model.shapedef.PathDef;
import fr.an.drawingboard.util.LsUtils;
import lombok.val;

/**
 * numerical object instance of a PathDef for evaluating on a NumericEvalCtx
 */
public class PathCtxEval {

	public final PathDef def;

	public final ImmutableList<PathElementCtxEval> pathElements;
	
	public BoundingRect2D boundingRect;

	public PathCtxEval(PathDef def) {
		this.def = def;
		this.pathElements = ImmutableList.copyOf(LsUtils.map(def.pathElements, x -> PathElementCtxEval.create(x)));
	}
	
	public void update(NumericEvalCtx ctx) {
		val boundingRectBuilder = BoundingRect2D.builder();
		for(val pathElement: pathElements) {
			pathElement.update(ctx);
			pathElement.addEnclosing(boundingRectBuilder);
		}
		this.boundingRect = boundingRectBuilder.build();
	}

	public List<PathElementCtxEval> toPathElementCtxEvals() {
		List<PathElementCtxEval> res = new ArrayList<>();
		for (val pathElt: pathElements) {
			res.add(pathElt);
		}
		return res;
	}

}