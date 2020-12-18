package fr.an.drawingboard.model.shapedef.ctxeval;

import lombok.AllArgsConstructor;


/**
 * a fragment of a PathElementCtxEval
 * (if fromPathParam=0.0 and toPath=1.0 .. then it represents the full pathEmentDef)
 */
@AllArgsConstructor
public class PathElementCtxEvalFragment {
	
	public final PathElementCtxEval pathElement;
	public final double fromPathParam; // 0.0 for start
	public final double toPathParam; // 1.0 for end

}