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

	private final double[] dist;
	private final PathElementCtxEval[] elements;
	
	public CompositePathElementsCtxEval(List<PathElementCtxEval> elements) {
		int count = elements.size();
		this.dist = new double[count];
		this.elements = new PathElementCtxEval[count];
		double currDist = 0.0;
		for(int i = 0; i < count; i++) {
			val e = elements.get(i);
			currDist += e.getDist();
			dist[i] = currDist;
			this.elements[i] = e;
		}
	}	

	public int getElementCount() {
		return elements.length;
	}

	public PathElementCtxEval getElement(int n) {
		return elements[n];
	}
	
	@AllArgsConstructor
	public static class PtAtPathElementCtxEval {
		public PathElementCtxEval element;
		public double startDist;
		public double endDist;
		public double paramIn01;
		// Pt2D pt;
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
			return new PtAtPathElementCtxEval(elements[0], 0.0, dist[0], 0.0);
		}
		double totalDist = dist[count-1];
		if (ptDist >= totalDist) {
			return new PtAtPathElementCtxEval(elements[count-1], (count>1)?dist[count-2]:0.0, totalDist, 1.0);
		}
		int found = Arrays.binarySearch(dist, ptDist);
		if (found >= 0) {
			// found exact pt
			double distBefore = (found>1)? dist[found-1] : 0.0;
			double distAfter = dist[found];
			return new PtAtPathElementCtxEval(elements[found], distBefore, distAfter, 0.0); // ??TOCHECK
		} else {
			int indexBefore = -(found+1);
			double distBefore = (indexBefore>1)? dist[indexBefore-1] : 0.0;
			double distAfter = dist[indexBefore];
			double param = (ptDist - distBefore) / (distAfter - distBefore);
			return new PtAtPathElementCtxEval(elements[indexBefore], distBefore, distAfter, param); // ??TOCHECK
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
		val totalDist = dist[elements.length-1];
		val step = totalDist / (N-1);
		res.add(startPt());
		double currDist = 0.0;
		int currElementIndex = 0;
		PathElementCtxEval currElement = elements[currElementIndex];
		double currElementStartDist = 0.0;
		double currElementEndDist = dist[0];
		for(int i = 1; i < N-1; i++) {
			currDist += step;
			if (currDist > dist[currElementIndex]) {
				currElementIndex++;
				if (currElementIndex == elements.length) {
					break; // should not occur
				}
				currElement = elements[currElementIndex];
				currElementStartDist = currElementEndDist;
				currElementEndDist += dist[currElementIndex];
			}
			// eval pt at currDist in currElement
			double param = (currDist - currElementStartDist) / (currElementEndDist - currElementStartDist);
			res.add(currElement.pointAtParam(param));
		}
		res.add(endPt());
		return res;
	}

}
