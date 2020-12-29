package fr.an.drawingboard.model.shapedef.obj;

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
public class PathObj {

	public final PathDef def;

	public final ImmutableList<PathElementObj> pathElements;
	
	public BoundingRect2D boundingRect;

	public PathObj(PathDef def) {
		this.def = def;
		this.pathElements = ImmutableList.copyOf(LsUtils.map(def.pathElements, x -> PathElementObj.create(x)));
	}
	
	public void update(NumericEvalCtx ctx) {
		val boundingRectBuilder = BoundingRect2D.builder();
		for(val pathElement: pathElements) {
			pathElement.update(ctx);
			pathElement.addEnclosing(boundingRectBuilder);
		}
		this.boundingRect = boundingRectBuilder.build();
	}

	public List<PathElementObj> toPathElementObjs() {
		List<PathElementObj> res = new ArrayList<>();
		for (val pathElt: pathElements) {
			res.add(pathElt);
		}
		return res;
	}

}