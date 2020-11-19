package fr.an.drawingboard.ui.impl;

import fr.an.drawingboard.model.trace.TraceMultiStroke;
import fr.an.drawingboard.model.trace.TraceStroke;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TouchEvent;
import javafx.scene.input.ZoomEvent;

public class CanvasEventHandler {

	public void onMouseEntered(MouseEvent e) {
	}

	public void onMouseExited(MouseEvent e) {
	}

	public void onMousePressed(MouseEvent e) {
	}

	public void onMouseReleased(MouseEvent e) {
	}
	
	public void onMouseClicked(MouseEvent e) {
	}

	public void onMouseMoved(MouseEvent e) {
	}

	public void onMouseDragged(MouseEvent e) {
	}


	public void onKeyPressed(KeyEvent e) {
		System.out.println("key pressed " + e.getText());
	}
	
	public void onKeyReleased(KeyEvent e) {
		System.out.println("key released " + e.getText());
	}

	public void onKeyTyped(KeyEvent e) {
		System.out.println("key typed " + e.getText());
	}

	
	public void onZoom(ZoomEvent e) {
		System.out.println("zoom " + e);
	}

	public void onZoomStarted(ZoomEvent e) {
		System.out.println("zoom " + e);
	}

	public void onZoomFinished(ZoomEvent e) {
		System.out.println("zoom " + e);
	}

	public void onTouchPressed(TouchEvent e) {
		System.out.println("touch event " + e);
	}
	
	public void onTouchMoved(TouchEvent e) {
		System.out.println("touch event " + e);
	}
	
	public void onTouchReleased(TouchEvent e) {
		System.out.println("touch event " + e);
	}
	
	public void onTouchStationary(TouchEvent e) {
		System.out.println("touch event " + e);
	}

}
