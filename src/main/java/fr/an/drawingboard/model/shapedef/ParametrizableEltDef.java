package fr.an.drawingboard.model.shapedef;

import java.util.LinkedHashMap;
import java.util.Map;

import fr.an.drawingboard.math.expr.Expr.VariableExpr;
import fr.an.drawingboard.math.expr.VarDef;
import lombok.Getter;

public abstract class ParametrizableEltDef {
	
	// ???
	@Getter
	private ParametrizableEltDef parent;
	
	@Getter
	private final Map<String,VarDef> params = new LinkedHashMap<>();

	// --------------------------------------------------------------------------------------------

	public ParametrizableEltDef() {
	}

	public ParametrizableEltDef(ParametrizableEltDef parent) {
		this.parent = parent;
	}

	// --------------------------------------------------------------------------------------------

	public VarDef addVarDef(String name) {
		VarDef res = new VarDef(name); // TODO add namespace for printing unambiguously ??
		params.put(name, res);
		return res;
	}
	
	public VarDef getParam(String name) {
		VarDef res = params.get(name);
		if (res == null && parent != null) {
			res = parent.getParam(name);
		}
		return res;
	}

	public VariableExpr getParamExpr(String name) {
		return getParam(name).expr;
	}

}
