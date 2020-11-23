package fr.an.drawingboard.stddefs.shapedef;

import fr.an.drawingboard.model.shapedef.RectExpr;
import fr.an.drawingboard.model.shapedef.ShapeDef;
import fr.an.drawingboard.model.shapedef.ShapeDefRegistry;
import fr.an.drawingboard.recognizer.initialParamEstimators.StdInitialParamEstimators;
import fr.an.drawingboard.recognizer.shape.InitialParamForMultiStrokeEstimator;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ShapeDefRegistryBuilder {

	private final ShapeDefRegistry dest;

	public void addStdShapes() {
		addRectangleDef();
		addVCrossDef();
		addHCrossDef();
		addZDef();
		addNDef();
	}

	public void addRectangleDef() {
		ShapeDef shapeDef = new ShapeDef("rectangle");
		RectExpr r = shapeDef.getCoordRectExpr();
		// clock-wise gesture
		//  PtUL -----> PtUR
		//   /\          |
		//   |           \/
		//  PtDL <----- PtDR
		InitialParamForMultiStrokeEstimator paramEstimator = rectParamEstimator();
		shapeDef.addGesture_Segments(paramEstimator, r.ptUL, r.ptUR, r.ptDR, r.ptDL, r.ptUL);
		shapeDef.addGesture_Segments(paramEstimator, r.ptDL, r.ptUL, r.ptUR, r.ptDR, r.ptDL);
		
		dest.addShapeDef(shapeDef);
	}
	
	public void addVCrossDef() {
		ShapeDef shapeDef = new ShapeDef("vcross");
		RectExpr r = shapeDef.getCoordRectExpr();
		// gesture
		//  PtUL    ->  PtUR
		//       \ /      
		//        \
		//       / \
		//      /   |     
		//  PtDL <--+ PtDR
		InitialParamForMultiStrokeEstimator paramEstimator = rectParamEstimator();
		shapeDef.addGesture_Segments(paramEstimator, r.ptUL, r.ptDR, r.ptDL, r.ptUR);

		dest.addShapeDef(shapeDef);
	}

	public void addHCrossDef() {
		ShapeDef shapeDef = new ShapeDef("hcross");
		RectExpr r = shapeDef.getCoordRectExpr();
		// gesture
		//  PtUL      PtUR
		//    /\  \ /      
		//    |    \
		//    |   / \
		//    |  /   >     
		//  PtDL      PtDR
		InitialParamForMultiStrokeEstimator paramEstimator = rectParamEstimator();
		shapeDef.addGesture_Segments(paramEstimator, r.ptUR, r.ptDL, r.ptUL, r.ptDR);
		dest.addShapeDef(shapeDef);
	}
	
	public void addZDef() {
		ShapeDef shapeDef = new ShapeDef("z");
		RectExpr r = shapeDef.getCoordRectExpr();
		// gesture
		//  PtUL----+ PtUR
		//         /      
		//        / 
		//       /        
		//  PtDL+----> PtDR
		InitialParamForMultiStrokeEstimator paramEstimator = rectParamEstimator();
		shapeDef.addGesture_Segments(paramEstimator, r.ptUL, r.ptUR, r.ptDL, r.ptDR);
		dest.addShapeDef(shapeDef);
	}

	public void addNDef() {
		ShapeDef shapeDef = new ShapeDef("n");
		RectExpr r = shapeDef.getCoordRectExpr();
		// gesture
		//  PtUL      PtUR
		//    /\ \     /\      
		//    |   \    |
		//    |    \   |        
		//  PtDL    > PtDR
		InitialParamForMultiStrokeEstimator paramEstimator = rectParamEstimator();
		shapeDef.addGesture_Segments(paramEstimator, r.ptDL, r.ptUL, r.ptDR, r.ptUR);
		dest.addShapeDef(shapeDef);
	}

	
	private InitialParamForMultiStrokeEstimator rectParamEstimator() {
		return StdInitialParamEstimators.rectParamEstimator();
	}


}
