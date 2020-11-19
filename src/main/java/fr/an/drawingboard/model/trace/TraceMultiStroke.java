package fr.an.drawingboard.model.trace;

import java.util.ArrayList;
import java.util.List;

import javafx.scene.paint.Color;

public class TraceMultiStroke {

	public Color color;
	public int lineWidth;
	
	public List<TraceStroke> strokes = new ArrayList<>();
	
	public void removeLastStroke() {
		if (! strokes.isEmpty()) {
			strokes.remove(strokes.size() - 1);
		}
	}

	public boolean isEmpty() {
		return strokes.isEmpty();
	}
}
