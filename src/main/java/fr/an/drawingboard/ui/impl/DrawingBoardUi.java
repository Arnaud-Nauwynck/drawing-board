package fr.an.drawingboard.ui.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import fr.an.drawingboard.geom2d.BoundingRect2D;
import fr.an.drawingboard.geom2d.BoundingRect2D.BoundingRect2DBuilder;
import fr.an.drawingboard.geom2d.CubicBezier2D;
import fr.an.drawingboard.geom2d.Pt2D;
import fr.an.drawingboard.geom2d.bezier.BezierEnclosingRect2DUtil;
import fr.an.drawingboard.math.numeric.NumericEvalCtx;
import fr.an.drawingboard.model.shape.ShapeCtxEval;
import fr.an.drawingboard.model.shapedef.GesturePathesDef;
import fr.an.drawingboard.model.shapedef.PtExpr;
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
import fr.an.drawingboard.recognizer.shape.GesturePtToAbscissMatch;
import fr.an.drawingboard.recognizer.shape.MatchShapeToCostExprBuilder;
import fr.an.drawingboard.recognizer.shape.TraceGestureDefMatching;
import fr.an.drawingboard.recognizer.shape.TraceGestureDefMatchingBuilder;
import fr.an.drawingboard.recognizer.trace.AlmostAlignedPtsSimplifier;
import fr.an.drawingboard.recognizer.trace.StopPointDetector;
import fr.an.drawingboard.recognizer.trace.TooNarrowPtsSimplifier;
import fr.an.drawingboard.recognizer.trace.TracePathElementDetector;
import fr.an.drawingboard.recognizer.trace.WeightedDiscretizationPathPtsBuilder;
import fr.an.drawingboard.stddefs.shapedef.ShapeDefRegistryBuilder;
import javafx.beans.property.BooleanProperty;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.RadioMenuItem;
import javafx.scene.control.Slider;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TouchEvent;
import javafx.scene.input.ZoomEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import lombok.val;

public class DrawingBoardUi {

	int paintCount = 0;
	Canvas canvas;
	String currDisplayText = "";
	
	Parent root;
	BooleanProperty debugTrace;
	BooleanProperty debugTraceStopPoints;
	BooleanProperty debugVerboseStopPointDetector;
	BooleanProperty debugMatchShape;
	BooleanProperty debugMatchPtToAbsciss;
	
	Color currLineColor = Color.BLACK;
	
	CanvasEventHandler drawCanvasEventHandler = new InnerDrawCanvasEventHandler();
	
	CanvasEventHandler currCanvasEventHandler = drawCanvasEventHandler;
	

	// model
	private TraceShape traceShape = new TraceShape();

	private List<ShapeCtxEval> shapes = new ArrayList<>();
	
	TooNarrowPtsSimplifier tooNarrowPtsSimplifier = new TooNarrowPtsSimplifier();
	AlmostAlignedPtsSimplifier almostAlignedPtsSimplifier = new AlmostAlignedPtsSimplifier();
	StopPointDetector stopPointDetector = new StopPointDetector();
	TracePathElementDetector pathElementDetector = new TracePathElementDetector();
	MatchShapeToCostExprBuilder matchShapeToCostExprBuilder = new MatchShapeToCostExprBuilder();
	int discretizationPrecision = 30;
	Function<NumericEvalCtx,NumericEvalCtx> paramCtxInitTransformer;
	
	boolean showSettingsStopPointDetector = false;
	BooleanProperty showSettingsAlmostAlignedPtsSimplifier;
	
	// 
	private double currLineWidth = 2;
	
	private TraceGesture currGesture;
	private TracePath currPath;
	private TracePathElementBuilder currPathElementBuilder;

	private ShapeDefRegistry shapeDefRegistry;

	private ShapeCtxEval currMatchShape;
	private NumericEvalCtx currMatchParamCtx;
	private TraceGestureDefMatching currTraceGestureDefMatching;
	
	boolean debugBezier = false;
	private Pt2D debugCubicBezierEditPt;
	private CubicBezier2D debugCurrCubicBezier = new CubicBezier2D(new Pt2D(0, 0), new Pt2D(100, 100), new Pt2D(100, 200), new Pt2D(0, 300));
	
	// --------------------------------------------------------------------------------------------

	public DrawingBoardUi() {
		shapeDefRegistry = new ShapeDefRegistry();
		new ShapeDefRegistryBuilder(shapeDefRegistry).addStdShapes();
		
		this.canvas = new Canvas(1000, 500);
		Node toolbar = createToolbar();

		installCanvasHandler();

		VBox vbox = new VBox(toolbar, canvas);
		
		this.root = new Pane(vbox);

		paintCanvas();
	}

	// --------------------------------------------------------------------------------------------

	public Parent getUi() {
		return root;
	}

	private Node createToolbar() {
		HBox toolbar = new HBox();

		ObservableList<Node> toolbarItems = toolbar.getChildren();
		
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
			currTraceGestureDefMatching = null;
			
			paintCanvas();
		});
		
		{
			MenuButton debugMenu = new MenuButton("Debug");
			toolbarItems.add(debugMenu);
			List<MenuItem> debugItems = debugMenu.getItems();
			
			debugTrace = addCheckMenuItem(debugItems, "show pt", () -> paintCanvas()).selectedProperty();
			
			debugTraceStopPoints = addCheckMenuItem(debugItems, "show stop-pt", () -> paintCanvas()).selectedProperty();
			debugTraceStopPoints.set(true);

			debugVerboseStopPointDetector = addCheckMenuItem(debugItems, "debug stop-pt", () -> {
				stopPointDetector.setDebugPrint(debugVerboseStopPointDetector.get());
				paintCanvas();
			}).selectedProperty();
			
			showSettingsAlmostAlignedPtsSimplifier = addCheckMenuItem(debugItems, "show settings almost aligned", () -> {}).selectedProperty();
			
			debugMatchShape = addCheckMenuItem(debugItems, "debug match", () -> paintCanvas()).selectedProperty();
			
			debugMatchPtToAbsciss = addCheckMenuItem(debugItems, "debug absciss", () -> paintCanvas()).selectedProperty();
			
			{ // 
				Menu paramShifterMenu = new Menu("inital param shifter");
				debugItems.add(paramShifterMenu);
				
				ToggleGroup group = new ToggleGroup();
				addParamCtxTransformer(paramShifterMenu, group, ".", null);
				addParamCtxTransformer(paramShifterMenu, group, "->", 
						ctx -> {
							val xDef = ctx.findVarByName("x");
							ctx.put(xDef, ctx.get(xDef) + 50);
							return ctx;
						});
				addParamCtxTransformer(paramShifterMenu, group, "x2", 
						ctx -> {
							val wDef = ctx.findVarByName("w");
							double wValue = ctx.get(wDef);
							ctx.put(wDef, wValue * 2);
							val hDef = ctx.findVarByName("h");
							ctx.put(hDef, ctx.get(hDef) * 2);
							return ctx;
						});
			}
			
		}

		{
			HBox settingsPanel = new HBox(4);
			settingsPanel.visibleProperty().bind(showSettingsAlmostAlignedPtsSimplifier);
			settingsPanel.managedProperty().bind(showSettingsAlmostAlignedPtsSimplifier);
			toolbarItems.add(settingsPanel);

			{ // maxAngleChange = 5;
				Slider slider = new Slider();
				slider.setTooltip(new Tooltip("almost aligned threshold for angle to remove pt "));
				slider.setMin(0);
				slider.setMax(20);
				slider.setShowTickMarks(true);
				slider.setShowTickLabels(true);
				slider.setValue(this.almostAlignedPtsSimplifier.getMaxAngleChange()*180/Math.PI);
				slider.valueProperty().addListener((ctrl,oldValue,newValue) -> {
					this.almostAlignedPtsSimplifier.setMaxAngleChange(newValue.doubleValue()*Math.PI/180);
				});
				settingsPanel.getChildren().add(slider);
			}
		}
		
		{
			MenuButton menu = new MenuButton("Simplify");
			toolbarItems.add(menu);
			List<MenuItem> menuItems = menu.getItems();
			addMenuItem(menuItems, "Rm Pts", () -> {
				TraceGesture gesture = (currGesture != null)? currGesture : traceShape.getLast();
				if (gesture != null) {
					almostAlignedPtsSimplifier.simplifyGestureLines(gesture);
					tooNarrowPtsSimplifier.simplifyTooNarrowPts(gesture);
					almostAlignedPtsSimplifier.simplifyGestureLines(gesture);
					paintCanvas();
				}
			});
			addMenuItem(menuItems, "Rm Narrow Pts only", () -> {
				TraceGesture gesture = (currGesture != null)? currGesture : traceShape.getLast();
				if (gesture != null) {
					tooNarrowPtsSimplifier.simplifyTooNarrowPts(gesture);
					paintCanvas();
				}
			});
			addMenuItem(menuItems, "Rm Aligned Pts only", () -> {
				TraceGesture gesture = (currGesture != null)? currGesture : traceShape.getLast();
				if (gesture != null) {
					almostAlignedPtsSimplifier.simplifyGestureLines(gesture);
					paintCanvas();
				}
			});
		}
		
		{ // recognized shapes menu
			MenuButton recognizeShapeMenu = new MenuButton("Shape Recognizer");
			toolbarItems.add(recognizeShapeMenu);
			List<MenuItem> recognizeItems = recognizeShapeMenu.getItems();
			
			addMenuItem(recognizeItems, "Reset", () -> onClickClearMatchShapeDef());
			addMatchShapeItem(recognizeItems, "Line", "line", 0);
			addMatchShapeItem(recognizeItems, "Line2", "line2", 0);
			addMatchShapeItem(recognizeItems, "Rect", "rectangle", 0);
			addMatchShapeItem(recognizeItems, "R(DL->UR..)", "rectangle", 1);
			addMatchShapeItem(recognizeItems, "HCross", "hcross", 0);
		}
		
		if (debugBezier) {
			MenuButton menu = new MenuButton("Debug CubicBezier");
			toolbarItems.add(menu);
			List<MenuItem> menuItems = menu.getItems();
			ToggleGroup group = new ToggleGroup();

			addRadioMenuItem(menuItems, group, "stop edit pt", () -> { debugCubicBezierEditPt = null; });
			addRadioMenuItem(menuItems, group, "edit pt0", () -> { debugCubicBezierEditPt = debugCurrCubicBezier.startPt; });
			addRadioMenuItem(menuItems, group, "edit pt1", () -> { debugCubicBezierEditPt = debugCurrCubicBezier.p1; });
			addRadioMenuItem(menuItems, group, "edit pt2", () -> { debugCubicBezierEditPt = debugCurrCubicBezier.p2; });
			addRadioMenuItem(menuItems, group, "edit pt3", () -> { debugCubicBezierEditPt = debugCurrCubicBezier.endPt; });
		}
		
		return toolbar;
	}

	private MenuItem addMenuItem(List<MenuItem> parent, String label, Runnable action) {
		MenuItem res = new MenuItem(label);
		res.setOnAction(e -> action.run());
		parent.add(res);
		return res;
	}

	private CheckMenuItem addCheckMenuItem(List<MenuItem> parent, String label, Runnable action) {
		CheckMenuItem res = new CheckMenuItem(label);
		res.setSelected(false);
		res.setOnAction(e -> action.run());
		parent.add(res);
		return res;
	}
	
	private Button createButton(String label, Runnable action) {
		Button but = new Button(label);
		but.setOnAction(e -> action.run());
		return but;
	}

	private RadioMenuItem addRadioMenuItem(List<MenuItem> parent, ToggleGroup group, String label, Runnable action) {
		RadioMenuItem res = new RadioMenuItem(label);
		res.setToggleGroup(group);
		res.setOnAction(e -> action.run());
		parent.add(res);
		return res;
	}
	
	private RadioMenuItem addParamCtxTransformer(Menu menu, ToggleGroup group, String label, Function<NumericEvalCtx, NumericEvalCtx> transformer) {
		RadioMenuItem res = new RadioMenuItem(label);
		res.setToggleGroup(group);
		res.setOnAction(e -> {
			paramCtxInitTransformer = transformer;
		});
		menu.getItems().add(res);
		return res;
	}
	
	private void addMatchShapeItem(List<MenuItem> items, String label, String shapeName, int gestureIndex) {
		MenuItem res = new MenuItem(label);
		res.setOnAction(e -> onClickMatchShapeDef(shapeName, gestureIndex));
		items.add(res);
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
			if (debugBezier && null != debugCubicBezierEditPt) {
				debugCubicBezierEditPt.x = e.getSceneX();
				debugCubicBezierEditPt.y = e.getSceneY();
				paintCanvas();
				return;
			}

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

	private void onClickClearMatchShapeDef() {
		this.currMatchParamCtx = null;
		this.currTraceGestureDefMatching = null;
		this.currMatchShape = null;
		paintCanvas();
	}
	
	private void onClickMatchShapeDef(String shapeName, int gestureIndex) {
		ShapeDef shapeDef = shapeDefRegistry.getShapeDef(shapeName);
		tryMatchShape(shapeDef, gestureIndex);
	}
	
	private void tryMatchShape(ShapeDef currMatchShapeDef, int gestureIndex) {
		GesturePathesDef gestureDef = currMatchShapeDef.gestures.get(gestureIndex);
		TraceGesture matchGesture = traceShape.getLast();
		if (matchGesture == null) {
			return;
		}
//		if (matchGesture.recognizedShape != null) {
//			return; // ??
//		}

		this.currMatchParamCtx = new NumericEvalCtx();
		
		gestureDef.initalParamEstimator.estimateInitialParamsFor(
				matchGesture, gestureDef, currMatchParamCtx);

		if (paramCtxInitTransformer != null) {
			this.currMatchParamCtx = paramCtxInitTransformer.apply(currMatchParamCtx);
		}
		
		this.currTraceGestureDefMatching = TraceGestureDefMatchingBuilder.match(gestureDef, 
				matchGesture, discretizationPrecision, 
				currMatchParamCtx);
		
//		this.currGesturePtToAbscissMatch = new GesturePtToAbscissMatch(matchGesture, gestureDef, 
//				discretizationPrecision, 
//				currMatchParamCtx);
			
//			Expr costExpr = matchShapeToCostExprBuilder.costMatchGestureWithAbsciss(
//					matchGesture,
//					gestureDef, 
//					currMatchIndexToAbsciss);
//
			// TODO ..
			
					
		// TODO .. choice + optim steps
		
		
		this.currMatchShape = new ShapeCtxEval(currMatchShapeDef);
		this.currMatchShape.eval(currMatchParamCtx);
		
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
		
		val gcRenderer = new ShapeDefGcRenderer(gc);
		for(val shape : shapes) {
			gcRenderer.draw(shape);
		}
		
		if (currMatchShape != null) {
			gcRenderer.draw(currMatchShape);
			if (debugMatchPtToAbsciss.get()) {
				if (currTraceGestureDefMatching != null) {
// TODO 					drawPtToAbscissMatch(gc, currTraceGestureDefMatching);
				}
			}
		}
		
		
		// Debug Bezier Curve
		if (debugBezier) {
			double w = 100;
			int maxStep = 100;
			for(int step = 0; step <= maxStep; step++) {
				double s = ((double)step) / maxStep;
				Pt2D pt = debugCurrCubicBezier.eval(s);
				drawPtCircle(gc, pt, 1);
			}

			BoundingRect2DBuilder bboxBuider = new BoundingRect2DBuilder();
			BezierEnclosingRect2DUtil.bestEnclosing_CubicBezier(bboxBuider, debugCurrCubicBezier);
			BoundingRect2D bbox = bboxBuider.build();
			
			gc.beginPath();
			gc.rect(bbox.minx, bbox.miny, (bbox.maxx-bbox.minx), (bbox.maxy-bbox.miny));
			gc.stroke();
			
			Paint prevStroke = gc.getStroke();
			gc.setStroke(Color.RED);
			drawPtCircle(gc, debugCurrCubicBezier.startPt, 3);
			drawPtCircle(gc, debugCurrCubicBezier.p1, 3);
			drawPtCircle(gc, debugCurrCubicBezier.p2, 3);
			drawPtCircle(gc, debugCurrCubicBezier.endPt, 3);
			gc.setStroke(prevStroke);
			
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
		drawSegment(gc, segment.startPt, segment.endPt);
	}
	private void drawSegment(GraphicsContext gc, TracePt startPt, TracePt endPt) {
		drawSegment(gc, startPt.xy(), endPt.xy());
	}
	private void drawSegment(GraphicsContext gc, Pt2D startPt, Pt2D endPt) {
		gc.beginPath();
		gc.moveTo(startPt.x, startPt.y);
		gc.lineTo(endPt.x, endPt.y);
		gc.stroke();
	}

	@SuppressWarnings("unused")
	private void drawSegment(GraphicsContext gc, double startX, double startY, double endX, double endY) {
		gc.beginPath();
		gc.moveTo(startX, startY);
		gc.lineTo(endX, endY);
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
			val debugTrace = debugTrace.get();
			val dbgTraceStopPoint = debugTraceStopPoints.get();
			val dbgTraceEndPoint = false;
			if (dbgTraceEndPoint) {
				drawPtCircle(gc, pt0, 5);
			}
			TracePt prevDisplayIndexPt = null;
			for(int i = 1; i < tracePtsLen; i++) {
				val pt = tracePts.get(i);
				if (dbgTraceStopPoint && pt.isStopPoint()) {
					drawPtCircle(gc, pt, 5);
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
				drawPtCircle(gc, lastPt, 3);
			}
			
		}
	}

	private void drawPtToAbscissMatch(GraphicsContext gc, GesturePtToAbscissMatch ptToAbscissMatch) {
		Paint prevStroke = gc.getStroke();
		gc.setStroke(Color.RED);
		for (val matchPt : ptToAbscissMatch.gestureMatchDiscretizedPts) {
			TracePt pt = matchPt.weighedPt().pt;
			PtExpr ptDefExpr = matchPt.currMatchPtExpr.build();
			Pt2D ptDef = currMatchParamCtx.evalPtExpr(ptDefExpr);

			// drawPtCircle(gc, pt, 3);
			drawSegment(gc, pt.xy(), ptDef);
			// drawPtCircle(gc, ptDef, 3);
			
		}
		gc.setStroke(prevStroke);
	}

	private void drawPtCircle(GraphicsContext gc, Pt2D pt, int r) {
		drawPtCircle(gc, pt.x, pt.y, r);
	}
	private void drawPtCircle(GraphicsContext gc, TracePt pt, int r) {
		drawPtCircle(gc, pt.x, pt.y, r);
	}
	private void drawPtCircle(GraphicsContext gc, double x, double y, int r) {
		gc.strokeOval(x-r, y-r, r+r, r+r);
	}

}
