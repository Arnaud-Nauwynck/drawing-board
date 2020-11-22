package fr.an.drawingboard.stddefs.shapedef;

import fr.an.drawingboard.model.shapedef.RectExpr;
import fr.an.drawingboard.model.shapedef.ShapeDef;
import fr.an.drawingboard.model.shapedef.ShapeDefRegistry;
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
		shapeDef.addGesture_Segments(r.ptUL, r.ptUR, r.ptDR, r.ptDL, r.ptUL);
		shapeDef.addGesture_Segments(r.ptDL, r.ptUL, r.ptUR, r.ptDR, r.ptDL);
		
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
		crossDef.addGesture_Segments(r.ptUL, r.ptDR, r.ptDL, r.ptUR);
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
		hcrossDef.addGesture_Segments(r.ptUR, r.ptDL, r.ptUL, r.ptDR);
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
		zDef.addGesture_Segments(r.ptUL, r.ptUR, r.ptDL, r.ptDR);
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
		nDef.addGesture_Segments(r.ptDL, r.ptUL, r.ptDR, r.ptUR);
	}

}
