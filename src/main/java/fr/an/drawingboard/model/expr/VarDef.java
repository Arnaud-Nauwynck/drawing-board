package fr.an.drawingboard.model.expr;

import fr.an.drawingboard.model.expr.Expr.VariableExpr;

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
	
	public VarDef(String name) {
		this.name = name;
		this.precomputeHashcode = precomputeHashCode();
	}
	

	private final int precomputeHashcode;
	
	@Override
	public int hashCode() {
		return precomputeHashcode;
	}

	private int precomputeHashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}
	
	@Override
	public boolean equals(Object obj) {
		return obj == this;
	}

	@Override
	public String toString() {
		return "${" + name + "}";
	}

}
