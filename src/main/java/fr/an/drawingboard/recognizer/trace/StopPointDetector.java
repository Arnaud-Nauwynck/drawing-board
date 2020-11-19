package fr.an.drawingboard.recognizer.trace;

import fr.an.drawingboard.model.trace.TracePt;
import fr.an.drawingboard.model.trace.TraceStroke;
import lombok.Getter;
import lombok.Setter;

public class StopPointDetector {

	/**
	 * unit: millis
	 */
	@Getter @Setter
	private double stationaryThresholdMillis = 200.0;

	/**
	 * unit: millis
	 */
	@Getter @Setter
	private double intervalThresholdMillis = 40.0;

	/**
	 * unit: pixel
	 */
	@Getter @Setter
	private double moveThresholdPerTime = 3;
	
	private double distMergeStopPoint = 5;
	
	private boolean debugPrint = false;
	
	public void onNewTracePt(TraceStroke stroke, TracePt pt) {
		int count = stroke.tracePtCount();
		if (count > 1 ) {
			TracePt prevPt = stroke.tracePt(count - 2);
			double dist = (pt.strokeCurveAbsciss - prevPt.strokeCurveAbsciss);
			int dt = (int) (pt.time - prevPt.time);
			// double speed = dist / Math.max(1, pt.time - prevPt.time);
			boolean stopPoint = (dist < moveThresholdPerTime && dt > intervalThresholdMillis)
					|| (dt > stationaryThresholdMillis);
			
			// merge too narrow stop-points
			boolean mergeStopPoint = false;
			if (stopPoint) {
				int toIndex = Math.max(0, count-5);
				for(int i = count-2; i >= toIndex; i--) {
					TracePt pastPt = stroke.tracePt(i);
					boolean wasStop = pastPt.isStopPoint() || pastPt.isMergeStopPoint();
					if (wasStop && (pt.strokeCurveAbsciss - pastPt.strokeCurveAbsciss < distMergeStopPoint)) {
						mergeStopPoint = true;
						stopPoint = false;
						break;
					}
				}
			}
			
			if (debugPrint) {
				int dx = pt.x - prevPt.x, dy = pt.y - prevPt.y;
				System.out.println("pt[" + (count-1) + "] move:" + dx + "," + dy 
						+ " dist:" + dist + " ?< " + moveThresholdPerTime
						+ " dt:" + dt + " ?> " + stationaryThresholdMillis
						+ ((stopPoint)? " STOP" : "")
						+ ((mergeStopPoint)? " MERGE-STOP" : "")
						+ ((dt > stationaryThresholdMillis)? " STATIONNARY dt > " + stationaryThresholdMillis : "") 
						);
			}
			stroke.addComputedStopPointAt(count-1, stopPoint, mergeStopPoint);
		}
	}

}
