package fr.an.drawingboard.stddefs.shapedef;

import java.util.ArrayList;
import java.util.List;

import fr.an.drawingboard.math.expr.Expr;
import fr.an.drawingboard.math.expr.ExprBuilder;
import fr.an.drawingboard.model.shapedef.GestureDef;
import fr.an.drawingboard.model.shapedef.PathElementDef;
import fr.an.drawingboard.model.shapedef.PathElementDef.DiscretePointsPathElementDef;
import fr.an.drawingboard.model.shapedef.PtExpr;
import fr.an.drawingboard.model.shapedef.RectExpr;
import fr.an.drawingboard.model.shapedef.ShapeDef;
import fr.an.drawingboard.model.shapedef.ShapeDefRegistry;
import fr.an.drawingboard.model.shapedef.paramdef.ParamCategoryRegistry;
import fr.an.drawingboard.recognizer.initialParamEstimators.InitialParamForShapeEstimator;
import fr.an.drawingboard.recognizer.initialParamEstimators.StdInitialParamEstimators;
import lombok.RequiredArgsConstructor;
import lombok.val;

@RequiredArgsConstructor
public class ShapeDefRegistryBuilder {

	private final ShapeDefRegistry dest;
	private final ParamCategoryRegistry paramCategories;
	
	public void addStdShapes() {
		addLineDef();
		// addLine2Def();
		addRectangleDef();
		addRectangleDef_DL();
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
		addDownRightDef();
		addDownLeftDef();
		addUpRightDef();
		addRightUpDef();
		addUpLeftDef();
		addLeftUpDef();
		addLeftDownDef();
			
		addCircleDefs();

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

	public void addRectangleDef_DL() {
		ShapeDef shapeDef = new ShapeDef("rectangle(DL->UL->..)", paramCategories);
		RectExpr r = shapeDef.getCoordRectExpr();
		// clock-wise gesture
		//  PtUL -----> PtUR
		//   /\          |
		//   |           \/
		//  PtDL <----- PtDR
		InitialParamForShapeEstimator paramEstimator = rectParamEstimator();
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
		//            PtDR
		InitialParamForShapeEstimator paramEstimator = rectParamEstimator();
		shapeDef.addGesture_Segments(paramEstimator, r.ptUL, r.ptUR, r.ptDR);
		dest.addShapeDef(shapeDef);
	}

	public void addDownRightDef() {
		ShapeDef shapeDef = new ShapeDef("down,right", paramCategories);
		RectExpr r = shapeDef.getCoordRectExpr();
		// gesture
		//  PtUL
		//    |             
		//    |       
		//    \/              
		//  PtDL ---> PtDR
		InitialParamForShapeEstimator paramEstimator = rectParamEstimator();
		shapeDef.addGesture_Segments(paramEstimator, r.ptUL, r.ptDL, r.ptDR);
		dest.addShapeDef(shapeDef);
	}

	public void addDownLeftDef() {
		ShapeDef shapeDef = new ShapeDef("down,left", paramCategories);
		RectExpr r = shapeDef.getCoordRectExpr();
		// gesture
		//           PtUR
		//             |             
		//             |       
		//             \/              
		//  PtDL <--- PtDR
		InitialParamForShapeEstimator paramEstimator = rectParamEstimator();
		shapeDef.addGesture_Segments(paramEstimator, r.ptUR, r.ptDR, r.ptDL);
		dest.addShapeDef(shapeDef);
	}

	public void addUpRightDef() {
		ShapeDef shapeDef = new ShapeDef("up,right", paramCategories);
		RectExpr r = shapeDef.getCoordRectExpr();
		// gesture
		//  PtUL ---> PtUR
		//   /\             
		//    |       
		//    |              
		//  PtDL
		InitialParamForShapeEstimator paramEstimator = rectParamEstimator();
		shapeDef.addGesture_Segments(paramEstimator, r.ptDL, r.ptUL, r.ptUR);
		dest.addShapeDef(shapeDef);
	}

	public void addRightUpDef() {
		ShapeDef shapeDef = new ShapeDef("right,up", paramCategories);
		RectExpr r = shapeDef.getCoordRectExpr();
		// gesture
		//           PtUR
		//             /\             
		//             |       
		//             |            
		//  PtDL ---> PtDR
		InitialParamForShapeEstimator paramEstimator = rectParamEstimator();
		shapeDef.addGesture_Segments(paramEstimator, r.ptDL, r.ptDR, r.ptUR);
		dest.addShapeDef(shapeDef);
	}

	public void addUpLeftDef() {
		ShapeDef shapeDef = new ShapeDef("up,left", paramCategories);
		RectExpr r = shapeDef.getCoordRectExpr();
		// gesture
		// PtUL <---- PtUR
		//             /\             
		//             |       
		//             |            
		//           PtDR
		InitialParamForShapeEstimator paramEstimator = rectParamEstimator();
		shapeDef.addGesture_Segments(paramEstimator, r.ptDR, r.ptUR, r.ptUL);
		dest.addShapeDef(shapeDef);
	}

	public void addLeftUpDef() {
		ShapeDef shapeDef = new ShapeDef("left,up", paramCategories);
		RectExpr r = shapeDef.getCoordRectExpr();
		// gesture
		// PtUL
		//   /\             
		//   |       
		//   |            
		//  PtDL <--- PtDR
		InitialParamForShapeEstimator paramEstimator = rectParamEstimator();
		shapeDef.addGesture_Segments(paramEstimator, r.ptDR, r.ptDL, r.ptUL);
		dest.addShapeDef(shapeDef);
	}

	public void addLeftDownDef() {
		ShapeDef shapeDef = new ShapeDef("left,down", paramCategories);
		RectExpr r = shapeDef.getCoordRectExpr();
		// gesture
		// PtUL <---- PtUR
		//   |             
		//   |       
		//   \/            
		// PtDL
		InitialParamForShapeEstimator paramEstimator = rectParamEstimator();
		shapeDef.addGesture_Segments(paramEstimator, r.ptUR, r.ptUL, r.ptDL);
		dest.addShapeDef(shapeDef);
	}

	
	public void addCircleDefs() {
		addArcDef("circle(top,clockwise)", -Math.PI/2, -Math.PI/2-2*Math.PI, 160);
		addArcDef("circle(top,counter-clockwise)", -Math.PI/2, -Math.PI/2+2*Math.PI, 160);
		addArcDef("circle(left,clockwise)", Math.PI, Math.PI-2*Math.PI, 160);
		addArcDef("circle(left,counter-clockwise)", Math.PI, Math.PI+2*Math.PI, 160);
		addArcDef("circle(bottom,clockwise)", Math.PI/2, Math.PI/2-2*Math.PI, 160);
		addArcDef("circle(bottom,counter-clockwise)", Math.PI/2, Math.PI/2+2*Math.PI, 160);
	}

	public void addArcDef(String name, double startAngle, double endAngle, int N) {
		ShapeDef shapeDef = new ShapeDef(name, paramCategories);
		val b = ExprBuilder.INSTANCE;
		val coords = shapeDef.getCoordParams();
		val centerX = coords.x.expr, centerY = coords.y.expr, w = coords.w.expr, h = coords.h.expr;
		InitialParamForShapeEstimator paramEstimator = rectParamEstimator();
		// discretise with polyline (TOCHANGE, use bezier..)
		List<PtExpr> pts = new ArrayList<PtExpr>(N);
		val dAngle = (endAngle - startAngle) / N;
		double angle = startAngle;
		for(int i = 0; i < N; i++, angle+=dAngle) {
			val ptx = b.sum(centerX, b.mult(0.5 * Math.cos(angle), w));
			val pty = b.sum(centerY, b.mult(0.5 * Math.sin(angle), h));
			pts.add(new PtExpr(ptx, pty));
		}
		GestureDef gesture = shapeDef.addGesture(paramEstimator);
		PathElementDef pathElement = new DiscretePointsPathElementDef(pts);
		gesture.addPath(pathElement);
		dest.addShapeDef(shapeDef);
	}

	
	private InitialParamForShapeEstimator rectParamEstimator() {
		return StdInitialParamEstimators.rectParamEstimator();
	}

	private InitialParamForShapeEstimator lineParamEstimator() {
		return StdInitialParamEstimators.lineParamEstimator();
	}


}
