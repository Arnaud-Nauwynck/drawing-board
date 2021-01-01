package fr.an.drawingboard.recognizer.shape;

import java.util.ArrayList;
import java.util.List;

import fr.an.drawingboard.geom2d.ParamWeightedPt2D;
import fr.an.drawingboard.geom2d.Pt2D;
import fr.an.drawingboard.geom2d.utils.PolygonalDistUtils;
import fr.an.drawingboard.geom2d.utils.PolygonalDistUtils.TotalDistAndWeigthPts;
import fr.an.drawingboard.math.expr.Expr;
import fr.an.drawingboard.math.expr.ExprBuilder;
import fr.an.drawingboard.model.shapedef.PtExpr;
import fr.an.drawingboard.model.shapedef.obj.CompositePathElementsObj;
import fr.an.drawingboard.model.shapedef.obj.GestureObj;
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
			return (!distAndWPts.pts.isEmpty())? distAndWPts.pts.get(0).pt : null;
		}

		public Pt2D endPt() {
			return (!distAndWPts.pts.isEmpty())? distAndWPts.pts.get(distAndWPts.pts.size()-1).pt : null;
		}
	}
	
	/**
	 * call symbol... for analogy to Levenshtein edit distance between words of symbols/letters
	 */
	@AllArgsConstructor
	public static class PathObjSymbol {
		final PathObj path;
		// final CompositePathElementsCtxEval elementsObj;
		final CompositePathElementsObj elementsObj;
		final List<ParamWeightedPt2D> pts;
		
		public static List<PathObjSymbol> gestureCtxToTargetSymbols(GestureObj gestureCtxEval) {
			return LsUtils.map(gestureCtxEval.pathes, path -> {
				List<PathElementObj> elementObjs = path.toPathElementObjs();
				List<Double> splitParams = CompositePathElementsObj.splitParamProportionalToDists(elementObjs); 
				val elementsObj = new CompositePathElementsObj(splitParams, elementObjs);
				List<ParamWeightedPt2D> weigthedPts = elementsObj.discretizeWeigthedPts();
				return new PathObjSymbol(path, elementsObj, weigthedPts);
			});
		}

		public Pt2D startPt() {
			return (!pts.isEmpty())? pts.get(0).pt : null;
		}

		public Pt2D endPt() {
			return (!pts.isEmpty())? pts.get(pts.size()-1).pt : null;
		}

		public PtExpr startPtExpr() {
			return elementsObj.startPtExpr();
		}

		public PtExpr endPtExpr() {
			return elementsObj.endPtExpr();
		}
	}

	public static enum LevensteinEditOp {
		Start, 

		// src:     [----+-
		//               |
		// target:       [-
		StartDeleteSource,

		// src:          [-
		//               |
		// target: [-----+-
		StartInsertTarget,

		// src:     [----------+-
		//          |          |
		// target:  [----+-----+-
		StartMergeTargets,

		// src:     [----+-----+-
		//          |          |
		// target:  [----------+-
		StartMergeSources,

		
		// src:    -+----------+-
		//          |          |
		// target: -+----------+-
		Match, 
		
		// src:     -+----+-----+-
		//           |          |
		// target:  -+----------+-
		MatchMergeSources, 

		// src:     -+----------+-
		//           |          |
		// target:  -+----+-----+-
		MatchMergeTargets,
		
		//             _ 
		//            / \
		// src:     -+   +-
		//           | /
		// target:  -+ -
		InsertSource,

		// src:     -+ -
		//           | \
		// target:  -+   +-
		//            \_/
		DeleteSource,

		// src:     -+----+-----]
		//           |          |
		// target:  -+----------]
		EndMergeSource,

		// src:     -+----------]
		//           |          |
		// target:  -+----+-----]
		EndMergeTarget,
		
		// src:     -+----]
		//           |
		// target:  -]
		EndDeleteSource,

		// src:     -]
		//           |
		// target:  -+-------]
		EndInsertTarget
	}
	
	@AllArgsConstructor
	public static class TraceSymbolLevensteinDist {
		public final int srcIndex, targetIndex;
		public TraceSymbolLevensteinDist prev;
		public double cost;
		public double editCost;
		public LevensteinEditOp editOp;
		
		public TracePathSymbol sourceSymbol;
		public Pt2D sourceEndPt() {
			return (sourceSymbol != null)? sourceSymbol.endPt() : null;
		}
		public TracePath mergeSource;
		
		public PathObjSymbol targetSymbol;
		public Pt2D targetEndPt() {
			return (targetSymbol != null)? targetSymbol.endPt() : null;
		}
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
	
	private final PathObjSymbol[] targetSymbols;

	
	public static TraceSymbolLevenshteinEditOptimizer computeMatch(
			TraceSymbolMatchCostFunction costFunction, 
			List<TracePathSymbol> sourceSymbols,
			List<PathObjSymbol> targetSymbols 
			) {
		val matchOptimizer = new TraceSymbolLevenshteinEditOptimizer(costFunction, targetSymbols);
		for(TracePathSymbol sourceSymbol: sourceSymbols) {
			matchOptimizer.addSourceSymbol(sourceSymbol);
		}
		return matchOptimizer;
	}
	
	public TraceSymbolLevenshteinEditOptimizer(TraceSymbolMatchCostFunction costFunction, List<PathObjSymbol> targetPathSymbols) {
		this.costFunction = costFunction;
		val targetSymbolsCount = targetPathSymbols.size();
		this.targetSymbols = new PathObjSymbol[targetSymbolsCount + 1];
		this.targetSymbols[0] = null;
		for(int j = 0; j < targetSymbolsCount; j++) {
			this.targetSymbols[j+1] = targetPathSymbols.get(j);
		}
		
		Pt2D startPt = this.targetSymbols[1].elementsObj.startPt();
		TraceSymbolLevensteinDist[] nextDist = new TraceSymbolLevensteinDist[targetSymbolsCount+1];
		nextDist[0] = new TraceSymbolLevensteinDist(0, 0, null, 0, 0, LevensteinEditOp.Start, null, null, null, null);
		// target prefixes can be reached from empty source prefix by inserting every symbol
		
		{
			val mergeTarget = targetSymbols[1].elementsObj;
			double cost0 = costFunction.cost(startPt, mergeTarget);
			nextDist[1] = new TraceSymbolLevensteinDist(0, 1, null, cost0, cost0, LevensteinEditOp.StartMergeTargets, null, null, targetSymbols[1], mergeTarget);
		}
		
		CompositePathElementsObj compositeTarget = this.targetSymbols[1].elementsObj;
		for (int j = 1; j < targetSymbolsCount; j++) {
			double trySplitParam = 0.5;
			val mergeTarget = new CompositePathElementsObj(compositeTarget, trySplitParam , targetSymbols[j+1].elementsObj);
			double cost = costFunction.cost(startPt, mergeTarget);
			nextDist[j+1] = new TraceSymbolLevensteinDist(0, j+1, null, cost, cost, LevensteinEditOp.StartMergeTargets, null, null, targetSymbols[j+1], mergeTarget);
		}
		distFirstSourceSymbols.add(nextDist);
	}

	public void addSourceSymbol(TracePathSymbol src) {
		val srcIndex = distFirstSourceSymbols.size();
		TraceSymbolLevensteinDist[] prevDist = (srcIndex>=0)? distFirstSourceSymbols.get(srcIndex-1) : null;
		val targetSymbolsCount = targetSymbols.length-1;
		TraceSymbolLevensteinDist[] nextDist = new TraceSymbolLevensteinDist[targetSymbolsCount+1];
		
		{ // eval nextDist[0]: source prefixes can be transformed into empty string by dropping all symbols
		Pt2D startPt = targetSymbols[1].elementsObj.startPt();
		double delCost = costFunction.deletionCost(src, startPt);
		double cost = ((prevDist != null)? prevDist[0].cost : 0.0) + delCost;
		nextDist[0] = new TraceSymbolLevensteinDist(srcIndex, 0, null, cost, delCost, LevensteinEditOp.DeleteSource, 
				src, src.tracePath, targetSymbols[1], null);
		}
			
		for(int j = 0; j < targetSymbolsCount; j++) {
			// nextDist[j+1] = min(prevDist[j] + matchCost(src,j),
			// 					   prevDist[j+1] + deletionCost(src),
			// 					   nextDist[j] + insertionCost(j))
			
			PathObjSymbol targetSymbol = targetSymbols[j+1];
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

			if (j > 0) {
				TraceSymbolLevensteinDist prevJm1 = prevDist[j-1];
				PathObjSymbol prevTargetSymbol = targetSymbols[j];
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
					double nextCostMerge = prevJm1.cost + matchMergeTargetCost;
					if (nextCostMerge < nextCost) {
						prev = prevJm1;
						op = LevensteinEditOp.MatchMergeTargets;
						nextCost = nextCostMerge;
						nextEditCost = matchMergeTargetCost;
						editMergeTarget = mergeTarget;
					}
				}
			}

			// TOADD Match2Sources1Target
			
			//  + ----- src ---- +
			//  |
			//  + -- target -- +
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
			
			nextDist[j+1] = new TraceSymbolLevensteinDist(srcIndex, j+1, prev, nextCost, nextEditCost, op, src, null, targetSymbol, editMergeTarget);
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
			case StartMergeTargets:
				if (target != null) {
					Pt2D targetStartPt = target.pts.get(0).pt;
					costFunction.drawCost(gcRenderer, targetStartPt, edit.mergeTarget);
				} else {
					// ??
				}
				break;
			case Match:
				costFunc.drawCost(gcRenderer, src, target);
				break;
			case DeleteSource:
				if (target != null) {
					Pt2D targetEndPt = target.pts.get(target.pts.size()-1).pt;
					costFunc.drawCost(gcRenderer, src, targetEndPt);
				} else {
					// TODO??
				}
				break;
			case InsertSource:
				Pt2D srcStartPt = src.startPt();
				costFunc.drawCost(gcRenderer, srcStartPt, target);
				break;
			case MatchMergeSources:
				// TODO
				break;
			case MatchMergeTargets:
				costFunction.drawCost(gcRenderer, src.distAndWPts.pts, edit.mergeTarget);
				break;
			}
		}
	}


	public Expr costExpr(TraceSymbolMatchCostFunction costFunc, List<TraceSymbolLevensteinDist> editPath) {
		List<Expr> res = new ArrayList<>();
		ExprBuilder b = ExprBuilder.INSTANCE;
		for(val edit: editPath) {
			TraceSymbolLevensteinDist prev = edit.prev;
			TracePathSymbol src = edit.sourceSymbol;
			val target = edit.targetSymbol;
			switch(edit.editOp) {
			case Start:
				break;
			case StartMergeTargets:
				if (target != null) {
					Pt2D targetStartPt = target.pts.get(0).pt;
					costFunction.costExprs(res, targetStartPt, edit.mergeTarget);
				} else {
					// ??
				}
				break;
			case Match:
				costFunction.costExprs(res, src, target);
				break;
			case DeleteSource:
				if (target != null) {
					PtExpr targetEndPt = target.endPtExpr();
					costFunction.costExprs(res, src, targetEndPt);
				} else {
					// TODO??
				}
				break;
			case InsertSource:
				Pt2D srcStartPt = src.startPt();
				costFunction.costExprs(res, srcStartPt, target);
				break;
			case MatchMergeSources:
				// TODO
				break;
			case MatchMergeTargets:
				costFunction.costExprs(res, src.distAndWPts, edit.mergeTarget);
				break;
			}
		}
		return b.sum(res);
	}

}
