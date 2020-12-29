package fr.an.drawingboard.model.shapedef.obj;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import fr.an.drawingboard.geom2d.ParamWeightedPt2D;
import fr.an.drawingboard.geom2d.Pt2D;
import fr.an.drawingboard.geom2d.utils.PolygonalDistUtils;
import fr.an.drawingboard.util.LsUtils;
import lombok.AllArgsConstructor;
import lombok.val;

/**
 * <PRE>
 * 
 *    s=0.0             s=splitParam[0]             s=splitParam[1]     s=1.0
 *    |      elt[0]        |               elt[1]        |                |
 *    + ----------------   +     ----------------------- +        ....    +
 *    startPt                                                            endPt
 *                                     /\
 *                                      |
 *                                      s=..
 *                                      pt=pointAtParam(s)
 * </PRE>
 *
 */
@AllArgsConstructor
public class CompositePathElementsObj {

	private final double[] splitParams;
	private final PathElementObj[] elements;

	public CompositePathElementsObj(PathElementObj elt0) {
		this.splitParams = new double [] { 1.0 };
		this.elements = new PathElementObj[] { elt0 };
	}

	public CompositePathElementsObj(PathElementObj elt0, double splitParam, PathElementObj elt1) {
		this.splitParams = new double [] { splitParam, 1.0 };
		this.elements = new PathElementObj[] { elt0, elt1 };
	}
	
	public static List<Double> splitParamProportionalToDists(List<PathElementObj> elements) {
		List<Double> pathEltDists = LsUtils.map(elements, e -> e.getDist());
		double sumDist = 0.0;
		for(val d : pathEltDists) {
			sumDist += d;
		}
		double normDist = 1.0 / sumDist;
		int count = pathEltDists.size() - 1;
		List<Double> splitParams = new ArrayList<>(count);
		double cumulDist = 0.0;
		for(int i = 0; i < count; i++) {
			cumulDist += pathEltDists.get(i);
			splitParams.add(cumulDist * normDist);
		}
		return splitParams;
	}
	
	public CompositePathElementsObj(List<Double> splitParams, List<PathElementObj> elements) {
		int elementCount = elements.size();
		int splitCount = splitParams.size();
		if (elementCount != splitCount+1) {
			throw new IllegalArgumentException();
		}
		if (elementCount == 1) {
			this.splitParams = new double [] { 1.0 };
			this.elements = new PathElementObj[] { elements.get(0) };
			return;
		} else if (elementCount == 2) {
			this.splitParams = new double [] { splitParams.get(0), 1.0 };
			this.elements = new PathElementObj[] { elements.get(0), elements.get(1) };
			return;
		} else {
			this.splitParams = new double[splitCount+1];
			double prevSplit = this.splitParams[0] = splitParams.get(0);
			for(int i = 1; i < splitCount; i++) {
				double nextSplit = this.splitParams[i] = splitParams.get(i);
				if (nextSplit < prevSplit) {
					throw new IllegalArgumentException();
				}
				prevSplit = nextSplit;
			}
			this.elements = elements.toArray(new PathElementObj[elementCount]);
			this.splitParams[splitCount] = 1.0;
		}
	}	

	public CompositePathElementsObj(CompositePathElementsObj comp1, double splitParam, CompositePathElementsObj comp2) {
		int comp1Count = comp1.elements.length;
		int comp2Count = comp2.elements.length;
		int count = comp1Count + comp2Count;
		this.splitParams = new double[count];
		this.elements = new PathElementObj[count];
		for(int i = 0; i < comp1Count; i++) {
			this.splitParams[i] = comp1.splitParams[i] * splitParam;
			this.elements[i] = comp1.elements[i];
		}
		for(int j = 0; j < comp2Count; j++) {
			this.splitParams[comp1Count+j] = splitParam + comp2.splitParams[j] * (1-splitParam);
			this.elements[comp1Count+j] = comp2.elements[j];
		}
	}
			
	public int getElementCount() {
		return elements.length;
	}

	public PathElementObj getElement(int n) {
		return elements[n];
	}
	
	@AllArgsConstructor
	public static class PtAtPathElementObj {
		public Pt2D pt;
		public PathElementObj element;
		public double elementParam;
	}

	public Pt2D startPt() {
		if (elements.length == 0) return null;
		return elements[0].getStartPt();
	}

	public Pt2D endPt() {
		if (elements.length == 0) return null;
		return elements[elements.length-1].getEndPt();
	}

	public PtAtPathElementObj pointAtParam(double param) {
		int count = elements.length;
		if (count == 0) {
			return null;
		}
		if (param <= 0.0) {
			PathElementObj e = elements[0];
			return new PtAtPathElementObj(e.getStartPt(), e, 0.0);
		} else if (param >= 1.0) {
			PathElementObj e = elements[count-1];
			return new PtAtPathElementObj(e.getEndPt(), e, 1.0);
		}
		int found = Arrays.binarySearch(splitParams, param);
		if (found >= 0) {
			// found exact pt
			PathElementObj e = elements[found];
			return new PtAtPathElementObj(e.getStartPt(), e, 0.0);
		} else {
			int indexBefore = -(found+1);
			double paramBefore = splitParams[indexBefore];
			double paramAfter = (indexBefore+1 < count)? splitParams[indexBefore+1] : 1.0;
			double elementParam = (param - paramBefore) / (paramAfter - paramBefore);
			PathElementObj e = elements[indexBefore];
			return new PtAtPathElementObj(e.pointAtParam(elementParam), e, elementParam);
		}
	}

	public List<ParamWeightedPt2D> discretizeWeigthedPts() {
		// tochange, naive implementation using 20 pts
		return discretizeWeigthedPts_fixedStep(20);
	}
	
	public List<ParamWeightedPt2D> discretizeWeigthedPts_fixedStep(int N) {
		List<Pt2D> pts = discretizePts_fixedStep(N);
		return PolygonalDistUtils.ptsParamWeightedPts_polygonalDistance(pts).pts;
	}

	public List<Pt2D> discretizePts_fixedStep(int N) {
		List<Pt2D> res = new ArrayList<>(N);
		val step = 1.0 / (N-1);
		res.add(startPt());
		double currParam = 0.0;
		int currElementIndex = 0;
		PathElementObj currElement = elements[currElementIndex];
		double currElementStartParam = 0.0;
		double currElementEndParam = splitParams[0];
		for(int i = 1; i < N-1; i++) {
			currParam += step;
			if (currParam > currElementEndParam) {
				currElementIndex++;
				if (currElementIndex == elements.length) {
					break; // should not occur
				}
				currElement = elements[currElementIndex];
				currElementStartParam = currElementEndParam;
				currElementEndParam = (currElementIndex < splitParams.length)? splitParams[currElementIndex] : 1.0;
			}
			// eval pt at currDist in currElement
			double elementParam = (currParam - currElementStartParam) / (currElementEndParam - currElementStartParam);
			res.add(currElement.pointAtParam(elementParam));
		}
		res.add(endPt());
		return res;
	}

	public interface PtAtParamIterator {
		public PtAtPathElementObj nextPtAtParam(double param);
	}
	public PtAtParamIterator pointAtParamIterator() {
		return new InnerPtAtParamIterator();
	}
	
	private class InnerPtAtParamIterator implements PtAtParamIterator {
		// CompositePathElementsCtxEval composite;
		PtAtPathElementObj entry = new PtAtPathElementObj(null, null, 0.0);
		// double currParam;
		double currParam = 0.0;
		int currElementIndex = 0;
		PathElementObj currElement; // = elements[currElementIndex];
		double currElementStartParam = 0.0;
		double currElementEndParam; // = splitParams[currElementIndex];
		
		
		public InnerPtAtParamIterator() {
			currElementIndex = 0;
			entry.element = currElement = elements[currElementIndex];
			currElementStartParam = 0.0;
			currElementEndParam = splitParams[currElementIndex];
		}

		@Override
		public PtAtPathElementObj nextPtAtParam(double param) {
			if (param < currParam) {
				throw new IllegalArgumentException();
			}
			currParam = param;
			while(currElementEndParam < currParam && currElementIndex < elements.length) {
				currElementIndex++;
				if (currElementIndex < elements.length) {
					currElement = elements[currElementIndex];
					currElementStartParam = currElementEndParam;
					currElementEndParam = splitParams[currElementIndex];
					entry.element = currElement;
				}
			}
			if (currElementIndex >= elements.length) {
				entry.pt = currElement.getEndPt();
				entry.elementParam = 1.0;
				return entry;
			}
			double elementParam = entry.elementParam = (currParam - currElementStartParam) / (currElementEndParam - currElementStartParam);
			entry.pt = currElement.pointAtParam(elementParam);
			return entry;
		}
	}

	public double getDist() {
		double sum = 0.0;
		for(val e : elements) {
			sum += e.getDist();
		}
		return sum;
	}
	
}
