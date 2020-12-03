package fr.an.drawingboard.recognizer.trace;

import fr.an.drawingboard.model.trace.TracePathElementBuilder;
import fr.an.drawingboard.model.trace.TracePt;
import lombok.Getter;
import lombok.Setter;

public class StopPointDetector {

	/**
	 * unit: millis
	 */
	@Getter @Setter
	private double stationaryThresholdMillis = 90.0;

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

	/**
	 * unit: pixel
	 */
	@Getter @Setter
	private double moveThresholdFastChange = 2;

	/**
	 * unit: [-1, 1] .. Math.cos() of angle
	 */
	@Getter @Setter
	private double moveThresholdCosAngleFastChange = 0.6;

	/**
	 * distance in pixel to merge too-narrow stop points
	 */
	private double distMergeStopPoint = 30;
	/**
	 * number of previous points to search for stop point to merge 
	 */
	private int mergePrevCount = 20;
	
	
	@Getter @Setter
	private boolean debugPrint = false;
	
	public boolean onNewTracePt(TracePathElementBuilder curr, TracePt pt) {
		int count = curr.tracePtCount();
		if (count <= 1 ) {
			return false;
		}
		TracePt prevPt = curr.tracePt(count - 2);
		double dist = (pt.pathAbsciss - prevPt.pathAbsciss);
//		if (dist < distMergeStopPoint && prevPt.isStopPoint()) {
//			return false;
//		}
		int prevCount = Math.min(mergePrevCount, count);
		for(int i = 2; i < prevCount; i++) {
			TracePt prevPti = curr.tracePt(count - prevCount);
			if (prevPti.isStopPointOrMergeStop()) {
				double disti = TracePt.dist(pt, prevPti);
				if (disti <= distMergeStopPoint) {
					return false;
				}
			}
		}
		
		int dt = (int) (pt.time - prevPt.time);
		// double speed = dist / Math.max(1, pt.time - prevPt.time);
		boolean stopPoint = (dist < moveThresholdPerTime && dt > intervalThresholdMillis)
				|| (dt > stationaryThresholdMillis);
		
		StringBuilder debugMsg = null;
		if (debugPrint) {
			debugMsg = new StringBuilder();
			int dx = pt.x - prevPt.x, dy = pt.y - prevPt.y;
			debugMsg.append("pt[" + (count-1) + "] move:" + dx + "," + dy 
				+ " dist:" + dist + " ?< " + moveThresholdPerTime
				+ " dt:" + dt + " ?> " + stationaryThresholdMillis);
		}

		boolean mergeStopPoint = false;

		// detect big change of direction (even with high-speed)
		if (!stopPoint && count > 8) {
			TracePt pt4 = curr.tracePt(count - 7);
			TracePt pt2 = curr.tracePt(count - 3);
			int prevMoveX = pt2.x - pt4.x; 
			int prevMoveY = pt2.y - pt4.y; 
			int moveX = pt.x - pt2.x; 
			int moveY = pt.y - pt2.y; 
			double squarePrevMove = moveX * moveX + moveY * moveY;
			double squareMove = prevMoveX * prevMoveX + prevMoveY * prevMoveY;
			
			double scalarProduct = moveX * prevMoveX + moveY * prevMoveY;
			double normsProduct = Math.sqrt(squarePrevMove * squareMove);
			double cosAngleMove = scalarProduct / normsProduct;
			if (debugPrint) {
				debugMsg.append(" move prev^2:" + squarePrevMove + " move^2:" + squareMove
						+ " >=? " + (moveThresholdFastChange*moveThresholdFastChange)
						+ " cos:" + cosAngleMove + " <=? " + moveThresholdCosAngleFastChange);
			}
			if (squarePrevMove >= moveThresholdFastChange*moveThresholdFastChange
					&& squareMove >= moveThresholdFastChange*moveThresholdFastChange) {
				if (cosAngleMove <= moveThresholdCosAngleFastChange) {
					curr.addComputedStopPointAt(count-3, true, false);
					if (debugPrint) {
						debugMsg.append(" prev " + (count-3) + " .. STOP cos " + cosAngleMove + " <= " + moveThresholdCosAngleFastChange);
					}
					mergeStopPoint = true;
					stopPoint = false;
					return true;
				}
			}
			
		}
		
		// merge too narrow stop-points
		if (stopPoint) {
			int toIndex = Math.max(0, count-5);
			for(int i = count-2; i >= toIndex; i--) {
				TracePt pastPt = curr.tracePt(i);
				boolean wasStop = pastPt.isStopPoint() || pastPt.isMergeStopPoint();
				if (wasStop && (pt.pathAbsciss - pastPt.pathAbsciss < distMergeStopPoint)) {
					mergeStopPoint = true;
					stopPoint = false;
					break;
				}
			}
		}
		
		if (debugPrint) {
			System.out.println(debugMsg 
					+ ((stopPoint)? " STOP" : "")
					+ ((mergeStopPoint)? " MERGE-STOP" : "")
					+ ((dt > stationaryThresholdMillis)? " STATIONNARY dt > " + stationaryThresholdMillis : "") 
					);
		}
		curr.addComputedStopPointAt(count-1, stopPoint, mergeStopPoint);
		return stopPoint && !mergeStopPoint;
	}

}
