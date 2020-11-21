package fr.an.drawingboard.model.shapedef;

import java.util.ArrayList;
import java.util.List;

import fr.an.drawingboard.model.var.ParametrizableEltDef;

public class StrokeDef extends ParametrizableEltDef {

	public List<StrokePathElementDef> pathElements = new ArrayList<>();

	public StrokeDef() {
	}

	public StrokeDef(List<StrokePathElementDef> pathElements) {
		this.pathElements.addAll(pathElements);
	}
	
}
