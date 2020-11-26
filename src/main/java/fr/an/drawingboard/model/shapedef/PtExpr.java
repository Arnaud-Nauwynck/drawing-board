package fr.an.drawingboard.model.shapedef;

import fr.an.drawingboard.model.expr.Expr;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@AllArgsConstructor @Data
@Builder
public class PtExpr {

	public final Expr x;
	public final Expr y;

	public PtExprBuilder toBuilder() {
		return new PtExprBuilder().x(this.x).y(this.y);
	}

}
