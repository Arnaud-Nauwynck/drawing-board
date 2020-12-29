package fr.an.drawingboard.ui.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import fr.an.drawingboard.geom2d.BoundingRect2D;
import fr.an.drawingboard.geom2d.BoundingRect2D.BoundingRect2DBuilder;
import fr.an.drawingboard.geom2d.CubicBezier2D;
import fr.an.drawingboard.geom2d.Pt2D;
import fr.an.drawingboard.geom2d.QuadBezier2D;
import fr.an.drawingboard.geom2d.WeightedPt2D;
import fr.an.drawingboard.geom2d.bezier.BezierEnclosingRect2DUtil;
import fr.an.drawingboard.geom2d.bezier.BezierMatrixSplit;
import fr.an.drawingboard.geom2d.bezier.BezierPtsFittting;
import fr.an.drawingboard.geom2d.bezier.PtToBezierDistanceMinSolver;
import fr.an.drawingboard.geom2d.bezier.PtToBezierDistanceMinSolver.PtToCurveDistanceMinSolverResult;
import fr.an.drawingboard.geom2d.bezier.RaiseLowerBezierDegreeUtil;
import fr.an.drawingboard.geom2d.utils.PolygonalDistUtils;
import fr.an.drawingboard.model.drawingelt.DrawingElement;
import fr.an.drawingboard.model.drawingelt.TraceDrawingElement;
import fr.an.drawingboard.model.shapedef.GesturePathesDef;
import fr.an.drawingboard.model.shapedef.ShapeDef;
import fr.an.drawingboard.model.shapedef.ShapeDefRegistry;
import fr.an.drawingboard.model.shapedef.obj.GesturePathesObj;
import fr.an.drawingboard.model.shapedef.paramdef.ParamCategoryRegistry;
import fr.an.drawingboard.model.shapedef.paramdef.ParamDef;
import fr.an.drawingboard.model.trace.TraceGesture;
import fr.an.drawingboard.model.trace.TracePath;
import fr.an.drawingboard.model.trace.TracePathElement;
import fr.an.drawingboard.model.trace.TracePathElement.DiscretePointsTracePathElement;
import fr.an.drawingboard.model.trace.TracePathElementBuilder;
import fr.an.drawingboard.model.trace.TracePt;
import fr.an.drawingboard.model.trace.TraceShape;
import fr.an.drawingboard.model.varctx.DrawingCtxTreeNode;
import fr.an.drawingboard.model.varctx.DrawingCtxTreeNode.SimilarVarCostFunction;
import fr.an.drawingboard.model.varctx.DrawingVarDef;
import fr.an.drawingboard.recognizer.initialParamEstimators.ParamEvalCtx;
import fr.an.drawingboard.recognizer.shape.MatchShapeToCostExprBuilder;
import fr.an.drawingboard.recognizer.shape.TraceSymbolLevenshteinEditOptimizer;
import fr.an.drawingboard.recognizer.shape.TraceSymbolLevenshteinEditOptimizer.PathCtxEvalSymbol;
import fr.an.drawingboard.recognizer.shape.TraceSymbolLevenshteinEditOptimizer.TracePathSymbol;
import fr.an.drawingboard.recognizer.shape.TraceSymbolLevenshteinEditOptimizer.TraceSymbolLevensteinDist;
import fr.an.drawingboard.recognizer.shape.TraceSymbolMatchCostFunction;
import fr.an.drawingboard.recognizer.trace.AlmostAlignedPtsSimplifier;
import fr.an.drawingboard.recognizer.trace.StopPointDetector;
import fr.an.drawingboard.recognizer.trace.TooNarrowPtsSimplifier;
import fr.an.drawingboard.recognizer.trace.TraceDiscretisationPtsBuilder;
import fr.an.drawingboard.recognizer.trace.TracePathElementDetector;
import fr.an.drawingboard.stddefs.shapedef.ShapeDefRegistryBuilder;
import fr.an.drawingboard.stddefs.trace.StdTraceBuilder;
import fr.an.drawingboard.util.LsUtils;
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
import lombok.AllArgsConstructor;
import lombok.val;

@SuppressWarnings("deprecation")
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
	private TraceGesture currGesture;
	private TracePath currPath;
	private TracePathElementBuilder currPathElementBuilder;

	private DrawingCtxTreeNode drawingRootNode = DrawingCtxTreeNode.createRootNode();
	
	private TooNarrowPtsSimplifier tooNarrowPtsSimplifier = new TooNarrowPtsSimplifier();
	private AlmostAlignedPtsSimplifier almostAlignedPtsSimplifier = new AlmostAlignedPtsSimplifier();
	private StopPointDetector stopPointDetector = new StopPointDetector();
	private TracePathElementDetector pathElementDetector = new TracePathElementDetector();
	@SuppressWarnings({ "unused" })
	private MatchShapeToCostExprBuilder matchShapeToCostExprBuilder = new MatchShapeToCostExprBuilder();
	private Function<ParamEvalCtx,ParamEvalCtx> paramCtxInitTransformer;
	
	private boolean showSettingsStopPointDetector = false;
	private BooleanProperty showSettingsAlmostAlignedPtsSimplifier;
	
	// 
	private double currLineWidth = 2;
	

	private ShapeDefRegistry shapeDefRegistry;

	private TraceDiscretisationPtsBuilder traceDiscretisationPtsBuilder = new TraceDiscretisationPtsBuilder();
	private TraceSymbolMatchCostFunction traceSymbolMatchCostFunc = new TraceSymbolMatchCostFunction();
	
	private Map<String,MatchToShapeDef> currMatchPerShape = new HashMap<>();
	private MatchToShapeDef currMatchToShapeDef;
	
	private SimilarVarCostFunction varCostFunc = DrawingCtxTreeNode.DEFAULT_SimilarVarCostEvaluator;
	private double maxVarCostOrDefine = 50;
	private double maxVarDiffOrDefine = 30;
	
	private Pt2D currEditPt = null;

	private boolean debugDistPt = false;
	private final Pt2D debugDistEditPt = new Pt2D(300, 200);
	
	private boolean debugQuadBezier = false;
	private final QuadBezier2D debugCurrQuadBezier = new QuadBezier2D(new Pt2D(100, 0), new Pt2D(200, 100), new Pt2D(100, 200));
	private BooleanProperty debugQuadBezierShowBoundingBox;
	private BooleanProperty debugQuadBezierShowSplit2;
	private BooleanProperty debugQuadBezierShowSplit;
	private BooleanProperty debugQuadBezierShowRaiseCubic;
	
	private boolean debugCubicBezier = false;
	private final CubicBezier2D debugCurrCubicBezier = new CubicBezier2D(new Pt2D(100, 0), new Pt2D(200, 100), new Pt2D(200, 200), new Pt2D(100, 300));
	private BooleanProperty debugCubicBezierShowBoundingBox;
	private BooleanProperty debugCubicBezierShowSplit2;
	private BooleanProperty debugCubicBezierShowSplit;
	private BooleanProperty debugCubicBezierShowSplitWeight;
	private BooleanProperty debugCubicBezierShowLowerQuad;
	

	private boolean debugFittingBezier = false;
	private BooleanProperty showFittingQuadBezier;
	private BooleanProperty showFittingCubicBezier;
	private final QuadBezier2D debugCurrTraceFittingQuadBezier = new QuadBezier2D();
	private final CubicBezier2D debugCurrTraceFittingCubicBezier = new CubicBezier2D();

	// --------------------------------------------------------------------------------------------

	public DrawingBoardUi() {
		shapeDefRegistry = new ShapeDefRegistry();
		new ShapeDefRegistryBuilder(shapeDefRegistry, ParamCategoryRegistry.INSTANCE).addStdShapes();
		
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
			removeLastNode();
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
							val xDef = ctx.paramByName("x");
							ctx.put(xDef, ctx.get(xDef) + 50);
							return ctx;
						});
				addParamCtxTransformer(paramShifterMenu, group, "x2", 
						ctx -> {
							val wDef = ctx.paramByName("w");
							double wValue = ctx.get(wDef);
							ctx.put(wDef, wValue * 2);
							val hDef = ctx.paramByName("h");
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
				TraceGesture gesture = lastTraceGesture();
				if (gesture != null) {
					almostAlignedPtsSimplifier.simplifyGestureLines(gesture);
					tooNarrowPtsSimplifier.simplifyTooNarrowPts(gesture);
					almostAlignedPtsSimplifier.simplifyGestureLines(gesture);
					paintCanvas();
				}
			});
			addMenuItem(menuItems, "Rm Narrow Pts only", () -> {
				TraceGesture gesture = lastTraceGesture();
				if (gesture != null) {
					tooNarrowPtsSimplifier.simplifyTooNarrowPts(gesture);
					paintCanvas();
				}
			});
			addMenuItem(menuItems, "Rm Aligned Pts only", () -> {
				TraceGesture gesture = lastTraceGesture();
				if (gesture != null) {
					almostAlignedPtsSimplifier.simplifyGestureLines(gesture);
					paintCanvas();
				}
			});
		}

		{
			MenuButton menu = new MenuButton("Draw");
			toolbarItems.add(menu);
			List<MenuItem> menuItems = menu.getItems();
			addMenuItem(menuItems, "Down-Right", () -> {
				currGesture = StdTraceBuilder.traceDownRight();
				paintCanvas();
			});
			addMenuItem(menuItems, "Down-Right rounded", () -> {
				currGesture = StdTraceBuilder.traceDownRight_1Path();
				paintCanvas();
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
			addMatchShapeItem(recognizeItems, "Rect(DL->UR..)", "rectangle", 1);
			addMatchShapeItem(recognizeItems, "HCross", "hcross", 0);
			addMatchShapeItem(recognizeItems, "VCross", "vcross", 0);
			addMatchShapeItem(recognizeItems, "Z", "z", 0);
			addMatchShapeItem(recognizeItems, "inv Z", "inv z", 0);
			addMatchShapeItem(recognizeItems, "U", "u", 0);
			addMatchShapeItem(recognizeItems, "N", "n", 0);
			addMatchShapeItem(recognizeItems, "inv N", "inv n", 0);
			addMatchShapeItem(recognizeItems, "C", "c", 0);
			addMatchShapeItem(recognizeItems, "C(DR->DL..)", "c up", 0);
			addMatchShapeItem(recognizeItems, "inv C", "inv c", 0);
		}
		
		toolbarItems.add(createButton("clear match", () -> clearMatch()));
		toolbarItems.add(createMatchShapeButton("Rect", "rectangle", 0));
		toolbarItems.add(createMatchShapeButton("Z", "z", 0));
		toolbarItems.add(createMatchShapeButton("right,down", "right,down", 0));
		toolbarItems.add(createButton("match best", () -> tryMatchBestShape()));
		toolbarItems.add(createButton("dump match", () -> dumpMatch(currMatchToShapeDef)));
		
		if (debugDistPt) {
			MenuButton menu = new MenuButton("Debug Dist Pt");
			toolbarItems.add(menu);
			List<MenuItem> menuItems = menu.getItems();
			ToggleGroup group = new ToggleGroup();

			addRadioMenuItem(menuItems, group, "stop edit dist pt", () -> { currEditPt = null; });
			addRadioMenuItem(menuItems, group, "edit dist pt", () -> { currEditPt = debugDistEditPt; });
		}

		if (debugQuadBezier) {
			MenuButton menu = new MenuButton("Debug QuadBezier");
			toolbarItems.add(menu);
			List<MenuItem> menuItems = menu.getItems();
			ToggleGroup group = new ToggleGroup();

			addRadioMenuItem(menuItems, group, "stop edit pt", () -> { currEditPt = null; });
			addRadioMenuItem(menuItems, group, "edit start pt", () -> { currEditPt = debugCurrQuadBezier.startPt; });
			addRadioMenuItem(menuItems, group, "edit ctrl pt", () -> { currEditPt = debugCurrQuadBezier.controlPt; });
			addRadioMenuItem(menuItems, group, "edit end pt", () -> { currEditPt = debugCurrQuadBezier.endPt; });
			
			debugQuadBezierShowBoundingBox = addCheckMenuItem(menuItems, "show bounding box", () -> paintCanvas()).selectedProperty();
			debugQuadBezierShowSplit2 = addCheckMenuItem(menuItems, "show split2", () -> paintCanvas()).selectedProperty();
			debugQuadBezierShowSplit = addCheckMenuItem(menuItems, "show split", () -> paintCanvas()).selectedProperty();
			debugQuadBezierShowRaiseCubic = addCheckMenuItem(menuItems, "show raise to cubic", () -> paintCanvas()).selectedProperty();
		}
		
		if (debugFittingBezier) {
			MenuButton menu = new MenuButton("Debug Fitting Bezier");
			toolbarItems.add(menu);
			List<MenuItem> menuItems = menu.getItems();
			showFittingQuadBezier = addCheckMenuItem(menuItems, "show fitting Quad Bezier", () -> paintCanvas()).selectedProperty();
			showFittingCubicBezier = addCheckMenuItem(menuItems, "show fitting Cubic Bezier", () -> paintCanvas()).selectedProperty();
		}

		if (debugCubicBezier) {
			MenuButton menu = new MenuButton("Debug CubicBezier");
			toolbarItems.add(menu);
			List<MenuItem> menuItems = menu.getItems();
			ToggleGroup group = new ToggleGroup();

			addRadioMenuItem(menuItems, group, "stop edit pt", () -> { currEditPt = null; });
			addRadioMenuItem(menuItems, group, "edit start pt", () -> { currEditPt = debugCurrCubicBezier.startPt; });
			addRadioMenuItem(menuItems, group, "edit ctrl1 pt", () -> { currEditPt = debugCurrCubicBezier.p1; });
			addRadioMenuItem(menuItems, group, "edit ctrl2 pt", () -> { currEditPt = debugCurrCubicBezier.p2; });
			addRadioMenuItem(menuItems, group, "edit end pt", () -> { currEditPt = debugCurrCubicBezier.endPt; });

			debugCubicBezierShowBoundingBox = addCheckMenuItem(menuItems, "show bounding box", () -> paintCanvas()).selectedProperty();
			debugCubicBezierShowSplit2 = addCheckMenuItem(menuItems, "show split2", () -> paintCanvas()).selectedProperty();
			debugCubicBezierShowSplit = addCheckMenuItem(menuItems, "show split", () -> paintCanvas()).selectedProperty();
			debugCubicBezierShowSplitWeight = addCheckMenuItem(menuItems, "show split weight", () -> paintCanvas()).selectedProperty();
			debugCubicBezierShowLowerQuad = addCheckMenuItem(menuItems, "show lower to quad", () -> paintCanvas()).selectedProperty();
		}
		
		return toolbar;
	}

	private void clearMatch() {
		currMatchPerShape.clear();
		currMatchToShapeDef = null;
		paintCanvas();
	}

	private void removeLastNode() {
		DrawingCtxTreeNode lastChildNode = this.drawingRootNode.lastChildNode();
		if (lastChildNode != null) {
			this.drawingRootNode.removeChild(lastChildNode);
			currGesture = null; currPath = null;
			
			currMatchPerShape.clear();
			currMatchToShapeDef = null;
		}
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
	
	private RadioMenuItem addParamCtxTransformer(Menu menu, ToggleGroup group, String label, Function<ParamEvalCtx, ParamEvalCtx> transformer) {
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

	private Button createMatchShapeButton(String label, String shapeName, int gestureIndex) {
		return createButton(label, () -> onClickMatchShapeDef(shapeName, gestureIndex));
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
			// System.out.println("mouse pressed " + e.getClickCount());
			canvas.requestFocus();
			
			currGesture = new TraceGesture();
			currPath = new TracePath(); // currGesture.appendNewPath();
			currPathElementBuilder = new TracePathElementBuilder();
		}
		@Override
		public void onMouseReleased(MouseEvent e) {
			// System.out.println("mouse released ");
			
			if (currPathElementBuilder != null) {
				flushStopPointOrMouseReleased();
			
				if (currPath != null && !currPath.isEmpty()) {
					// remove too small lines ?
					// TODO recognize trace? ... add drawingElt
					currGesture.addPath(currPath);
				}
				if (currGesture != null && !currGesture.isEmpty()) {
					DrawingCtxTreeNode traceNode = drawingRootNode.addChildCtx_GenerateNameFor("trace");
					val traceShape = new TraceShape();
					traceShape.add(currGesture);
					traceNode.addDrawingElementTrace(traceShape);
				}
				
				currPathElementBuilder = null;
				currPath = null;
				currGesture = null;
				
				paintCanvas();
			}
		}

		@Override
		public void onMouseClicked(MouseEvent e) {
			// System.out.println("mouse clicked " + e.getClickCount());
			// canvas.requestFocus(); // otherwise KeyEvent not captured by canvas
		}
		
		@Override
		public void onMouseMoved(MouseEvent e) {
			// System.out.print(".");
		}

		@Override
		public void onMouseDragged(MouseEvent e) {
			// System.out.println("mouse dragged");
			if (null != currEditPt) {
				currEditPt.x = e.getX();
				currEditPt.y = e.getY();
				paintCanvas();
				return;
			}

			if (currPathElementBuilder != null) {
				TracePt prevPt = currPathElementBuilder.lastPt();

				// append point to current pathElement
				int pressure = 1; // not managed yet
				double x = e.getX(), y = e.getY();
				long time = System.currentTimeMillis();
				TracePt pt = currPathElementBuilder.appendTracePt(x, y, time, pressure);

				// detect if prev pt was a stop point
				if (prevPt != null) {
					boolean stop = stopPointDetector.onNewTracePt(currPathElementBuilder, pt);
					if (stop) {
						flushStopPointOrMouseReleased();
						if (pt.isStopPoint()) {
							// remove from prev?
						}
						
						// currGesture = new TraceGesture();
						// currPath = currGesture.appendNewPath();
					}
				}
				
				paintCanvas();
			}
		}

		private void flushStopPointOrMouseReleased() {
			// recognize segment, discrete points curve, or quad/cubic bezier...
			TracePathElement pathElement = pathElementDetector.recognizePathElement(currPathElementBuilder);
			boolean isEmpty = false;
			if (pathElement != null) {
				if (pathElement instanceof DiscretePointsTracePathElement) {
					val pathElement2 = (DiscretePointsTracePathElement) pathElement;
					isEmpty = pathElement2.tracePts.size() <= 2;
				}
				if (!isEmpty && pathElement.startPt.xy().distTo(pathElement.endPt.xy()) < 10) {
					// TOCHANGE.. more condition
					// do not add almost empty element
					isEmpty = true;
				}
				
				if (!isEmpty) {
					currPath.addPathElement(pathElement);
					
					currGesture.addPath(currPath);
					// currPath = null; // ??
					currPath = new TracePath();
				}
			}
			
			val pt = currPathElementBuilder.lastPt();
			if (pt != null) {
				currPathElementBuilder = new TracePathElementBuilder(pt);
			}
		}

	}

	// Match recognizer
	// --------------------------------------------------------------------------------------------

	private TraceGesture lastTraceGesture() {
		if (currGesture != null) {
			return currGesture;
		}
		val lastNode = drawingRootNode.lastChildNode();
		if (lastNode == null) {
			return null;
		}
		DrawingElement drawing = lastNode.lastDrawingElement();
		if (drawing == null) {
			return null;
		}
		if (drawing instanceof TraceDrawingElement) {
			TraceDrawingElement e = (TraceDrawingElement) drawing;
			TraceShape shape = e.getTrace();
			return shape.getLast();
		}
		return null;
	}

	private void onClickClearMatchShapeDef() {
		this.currMatchPerShape.clear();
		paintCanvas();
	}
	
	private void onClickMatchShapeDef(String shapeName, int gestureIndex) {
		ShapeDef shapeDef = shapeDefRegistry.getShapeDef(shapeName);
		tryMatchShape(shapeDef, gestureIndex);
	}
	
	@AllArgsConstructor
	public static class MatchToShapeDef {
		ShapeDef shapeDef;
		GesturePathesDef gestureDef;
		
		ParamEvalCtx matchParamCtx;
		GesturePathesObj shapeCtxEval;
		TraceSymbolLevenshteinEditOptimizer matchOptimizer;
		List<TraceSymbolLevensteinDist> resultEditPath;
		
		// PtToSlotDefDynamicProgOptimizer ptToDefOptimizer;
		double cost() {
			return matchOptimizer.getResultCost();
		}
	}

	
	
	private void tryMatchBestShape() {
		TraceGesture traceGesture = lastTraceGesture();
		if (traceGesture == null) {
			return;
		}
		List<TracePathSymbol> tracePathSymbols = TracePathSymbol.traceGestureToSourceSymbols(traceGesture, traceDiscretisationPtsBuilder);
		
		double bestCost = Double.MAX_VALUE;
		MatchToShapeDef bestMatchToShapeDef = null;
		for(val shapeDef: shapeDefRegistry.shapeDefs.values()) {
			for(val gestureDef: shapeDef.gestures) {
				MatchToShapeDef matchToShapeDef = computeMatchTraceToDef(traceGesture, tracePathSymbols, 
						shapeDef, gestureDef);
				double cost = matchToShapeDef.cost();
				if (cost < bestCost) {
					bestCost = cost;
					bestMatchToShapeDef = matchToShapeDef;
				}
			}
		}
		this.currMatchToShapeDef = bestMatchToShapeDef;
		System.out.println("best match: " + bestMatchToShapeDef.shapeDef.name + " cost:" + bestMatchToShapeDef.cost() + " avgDist:" + Math.sqrt(bestMatchToShapeDef.cost()));
		
		paintCanvas();
	}

	
	
	private void tryMatchShape(ShapeDef currMatchShapeDef, int gestureIndex) {
		TraceGesture traceGesture = lastTraceGesture();
		if (traceGesture == null) {
			return;
		}
		GesturePathesDef gestureDef = currMatchShapeDef.gestures.get(gestureIndex);

		List<TracePathSymbol> tracePathSymbols = TracePathSymbol.traceGestureToSourceSymbols(traceGesture, traceDiscretisationPtsBuilder);
		
		MatchToShapeDef matchToShapeDef = computeMatchTraceToDef(traceGesture, tracePathSymbols, 
				currMatchShapeDef, gestureDef);
		
		
//		DrawingCtxTreeNode shapeNodeCtx = this.drawingRootNode.addChildCtx_GenerateNameFor(currMatchShapeDef.name);
//		Map<ParamDef, Double> paramValues = matchToShapeDef.matchParamCtx.getParamValuesCopy();
//		Map<ParamDef, DrawingVarDef> paramBindings =
//				shapeNodeCtx.defineVarExpr(paramValues);
//				// shapeNodeCtx.resolveSimilarOrDefineVarExpr(paramValues, varCostFunc, maxVarCostOrDefine, maxVarDiffOrDefine);
//		
//		shapeNodeCtx.addDrawingElementShape(currMatchShapeDef, paramBindings);
		this.currMatchPerShape.clear();
		
		this.currMatchToShapeDef = matchToShapeDef;
		System.out.println("match: " + matchToShapeDef.shapeDef.name + " cost:" + matchToShapeDef.cost() + " avgDist:" + Math.sqrt(matchToShapeDef.cost()));
		
		paintCanvas();
	}

	private MatchToShapeDef computeMatchTraceToDef(
			TraceGesture traceGesture,
			List<TracePathSymbol> sourceSymbols,
			ShapeDef shapeDef, 
			GesturePathesDef gestureDef
			) {
		// initial param estimation
		ParamEvalCtx matchParamCtx = new ParamEvalCtx();
		gestureDef.initalParamEstimator.estimateInitialParamsFor(
				traceGesture, gestureDef, matchParamCtx);

		if (paramCtxInitTransformer != null) {
			matchParamCtx = paramCtxInitTransformer.apply(matchParamCtx);
		}

		// eval gestureDef for param
		GesturePathesObj gestureCtxEval = new GesturePathesObj(gestureDef); 
		gestureCtxEval.update(matchParamCtx.evalCtx);

		// pathElements between stop points as 'symbol' (to match on traceSymbols)
		List<PathCtxEvalSymbol> targetSymbols = PathCtxEvalSymbol.gestureCtxToTargetSymbols(gestureCtxEval);
		
		// compute Levenstein edit distance bewteen source traceSymbols and target shapeDef Symbols
		TraceSymbolLevenshteinEditOptimizer matchOptimizer = 
				TraceSymbolLevenshteinEditOptimizer.computeMatch(traceSymbolMatchCostFunc, sourceSymbols, targetSymbols);
		
		List<TraceSymbolLevensteinDist> resultEditPath = matchOptimizer.getResultEditPath();
		return new MatchToShapeDef(shapeDef, gestureDef, matchParamCtx, gestureCtxEval, matchOptimizer, resultEditPath);
	}

	public void dumpMatch(MatchToShapeDef matchToShapeDef) {
		TraceSymbolLevenshteinEditOptimizer matchOptimizer = matchToShapeDef.matchOptimizer;
		System.out.println("match: " + matchToShapeDef.shapeDef.name + " cost:" + matchToShapeDef.cost() + " avgDist:" + Math.sqrt(matchToShapeDef.cost()));
		List<TraceSymbolLevensteinDist> editPath = matchToShapeDef.resultEditPath;
		for(val edit: editPath) {
			System.out.println("op:" + edit.editOp + "  cost:" + edit.cost + " editCost:" + edit.editCost);
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

		val gcRenderer = new GcRendererHelper(gc);
		gcRenderer.debugTrace = debugTrace.get();
		gcRenderer.debugTraceStopPoints = debugTraceStopPoints.get();

		gc.setLineWidth(currLineWidth);
		gc.setStroke(currLineColor);
		
		if (currGesture != null) {
			gcRenderer.draw(currGesture);
		}
		if (currPath != null) {
			gcRenderer.draw(currPath);
		}

		gc.setLineWidth(currLineWidth);
		gc.setStroke(currLineColor);
		
		if (currPathElementBuilder != null) {
			gcRenderer.drawDiscretePoints(currPathElementBuilder.tracePts);
		}
		
		drawingRootNode.recursiveDraw(gcRenderer);
		
//		if (currMatchShapeCtxEval != null) {
//			gcRenderer.draw(currMatchShapeCtxEval);
//			if (debugMatchPtToAbsciss.get()) {
//				if (currTraceGestureDefMatching != null) {
//// TODO 					drawPtToAbscissMatch(gc, currTraceGestureDefMatching);
//				}
//			}
//		}
		
		if (debugDistPt) {
			Paint prevStroke = gc.getStroke();
			gc.setStroke(Color.BLUE);
			gcRenderer.drawPtCircle(debugDistEditPt, 5);
			gc.setStroke(prevStroke);
			
			if (debugQuadBezier) {
				// compute min project to QuadBezier
				PtToCurveDistanceMinSolverResult minProjResult = new PtToCurveDistanceMinSolverResult();
				PtToBezierDistanceMinSolver.projPtToQuadBezier(minProjResult, debugDistEditPt, debugCurrQuadBezier);

				gc.setStroke(Color.BLUE);
				gcRenderer.drawPtCircle(minProjResult.projPt, 4);
				gcRenderer.drawSegment(minProjResult.projPt, debugDistEditPt);
				gc.setStroke(prevStroke);
			}
			if (debugCubicBezier) {
				// compute min project to QuadBezier
				PtToCurveDistanceMinSolverResult minProjResult = new PtToCurveDistanceMinSolverResult();
				PtToBezierDistanceMinSolver.projPtToCubicBezier(minProjResult, debugDistEditPt, debugCurrCubicBezier);

				gc.setStroke(Color.BLUE);
				gcRenderer.drawPtCircle(minProjResult.projPt, 4);
				gcRenderer.drawSegment(minProjResult.projPt, debugDistEditPt);
				gc.setStroke(prevStroke);
			}
		}
		
		// Debug Quad Bezier Curve
		if (debugQuadBezier) {
			gcRenderer.drawBezier(debugCurrQuadBezier);

			if (debugQuadBezierShowBoundingBox.get()) {
				BoundingRect2DBuilder bboxBuider = new BoundingRect2DBuilder();
				BezierEnclosingRect2DUtil.bestEnclosing_QuadBezier(bboxBuider, debugCurrQuadBezier);
				BoundingRect2D bbox = bboxBuider.build();
				
				gc.beginPath();
				gc.rect(bbox.minx, bbox.miny, (bbox.maxx-bbox.minx), (bbox.maxy-bbox.miny));
				gc.stroke();
			}
            if (debugQuadBezierShowSplit2.get()) {
                Pt2D currTranslate = new Pt2D(150, 0);
                QuadBezier2D splitLeft = new QuadBezier2D(), splitRight = new QuadBezier2D();
                BezierMatrixSplit.splitQuadBezierIn2(splitLeft, splitRight, debugCurrQuadBezier);
                splitLeft.setTranslate(currTranslate);
                splitRight.setTranslate(currTranslate);
                gcRenderer.drawBezier(splitLeft);
                gcRenderer.drawBezier(splitRight);
            }
            if (debugQuadBezierShowSplit.get()) {
                double[] showSplits = new double[] { 0.25, 0.5, 0.75 };
                val offsetSplit = 60;
                Pt2D currTranslate = new Pt2D(150, 0);
                Pt2D translateRight = new Pt2D(0, 0);
                for(val showSplit: showSplits) {
                    QuadBezier2D splitLeft = new QuadBezier2D();
                    QuadBezier2D splitRight = new QuadBezier2D();
                    BezierMatrixSplit.splitQuadBezier(splitLeft, splitRight, showSplit, debugCurrQuadBezier);
                    splitLeft.setTranslate(currTranslate);
                    splitRight.setTranslate(currTranslate);
                    splitRight.setTranslate(translateRight);
                    gcRenderer.drawBezier(splitLeft);
                    gcRenderer.drawBezier(splitRight);
                    currTranslate.x += offsetSplit;
                }
            }
            if (debugQuadBezierShowRaiseCubic.get()) {
            	CubicBezier2D raiseBezier = new CubicBezier2D();
            	Pt2D currTranslate = new Pt2D(10, 0);
            	RaiseLowerBezierDegreeUtil.raiseQuadToCubicBezier(raiseBezier, debugCurrQuadBezier);
            	raiseBezier.setTranslate(currTranslate);
                gcRenderer.drawBezier(raiseBezier);
            }

		}

		if (debugFittingBezier) {
			if (showFittingQuadBezier.get() || showFittingCubicBezier.get()) {
				TracePath lastTrace = currPath;
				TracePathElement lastPathElt = (lastTrace != null)? lastTrace.getLastPathElement() : null;
				if (lastPathElt instanceof DiscretePointsTracePathElement) {
					List<TracePt> lastTracePts = ((DiscretePointsTracePathElement) lastPathElt).tracePts;
					List<Pt2D> lastPts = LsUtils.map(lastTracePts, tracePt -> new Pt2D(tracePt.x, tracePt.y));
					List<WeightedPt2D> wpts = PolygonalDistUtils.ptsToWeightedPts_polygonalDistance(lastPts);
					if (showFittingQuadBezier.get()) {
						// fitting QuadBezier to curr last trace
						BezierPtsFittting.fitControlPt_QuadBezier(debugCurrTraceFittingQuadBezier, wpts);
						gcRenderer.drawBezier(debugCurrTraceFittingQuadBezier);
					}
					if (showFittingCubicBezier.get()) {
						// fitting QuadBezier to curr last trace
						BezierPtsFittting.fitControlPts_CubicBezier(debugCurrTraceFittingCubicBezier, wpts);
						gcRenderer.drawBezier(debugCurrTraceFittingCubicBezier);
					}
				}
			}
		}
		
		// Debug Cubic Bezier Curve
		if (debugCubicBezier) {
			gcRenderer.drawBezier(debugCurrCubicBezier);
			
			if (debugCubicBezierShowBoundingBox.get()) {
				BoundingRect2DBuilder bboxBuider = new BoundingRect2DBuilder();
				BezierEnclosingRect2DUtil.bestEnclosing_CubicBezier(bboxBuider, debugCurrCubicBezier);
				BoundingRect2D bbox = bboxBuider.build();
			
				gc.beginPath();
				gc.rect(bbox.minx, bbox.miny, (bbox.maxx-bbox.minx), (bbox.maxy-bbox.miny));
				gc.stroke();
			}
            if (debugCubicBezierShowSplit2.get()) {
                Pt2D currTranslate = new Pt2D(150, 0);
                CubicBezier2D splitLeft = new CubicBezier2D(), splitRight = new CubicBezier2D();
                BezierMatrixSplit.splitCubicBezierIn2(splitLeft, splitRight, debugCurrCubicBezier);
                splitLeft.setTranslate(currTranslate);
                splitRight.setTranslate(currTranslate);
                gcRenderer.drawBezier(splitLeft);
                gcRenderer.drawBezier(splitRight);
            }
            if (debugCubicBezierShowSplit.get()) {
                // split at s=0.25, paint shifted by x+150
                double[] showSplits = new double[] { 0.25, 0.5, 0.75 };
                val offsetSplit = 60;
                Pt2D currTranslate = new Pt2D(150, 0);
                Pt2D translateRight = new Pt2D(0, 0);
                for(val showSplit: showSplits) {
                    CubicBezier2D splitLeft = new CubicBezier2D();
                    CubicBezier2D splitRight = new CubicBezier2D();
                    BezierMatrixSplit.splitCubicBezier(splitLeft, splitRight, showSplit, debugCurrCubicBezier);
                    splitLeft.setTranslate(currTranslate);
                    splitRight.setTranslate(currTranslate);
                    splitRight.setTranslate(translateRight);
                    gcRenderer.drawBezier(splitLeft);
                    gcRenderer.drawBezier(splitRight);
                    currTranslate.x += offsetSplit;
                }
            }
            if (debugCubicBezierShowSplitWeight.get()) {
//                double[] showSplits = new double[] { 0.2, 0.7, };
//                Pt2D currTranslate = new Pt2D(100, 0);
//                for(int split = 0; split < showSplits.length-1; split++) {
//                	double start = showSplits[split];
//                	double end = showSplits[split+1];
//                    CubicBezier2D middleBezier = new CubicBezier2D();
//                    BezierMatrixSplit.middleSplitCubicBezier(middleBezier, start, end, debugCurrCubicBezier);
//                    middleBezier.setTranslate(currTranslate);
//                    paintCubicBezier(gc, middleBezier);
//                }
                
                CubicBezier2D[] splitB = new CubicBezier2D[] { new CubicBezier2D(), new CubicBezier2D(), new CubicBezier2D(), new CubicBezier2D()};
                BezierMatrixSplit.splitWeight4CubicBezier(splitB[0],splitB[1],splitB[2],splitB[3], debugCurrCubicBezier);
                Pt2D currTranslate = new Pt2D(100, 0);
                for(val b: splitB) {
                	b.setTranslate(currTranslate);
                	gcRenderer.drawBezier(b);
                	// currTranslate.x += 10;
                }
            }
            if (debugCubicBezierShowLowerQuad.get()) {
            	QuadBezier2D lowerBezier = new QuadBezier2D();
            	Pt2D currTranslate = new Pt2D(10, 0);
            	RaiseLowerBezierDegreeUtil.lowerCubicToQuadBezier(lowerBezier, debugCurrCubicBezier);
            	lowerBezier.setTranslate(currTranslate);
                gcRenderer.drawBezier(lowerBezier);
            }
		}

		if (this.currMatchToShapeDef != null) {
			gcRenderer.draw(Color.GRAY, currMatchToShapeDef.shapeCtxEval);
			
			Paint prevStroke = gc.getStroke();
			gc.setStroke(Color.BLUE);
			currMatchToShapeDef.matchOptimizer.drawMatch(gcRenderer, traceSymbolMatchCostFunc, currMatchToShapeDef.resultEditPath);
			gc.setStroke(prevStroke);
		}
	}

}
