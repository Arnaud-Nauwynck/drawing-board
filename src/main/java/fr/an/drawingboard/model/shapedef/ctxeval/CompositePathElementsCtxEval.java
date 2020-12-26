package fr.an.drawingboard.model.shapedef.ctxeval;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import fr.an.drawingboard.geom2d.ParamWeightedPt2D;
import fr.an.drawingboard.geom2d.Pt2D;
import fr.an.drawingboard.geom2d.utils.PolygonalDistUtils;
import lombok.AllArgsConstructor;
import lombok.val;

@AllArgsConstructor
public class CompositePathElementsCtxEval {

	private final double[] cumulDist;
	private final PathElementCtxEval[] elements;
	
	public CompositePathElementsCtxEval(List<PathElementCtxEval> elements) {
		int count = elements.size();
		this.cumulDist = new double[count];
		this.elements = new PathElementCtxEval[count];
		double currDist = 0.0;
		for(int i = 0; i < count; i++) {
			val e = elements.get(i);
			currDist += e.getDist();
			cumulDist[i] = currDist;
			this.elements[i] = e;
		}
	}	

	public CompositePathElementsCtxEval(CompositePathElementsCtxEval comp1, CompositePathElementsCtxEval comp2) {
		int comp1Count = comp1.elements.length;
		int comp2Count = comp2.elements.length;
		int count = comp1Count + comp2Count;
		this.cumulDist = new double[count];
		this.elements = new PathElementCtxEval[count];
		for(int i = 0; i < comp1Count; i++) {
			cumulDist[i] = comp1.cumulDist[i];
			this.elements[i] = comp1.elements[i];
		}
		double cumulDist1 = cumulDist[comp1Count-1];
		for(int j = 0; j < comp2Count; j++) {
			cumulDist[comp1Count+j] = cumulDist1 + comp2.cumulDist[j];
			this.elements[comp1Count+j] = comp2.elements[j];
		}
	}
			
	public double getTotalDist() {
		return (elements.length != 0)? cumulDist[elements.length-1] : 0.0;
	}
	
	public int getElementCount() {
		return elements.length;
	}

	public PathElementCtxEval getElement(int n) {
		return elements[n];
	}
	
	@AllArgsConstructor
	public static class PtAtPathElementCtxEval {
		public Pt2D pt;
		public PathElementCtxEval element;
		public double startDist;
		public double endDist;
		public double paramIn01;
	}

	public Pt2D startPt() {
		if (elements.length == 0) return null;
		return elements[0].getStartPt();
	}

	public Pt2D endPt() {
		if (elements.length == 0) return null;
		return elements[elements.length-1].getEndPt();
	}

	public PtAtPathElementCtxEval pointAtDist(double ptDist) {
		int count = elements.length;
		if (count == 0) {
			return null;
		}
		if (ptDist <= 0.0) {
			PathElementCtxEval e = elements[0];
			return new PtAtPathElementCtxEval(e.getStartPt(), e, 0.0, cumulDist[0], 0.0);
		}
		double totalDist = cumulDist[count-1];
		if (ptDist >= totalDist) {
			PathElementCtxEval e = elements[count-1];
			return new PtAtPathElementCtxEval(e.getEndPt(), e, (count>1)?cumulDist[count-2]:0.0, totalDist, 1.0);
		}
		int found = Arrays.binarySearch(cumulDist, ptDist);
		if (found >= 0) {
			// found exact pt
			double distBefore = (found>1)? cumulDist[found-1] : 0.0;
			double distAfter = cumulDist[found];
			PathElementCtxEval e = elements[found];
			return new PtAtPathElementCtxEval(e.getStartPt(), e, distBefore, distAfter, 0.0); // ??TOCHECK
		} else {
			int indexBefore = -(found+1);
			double distBefore = (indexBefore>1)? cumulDist[indexBefore-1] : 0.0;
			double distAfter = cumulDist[indexBefore];
			double param = (ptDist - distBefore) / (distAfter - distBefore);
			PathElementCtxEval e = elements[indexBefore];
			return new PtAtPathElementCtxEval(e.pointAtParam(param), e, distBefore, distAfter, param); // ??TOCHECK
		}
	}

	public List<ParamWeightedPt2D> discretizeWeigthedPts() {
		// tochange, naive implementation using 20 pts
		return discretizeWeigthedPts_fixedStep(20);
	}
	
	public List<ParamWeightedPt2D> discretizeWeigthedPts_fixedStep(int N) {
		List<Pt2D> pts = discretizePts_fixedStep(N);
		return PolygonalDistUtils.ptsParamWeightedPts_polygonalDistance(pts);
	}

	public List<Pt2D> discretizePts_fixedStep(int N) {
		List<Pt2D> res = new ArrayList<>(N);
		val totalDist = cumulDist[elements.length-1];
		val step = totalDist / (N-1);
		res.add(startPt());
		double currDist = 0.0;
		int currElementIndex = 0;
		PathElementCtxEval currElement = elements[currElementIndex];
		double currElementStartDist = 0.0;
		double currElementEndDist = cumulDist[0];
		for(int i = 1; i < N-1; i++) {
			currDist += step;
			if (currDist > cumulDist[currElementIndex]) {
				currElementIndex++;
				if (currElementIndex == elements.length) {
					break; // should not occur
				}
				currElement = elements[currElementIndex];
				currElementStartDist = currElementEndDist;
				currElementEndDist += cumulDist[currElementIndex];
			}
			// eval pt at currDist in currElement
			double param = (currDist - currElementStartDist) / (currElementEndDist - currElementStartDist);
			res.add(currElement.pointAtParam(param));
		}
		res.add(endPt());
		return res;
	}

	public interface PtAtDistIterator {
		public PtAtPathElementCtxEval nextPtAtDist(double atDist);
	}
	public PtAtDistIterator pointAtDistIterator() {
		return new InnerPtAtDistIterator();
	}
	
	private class InnerPtAtDistIterator implements PtAtDistIterator {
		// CompositePathElementsCtxEval composite;
		PtAtPathElementCtxEval entry = new PtAtPathElementCtxEval(null, null, 0, 0, 0);
		// double currParam;
		double currDist = 0.0;
		int currElementIndex = 0;
		PathElementCtxEval currElement; // = elements[currElementIndex];
		double currElementStartDist = 0.0;
		double currElementEndDist; // = dist[0];
		
		
		public InnerPtAtDistIterator() {
			currElementIndex = 0;
			entry.element = currElement = elements[currElementIndex];
			currElementStartDist = 0.0;
			currElementEndDist = cumulDist[currElementIndex];
		}

		@Override
		public PtAtPathElementCtxEval nextPtAtDist(double atDist) {
			if (atDist < currDist) {
				throw new IllegalArgumentException();
			}
			currDist = atDist;
			while(currElementEndDist < currDist && currElementIndex < elements.length) {
				currElementIndex++;
				if (currElementIndex < elements.length) {
					currElement = elements[currElementIndex];
					currElementStartDist = currElementEndDist;
					currElementEndDist = cumulDist[currElementIndex];
					entry.element = currElement;
				}
			}
			if (currElementIndex >= elements.length) {
				entry.pt = currElement.getEndPt();
				entry.paramIn01 = 1.0;
				return entry; 
			}
			// eval pt at currDist in currElement
			double param = entry.paramIn01 = (currDist - currElementStartDist) / (currElementEndDist - currElementStartDist);
			entry.pt = currElement.pointAtParam(param);
			return entry;
		}
	}
	
	
}
