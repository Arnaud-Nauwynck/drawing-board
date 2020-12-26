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

public class TooNarrowPtsSimplifier {

	@Getter @Setter
	protected double simplifyMergePtDist = 5; 

	public void simplifyTooNarrowPts(TraceGesture gesture) {
		for(val path: gesture.pathes()) {
			simplifyTooNarrowPts(path);
		}
	}

	public void simplifyTooNarrowPts(TracePath path) {
		for(val pathElt: path.pathElements) {
			simplifyTooNarrowPts(pathElt);
		}
	}

	public void simplifyTooNarrowPts(TracePathElement pathElt) {
		if (pathElt instanceof DiscretePointsTracePathElement) {
			DiscretePointsTracePathElement ptsElt = (DiscretePointsTracePathElement) pathElt;
			simplifyTooNarrowPts(ptsElt.tracePts);
		}
	}

	public void simplifyTooNarrowPts(List<TracePt> tracePts) {
		int ptsCount = tracePts.size();
		if (ptsCount <= 2) {
			return;
		}
		PriorityQueue<DistToNeighboorTracePt> ptsByMinDistQueue = new PriorityQueue<>();
		{ // put pts in priorityQueue by dist to neighboor
			// wrap pt with DistToNeighboorTracePt for dist,prev,next
			List<DistToNeighboorTracePt> neightboorPts = LsUtils.map(tracePts, pt -> new DistToNeighboorTracePt(pt));
			// link pts with updated prev,next
			DistToNeighboorTracePt prevNeightboorPt = neightboorPts.get(0);
			DistToNeighboorTracePt neightboorPt = neightboorPts.get(1);
			prevNeightboorPt.initNeighboors(null, neightboorPt);
			for(int i = 1; i < ptsCount-1; i++) {
				val nextNeighboorPt = neightboorPts.get(i+1);
				neightboorPt.initNeighboors(prevNeightboorPt, nextNeighboorPt);
				ptsByMinDistQueue.add(neightboorPt);
				prevNeightboorPt = neightboorPt; neightboorPt = nextNeighboorPt;
			}
			neightboorPt.initNeighboors(prevNeightboorPt, null);
		}
		// loop on ptToRemove, test for removal, and update neighboors + neighboor dist
		while(!ptsByMinDistQueue.isEmpty()) {
			DistToNeighboorTracePt ptToRemove = ptsByMinDistQueue.poll();
			if (ptToRemove.sumDistNeighboor > simplifyMergePtDist
//					&& ptToRemove.initialSumDistNeighboor > simplifyMergePtDist // ??
					) {
				break; // finished: all other pts have greater dist
			}
			val prevNeightboorPt = ptToRemove.prev;
			val nextNeighboorPt = ptToRemove.next;
			// remove pt from trace, except if it is a stop-point
			if (!ptToRemove.pt.isStopPoint()) {
				tracePts.remove(ptToRemove.pt);
			}
			// update neighboors
			if (prevNeightboorPt != null) {
				ptsByMinDistQueue.remove(prevNeightboorPt); // tmp remove before update
				prevNeightboorPt.updateNeighboors(prevNeightboorPt.prev, nextNeighboorPt);
				ptsByMinDistQueue.add(prevNeightboorPt); // re-add
			}
			if (nextNeighboorPt != null) {
				ptsByMinDistQueue.remove(nextNeighboorPt); // tmp remove before update
				nextNeighboorPt.updateNeighboors(prevNeightboorPt, nextNeighboorPt.next);
				ptsByMinDistQueue.add(nextNeighboorPt); // re-add
			}
		}
		
	}

	@RequiredArgsConstructor
	private static class DistToNeighboorTracePt implements Comparable<DistToNeighboorTracePt> {
		private final TracePt pt;
		private DistToNeighboorTracePt prev;
		private DistToNeighboorTracePt next;
		
		private double initialSumDistNeighboor;
		private double sumDistNeighboor;

		public void initNeighboors(DistToNeighboorTracePt prev, DistToNeighboorTracePt next) {
			updateNeighboors(prev, next);
			this.initialSumDistNeighboor = sumDistNeighboor;
		}

		public void updateNeighboors(DistToNeighboorTracePt prev, DistToNeighboorTracePt next) {
			this.prev = prev;
			this.next = next;
			this.sumDistNeighboor = (prev != null && next != null)? 0.5 * (TracePt.dist(prev.pt, pt) + TracePt.dist(pt, next.pt))
					: (prev != null)? TracePt.dist(prev.pt, pt)
							: (next != null)? TracePt.dist(pt, next.pt)
									: 0.0;
		}

		@Override
		public int compareTo(DistToNeighboorTracePt other) {
			int res = Double.compare(sumDistNeighboor, other.sumDistNeighboor);
			return res;
		}
		
		
	}
}
