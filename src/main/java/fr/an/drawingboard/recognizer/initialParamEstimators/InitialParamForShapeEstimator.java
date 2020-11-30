package fr.an.drawingboard.recognizer.initialParamEstimators;

import fr.an.drawingboard.model.expr.helper.NumericExprEvalCtx;
import fr.an.drawingboard.model.shapedef.GesturePathesDef;
import fr.an.drawingboard.model.trace.TraceGesture;

@FunctionalInterface
public interface InitialParamForShapeEstimator {

	public void estimateInitialParamsFor( //
			TraceGesture gesture,
			GesturePathesDef gestureDef,
			NumericExprEvalCtx res);

}
