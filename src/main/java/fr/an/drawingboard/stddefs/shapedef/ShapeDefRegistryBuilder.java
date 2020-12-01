package fr.an.drawingboard.stddefs.shapedef;

import fr.an.drawingboard.math.expr.Expr;
import fr.an.drawingboard.model.shapedef.PtExpr;
import fr.an.drawingboard.model.shapedef.RectExpr;
import fr.an.drawingboard.model.shapedef.ShapeDef;
import fr.an.drawingboard.model.shapedef.ShapeDefRegistry;
import fr.an.drawingboard.recognizer.initialParamEstimators.InitialParamForShapeEstimator;
import fr.an.drawingboard.recognizer.initialParamEstimators.StdInitialParamEstimators;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ShapeDefRegistryBuilder {

	private final ShapeDefRegistry dest;

	public void addStdShapes() {
		addLineDef();
		addLine2Def();
		addRectangleDef();
		addVCrossDef();
		addHCrossDef();
		addZDef();
		addNDef();
	}

	public void addLineDef() {
		ShapeDef shapeDef = new ShapeDef("line");
		RectExpr r = shapeDef.getCoordRectExpr();
		InitialParamForShapeEstimator paramEstimator = lineParamEstimator();
		//  PtUL -----> PtUR
		shapeDef.addGesture_Segments(paramEstimator, r.ptUL, r.ptDR);
		
		dest.addShapeDef(shapeDef);
	}
	
	public void addLine2Def() {
		ShapeDef shapeDef = new ShapeDef("line2");
		RectExpr r = shapeDef.getCoordRectExpr();
		InitialParamForShapeEstimator paramEstimator = StdInitialParamEstimators.line2ParamEstimator();
		Expr ctrlPtX = shapeDef.addVarDef("ctrlPtX").expr;
		Expr ctrlPtY = shapeDef.addVarDef("ctrlPtY").expr;
		PtExpr midPt = new PtExpr(ctrlPtX, ctrlPtY);
		//  PtUL -----> MidPt ---> PtUR
		shapeDef.addGesture_Segments(paramEstimator, r.ptUL, midPt, r.ptDR);
		
		dest.addShapeDef(shapeDef);
	}
	

	public void addRectangleDef() {
		ShapeDef shapeDef = new ShapeDef("rectangle");
		RectExpr r = shapeDef.getCoordRectExpr();
		// clock-wise gesture
		//  PtUL -----> PtUR
		//   /\          |
		//   |           \/
		//  PtDL <----- PtDR
		InitialParamForShapeEstimator paramEstimator = rectParamEstimator();
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
		InitialParamForShapeEstimator paramEstimator = rectParamEstimator();
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
		InitialParamForShapeEstimator paramEstimator = rectParamEstimator();
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
		InitialParamForShapeEstimator paramEstimator = rectParamEstimator();
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
		InitialParamForShapeEstimator paramEstimator = rectParamEstimator();
		shapeDef.addGesture_Segments(paramEstimator, r.ptDL, r.ptUL, r.ptDR, r.ptUR);
		dest.addShapeDef(shapeDef);
	}

	
	private InitialParamForShapeEstimator rectParamEstimator() {
		return StdInitialParamEstimators.rectParamEstimator();
	}

	private InitialParamForShapeEstimator lineParamEstimator() {
		return StdInitialParamEstimators.lineParamEstimator();
	}


}
