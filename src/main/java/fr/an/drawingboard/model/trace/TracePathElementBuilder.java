package fr.an.drawingboard.model.trace;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;

public class TracePathElementBuilder {

	public final List<TracePt> tracePts = new ArrayList<>();

	@Getter
	private int computedStopPointUpTo = 0;
	
	public TracePathElementBuilder() {
	}

	public TracePathElementBuilder(TracePt startPt) {
		this.tracePts.add(startPt);
	}

	public boolean isEmpty() {
		return tracePts.size() < 2;
	}
	
	public TracePt appendTracePt(double x, double y, long time, int pressure) {
		double pathAbsciss;
		if (tracePts.isEmpty()) {
			pathAbsciss = 0;
		} else {
			TracePt last = lastPt(); 
			pathAbsciss = last.getPathAbsciss() + TracePt.distRoundedPixel(last.x,  last.y, x, y);
		}
		TracePt pt = new TracePt(x, y);
		pt.time = time;
		pt.pressure = pressure;
		pt.pathAbsciss = pathAbsciss;
		tracePts.add(pt);
		return pt;
	}
	
	public void addComputedStopPointAt(int index, boolean stopPoint, boolean mergeStopPoint) {
		computedStopPointUpTo = index;
		TracePt pt = tracePts.get(index);
		pt.setStopPoint(stopPoint);
		pt.setMergeStopPoint(mergeStopPoint);
	}

	public int tracePtCount() {
		return tracePts.size();
	}
	
	public TracePt tracePt(int index) {
		return tracePts.get(index);
	}

	public TracePt lastPt() {
		int len = tracePts.size();
		return (len == 0)? null : tracePts.get(len - 1); 
	}

}
