package fr.an.drawingboard.model.shapedef;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

public class ShapeDefRegistry {

	public Map<String,ShapeDef> shapeDefs = new LinkedHashMap<>();

	public void addShapeDef(ShapeDef shapeDef) {
		shapeDefs.put(shapeDef.name, shapeDef);
	}

	public ShapeDef getShapeDef(String name) {
		return shapeDefs.get(name);
	}

	public Collection<ShapeDef> getShapeDefs() {
		return shapeDefs.values();
	}

}
