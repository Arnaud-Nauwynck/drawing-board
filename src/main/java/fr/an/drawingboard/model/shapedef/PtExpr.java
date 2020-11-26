package fr.an.drawingboard.model.shapedef;

import fr.an.drawingboard.model.expr.Expr;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor @Data
@Builder
public class PtExpr {

	public final Expr x;
	public final Expr y;

}
