package fr.an.drawingboard.model.trace;

import java.util.ArrayList;
import java.util.List;

import lombok.val;

public class TraceMultiStrokeList {

	public List<TraceMultiStroke> multiStrokes = new ArrayList<>();

	public TraceMultiStroke appendNewMultiStroke() {
		val res = new TraceMultiStroke();
		multiStrokes.add(res);
		return res;
	}

	public TraceMultiStroke getLast() {
		return (multiStrokes.isEmpty())? null : multiStrokes.get(multiStrokes.size() - 1);
	}

	public void remove(TraceMultiStroke p) {
		this.multiStrokes.remove(p);
	}

}
