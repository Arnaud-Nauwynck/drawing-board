package fr.an.drawingboard.model.trace;

import java.util.ArrayList;
import java.util.List;

import lombok.val;

public class TracePath {

	public List<TracePathElement> pathElements = new ArrayList<>();

	public void add(TracePathElement pathElement) {
		this.pathElements.add(pathElement);
	}

	
	public double pathDistLength() {
		double res = 0.0;
		for(val pathElt : pathElements) {
			double pathDistLen = pathElt.endPt.pathAbsciss - pathElt.startPt.pathAbsciss; 
			res += pathDistLen;
		}
		return res;
	}

}
