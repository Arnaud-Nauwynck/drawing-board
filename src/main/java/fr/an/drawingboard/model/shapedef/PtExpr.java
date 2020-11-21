package fr.an.drawingboard.model.shapedef;

import fr.an.drawingboard.model.expr.Expr;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor @Data
public class PtExpr {

	public final Expr x;
	public final Expr y;

}
