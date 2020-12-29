package fr.an.drawingboard.model.shapedef.obj;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.ImmutableList;

import fr.an.drawingboard.geom2d.BoundingRect2D;
import fr.an.drawingboard.math.numeric.NumericEvalCtx;
import fr.an.drawingboard.model.shapedef.ShapeDef;
import fr.an.drawingboard.recognizer.initialParamEstimators.ParamEvalCtx;
import fr.an.drawingboard.util.LsUtils;
import lombok.val;

/**
 * numerical object instance of a ShapeDef for evaluating on a NumericEvalCtx
 */
public class ShapeObj {

	public final ShapeDef def;

	public final ImmutableList<GesturePathesObj> gestures;
	public BoundingRect2D boundingRect;

	public ShapeObj(ShapeDef def) {
		this.def = def;
		this.gestures = ImmutableList.copyOf(LsUtils.map(def.gestures, x -> new GesturePathesObj(x)));
	}

	public void eval(ParamEvalCtx paramCtx) {
		eval(paramCtx.evalCtx);
	}
	
	public void eval(NumericEvalCtx ctx) {
		val boundingRectBuilder = BoundingRect2D.builder();
		for(val gesture: gestures) {
			// *** recurse shape->gestures->traces->pathElements ***
			gesture.update(ctx);
			
			boundingRectBuilder.enclosingBoundingRect(gesture.boundingRect);
		}
		this.boundingRect = boundingRectBuilder.build();
	}

	public List<PathElementObj> toPathElementCtxEvals() {
		List<PathElementObj> res = new ArrayList<>();
		for(val gesture: gestures) {
			for(val path: gesture.pathes()) {
				for (val pathElt: path.pathElements) {
					res.add(pathElt);
				}
			}
		}
		return res;
	}
}
