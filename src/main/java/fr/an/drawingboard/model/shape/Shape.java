package fr.an.drawingboard.model.shape;

import java.util.HashMap;
import java.util.Map;

import fr.an.drawingboard.model.shapedef.GesturePathesDef;
import fr.an.drawingboard.model.shapedef.ShapeDef;
import fr.an.drawingboard.model.var.ParamDef;
import fr.an.drawingboard.ui.impl.ShapeDefGcRenderer;
import javafx.scene.canvas.GraphicsContext;
import lombok.val;

public class Shape {

	public final ShapeDef shapeDef;
	
	public final Map<ParamDef, Double> paramValues = new HashMap<>();
	
	public Shape(ShapeDef shapeDef, Map<ParamDef, Double> paramValues) {
		this.shapeDef = shapeDef;
		this.paramValues.putAll(paramValues);
	}

	public void draw(GraphicsContext gc) {
		GesturePathesDef gestureDef = shapeDef.gestures.get(0);
		val gcRenderer = new ShapeDefGcRenderer(gc);
		gcRenderer.draw(gestureDef, paramValues);
	}
	
	// --------------------------------------------------------------------------------------------

//	/**
//	 * class for Rectangle shape .. similar to javafx com.sun.javafx.geom.RoundRectangle2D
//	 */
//	public static class RectShape extends Shape {
//	    private float x;
//	    private float y;
//	    private float width;
//	    private float height;
//
//	    @Override
//		public void draw(GraphicsContext gc) {
//			gc.strokeRect(x, y, width, height);
//		}
//
//	}
	
}
