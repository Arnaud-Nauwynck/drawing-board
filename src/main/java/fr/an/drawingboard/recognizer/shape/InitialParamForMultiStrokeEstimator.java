package fr.an.drawingboard.recognizer.shape;

import fr.an.drawingboard.model.expr.helper.NumericExprEvalCtx;
import fr.an.drawingboard.model.shapedef.MultiStrokeDef;
import fr.an.drawingboard.model.trace.TraceMultiStroke;

@FunctionalInterface
public interface InitialParamForMultiStrokeEstimator {

	public void estimateInitialParamsFor( //
			TraceMultiStroke traceMultiStroke,
			MultiStrokeDef multiStrokeDef,
			NumericExprEvalCtx res);

}
