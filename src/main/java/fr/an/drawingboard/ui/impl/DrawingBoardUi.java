package fr.an.drawingboard.ui.impl;

import java.util.ArrayList;
import java.util.List;

import fr.an.drawingboard.model.expr.Expr;
import fr.an.drawingboard.model.expr.helper.NumericExprEvalCtx;
import fr.an.drawingboard.model.shape.Shape;
import fr.an.drawingboard.model.shapedef.GesturePathesDef;
import fr.an.drawingboard.model.shapedef.ShapeDef;
import fr.an.drawingboard.model.shapedef.ShapeDefRegistry;
import fr.an.drawingboard.model.trace.TraceGesture;
import fr.an.drawingboard.model.trace.TracePath;
import fr.an.drawingboard.model.trace.TracePathElement;
import fr.an.drawingboard.model.trace.TracePathElement.DiscretePointsTracePathElement;
import fr.an.drawingboard.model.trace.TracePathElement.SegmentTracePathElement;
import fr.an.drawingboard.model.trace.TracePathElementBuilder;
import fr.an.drawingboard.model.trace.TracePt;
import fr.an.drawingboard.model.trace.TraceShape;
import fr.an.drawingboard.model.trace2shape.GesturePtToAbscissMatch;
import fr.an.drawingboard.recognizer.shape.MatchShapeToCostExprBuilder;
import fr.an.drawingboard.recognizer.trace.StopPointDetector;
import fr.an.drawingboard.recognizer.trace.TracePathElementDetector;
import fr.an.drawingboard.recognizer.trace.WeightedDiscretizationPathPtsBuilder;
import fr.an.drawingboard.stddefs.shapedef.ShapeDefRegistryBuilder;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Slider;
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
	CheckBox checkBoxDebugTrace;
	CheckBox checkBoxDebugTraceStopPoints;
	CheckBox checkBoxDebugVerboseStopPointDetector;
	CheckBox checkBoxDebugMatchShape;
	
	Button matchRectButton;
	Button matchCrossButton;

	Color currLineColor = Color.BLACK;
	
	CanvasEventHandler drawCanvasEventHandler = new InnerDrawCanvasEventHandler();
	
	CanvasEventHandler currCanvasEventHandler = drawCanvasEventHandler;
	

	// model
	private TraceShape traceShape = new TraceShape();

	private List<Shape> shapes = new ArrayList<>();
	
	StopPointDetector stopPointDetector = new StopPointDetector();
	TracePathElementDetector pathElementDetector = new TracePathElementDetector();
	MatchShapeToCostExprBuilder matchShapeToCostExprBuilder = new MatchShapeToCostExprBuilder();
	
	boolean showSettingsStopPointDetector = false;
	
	// 
	private double currLineWidth = 2;
	
	private TraceGesture currGesture;
	private TracePath currPath;
	private TracePathElementBuilder currPathElementBuilder;

	private ShapeDefRegistry shapeDefRegistry;

	private GesturePtToAbscissMatch currMatchIndexToAbsciss;
	private Shape currMatchShape;
	
	// --------------------------------------------------------------------------------------------

	public DrawingBoardUi() {
		shapeDefRegistry = new ShapeDefRegistry();
		new ShapeDefRegistryBuilder(shapeDefRegistry).addStdShapes();
		
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
		
		Button button = new Button("Del");
		toolbarItems.add(button);
		button.setOnAction(event -> {
			// remove last
			TraceGesture lastGesture = traceShape.getLast();
			if (lastGesture != null) {
				lastGesture.removeLastPath();
				if (lastGesture.isEmpty()) {
					traceShape.remove(lastGesture);
				}
			}
			if (currMatchShape != null) {
				currMatchShape = null;
			}
			currMatchIndexToAbsciss = null;
			
			paintCanvas();
		});
		
		checkBoxDebugTrace = new CheckBox("show pt");
		checkBoxDebugTrace.setSelected(false);
		toolbarItems.add(checkBoxDebugTrace);

		checkBoxDebugTraceStopPoints = new CheckBox("show stop-pt");
		checkBoxDebugTraceStopPoints.setSelected(true);
		toolbarItems.add(checkBoxDebugTraceStopPoints);
		
		checkBoxDebugVerboseStopPointDetector = new CheckBox("debug stop-pt");
		checkBoxDebugVerboseStopPointDetector.setSelected(stopPointDetector.isDebugPrint());
		checkBoxDebugVerboseStopPointDetector.setOnAction(e -> {
			stopPointDetector.setDebugPrint(checkBoxDebugVerboseStopPointDetector.isSelected());
		});
		toolbarItems.add(checkBoxDebugVerboseStopPointDetector);
		
		checkBoxDebugMatchShape = new CheckBox("debug match");
		toolbarItems.add(checkBoxDebugMatchShape);
		
//		final TextField nameText = new TextField();
//		nameText.setText("");
//		toolbarItems.add(nameText);

		matchRectButton = new Button("Rect");
		matchRectButton.setOnAction(e -> onClickMatchRect());
		toolbarItems.add(matchRectButton);

		matchCrossButton = new Button("Cross");
		matchCrossButton.setOnAction(e -> onClickMatchHCross());
		toolbarItems.add(matchCrossButton);

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

			currOrAppendPath();
			currPathElementBuilder = new TracePathElementBuilder();
		}
		@Override
		public void onMouseReleased(MouseEvent e) {
			System.out.println("mouse released ");
			
			if (currPathElementBuilder != null) {
				flushStopPointOrMouseReleased();
				currPathElementBuilder = null;
				currPath = null;
				if (currGesture != null) {
					WeightedDiscretizationPathPtsBuilder.updatePtCoefs(currGesture);
					currGesture = null;
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

				// append point to current pathElement
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
			TracePathElement pathElement = pathElementDetector.recognizePathElement(currPathElementBuilder);
			
			if (pathElement != null) {
				// append a new pathElement(Builder) to current path
				if (currPath == null) { 
					currPath = currOrAppendPath(); // should not occur?
				}
				currPath.add(pathElement);
				val pt = currPathElementBuilder.lastPt();
				if (pt != null) {
					currPathElementBuilder = new TracePathElementBuilder(pt);
				}
			} else {
				// do not add small lines? .. convert to point (or circle / disk)
			}
		}

	}
	

	private TraceGesture currOrAppendGesture() { 
		 if (currGesture == null) {
			 currGesture = traceShape.appendNewGesture();
		 }
		 return currGesture;
	}
	private TracePath currOrAppendPath() { 
		 if (currPath == null) {
			 TraceGesture gesture = currOrAppendGesture();
			 currPath = gesture.appendNewPath();
		 }
		 return currPath;
	}

	// Match recognizer
	// --------------------------------------------------------------------------------------------

	private void onClickMatchRect() {
		ShapeDef currMatchShapeDef = shapeDefRegistry.getShapeDef("rectangle");
		tryMatchShape(currMatchShapeDef);
	}

	private void onClickMatchHCross() {
		ShapeDef currMatchShapeDef = shapeDefRegistry.getShapeDef("hcross");
		tryMatchShape(currMatchShapeDef);
	}

	private void tryMatchShape(ShapeDef currMatchShapeDef) {
		GesturePathesDef gestureDef = currMatchShapeDef.gestures.get(0);
		TraceGesture matchGesture = traceShape.getLast();
		if (matchGesture == null) {
			return;
		}
//		if (matchGesture.recognizedShape != null) {
//			return; // ??
//		}

		NumericExprEvalCtx currInitialParamCtx = new NumericExprEvalCtx();
		
		gestureDef.initalParamEstimator.estimateInitialParamsFor(
				matchGesture, gestureDef, currInitialParamCtx);
		
		// check same number of path (stop points) in trace and in gestureDef
		if (gestureDef.pathes.size() == matchGesture.pathes.size()) {
			// int gesturePtsCount = gestureDef.

			// TODO currMatchIndexToAbsciss = new DiscreteTimesToAbsciss();
			
//			Expr costExpr = matchShapeToCostExprBuilder.costMatchGestureWithAbsciss(
//					matchGesture,
//					gestureDef, 
//					currMatchIndexToAbsciss);
//
			// TODO ..
			
					
		} else {
			//?? return;
		}
		// TODO .. choice + optim steps
		
		
		currMatchShape = new Shape(currMatchShapeDef, currInitialParamCtx.paramValues);
		matchGesture.recognizedShape = currMatchShape;
		
		paintCanvas();
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

		gc.setLineWidth(currLineWidth);
		gc.setStroke(currLineColor);
		for(val gesture : traceShape.gestures) {
//			gc.setStroke(multiStroke.color);
//			gc.setLineWidth(gesture.lineWidth);

			for (val path : gesture.pathes) {
				drawPath(gc, path);
			}
		}

		gc.setLineWidth(currLineWidth);
		gc.setStroke(currLineColor);
		
		if (currPathElementBuilder != null) {
			drawDiscretePoints(gc, currPathElementBuilder.tracePts);
		}
		
		for(val shape : shapes) {
			shape.draw(gc);
		}
		
		if (currMatchShape != null) {
			currMatchShape.draw(gc);
		}
		
	}

	private void drawPath(GraphicsContext gc, TracePath path) {
		for(TracePathElement pathElement : path.pathElements) {
			switch(pathElement.getType()) {
			case Segment:
				drawSegment(gc, (SegmentTracePathElement) pathElement);
				break;
			case DiscretePoints:
				drawDiscretePoints(gc, (DiscretePointsTracePathElement) pathElement);
				break;
			case QuadBezier:
				break;
			case CubicBezier: 
				break;
			}
		}
	}
	
	private void drawSegment(GraphicsContext gc, SegmentTracePathElement segment) {
		gc.beginPath();
		gc.moveTo(segment.startPt.x, segment.startPt.y);
		gc.lineTo(segment.endPt.x, segment.endPt.y);
		gc.stroke();
	}

	private void drawDiscretePoints(GraphicsContext gc, DiscretePointsTracePathElement curve) {
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
			val debugTrace = checkBoxDebugTrace.isSelected();
			val dbgTraceStopPoint = checkBoxDebugTraceStopPoints.isSelected();
			val dbgTraceEndPoint = false;
			if (dbgTraceEndPoint) {
				gc.strokeOval(pt0.x, pt0.y, 5, 5);
			}
			TracePt prevDisplayIndexPt = null;
			for(int i = 1; i < tracePtsLen; i++) {
				val pt = tracePts.get(i);
				if (dbgTraceStopPoint && pt.isStopPoint()) {
					gc.strokeOval(pt.x-5, pt.y-5, 10, 10);
				}
				if (debugTrace) {
					if (prevDisplayIndexPt == null || TracePt.dist(pt, prevDisplayIndexPt) > 20) {
						gc.strokeText("" + i, pt.x, pt.y + 10);
						prevDisplayIndexPt = pt;
					}
				}
			}
			if (dbgTraceEndPoint) {
				val lastPt = tracePts.get(tracePtsLen - 1);
				gc.strokeOval(lastPt.x-3, lastPt.y-3, 6, 6);
			}
			
		}
	}

}
