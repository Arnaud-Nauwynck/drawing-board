package fr.an.drawingboard.model.shapedef.ctxeval;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.ImmutableList;

import fr.an.drawingboard.geom2d.BoundingRect2D;
import fr.an.drawingboard.math.numeric.NumericEvalCtx;
import fr.an.drawingboard.model.shapedef.GesturePathesDef;
import fr.an.drawingboard.util.LsUtils;
import lombok.val;

/**
 * numerical objects instance of a GesturePathesDef for evaluating on a NumericEvalCtx
 */
public class GesturePathesCtxEval {

	public final GesturePathesDef def;
	
	public final ImmutableList<PathCtxEval> pathes;
	public BoundingRect2D boundingRect;

	public GesturePathesCtxEval(GesturePathesDef def) {
		this.def = def;
		this.pathes = ImmutableList.copyOf(LsUtils.map(def.pathes, x -> new PathCtxEval(x)));
	}

	public Iterable<PathCtxEval> pathes() {
		return pathes;
	}

	public void update(NumericEvalCtx ctx) {
		val boundingRectBuilder = BoundingRect2D.builder();
		for(val path: pathes()) {
			path.update(ctx);
			boundingRectBuilder.enclosingBoundingRect(path.boundingRect);
		}
		this.boundingRect = boundingRectBuilder.build();
	}

	public List<PathElementCtxEval> toPathElementCtxEvals() {
		List<PathElementCtxEval> res = new ArrayList<>();
		for(val path: pathes()) {
			for (val pathElt: path.pathElements) {
				res.add(pathElt);
			}
		}
		return res;
	}

}