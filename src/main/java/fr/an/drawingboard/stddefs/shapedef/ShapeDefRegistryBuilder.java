package fr.an.drawingboard.stddefs.shapedef;

import fr.an.drawingboard.math.expr.Expr;
import fr.an.drawingboard.model.shapedef.PtExpr;
import fr.an.drawingboard.model.shapedef.RectExpr;
import fr.an.drawingboard.model.shapedef.ShapeDef;
import fr.an.drawingboard.model.shapedef.ShapeDefRegistry;
import fr.an.drawingboard.model.shapedef.paramdef.ParamCategoryRegistry;
import fr.an.drawingboard.recognizer.initialParamEstimators.InitialParamForShapeEstimator;
import fr.an.drawingboard.recognizer.initialParamEstimators.StdInitialParamEstimators;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ShapeDefRegistryBuilder {

	private final ShapeDefRegistry dest;
	private final ParamCategoryRegistry paramCategories;
	
	public void addStdShapes() {
		addLineDef();
		// addLine2Def();
		addRectangleDef();
		addVCrossDef();
		addHCrossDef();
		addZDef();
		addInvZDef();
		addNDef();
		addInvNDef();
		addnDef();
		addUDef();
		addCDef();
		addCUpDef();
		addInvCDef();
		addRightDownDef();
	}

	public void addLineDef() {
		ShapeDef shapeDef = new ShapeDef("line", paramCategories);
		RectExpr r = shapeDef.getCoordRectExpr();
		InitialParamForShapeEstimator paramEstimator = lineParamEstimator();
		//  PtUL -----> PtUR
		shapeDef.addGesture_Segments(paramEstimator, r.ptUL, r.ptDR);
		
		dest.addShapeDef(shapeDef);
	}
	
	public void addLine2Def() {
		ShapeDef shapeDef = new ShapeDef("line2", paramCategories);
		RectExpr r = shapeDef.getCoordRectExpr();
		InitialParamForShapeEstimator paramEstimator = StdInitialParamEstimators.line2ParamEstimator();
		Expr ctrlPtX = shapeDef.addParamDef("ctrlPtX", paramCategories.STD_X).expr;
		Expr ctrlPtY = shapeDef.addParamDef("ctrlPtY", paramCategories.STD_Y).expr;
		PtExpr midPt = new PtExpr(ctrlPtX, ctrlPtY);
		//  PtUL -----> MidPt ---> PtUR
		shapeDef.addGesture_Segments(paramEstimator, r.ptUL, midPt, r.ptDR);
		
		dest.addShapeDef(shapeDef);
	}
	

	public void addRectangleDef() {
		ShapeDef shapeDef = new ShapeDef("rectangle", paramCategories);
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
		ShapeDef shapeDef = new ShapeDef("vcross", paramCategories);
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
		ShapeDef shapeDef = new ShapeDef("hcross", paramCategories);
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
		ShapeDef shapeDef = new ShapeDef("z", paramCategories);
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

	public void addInvZDef() {
		ShapeDef shapeDef = new ShapeDef("inv z", paramCategories);
		RectExpr r = shapeDef.getCoordRectExpr();
		// gesture
		//  PtUL<---- PtUR
		//       \      
		//        \ 
		//         \        
		//  PtDL<--- PtDR
		InitialParamForShapeEstimator paramEstimator = rectParamEstimator();
		shapeDef.addGesture_Segments(paramEstimator, r.ptUR, r.ptUL, r.ptDR, r.ptDL);
		dest.addShapeDef(shapeDef);
	}
	
	public void addNDef() {
		ShapeDef shapeDef = new ShapeDef("N", paramCategories);
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

	public void addInvNDef() {
		ShapeDef shapeDef = new ShapeDef("inv N", paramCategories);
		RectExpr r = shapeDef.getCoordRectExpr();
		// gesture
		//  PtUL     PtUR
		//    /\      /\
		//    |     / |
		//    |    /  |        
		//  PtDL <   PtDR
		InitialParamForShapeEstimator paramEstimator = rectParamEstimator();
		shapeDef.addGesture_Segments(paramEstimator, r.ptDR, r.ptUR, r.ptDL, r.ptUL);
		dest.addShapeDef(shapeDef);
	}
	
	public void addUDef() {
		ShapeDef shapeDef = new ShapeDef("u", paramCategories);
		RectExpr r = shapeDef.getCoordRectExpr();
		// gesture
		//  PtUL      PtUR
		//    |      /\      
		//    |       |
		//    \/      |        
		//  PtDL ---> PtDR
		InitialParamForShapeEstimator paramEstimator = rectParamEstimator();
		shapeDef.addGesture_Segments(paramEstimator, r.ptUL, r.ptDL, r.ptDR, r.ptUR);
		dest.addShapeDef(shapeDef);
	}

	public void addnDef() {
		ShapeDef shapeDef = new ShapeDef("n", paramCategories);
		RectExpr r = shapeDef.getCoordRectExpr();
		// gesture
		//  PtUL----> PtUR
		//    /\       |      
		//    |        |
		//    |        \/        
		//  PtDL      PtDR
		InitialParamForShapeEstimator paramEstimator = rectParamEstimator();
		shapeDef.addGesture_Segments(paramEstimator, r.ptDL, r.ptUL, r.ptUR, r.ptDR);
		dest.addShapeDef(shapeDef);
	}
	
	public void addCDef() {
		ShapeDef shapeDef = new ShapeDef("c", paramCategories);
		RectExpr r = shapeDef.getCoordRectExpr();
		// gesture
		//  PtUL<-----PtUR
		//    |             
		//    |       
		//    \/              
		//  PtDL ---> PtDR
		InitialParamForShapeEstimator paramEstimator = rectParamEstimator();
		shapeDef.addGesture_Segments(paramEstimator, r.ptUR, r.ptUL, r.ptDL, r.ptDR);
		dest.addShapeDef(shapeDef);
	}

	public void addCUpDef() {
		ShapeDef shapeDef = new ShapeDef("c up", paramCategories);
		RectExpr r = shapeDef.getCoordRectExpr();
		// gesture
		//  PtUL----->PtUR
		//    /\             
		//    |       
		//    |              
		//  PtDL <--- PtDR
		InitialParamForShapeEstimator paramEstimator = rectParamEstimator();
		shapeDef.addGesture_Segments(paramEstimator, r.ptDR, r.ptDL, r.ptUL, r.ptUR);
		dest.addShapeDef(shapeDef);
	}

	
	public void addInvCDef() {
		ShapeDef shapeDef = new ShapeDef("inv c", paramCategories);
		RectExpr r = shapeDef.getCoordRectExpr();
		// gesture
		//  PtUL----->PtUR
		//             |             
		//             |       
		//            \/              
		//  PtDL <--- PtDR
		InitialParamForShapeEstimator paramEstimator = rectParamEstimator();
		shapeDef.addGesture_Segments(paramEstimator, r.ptUL, r.ptUR, r.ptDR, r.ptDL);
		dest.addShapeDef(shapeDef);
	}

	public void addRightDownDef() {
		ShapeDef shapeDef = new ShapeDef("right,down", paramCategories);
		RectExpr r = shapeDef.getCoordRectExpr();
		// gesture
		//  PtUL----->PtUR
		//             |             
		//             |       
		//            \/              
		//  PtDL      PtDR
		InitialParamForShapeEstimator paramEstimator = rectParamEstimator();
		shapeDef.addGesture_Segments(paramEstimator, r.ptUL, r.ptUR, r.ptDR);
		dest.addShapeDef(shapeDef);
	}
	
	private InitialParamForShapeEstimator rectParamEstimator() {
		return StdInitialParamEstimators.rectParamEstimator();
	}

	private InitialParamForShapeEstimator lineParamEstimator() {
		return StdInitialParamEstimators.lineParamEstimator();
	}


}
