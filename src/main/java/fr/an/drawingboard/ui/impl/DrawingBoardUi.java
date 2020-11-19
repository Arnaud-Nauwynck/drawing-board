package fr.an.drawingboard.ui.impl;

import java.util.List;

import fr.an.drawingboard.model.trace.TraceMultiStroke;
import fr.an.drawingboard.model.trace.TraceMultiStrokeList;
import fr.an.drawingboard.model.trace.TracePt;
import fr.an.drawingboard.model.trace.TraceStroke;
import fr.an.drawingboard.recognizer.trace.StopPointDetector;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.control.ToolBar;
import javafx.scene.control.Tooltip;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TouchEvent;
import javafx.scene.input.ZoomEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import lombok.val;

public class DrawingBoardUi {

	int paintCount = 0;
	Canvas canvas;
	String currDisplayText = "";
	
	Parent root;
	ToolBar toolbar;
	CheckBox checkBoxDebugStroke;
	CheckBox checkBoxDebugStrokeStopPoints;
	
	// model
	private TraceMultiStrokeList multiStrokeList = new TraceMultiStrokeList();

	StopPointDetector stopPointDetector = new StopPointDetector();

	boolean showSettingsStopPointDetector = false;
	
	// 
	private double currStrokeLineWidth = 2;
	private TraceStroke currTraceStroke;
	
	Color currStrokeColor = Color.BLACK;
	
	CanvasEventHandler drawCanvasEventHandler = new InnerDrawCanvasEventHandler();
	
	CanvasEventHandler currCanvasEventHandler = drawCanvasEventHandler;
	
	// --------------------------------------------------------------------------------------------

	public DrawingBoardUi() {
		toolbar = new ToolBar();
		createToolbar();

		this.canvas = new Canvas(500, 500);
		installCanvasHandler();

		VBox vbox = new VBox(toolbar, canvas);
		
		this.root = new Pane(vbox);

		paintCanvas();
	}

	// --------------------------------------------------------------------------------------------

	public Parent getUi() {
		return root;
	}

	private void createToolbar() {
		ObservableList<Node> toolbarItems = toolbar.getItems();
		
		if (showSettingsStopPointDetector) {
			{ // stationnaryThreshold
				Slider stationnaryThresholdMsSlider = new Slider();
				stationnaryThresholdMsSlider.setTooltip(new Tooltip("stop-point stationnary threshold time in millis"));
				stationnaryThresholdMsSlider.setMin(100);
				stationnaryThresholdMsSlider.setMax(600);
				stationnaryThresholdMsSlider.setValue(this.stopPointDetector.getStationaryThresholdMillis());
				stationnaryThresholdMsSlider.valueProperty().addListener((ctrl,oldValue,newValue) -> {
					this.stopPointDetector.setStationaryThresholdMillis(newValue.doubleValue());
				});
				toolbarItems.add(stationnaryThresholdMsSlider);
			}
			
			{ // moveThresholdPerTime
				Slider moveThresholdPerTimeSlider = new Slider();
				moveThresholdPerTimeSlider.setTooltip(new Tooltip("stop-point move threshold in pixels per stationnary time"));
				moveThresholdPerTimeSlider.setMin(1);
				moveThresholdPerTimeSlider.setMax(10);
				moveThresholdPerTimeSlider.setValue(this.stopPointDetector.getMoveThresholdPerTime());
				moveThresholdPerTimeSlider.valueProperty().addListener((ctrl,oldValue,newValue) -> {
					this.stopPointDetector.setMoveThresholdPerTime(newValue.doubleValue());
				});
				toolbarItems.add(moveThresholdPerTimeSlider);
			}
		}
		
		final TextField nameText = new TextField();
		nameText.setText("");
		toolbarItems.add(nameText);
		
		Button button = new Button("Del");
		toolbarItems.add(button);
		button.setOnAction(event -> {
			// remove last stroke
			TraceMultiStroke lastMultiStroke = multiStrokeList.getLast();
			if (lastMultiStroke != null) {
				lastMultiStroke.removeLastStroke();
				if (lastMultiStroke.isEmpty()) {
					multiStrokeList.remove(lastMultiStroke);
				}
			}
			paintCanvas();
		});
		
		checkBoxDebugStroke = new CheckBox("show pt");
		checkBoxDebugStroke .setSelected(false);
		toolbarItems.add(checkBoxDebugStroke);

		checkBoxDebugStrokeStopPoints = new CheckBox("show stop-pt");
		checkBoxDebugStrokeStopPoints.setSelected(true);
		toolbarItems.add(checkBoxDebugStrokeStopPoints);
	}


	private void installCanvasHandler() {
		canvas.setFocusTraversable(true);

		canvas.addEventHandler(MouseEvent.MOUSE_ENTERED, e -> currCanvasEventHandler.onMouseEntered(e));
		canvas.addEventHandler(MouseEvent.MOUSE_EXITED, e -> currCanvasEventHandler.onMouseExited(e));
		canvas.addEventHandler(MouseEvent.MOUSE_PRESSED, e -> currCanvasEventHandler.onMousePressed(e));
		canvas.addEventHandler(MouseEvent.MOUSE_RELEASED, e -> currCanvasEventHandler.onMouseReleased(e));;
		canvas.addEventHandler(MouseEvent.MOUSE_CLICKED, e -> currCanvasEventHandler.onMouseClicked(e));
		canvas.addEventHandler(MouseEvent.MOUSE_MOVED, e -> currCanvasEventHandler.onMouseMoved(e));
		canvas.addEventHandler(MouseEvent.MOUSE_DRAGGED, e -> currCanvasEventHandler.onMouseDragged(e));

		canvas.addEventHandler(KeyEvent.KEY_PRESSED, e -> currCanvasEventHandler.onKeyPressed(e));
		canvas.addEventHandler(KeyEvent.KEY_RELEASED, e -> currCanvasEventHandler.onKeyReleased(e));
		canvas.addEventHandler(KeyEvent.KEY_TYPED, e -> currCanvasEventHandler.onKeyTyped(e));

		canvas.addEventHandler(ZoomEvent.ZOOM, e -> currCanvasEventHandler.onZoom(e));
		canvas.addEventHandler(ZoomEvent.ZOOM_STARTED, e -> currCanvasEventHandler.onZoomStarted(e));
		canvas.addEventHandler(ZoomEvent.ZOOM_FINISHED, e -> currCanvasEventHandler.onZoomFinished(e));

		canvas.addEventHandler(TouchEvent.TOUCH_PRESSED, e -> currCanvasEventHandler.onTouchPressed(e));
		canvas.addEventHandler(TouchEvent.TOUCH_MOVED, e -> currCanvasEventHandler.onTouchMoved(e));
		canvas.addEventHandler(TouchEvent.TOUCH_RELEASED, e -> currCanvasEventHandler.onTouchReleased(e));
		canvas.addEventHandler(TouchEvent.TOUCH_STATIONARY, e -> currCanvasEventHandler.onTouchStationary(e));

	}

	protected class InnerDrawCanvasEventHandler extends CanvasEventHandler {
		@Override
		public void onMouseEntered(MouseEvent e) {
			System.out.println("mouse entered ");
			// canvas.requestFocus(); // otherwise KeyEvent not captured by canvas
		}
		@Override
		public void onMouseExited(MouseEvent e) {
			System.out.println("mouse exited ");
			// canvas.requestFocus();
			// foxus ??
		}
		@Override
		public void onMousePressed(MouseEvent e) {
			System.out.println("mouse pressed " + e.getClickCount());
			canvas.requestFocus(); // otherwise KeyEvent not captured by canvas
			// if
			
			currTraceStroke = new TraceStroke();
		}
		@Override
		public void onMouseReleased(MouseEvent e) {
			System.out.println("mouse released ");
			
			if (currTraceStroke != null) {
				// do not add small lines? .. convert to point
				TraceMultiStroke currMultiStroke = multiStrokeList.appendNewMultiStroke();
				currMultiStroke.strokes.add(currTraceStroke);
				currTraceStroke = null;
				paintCanvas();
			}
		}

		@Override
		public void onMouseClicked(MouseEvent e) {
			System.out.println("mouse clicked " + e.getClickCount());
				// canvas.requestFocus(); // otherwise KeyEvent not captured by canvas
		}
		
		@Override
		public void onMouseMoved(MouseEvent e) {
//	   	            	System.out.print(".");
		}

		@Override
		public void onMouseDragged(MouseEvent e) {
			// System.out.println("mouse dragged");
			if (currTraceStroke != null) {
				TracePt prevPt = currTraceStroke.lastPt();

				// append point to current stroke
				int pressure = 1; // not managed yet
				int x = (int) (e.getSceneX() - canvas.getLayoutX());
				int y = (int) (e.getSceneY() - canvas.getLayoutY());
				long time = System.currentTimeMillis();
				TracePt pt = currTraceStroke.appendTracePt(x, y, time, pressure);

				// detect if prev pt was a stop point
				if (prevPt != null) {
					stopPointDetector.onNewTracePt(currTraceStroke, pt);
				}
				
				paintCanvas();
			}
		}

	}
	
	// --------------------------------------------------------------------------------------------

	public void paintCanvas() {
		GraphicsContext gc = canvas.getGraphicsContext2D();

		paintCount++;

		gc.setFill(Color.WHITE);
		gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
		gc.setFill(Color.BLACK);
		
		// gc.setStroke(Paint.);
//		currDisplayText = "";
//		gc.fillText(currDisplayText, 10, 50);

		gc.setLineWidth(currStrokeLineWidth);
		gc.setStroke(currStrokeColor);
		for(val multiStroke : multiStrokeList.multiStrokes) {
//			gc.setStroke(multiStroke.color);
//			gc.setLineWidth(multiStroke.lineWidth);

			for (val stroke : multiStroke.strokes) {
				drawStroke(gc, stroke);
			}
		}

		gc.setLineWidth(currStrokeLineWidth);
		gc.setStroke(currStrokeColor);
		
		if (currTraceStroke != null) {
			drawStroke(gc, currTraceStroke);
		}
	}

	private void drawStroke(GraphicsContext gc, final fr.an.drawingboard.model.trace.TraceStroke stroke) {
		List<TracePt> tracePts = stroke.tracePts;
		int tracePtsLen = tracePts.size();
		if (tracePtsLen > 0) {
			val pt0 = tracePts.get(0);
			gc.beginPath();
			gc.moveTo(pt0.x, pt0.y);
			for(int i = 1; i < tracePtsLen; i++) {
				val pt = tracePts.get(i);
				gc.lineTo(pt.x, pt.y);
			}
			gc.stroke();
			
			// debug
			val debugStroke = checkBoxDebugStroke.isSelected();
			val dbgStrokeStopPoint = checkBoxDebugStrokeStopPoints.isSelected();
			val dbgStrokeEndPoint = false;
			if (dbgStrokeEndPoint) {
				gc.strokeOval(pt0.x, pt0.y, 5, 5);
			}
			TracePt prevDisplayIndexPt = null;
			for(int i = 1; i < tracePtsLen; i++) {
				val pt = tracePts.get(i);
				if (dbgStrokeStopPoint && pt.isStopPoint()) {
					gc.strokeOval(pt.x-5, pt.y-5, 10, 10);
				}
				if (debugStroke) {
					if (prevDisplayIndexPt == null || TracePt.dist(pt, prevDisplayIndexPt) > 20) {
						gc.strokeText("" + i, pt.x, pt.y + 10);
						prevDisplayIndexPt = pt;
					}
				}
			}
			if (dbgStrokeEndPoint) {
				val lastPt = tracePts.get(tracePtsLen - 1);
				gc.strokeOval(lastPt.x-3, lastPt.y-3, 6, 6);
			}
			
		}
	}
	
	
}
