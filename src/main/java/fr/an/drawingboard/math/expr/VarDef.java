package fr.an.drawingboard.math.expr;

import fr.an.drawingboard.math.expr.Expr.VariableExpr;

public class VarDef {
	
	private static final Object CHECK_creator = new Object();
	public static void checkCreator(Object obj) {
		if (obj != CHECK_creator) {
			throw new IllegalStateException();
		}
	}

	public final String name;

	/**
	 * adapter as expression, unique per VarDef
	 */
	public final VariableExpr expr = new VariableExpr(this, CHECK_creator);

	private final int precomputeHashcode;
	

	public VarDef(String name) { // TODO add namespace for printing unambiguously
		this.name = name;
		this.precomputeHashcode = name.hashCode();
	}

	@Override
	public int hashCode() {
		return precomputeHashcode;
	}
	
	@Override
	public boolean equals(Object obj) {
		return obj == this;
	}

	@Override
	public String toString() {
		return name;
	}

}
