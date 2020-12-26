package fr.an.drawingboard.recognizer.trace;

import java.util.List;
import java.util.PriorityQueue;

import fr.an.drawingboard.model.trace.TraceGesture;
import fr.an.drawingboard.model.trace.TracePath;
import fr.an.drawingboard.model.trace.TracePathElement;
import fr.an.drawingboard.model.trace.TracePathElement.DiscretePointsTracePathElement;
import fr.an.drawingboard.model.trace.TracePt;
import fr.an.drawingboard.util.LsUtils;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.val;

public class AlmostAlignedPtsSimplifier {

	@Getter @Setter
	protected double maxAngleChange = 7 *Math.PI/180;

	public void simplifyGestureLines(TraceGesture gesture) {
		for(val path: gesture.pathes()) {
			simplifyAlmostAlignedPts(path);
		}
	}

	public void simplifyAlmostAlignedPts(TracePath path) {
		for(val pathElt: path.pathElements) {
			simplifyAlmostAlignedPts(pathElt);
		}
	}

	public void simplifyAlmostAlignedPts(TracePathElement pathElt) {
		if (pathElt instanceof DiscretePointsTracePathElement) {
			DiscretePointsTracePathElement ptsElt = (DiscretePointsTracePathElement) pathElt;
			simplifyAlmostAlignedPts(ptsElt.tracePts);
		}
	}

	public void simplifyAlmostAlignedPts(List<TracePt> tracePts) {
		int ptsCount = tracePts.size();
		if (ptsCount <= 2) {
			return;
		}
		PriorityQueue<AngleToNeighboorTracePt> ptsByAngleMinChangeQueue = new PriorityQueue<>();
		{ // put pts in priorityQueue by angle to neighboor
			// wrap pt with AngleToNeighboorTracePt for dist,prev,next
			List<AngleToNeighboorTracePt> neightboorPts = LsUtils.map(tracePts, pt -> new AngleToNeighboorTracePt(pt));
			// link pts with updated prev,next
			AngleToNeighboorTracePt prevNeightboorPt = neightboorPts.get(0);
			AngleToNeighboorTracePt neightboorPt = neightboorPts.get(1);
			prevNeightboorPt.initNeighboors(null, neightboorPt);
			for(int i = 1; i < ptsCount-1; i++) {
				val nextNeighboorPt = neightboorPts.get(i+1);
				neightboorPt.initNeighboors(prevNeightboorPt, nextNeighboorPt);
				ptsByAngleMinChangeQueue.add(neightboorPt);
				prevNeightboorPt = neightboorPt; neightboorPt = nextNeighboorPt;
			}
			neightboorPt.initNeighboors(prevNeightboorPt, null);
		}
		// loop on ptToRemove, test for removal, and update neighboors + neighboor dist
		while(!ptsByAngleMinChangeQueue.isEmpty()) {
			AngleToNeighboorTracePt ptToRemove = ptsByAngleMinChangeQueue.poll();
			if (ptToRemove.angleChange > maxAngleChange
					// && ptToRemove.initialAngleChange > maxAngleChange // ??
					) {
				break; // finished: all other pts have greater angle change
			}
			val prevNeightboorPt = ptToRemove.prev;
			val nextNeighboorPt = ptToRemove.next;
			// remove pt from trace, except if it is a stop-point
			if (!ptToRemove.pt.isStopPoint()) {
				tracePts.remove(ptToRemove.pt);
			}
			// update neighboors
			if (prevNeightboorPt != null) {
				ptsByAngleMinChangeQueue.remove(prevNeightboorPt); // tmp remove before update
				prevNeightboorPt.updateNeighboors(prevNeightboorPt.prev, nextNeighboorPt);
				ptsByAngleMinChangeQueue.add(prevNeightboorPt); // re-add
			}
			if (nextNeighboorPt != null) {
				ptsByAngleMinChangeQueue.remove(nextNeighboorPt); // tmp remove before update
				nextNeighboorPt.updateNeighboors(prevNeightboorPt, nextNeighboorPt.next);
				ptsByAngleMinChangeQueue.add(nextNeighboorPt); // re-add
			}
		}
		
	}

	@RequiredArgsConstructor
	private static class AngleToNeighboorTracePt implements Comparable<AngleToNeighboorTracePt> {
		private final TracePt pt;
		private AngleToNeighboorTracePt prev;
		private AngleToNeighboorTracePt next;
		
		private double initialAngleChange;
		private double angleChange;

		public void initNeighboors(AngleToNeighboorTracePt prev, AngleToNeighboorTracePt next) {
			updateNeighboors(prev, next);
			this.initialAngleChange = angleChange;
		}

		public void updateNeighboors(AngleToNeighboorTracePt prev, AngleToNeighboorTracePt next) {
			this.prev = prev;
			this.next = next;
			if (prev != null && next != null) {
				double prevVectx = pt.x - prev.pt.x;
				double prevVecty = pt.y - prev.pt.y;
				double prevAngle = Math.atan2(prevVecty, prevVectx);
				double nextVectx = next.pt.x - pt.x;
				double nextVecty = next.pt.y - pt.y;
				double nextAngle = Math.atan2(nextVecty, nextVectx);
				this.angleChange = Math.abs(nextAngle - prevAngle);
			} else {
				this.angleChange = 180.0; // dummy value.. do not remove end points
			}
		}

		@Override
		public int compareTo(AngleToNeighboorTracePt other) {
			int res = Double.compare(angleChange, other.angleChange);
			return res;
		}
	}

}
