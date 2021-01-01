package fr.an.drawingboard.recognizer.initialParamEstimators;

import fr.an.drawingboard.model.shapedef.GestureDef;
import fr.an.drawingboard.model.trace.TraceGesture;

@FunctionalInterface
public interface InitialParamForShapeEstimator {

	public void estimateInitialParamsFor( //
			TraceGesture gesture,
			GestureDef gestureDef,
			ParamEvalCtx res);

}
