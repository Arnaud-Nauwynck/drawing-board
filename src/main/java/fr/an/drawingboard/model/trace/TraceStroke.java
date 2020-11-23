package fr.an.drawingboard.model.trace;

import java.util.ArrayList;
import java.util.List;

import lombok.val;

public class TraceStroke {

	public List<TraceStrokePathElement> pathElements = new ArrayList<>();

	public void add(TraceStrokePathElement pathElement) {
		this.pathElements.add(pathElement);
	}

	
	public double pathDistLength() {
		double res = 0.0;
		for(val pathElt : pathElements) {
			double pathDistLen = pathElt.endPt.strokeCurveAbsciss - pathElt.startPt.strokeCurveAbsciss; 
			res += pathDistLen;
		}
		return res;
	}

}
