package fr.an.drawingboard.recognizer.shape;

import java.util.ArrayList;
import java.util.List;

import fr.an.drawingboard.geom2d.Pt2D;
import fr.an.drawingboard.model.shapedef.ctxeval.PathElementCtxEval;
import fr.an.drawingboard.model.shapedef.ctxeval.PathElementCtxEvalFragment;
import fr.an.drawingboard.model.trace.TracePt;
import fr.an.drawingboard.recognizer.shape.PtToPathElementLoweringDistUtils.PtToPathElementLoweringDistResult;
import fr.an.drawingboard.recognizer.trace.WeightedDiscretizationPathPtsBuilder.WeightedTracePt;
import lombok.AllArgsConstructor;
import lombok.val;

/**
 * 
 * Cost to optimize:
 * <PRE>
 * weighted pts: 0..N-1
 * pathElementDefFragments: 0..D-1
 * 
 * E =       sum     (  weithPt_i *  dist( Pt_i, proj_i )^2  )
 *      i=0..N-1
 *      proj_i= min dist projection of pt_i on pathElementFragment[..]
 *      proj_0 <= proj_1 <= .. proj_{N-1}
 *     
 *     proj_A <= proj_B ... means fragmentIndexA < fragmentIndexB
 *     						 OR (fragmentIndexA = fragmentIndexB and projToPathParamA <= projToPathParamB)
 * </PRE>
 * 
 * 
 * Dynamic programming algorithm:
 * <PRE>
 * E_I(P) = partial minimum for pts[0].. pts[I], with constraint proj_I<=P
 * 
 * E_I(P) =  min  sum    (  weithPt_i *  dist( Pt_i, proj_i )^2  )
 *                i=0..I
 *      proj_0 <= proj_1 <= .. proj_I
 *                                    <= P
 *      
 * E_{N-1}(D-1) = min E   ... the optimized solution to compute
 * 
 * for I>0
 * E_{I+1}(P) = min  sum ( weithPt_i *  dist( Pt_i, proj_i )^2  )
 *                   i=0..I+1
 *         proj_0 <= proj_1 <= .. proj_I   <= proj_{I+1} 
 *                                                       <= P
 *                                           
 *            = min              ( min (sum..          )       + weithPt_{I+1} *  dist( Pt_{I+1}, proj_{I+1} )^2 )
 *              proj_{I+1}<=P     proj_0<=..proj_I<=proj_{I+1}
 *
 *            = min              ( E_I(x)             + weithPt_{I+1} *  dist( Pt_{I+1}, x )^2 )
 *              x<=P
 * </PRE>
 */
public class PtToSlotDefDynamicProgOptimizer {

	private final List<WeightedTracePt> pts = new ArrayList<>();
	
	private final List<PathElementCtxEvalFragment> defFragments = new ArrayList<>();
	
	/*pp*/ final List<ProjToPathUpToIndex> currProjToPathUpToIndexes = new ArrayList<>();
	
	@AllArgsConstructor
	public static class ProjToPathUpToIndex {
		public final WeightedTracePt pt;
		public final int maxDefFragmentIndex;
		public final PathElementCtxEvalFragment pathElementFragment;
		public final double projToPathParam;
		public final ProjToPathUpToIndex prevPtToSlot;
		public final double minCostValue;
	}

	// ------------------------------------------------------------------------
	
	public PtToSlotDefDynamicProgOptimizer(List<PathElementCtxEval> pathElements) {
		int fragIndex = 0;
		for(val pathElement : pathElements) {
			val frag = new PathElementCtxEvalFragment(pathElement, 0.0, 1.0);
			this.defFragments.add(frag);
			
			currProjToPathUpToIndexes.add(new ProjToPathUpToIndex(null, fragIndex, frag, 0.0, null, 0.0));
		
			fragIndex++;
		}
	}
	
	public void addPt(WeightedTracePt weigthedPt) {
		pts.add(weigthedPt);
		
		final TracePt tracePt = weigthedPt.pt;
		final Pt2D pt = tracePt.xy();
		final double weight = weigthedPt.ptWeight;
		final int fragCount = defFragments.size();
		
		PtToPathElementLoweringDistResult resDist = new PtToPathElementLoweringDistResult();
		if (pts.size() == 1) {
			// first pt
			for(int p = 0; p < fragCount; p++) {
				PathElementCtxEvalFragment projToFrag = defFragments.get(p);
				PathElementCtxEval projTo = projToFrag.pathElement; // TODO fragment dist NOT IMPLEMENTED YET... 

				PtToPathElementLoweringDistUtils.evalMinDistIfLowerThan(resDist, pt, projTo, Double.MAX_VALUE);
				double distPtProj = resDist.resultDist;
				double pathParam = resDist.resultProjPathParam;
				double minCostValue = weight * distPtProj * distPtProj;
				
				currProjToPathUpToIndexes.set(p, new ProjToPathUpToIndex(null, p, projToFrag, pathParam, null, minCostValue));
			}
			
			return;
		} else {
			for(int p = 0; p < fragCount; p++) {
				// eval new bestCost(P) .. i.e. with constraint proj(pt)<=p, and point added
				double minCostValue = Double.MAX_VALUE;
				int bestProjIndex = 0;
				PathElementCtxEvalFragment bestProjToFrag = null;
				double bestProjToPathParam = 0.0;
				for(int projIndex = 0; projIndex < p; projIndex++) {
					PathElementCtxEvalFragment projToFrag = defFragments.get(projIndex);
					PathElementCtxEval projTo = projToFrag.pathElement; // TODO fragment dist NOT IMPLEMENTED YET... 
					
					ProjToPathUpToIndex prevMin_ToProjIndex = currProjToPathUpToIndexes.get(projIndex);
					double prevCost = prevMin_ToProjIndex.minCostValue;
					// ( E_I(proj_{I+1})  +  weithPt_{I+1} *  dist( Pt_{I+1}, proj_{I+1} )^2 )
	
					// condition for: minCostValue ?>? prevCost + weight * distPtProj * distPtProj
					// <=> distPtProj*distPtProj < (minCostValue - prevCost) / weight
					double squareLowerDist = (minCostValue - prevCost) / weight;
					if (squareLowerDist > 0) {
						double ifLowerThanDist = Math.sqrt(squareLowerDist);
						
						boolean foundLower = PtToPathElementLoweringDistUtils.evalMinDistIfLowerThan(resDist, pt, projTo, ifLowerThanDist);
						if (foundLower) {
							double distPtProj = resDist.resultDist;
							double cost = prevCost + weight * distPtProj * distPtProj;
							if (cost < minCostValue) {
								minCostValue = cost;
								bestProjIndex = projIndex;
								bestProjToPathParam = resDist.resultProjPathParam;
							}
						}
					}
				} // for projIndex
				
				// TODO
				ProjToPathUpToIndex prevPtToSlot = null; // TODO
				
				val projPt = new ProjToPathUpToIndex(weigthedPt, p,
						bestProjToFrag, bestProjToPathParam, prevPtToSlot, minCostValue);
				currProjToPathUpToIndexes.set(p, projPt);
				
			} // for p
		}
	}
}
