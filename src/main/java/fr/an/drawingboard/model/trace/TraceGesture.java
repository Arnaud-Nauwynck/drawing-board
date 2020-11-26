package fr.an.drawingboard.model.trace;

import java.util.ArrayList;
import java.util.List;

import fr.an.drawingboard.model.shape.Shape;
import javafx.scene.paint.Color;
import lombok.val;

public class TraceGesture {

	public Color color;
	public int lineWidth;
	
	public List<TracePath> pathes = new ArrayList<>();
	
	public Shape recognizedShape;
	
	public void removeLastPath() {
		if (! pathes.isEmpty()) {
			pathes.remove(pathes.size() - 1);
		}
	}

	public boolean isEmpty() {
		return pathes.isEmpty();
	}
	
	public TracePath getLast() {
		return (pathes.isEmpty())? null : pathes.get(pathes.size() - 1);
	}
	
	public TracePath appendNewPath() {
		val res = new TracePath();
		pathes.add(res);
		return res;
	}
	

	public List<Double> pathDistLengths() {
		List<Double> res = new ArrayList<>(pathes.size());
		for(val path : pathes) {
			res.add(path.pathDistLength());
		}
		return res;
	}

	

	public static double sum(List<Double> values) {
		double res = 0.0;
		for(val d : values) {
			res += d;
		}
		return res;
	}


}
