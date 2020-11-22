package fr.an.drawingboard.util;

public class DrawingValidationUtils {

	public RuntimeException throwDefault() {
		throw new IllegalStateException("should not occur: default case");
	}

	public static RuntimeException notImplYet() {
		throw new UnsupportedOperationException("NOT IMPLEMENTED YET");
	}

}
