package fr.an.drawingboard.ui.impl;

import java.util.List;

import fr.an.drawingboard.model.trace.TraceMultiStroke;
import fr.an.drawingboard.model.trace.TraceMultiStrokeList;
import fr.an.drawingboard.model.trace.TracePt;
import fr.an.drawingboard.model.trace.TraceStroke;
import fr.an.drawingboard.model.trace.TraceStrokePathElement;
import fr.an.drawingboard.model.trace.TraceStrokePathElement.DiscretePointsTraceStrokePathElement;
import fr.an.drawingboard.model.trace.TraceStrokePathElement.SegmentTraceStrokePathElement;
import fr.an.drawingboard.model.trace.TraceStrokePathElementBuilder;
import fr.an.drawingboard.recognizer.trace.StopPointDetector;
import fr.an.drawingboard.recognizer.trace.TraceStrokePathElementDetector;
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
	CheckBox checkBoxDebugVerboseStopPointDetector;
	
	// model
	private TraceMultiStrokeList multiStrokeList = new TraceMultiStrokeList();

	StopPointDetector stopPointDetector = new StopPointDetector();
	TraceStrokePathElementDetector pathElementDetector = new TraceStrokePathElementDetector();
	
	boolean showSettingsStopPointDetector = false;
	
	// 
	private double currStrokeLineWidth = 2;
	
	private TraceMultiStroke currMultiStroke;
	private TraceStroke currStroke;
	private TraceStrokePathElementBuilder currPathElementBuilder;
	
	
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
		checkBoxDebugStroke.setSelected(false);
		toolbarItems.add(checkBoxDebugStroke);

		checkBoxDebugStrokeStopPoints = new CheckBox("show stop-pt");
		checkBoxDebugStrokeStopPoints.setSelected(true);
		toolbarItems.add(checkBoxDebugStrokeStopPoints);
		
		checkBoxDebugVerboseStopPointDetector = new CheckBox("debug stop-pt");
		checkBoxDebugVerboseStopPointDetector.setSelected(stopPointDetector.isDebugPrint());
		checkBoxDebugVerboseStopPointDetector.setOnAction(e -> {
			stopPointDetector.setDebugPrint(checkBoxDebugVerboseStopPointDetector.isSelected());
		});
		toolbarItems.add(checkBoxDebugVerboseStopPointDetector);
		
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
			// System.out.println("mouse entered ");
			// canvas.requestFocus(); // otherwise KeyEvent not captured by canvas
		}
		@Override
		public void onMouseExited(MouseEvent e) {
			// System.out.println("mouse exited ");
			// canvas.requestFocus();
			// foxus ??
		}
		@Override
		public void onMousePressed(MouseEvent e) {
			System.out.println("mouse pressed " + e.getClickCount());
			canvas.requestFocus(); // otherwise KeyEvent not captured by canvas

			currOrAppendStroke();
			currPathElementBuilder = new TraceStrokePathElementBuilder();
		}
		@Override
		public void onMouseReleased(MouseEvent e) {
			System.out.println("mouse released ");
			
			if (currPathElementBuilder != null) {
				flushStopPointOrMouseReleased();
				currPathElementBuilder = null;
				currStroke = null;
				if (currMultiStroke != null) {
					currMultiStroke.updatePtCoefs();
					currMultiStroke = null;
				}
				
				// remove too small lines ?
				
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
			// System.out.print(".");
		}

		@Override
		public void onMouseDragged(MouseEvent e) {
			// System.out.println("mouse dragged");
			if (currPathElementBuilder != null) {
				TracePt prevPt = currPathElementBuilder.lastPt();

				// append point to current stroke
				int pressure = 1; // not managed yet
				int x = (int) (e.getSceneX() - canvas.getLayoutX());
				int y = (int) (e.getSceneY() - canvas.getLayoutY());
				long time = System.currentTimeMillis();
				TracePt pt = currPathElementBuilder.appendTracePt(x, y, time, pressure);

				// detect if prev pt was a stop point
				if (prevPt != null) {
					boolean stop = stopPointDetector.onNewTracePt(currPathElementBuilder, pt);
					if (stop) {
						flushStopPointOrMouseReleased();
					}
				}
				
				paintCanvas();
			}
		}
		private void flushStopPointOrMouseReleased() {
			// recognize segment, discrete points curve, or quad/cubic bezier...
			TraceStrokePathElement pathElement = pathElementDetector.recognizePathElement(currPathElementBuilder);
			
			if (pathElement != null) {
				// append a new pathElement(Builder) to current stroke
				if (currStroke == null) { 
					currStroke = currOrAppendStroke(); // should not occur?
				}
				currStroke.add(pathElement);
				val pt = currPathElementBuilder.lastPt();
				if (pt != null) {
					currPathElementBuilder = new TraceStrokePathElementBuilder(pt);
				}
			} else {
				// do not add small lines? .. convert to point (or circle / disk)
			}
		}

	}
	

	private TraceMultiStroke currOrAppendMultiStroke() { 
		 if (currMultiStroke == null) {
			 currMultiStroke = multiStrokeList.appendNewMultiStroke();
		 }
		 return currMultiStroke;
	}
	private TraceStroke currOrAppendStroke() { 
		 if (currStroke == null) {
			 TraceMultiStroke multiStroke = currOrAppendMultiStroke();
			 currStroke = multiStroke.appendNewStroke();
		 }
		 return currStroke;
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
		
		if (currPathElementBuilder != null) {
			drawDiscretePoints(gc, currPathElementBuilder.tracePts);
		}
	}

	private void drawStroke(GraphicsContext gc, TraceStroke stroke) {
		for(TraceStrokePathElement pathElement : stroke.pathElements) {
			switch(pathElement.getType()) {
			case Segment:
				drawSegment(gc, (SegmentTraceStrokePathElement) pathElement);
				break;
			case DiscretePoints:
				drawDiscretePoints(gc, (DiscretePointsTraceStrokePathElement) pathElement);
				break;
			case QuadBezier:
				break;
			case CubicBezier: 
				break;
			}
		}
	}
	
	private void drawSegment(GraphicsContext gc, SegmentTraceStrokePathElement segment) {
		gc.beginPath();
		gc.moveTo(segment.startPt.x, segment.startPt.y);
		gc.lineTo(segment.endPt.x, segment.endPt.y);
		gc.stroke();
	}

	private void drawDiscretePoints(GraphicsContext gc, DiscretePointsTraceStrokePathElement curve) {
		drawDiscretePoints(gc, curve.tracePts);
	}
	
	private void drawDiscretePoints(GraphicsContext gc, List<TracePt> tracePts) {
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
