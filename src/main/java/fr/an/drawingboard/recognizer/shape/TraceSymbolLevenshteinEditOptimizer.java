package fr.an.drawingboard.recognizer.shape;

import java.util.ArrayList;
import java.util.List;

import fr.an.drawingboard.geom2d.ParamWeightedPt2D;
import fr.an.drawingboard.geom2d.Pt2D;
import fr.an.drawingboard.geom2d.utils.PolygonalDistUtils;
import fr.an.drawingboard.geom2d.utils.PolygonalDistUtils.TotalDistAndWeigthPts;
import fr.an.drawingboard.model.shapedef.obj.CompositePathElementsObj;
import fr.an.drawingboard.model.shapedef.obj.GesturePathesObj;
import fr.an.drawingboard.model.shapedef.obj.PathElementObj;
import fr.an.drawingboard.model.shapedef.obj.PathObj;
import fr.an.drawingboard.model.trace.TraceGesture;
import fr.an.drawingboard.model.trace.TracePath;
import fr.an.drawingboard.recognizer.trace.TraceDiscretisationPtsBuilder;
import fr.an.drawingboard.ui.impl.GcRendererHelper;
import fr.an.drawingboard.util.LsUtils;
import lombok.AllArgsConstructor;
import lombok.val;

public class TraceSymbolLevenshteinEditOptimizer {

	/**
	 * call symbol... for analogy to Levenshtein edit distance between words of symbols/letters
	 */
	@AllArgsConstructor
	public static class TracePathSymbol {
		final TracePath tracePath;
		final TotalDistAndWeigthPts distAndWPts;

		public static List<TracePathSymbol> traceGestureToSourceSymbols(TraceGesture traceGesture, TraceDiscretisationPtsBuilder traceDiscretisationPtsBuilder) {
			return LsUtils.map(traceGesture.pathes(), path -> {
				List<Pt2D> discretizedPts = traceDiscretisationPtsBuilder.discretizeToPts(path);
				val distAndPts = PolygonalDistUtils.ptsParamWeightedPts_polygonalDistance(discretizedPts);
				return new TracePathSymbol(path, distAndPts);
			});
		}

		public Pt2D startPt() {
			return distAndWPts.pts.get(0).pt;
		}
	}
	
	/**
	 * call symbol... for analogy to Levenshtein edit distance between words of symbols/letters
	 */
	@AllArgsConstructor
	public static class PathCtxEvalSymbol {
		final PathObj path;
		// final CompositePathElementsCtxEval elementsObj;
		final CompositePathElementsObj elementsObj;
		final List<ParamWeightedPt2D> pts;
		
		public static List<PathCtxEvalSymbol> gestureCtxToTargetSymbols(GesturePathesObj gestureCtxEval) {
			return LsUtils.map(gestureCtxEval.pathes, path -> {
				List<PathElementObj> elementObjs = path.toPathElementObjs();
				List<Double> splitParams = CompositePathElementsObj.splitParamProportionalToDists(elementObjs); 
				val elementsObj = new CompositePathElementsObj(splitParams, elementObjs);
				List<ParamWeightedPt2D> weigthedPts = elementsObj.discretizeWeigthedPts();
				return new PathCtxEvalSymbol(path, elementsObj, weigthedPts);
			});
		}
	}

	public static enum LevensteinEditOp {
		Start,
		Match, 
		Match2Sources1Target, Match1SourceMerge2Targets,
		InsertSource, DeleteSource, 
	}
	
	@AllArgsConstructor
	public static class TraceSymbolLevensteinDist {
		public TraceSymbolLevensteinDist prev;
		public double cost;
		public double editCost;
		public LevensteinEditOp editOp;
		public TracePathSymbol sourceSymbol;
		public PathCtxEvalSymbol targetSymbol;
		public CompositePathElementsObj mergeTarget;
	}

	private final TraceSymbolMatchCostFunction costFunction;
	
	/**
	 * for all i and j, distFirstSourceSymbols[i][j] will hold the Levenstein distance between
	 * the first i sourceSymbols and the first j targetSymbols
	 * 
	 * dimension: [0 .. sourceSymbols.length][0 .. targetSymbols.length]
	 */
	public List<TraceSymbolLevensteinDist[]> distFirstSourceSymbols = new ArrayList<>();
	
	private final PathCtxEvalSymbol[] targetSymbols;

	
	public static TraceSymbolLevenshteinEditOptimizer computeMatch(
			TraceSymbolMatchCostFunction costFunction, 
			List<TracePathSymbol> sourceSymbols,
			List<PathCtxEvalSymbol> targetSymbols 
			) {
		val matchOptimizer = new TraceSymbolLevenshteinEditOptimizer(costFunction, targetSymbols);
		for(TracePathSymbol sourceSymbol: sourceSymbols) {
			matchOptimizer.addSourceSymbol(sourceSymbol);
		}
		return matchOptimizer;
	}
	
	public TraceSymbolLevenshteinEditOptimizer(TraceSymbolMatchCostFunction costFunction, List<PathCtxEvalSymbol> targetPathSymbols) {
		this.costFunction = costFunction;
		val targetSymbolsCount = targetPathSymbols.size();
		this.targetSymbols = new PathCtxEvalSymbol[targetSymbolsCount + 1];
		this.targetSymbols[0] = null;
		for(int j = 0; j < targetSymbolsCount; j++) {
			this.targetSymbols[j+1] = targetPathSymbols.get(j);
		}
		
		// target prefixes can be reached from empty source prefix by inserting every symbol
		Pt2D startPt = this.targetSymbols[1].elementsObj.startPt();
		double costJ = 0.0;
		TraceSymbolLevensteinDist[] nextDist = new TraceSymbolLevensteinDist[targetSymbolsCount+1];
		
		nextDist[0] = new TraceSymbolLevensteinDist(null, 0, 0, LevensteinEditOp.Start, null, null, null);
		for (int j = 0; j < targetSymbolsCount; j++) {
			costJ += costFunction.insertionCost(startPt, this.targetSymbols[j+1]);
			nextDist[j+1] = new TraceSymbolLevensteinDist(null, costJ, costJ, LevensteinEditOp.Start, null, targetSymbols[j+1], null);
		}
		distFirstSourceSymbols.add(nextDist);
	}

	public void addSourceSymbol(TracePathSymbol src) {
		int sourceCount = distFirstSourceSymbols.size();
		TraceSymbolLevensteinDist[] prevDist = (sourceCount>=0)? distFirstSourceSymbols.get(sourceCount-1) : null;
		val targetSymbolsCount = targetSymbols.length-1;
		TraceSymbolLevensteinDist[] nextDist = new TraceSymbolLevensteinDist[targetSymbolsCount+1];
		
		{ // eval nextDist[0]: source prefixes can be transformed into empty string by dropping all symbols
		Pt2D startPt = targetSymbols[1].elementsObj.startPt();
		double delCost = costFunction.deletionCost(src, startPt);
		double cost = ((prevDist != null)? prevDist[0].cost : 0.0) + delCost;
		nextDist[0] = new TraceSymbolLevensteinDist(null, cost, delCost, LevensteinEditOp.DeleteSource, src, targetSymbols[0], null);
		}
			
		for(int j = 0; j < targetSymbolsCount; j++) {
			// nextDist[j+1] = min(prevDist[j] + matchCost(src,j),
			// 					   prevDist[j+1] + deletionCost(src),
			// 					   nextDist[j] + insertionCost(j))
			
			PathCtxEvalSymbol targetSymbol = targetSymbols[j+1];
			TraceSymbolLevensteinDist prev;
			LevensteinEditOp op;
			double nextCost, nextEditCost;
			
			double addMatchCost = costFunction.matchCost(src, targetSymbol);
			nextCost = prevDist[j].cost + addMatchCost;
			nextEditCost = addMatchCost;
			prev = prevDist[j];
			op = LevensteinEditOp.Match;
			CompositePathElementsObj editMergeTarget = null;
			
			TraceSymbolLevensteinDist prevJp1 = prevDist[j+1];

			TraceSymbolLevensteinDist prevJ = prevDist[j];
			if (prevJ != null && j > 0) {
				PathCtxEvalSymbol prevTargetSymbol = targetSymbols[j];
				double prevTargetDist = prevTargetSymbol.elementsObj.getDist();
				double targetDist = targetSymbol.elementsObj.getDist();
				double coefSplitParam = prevTargetDist / (prevTargetDist + targetDist);
				val trySplitParams = new double [] { 
						0.8*coefSplitParam, 0.9*coefSplitParam, 
						coefSplitParam, 
						1.1*coefSplitParam, 1.2*coefSplitParam 
						}; 
				for(val trySplitParam : trySplitParams) {
					val mergeTarget = new CompositePathElementsObj(prevTargetSymbol.elementsObj, trySplitParam, targetSymbol.elementsObj);
					double matchMergeTargetCost = costFunction.cost(src.distAndWPts, mergeTarget);
					double nextCostMerge = prevJ.cost + matchMergeTargetCost;
					if (nextCostMerge < nextCost) {
						prev = prevJ;
						op = LevensteinEditOp.Match1SourceMerge2Targets;
						nextCost = nextCostMerge;
						nextEditCost = matchMergeTargetCost;
						editMergeTarget = mergeTarget;
					}
				}
			}

			// TOADD Match2Sources1Target
			
			
			Pt2D targetEndPt = targetSymbol.pts.get(targetSymbol.pts.size()-1).pt;
			double addDeletionCost = costFunction.deletionCost(src, targetEndPt);
			double nextCostDeletion = prevJp1.cost + addDeletionCost;
			if (nextCostDeletion <= nextCost) {
				prev = prevJp1;
				op = LevensteinEditOp.DeleteSource;				
				nextCost = nextCostDeletion;
				nextEditCost = addDeletionCost;
			}

			if (j != 0) {
				Pt2D srcStartPt = src.startPt();
					// ?? endPt();
				double addInsertionCost = costFunction.insertionCost(srcStartPt, targetSymbol);
				double nextCostInsertion = nextDist[j].cost + addInsertionCost;
				if (nextCostInsertion < nextCost) {
					prev =  nextDist[j];
					op = LevensteinEditOp.InsertSource;
					nextCost = nextCostInsertion;
					nextEditCost = addInsertionCost;
				}
			}
			
			nextDist[j+1] = new TraceSymbolLevensteinDist(prev, nextCost, nextEditCost, op, src, targetSymbol, editMergeTarget);
		}
		distFirstSourceSymbols.add(nextDist);
	}

	public double getResultCost() {
		TraceSymbolLevensteinDist[] last = distFirstSourceSymbols.get(distFirstSourceSymbols.size()-1);
		return last[targetSymbols.length-1].cost;
	}

	public List<TraceSymbolLevensteinDist> getResultEditPath() {
		List<TraceSymbolLevensteinDist> res = new ArrayList<>();
		int currTargetIdx = distFirstSourceSymbols.size()-1;
		TraceSymbolLevensteinDist[] last = distFirstSourceSymbols.get(currTargetIdx);
		int currSourceIdx = targetSymbols.length-1;
		TraceSymbolLevensteinDist curr = last[currSourceIdx];
		for(; curr != null; ) {
			res.add(curr);
			curr = curr.prev;
		}
		// revert
		for(int start = 0, end = res.size()-1; start < end; start++,end--) {
			val startObj = res.get(start), endObj = res.get(end);
			res.set(start, endObj);
			res.set(end, startObj);
		}
		return res;
	}

	public void drawMatch(GcRendererHelper gcRenderer, TraceSymbolMatchCostFunction costFunc, List<TraceSymbolLevensteinDist> editPath) {
		for(val edit: editPath) {
			TraceSymbolLevensteinDist prev = edit.prev;
			TracePathSymbol src = edit.sourceSymbol;
			val target = edit.targetSymbol;
			switch(edit.editOp) {
			case Start:
				break;
			case Match:
				costFunc.drawCost(gcRenderer, src, target);
				break;
			case DeleteSource:
				Pt2D targetEndPt = target.pts.get(target.pts.size()-1).pt;
				costFunc.drawCost(gcRenderer, src, targetEndPt);
				break;
			case InsertSource:
				Pt2D srcStartPt = src.startPt();
				costFunc.drawCost(gcRenderer, srcStartPt, target);
				break;
			case Match2Sources1Target:
				// TODO
				break;
			case Match1SourceMerge2Targets:
				costFunction.drawCost(gcRenderer, src.distAndWPts.pts, edit.mergeTarget);
				break;
			}
		}
	}


}
