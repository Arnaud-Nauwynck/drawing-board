package fr.an.drawingboard.recognizer.shape;

import java.util.ArrayList;
import java.util.List;

import fr.an.drawingboard.geom2d.ParamWeightedPt2D;
import fr.an.drawingboard.geom2d.Pt2D;
import fr.an.drawingboard.geom2d.utils.PolygonalDistUtils;
import fr.an.drawingboard.model.shapedef.ctxeval.CompositePathElementsCtxEval;
import fr.an.drawingboard.model.shapedef.ctxeval.CompositePathElementsCtxEval.PtAtPathElementCtxEval;
import fr.an.drawingboard.model.shapedef.ctxeval.GesturePathesCtxEval;
import fr.an.drawingboard.model.shapedef.ctxeval.PathCtxEval;
import fr.an.drawingboard.model.trace.TraceGesture;
import fr.an.drawingboard.model.trace.TracePath;
import fr.an.drawingboard.recognizer.trace.TraceDiscretisationPtsBuilder;
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
		final List<ParamWeightedPt2D> pts;
				
		public static List<TracePathSymbol> traceGestureToSourceSymbols(TraceGesture traceGesture, TraceDiscretisationPtsBuilder traceDiscretisationPtsBuilder) {
			return LsUtils.map(traceGesture.pathes(), path -> {
				List<Pt2D> discretizedPts = traceDiscretisationPtsBuilder.discretizeToPts(path);
				val pts = PolygonalDistUtils.ptsParamWeightedPts_polygonalDistance(discretizedPts);
				return new TracePathSymbol(path, pts);
			});
		}

		public Pt2D startPt() {
			return pts.get(0).pt;
		}
	}
	
	/**
	 * call symbol... for analogy to Levenshtein edit distance between words of symbols/letters
	 */
	@AllArgsConstructor
	public static class PathCtxEvalSymbol {
		final PathCtxEval path;
		final CompositePathElementsCtxEval elementsCtxEval;				
		final List<ParamWeightedPt2D> pts;
		
		public static List<PathCtxEvalSymbol> gestureCtxToTargetSymbols(GesturePathesCtxEval gestureCtxEval) {
			return LsUtils.map(gestureCtxEval.pathes, path -> {
				val elements = new CompositePathElementsCtxEval(path.toPathElementCtxEvals());
				List<ParamWeightedPt2D> weigthedPts = elements.discretizeWeigthedPts();
				return new PathCtxEvalSymbol(path, elements, weigthedPts);
			});
		}
	}

	public static enum LevensteinEditOp {
		SplitInsertSource, Match, MergeDeleteSource
		// , DeleteSource
	}
	
	@AllArgsConstructor
	public static class TraceSymbolLevensteinDist {
		public TraceSymbolLevensteinDist prev;
		public double cost;
		public double editCost;
		public LevensteinEditOp editOp;
		public TracePathSymbol sourceSymbol;
		public PathCtxEvalSymbol targetSymbol;
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
		PtAtPathElementCtxEval ptAtStart = this.targetSymbols[1].elementsCtxEval.pointAtDist(0.0);
		Pt2D startPt = ptAtStart.element.getStartPt();
		double costJ = 0.0;
		TraceSymbolLevensteinDist[] nextDist = new TraceSymbolLevensteinDist[targetSymbolsCount+1];
		
		nextDist[0] = new TraceSymbolLevensteinDist(null, 0, 0, LevensteinEditOp.SplitInsertSource, null, null);
		for (int j = 0; j < targetSymbolsCount; j++) {
			costJ += costFunction.insertionCost(startPt, this.targetSymbols[j+1]);
			nextDist[j+1] = new TraceSymbolLevensteinDist(null, costJ, costJ, LevensteinEditOp.SplitInsertSource, null, targetSymbols[j+1]);
		}
		distFirstSourceSymbols.add(nextDist);
	}

	public void addSourceSymbol(TracePathSymbol src) {
		int sourceCount = distFirstSourceSymbols.size();
		TraceSymbolLevensteinDist[] prevDist = (sourceCount>=0)? distFirstSourceSymbols.get(sourceCount-1) : null;
		val targetSymbolsCount = targetSymbols.length-1;
		TraceSymbolLevensteinDist[] nextDist = new TraceSymbolLevensteinDist[targetSymbolsCount+1];
		
		{ // eval nextDist[0]: source prefixes can be transformed into empty string by dropping all symbols
		PtAtPathElementCtxEval ptAtStart = targetSymbols[1].elementsCtxEval.pointAtDist(0.0);
		Pt2D startPt = ptAtStart.element.getStartPt();
		double delCost = costFunction.deletionCost(src, startPt);
		double cost = ((prevDist != null)? prevDist[0].cost : 0.0) + delCost;
		nextDist[0] = new TraceSymbolLevensteinDist(null, cost, delCost, LevensteinEditOp.MergeDeleteSource, src, targetSymbols[0]);
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
		
			Pt2D targetStartPt = targetSymbol.pts.get(0).pt;
			double addDeletionCost = costFunction.deletionCost(src, targetStartPt);
			double nextCostDeletion = prevDist[j+1].cost + addDeletionCost;
			if (nextCostDeletion <= nextCost) {
				prev = prevDist[j+1];
				op = LevensteinEditOp.MergeDeleteSource;				
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
					op = LevensteinEditOp.SplitInsertSource;
					nextCost = nextCostInsertion;
					nextEditCost = addInsertionCost;
				}
			}
			
			nextDist[j+1] = new TraceSymbolLevensteinDist(prev, nextCost, nextEditCost, op, src, targetSymbol);
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
		for(; currSourceIdx != 0 && currTargetIdx != 0; ) {
			res.add(curr);
			switch(curr.editOp) {
			case Match:
				currSourceIdx--;	
				currTargetIdx--;
				break;
			case SplitInsertSource:
				currTargetIdx--;
				break;
			case MergeDeleteSource:
				currSourceIdx--;
				break;
			}
			curr = distFirstSourceSymbols.get(currTargetIdx)[currSourceIdx];
			
		}
		// revert
		for(int start = 0, end = res.size()-1; start < end; start++,end--) {
			val startObj = res.get(start), endObj = res.get(end);
			res.set(start, endObj);
			res.set(end, startObj);
		}
		return res;
	}

}
