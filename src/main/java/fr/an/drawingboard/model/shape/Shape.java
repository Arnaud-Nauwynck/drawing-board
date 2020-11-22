package fr.an.drawingboard.model.shape;

import java.util.HashMap;
import java.util.Map;

import fr.an.drawingboard.model.shapedef.ShapeDef;

public class Shape {

	public final ShapeDef shapeDef;
	
	public Map<String,ParamValue> paramValues = new HashMap<>();

	public Shape(ShapeDef shapeDef, Map<String, ParamValue> paramValues) {
		this.shapeDef = shapeDef;
		this.paramValues.putAll(paramValues);
	}
	
}
