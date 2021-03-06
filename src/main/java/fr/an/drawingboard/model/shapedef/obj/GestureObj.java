package fr.an.drawingboard.model.shapedef.obj;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.ImmutableList;

import fr.an.drawingboard.geom2d.BoundingRect2D;
import fr.an.drawingboard.math.numeric.NumericEvalCtx;
import fr.an.drawingboard.model.shapedef.GestureDef;
import fr.an.drawingboard.util.LsUtils;
import lombok.val;

/**
 * numerical objects instance of a GesturePathesDef for evaluating on a NumericEvalCtx
 */
public class GestureObj {

	public final GestureDef def;
	
	public final ImmutableList<PathObj> pathes;
	public BoundingRect2D boundingRect;

	public GestureObj(GestureDef def) {
		this.def = def;
		this.pathes = ImmutableList.copyOf(LsUtils.map(def.pathes, x -> new PathObj(x)));
	}

	public Iterable<PathObj> pathes() {
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

	public List<PathElementObj> toPathElementCtxEvals() {
		List<PathElementObj> res = new ArrayList<>();
		for(val path: pathes()) {
			for (val pathElt: path.pathElements) {
				res.add(pathElt);
			}
		}
		return res;
	}

	public double getDist() {
		double res = 0.0;
		for(val path: pathes()) {
			res += path.getDist();
		}
		return res;
	}

}