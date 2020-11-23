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
		ShapeDef crossDef = new ShapeDef("vcross");
		RectExpr r = crossDef.getCoordRectExpr();
		// gesture
		//  PtUL    ->  PtUR
		//       \ /      
		//        \
		//       / \
		//      /   |     
		//  PtDL <--+ PtDR
		InitialParamForMultiStrokeEstimator paramEstimator = rectParamEstimator();
		crossDef.addGesture_Segments(paramEstimator, r.ptUL, r.ptDR, r.ptDL, r.ptUR);
	}

	public void addHCrossDef() {
		ShapeDef hcrossDef = new ShapeDef("hcross");
		RectExpr r = hcrossDef.getCoordRectExpr();
		// gesture
		//  PtUL      PtUR
		//    /\  \ /      
		//    |    \
		//    |   / \
		//    |  /   >     
		//  PtDL      PtDR
		InitialParamForMultiStrokeEstimator paramEstimator = rectParamEstimator();
		hcrossDef.addGesture_Segments(paramEstimator, r.ptUR, r.ptDL, r.ptUL, r.ptDR);
	}
	
	public void addZDef() {
		ShapeDef zDef = new ShapeDef("z");
		RectExpr r = zDef.getCoordRectExpr();
		// gesture
		//  PtUL----+ PtUR
		//         /      
		//        / 
		//       /        
		//  PtDL+----> PtDR
		InitialParamForMultiStrokeEstimator paramEstimator = rectParamEstimator();
		zDef.addGesture_Segments(paramEstimator, r.ptUL, r.ptUR, r.ptDL, r.ptDR);
	}

	public void addNDef() {
		ShapeDef nDef = new ShapeDef("n");
		RectExpr r = nDef.getCoordRectExpr();
		// gesture
		//  PtUL      PtUR
		//    /\ \     /\      
		//    |   \    |
		//    |    \   |        
		//  PtDL    > PtDR
		InitialParamForMultiStrokeEstimator paramEstimator = rectParamEstimator();
		nDef.addGesture_Segments(paramEstimator, r.ptDL, r.ptUL, r.ptDR, r.ptUR);
	}

	
	private InitialParamForMultiStrokeEstimator rectParamEstimator() {
		return StdInitialParamEstimators.rectParamEstimator();
	}


}
