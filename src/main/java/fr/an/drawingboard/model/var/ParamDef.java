package fr.an.drawingboard.model.var;

import fr.an.drawingboard.model.expr.Expr.ParamDefExpr;

// TODO Deprecated .. replace by VarDef ? 
public class ParamDef {

	private static final Object CHECK_creator = new Object();
	public static void checkCreator(Object obj) {
		if (obj != CHECK_creator) {
			throw new IllegalStateException();
		}
	}

	public final ParametrizableEltDef owner;
	public final String name;
	
	/**
	 * adapter as expression, unique per ParamDef
	 */
	public final ParamDefExpr expr = new ParamDefExpr(this, CHECK_creator);

	public ParamDef(ParametrizableEltDef owner, String name) {
		this.owner = owner;
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
		result = prime * result + ((owner == null) ? 0 : owner.hashCode());
		return result;
	}
	
	@Override
	public boolean equals(Object obj) {
		return obj == this;
	}

	@Override
	public String toString() {
		return "${" 
				// + owner + "." 
				+ name + "}";
	}

}
