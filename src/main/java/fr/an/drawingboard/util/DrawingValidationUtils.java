package fr.an.drawingboard.util;

public class DrawingValidationUtils {

	public static void checkEquals(int expected, int actual) {
		if (expected != actual) {
			throw new IllegalArgumentException("wrong value " + actual + ", expected " + expected);
		}
	}

	public static void checkEquals(int expected, int actual, String msg) {
		if (expected != actual) {
			throw new IllegalArgumentException(
					"wrong value " + actual + ", expected " + expected + ((msg != null) ? " " + msg : ""));
		}
	}

	public RuntimeException throwDefault() {
		throw new IllegalStateException("should not occur: default case");
	}

	public static RuntimeException notImplYet() {
		throw new UnsupportedOperationException("NOT IMPLEMENTED YET");
	}

}
