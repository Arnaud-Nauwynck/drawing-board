package fr.an.drawingboard.ui.impl;

import java.util.ArrayList;
import java.util.List;

import fr.an.drawingboard.model.trace.Pt2D;
import fr.an.drawingboard.model.trace.TraceGesture;
import fr.an.drawingboard.model.trace.TracePath;
import fr.an.drawingboard.model.trace.TracePathElement;
import fr.an.drawingboard.model.trace.TracePathElement.DiscretePointsTracePathElement;
import fr.an.drawingboard.model.trace.TracePathElement.SegmentTracePathElement;
import fr.an.drawingboard.model.trace.TracePt;
import lombok.Getter;
import lombok.Setter;
import lombok.val;

/**
 * TODO does not work very well ... remove too many corners 
 *
 */
@Getter @Setter
public class AlmostAlignedPtsSimplifier {

	private static final boolean DEBUG = true;

	protected double simplifyMergePtDist = 5; 
	
	protected double maxOrth05DistRatio = .03; 
	protected double maxOrthDistOffset = 1.0; // unit: pixels 
	
	public void simplifyGestureLines(TraceGesture gesture) {
		for(val path: gesture.pathes) {
			simplifyAlmostAlignedPtLines(path);
		}
		
	}

	public void simplifyAlmostAlignedPtLines(TracePath path) {
		List<TracePathElement> pathElements = path.pathElements;
		int pathElementCount = pathElements.size();
		for(int pathElemntIndex = pathElementCount-1; pathElemntIndex >= 0; pathElemntIndex--) {
			val pathElt = pathElements.get(pathElemntIndex);
			if (pathElt instanceof DiscretePointsTracePathElement) {
				val pathEltLine = (DiscretePointsTracePathElement) pathElt;
				List<TracePt> tracePts = pathEltLine.tracePts;
				simplifyNarrowPtLines(tracePts);
				simplifyAlmostAlignedPtLines(tracePts);
				
				// if remaining only 2 points => simplify to SegmentTracePathElement
				if (tracePts.size() == 2) {
					val replPathElt = new SegmentTracePathElement(pathEltLine.startPt, pathEltLine.endPt);
					pathElements.set(pathElemntIndex, replPathElt);
				}
			}
		}
	}

	public void simplifyAlmostAlignedPtLines(List<TracePt> tracePts) {
		int ptsCount = tracePts.size();
		if (ptsCount < 3) {
			return;
		}
		Pt2D curStartLinePt = tracePts.get(0).xy();
		int currStartLineIndex = 0;
		Pt2D prevPt = tracePts.get(1).xy();
		List<Pt2D> prevPts = new ArrayList<>();
		AlignPtsInfo alignInfo = new AlignPtsInfo();
		alignInfo.debug = DEBUG;
		
		for(int ptIndex = 2; ptIndex < tracePts.size()-1; ptIndex++) {
			Pt2D pt = tracePts.get(ptIndex).xy();
			// all almost alined?
			// boolean almostAligned = areAllMidPtsAlmostAligned(curStartLinePt, prevPts, pt);
			boolean almostAligned = isMidPtAlmostAligned(curStartLinePt, prevPt, pt, alignInfo);

			if (almostAligned) {
				// remove pt, continue iterating descending on next
				// tracePts.remove(ptIndex);
				prevPts.add(pt);
				prevPt = pt;
				ptIndex--;
			} else {
				for (int i = currStartLineIndex+1; i < ptIndex; i++) {
					tracePts.remove(i);
				}
				
				curStartLinePt = prevPt;
				currStartLineIndex = ptIndex+1; // ??
				prevPt = pt;
				prevPts.clear();
			}
		}
	}
	
	// TODO.. very inneficient!!! .. use ptMaxAngle, ptMinAngle
	public boolean areAllMidPtsAlmostAligned(Pt2D startPt, List<Pt2D> prevPts, Pt2D endPt, AlignPtsInfo alignInfo) {
		for(val prevPt: prevPts) {
			if (!isMidPtAlmostAligned(startPt, prevPt, endPt, alignInfo)) {
				return false;
			}
		}
		return true;
	}
	
	public void simplifyNarrowPtLines(List<TracePt> tracePts) {
		TracePt prevPt = tracePts.get(0);
		for(int ptIndex = 1; ptIndex < tracePts.size()-1; ptIndex++) {
			TracePt  pt = tracePts.get(ptIndex);
			boolean isNarrow = (TracePt.dist(prevPt, pt) < simplifyMergePtDist);
			if (isNarrow) {
				// remove pt, continue iterating descending on next
				tracePts.remove(ptIndex);
				ptIndex--;
			} else {
				prevPt = pt;
			}
		}
	}

	protected static class AlignPtsInfo {
		boolean debug;
		
	}
	
	public boolean isMidPtAlmostAligned(Pt2D ptA, Pt2D ptM, Pt2D ptB, AlignPtsInfo alignInfo) {
		return isMidPtAlmostAligned(ptA.x, ptA.y, ptM.x, ptM.y, ptB.x, ptB.y, alignInfo);
	}
	
	/**
	 * <PRE>
	 *                     +  maxH/(0.5 AB)=maxRatio 
	 * n.AM +--     M +    |
	 *      |     __ /|    | 
	 *      | __/---  |    |
	 *      ------------------------------   /\ offset
	 *      +---------+----+--------------+  |
	 *      A       u.AM                  B
	 *   
	 * </PRE>
	 * H = orthogonal proj of P on (A,B)... h= dist
	 * ratio dist = h / ||AB||   <= maxRatio
	 * 
	 */
	public boolean isMidPtAlmostAligned(double ax, double ay, double x, double y, double bx, double by, AlignPtsInfo alignInfo) {
		double dist = TracePt.dist(ax, ay, bx, by);
		if (dist == 0) {
			// TOADD remove empty! (should not occur)
			return (ax == x && ay == y);
		}
		double renorm = 1.0 / dist;
		double vectU_x = renorm * (bx - ax);
		double vectU_y = renorm * (by - ay);

		double AM_x = x - ax, AM_y = y - ay; 
		double u_AM =    vectU_x * AM_x + vectU_y * AM_y;
		double n_AM = (-vectU_y) * AM_x + vectU_x * AM_y;
		
		double AM = Math.sqrt(AM_x * AM_x + AM_y * AM_y);
		if (AM < dist) {
			return false;
		}
		double renorm_AM = 1.0 / AM; 
		double angleA = Math.atan2(n_AM * renorm_AM, u_AM * renorm_AM);
		
		double BM_x = x - bx, BM_y = y - by; 
		double u_BM =    vectU_x * (x - bx) + vectU_y * (y - by);
		double BM = Math.sqrt(BM_x * BM_x + BM_y * BM_y);
		if (BM < dist) {
			return false;
		}
		double renorm_BM = 1.0 / BM; 
		double angleB = Math.atan2(n_AM * renorm_BM, u_BM * renorm_BM);
		
		
		
		if (alignInfo.debug) {
			System.out.println("A(" + ax + "," + ay+")" 
					+ " Pt(" + x + "," + y + ")"
					+ " B(" + bx+ "," + by + ")"
					+ " AB(" + (bx-ax) + "," + (by-ay) + ")"
					+ " AP(" + (x-ax) + "," + (y-ay) + ")"
					+ " |AB|:" + dist
					+ " u(" + vectU_x + "," + vectU_y + ")"
					+ "... Pt'(h:" + n_AM + ", s:" + u_AM + ", d-s:" + u_BM + ")" 
					+ "angle A°:" + (angleA *180/Math.PI)
					+ " B°:" + (angleB *180/Math.PI)
					);
		}
//		if (
//				Math.abs(n_AM) < maxOrthDistOffset
//				&& dist> maxOrthDistOffset 
//				) {
//			if (DEBUG) {
//				System.out.println(" ****** h " + n_AM + " < " + maxOrthDistOffset + " maxOrthDistOffset");
//			}
//			return true;
//		}
		
		double orthDistWithoutOffset = Math.abs(n_AM) - maxOrthDistOffset;
		// double pt_angleRatioA = Math.abs(orthDistWithoutOffset / ((u_AM != 0)? u_AM : 1));
		// double pt_angleRatioB = Math.abs(orthDistWithoutOffset / ((u_BM != 0)? u_BM : 1));
		if (orthDistWithoutOffset < Math.abs(u_AM)*maxOrth05DistRatio
				&& orthDistWithoutOffset < Math.abs(u_BM)*maxOrth05DistRatio
				) {
			if (alignInfo.debug) {
				System.out.println(" ****** ratioAngleA " + Math.abs(orthDistWithoutOffset/u_AM )
					+ " ratioAngleB: " + Math.abs(orthDistWithoutOffset/u_BM )
					+ " ?< " + maxOrth05DistRatio);
			}
			return true;
		}
		return false;
	}
	
}
