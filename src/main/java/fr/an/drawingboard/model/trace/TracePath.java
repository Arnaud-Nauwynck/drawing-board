package fr.an.drawingboard.model.trace;

import java.util.ArrayList;
import java.util.List;

import lombok.val;

public class TracePath {

	public List<TracePathElement> pathElements = new ArrayList<>();

	public boolean isEmpty() {
		return pathElements.isEmpty();
	}

	public void addPathElement(TracePathElement pathElement) {
		this.pathElements.add(pathElement);
	}

	public TracePathElement getLastPathElement() {
		return (pathElements.isEmpty())? null : pathElements.get(pathElements.size()-1); 
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
