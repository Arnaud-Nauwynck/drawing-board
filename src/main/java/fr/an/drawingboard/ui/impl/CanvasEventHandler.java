package fr.an.drawingboard.ui.impl;

import java.util.List;

import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TouchEvent;
import javafx.scene.input.TouchPoint;
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
		// System.out.println("zoom " + e);
		e.consume();
	}

	public void onZoomStarted(ZoomEvent e) {
		System.out.println("zoom " + e);
		e.consume();
	}

	public void onZoomFinished(ZoomEvent e) {
		System.out.println("zoom " + e);
		e.consume();
	}

	public void onTouchPressed(TouchEvent e) {
		List<TouchPoint> touchPoints = e.getTouchPoints();
		System.out.println("touch pressed " + e);
		e.consume(); // otherwise event translated to mouse clicked
	}
	
	public void onTouchMoved(TouchEvent e) {
		List<TouchPoint> touchPoints = e.getTouchPoints();
		// System.out.println("touch move " + e);
		e.consume(); // otherwise event translated to mouse move
	}
	
	public void onTouchReleased(TouchEvent e) {
		List<TouchPoint> touchPoints = e.getTouchPoints();
		System.out.println("touch released " + e);
		e.consume(); // otherwise event translated to mouse released
	}
	
	public void onTouchStationary(TouchEvent e) {
		// List<TouchPoint> touchPoints = e.getTouchPoints();
		// System.out.println("touch stationary " + e);
		e.consume(); // otherwise event translated to mouse released
	}

}
