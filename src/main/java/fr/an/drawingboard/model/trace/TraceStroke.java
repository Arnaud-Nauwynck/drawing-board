package fr.an.drawingboard.model.trace;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;

public class TraceStroke {

	public List<TracePt> tracePts = new ArrayList<>();

	@Getter
	private int computedStopPointUpTo = 0;
	
	public TracePt appendTracePt(int x, int y, long time, int pressure) {
		double strokeCurveAbsciss;
		if (tracePts.isEmpty()) {
			strokeCurveAbsciss = 0;
		} else {
			TracePt last = lastPt(); 
			strokeCurveAbsciss = last.getStrokeCurveAbsciss() + TracePt.distRoundedPixel(last.x,  last.y, x, y);
		}
		TracePt pt = new TracePt(x, y, time, pressure, strokeCurveAbsciss);
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
