package fr.an.drawingboard.model.trace;

import java.util.ArrayList;
import java.util.List;

public class TraceStroke {

	public List<TraceStrokePathElement> pathElements = new ArrayList<>();

	public void add(TraceStrokePathElement pathElement) {
		this.pathElements.add(pathElement);
	}
	
}
