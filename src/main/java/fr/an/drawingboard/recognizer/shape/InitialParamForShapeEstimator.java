package fr.an.drawingboard.recognizer.shape;

import fr.an.drawingboard.model.expr.helper.NumericExprEvalCtx;
import fr.an.drawingboard.model.shapedef.GesturePathesDef;
import fr.an.drawingboard.model.trace.TraceGesturePathes;

@FunctionalInterface
public interface InitialParamForShapeEstimator {

	public void estimateInitialParamsFor( //
			TraceGesturePathes gesture,
			GesturePathesDef gestureDef,
			NumericExprEvalCtx res);

}
