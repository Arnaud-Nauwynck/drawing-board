package fr.an.drawingboard.model.shape;

import java.util.ArrayList;
import java.util.List;

import fr.an.drawingboard.math.numeric.NumericEvalCtx;
import fr.an.drawingboard.model.shapedef.GesturePathesDef;
import fr.an.drawingboard.model.shapedef.PathDef;
import lombok.AllArgsConstructor;

/**
 * instanceof GesturePathesDef for a given NumericEvalCtx
 */
@AllArgsConstructor
public class CtxGesturePathes {

	public final GesturePathesDef def;
	public final NumericEvalCtx evalCtx;
	
	public List<PathDef> pathes = new ArrayList<>();

}
