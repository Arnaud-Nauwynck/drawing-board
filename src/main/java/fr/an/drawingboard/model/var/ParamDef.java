package fr.an.drawingboard.model.var;

import fr.an.drawingboard.model.expr.Expr;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ParamDef {

	public final ParametrizableEltDef owner;
	public final String name;
	
	/**
	 * (singleton) adapter as expression
	 */
	public final ParamDefExpr expr = new ParamDefExpr(this);
	
	public static final class ParamDefExpr extends Expr {
		
		public final ParamDef paramDef;

		private ParamDefExpr(ParamDef paramDef) {
			this.paramDef = paramDef;
		}
	}

}
