package com.akjava.gwt.bvhtools.client.player;

import com.akjava.gwt.lib.client.LogUtils;
import com.akjava.gwt.stats.client.Stats;
import com.akjava.gwt.three.client.THREE;
import com.akjava.gwt.three.client.renderers.WebGLRenderer;
import com.akjava.gwt.three.client.renderers.WebGLRenderer.WebGLCanvas;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.event.dom.client.MouseDownHandler;
import com.google.gwt.event.dom.client.MouseMoveEvent;
import com.google.gwt.event.dom.client.MouseMoveHandler;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.dom.client.MouseUpEvent;
import com.google.gwt.event.dom.client.MouseUpHandler;
import com.google.gwt.event.dom.client.MouseWheelEvent;
import com.google.gwt.event.dom.client.MouseWheelHandler;
import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.RootLayoutPanel;
import com.google.gwt.user.client.ui.TabBar.Tab;
import com.google.gwt.user.client.ui.TabLayoutPanel;
import com.google.gwt.user.client.ui.VerticalPanel;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
/**
 * @deprecated
 * @author aki
 *
 */
public abstract class TabDemoEntryPoint implements EntryPoint {

	private WebGLRenderer renderer;



	protected Timer timer;
	protected Stats stats;



	protected WebGLCanvas canvas;



	private PopupPanel dialog;



	private Button hideButton;



	private VerticalPanel main;
	
	protected int canvasWidth,canvasHeight;



	protected TabLayoutPanel tabPanel;



	protected PopupPanel dialog2;
	public WebGLCanvas getCanvas() {
		return canvas;
	}
	public abstract void onMouseClick(ClickEvent event);
	public abstract void onMouseWheel(MouseWheelEvent event);
	public abstract void onMouseMove(MouseMoveEvent event) ;
	public abstract void onMouseDown(MouseDownEvent event) ;
	public abstract void onMouseUp(MouseUpEvent event) ;
	public abstract void onMouseOut(MouseOutEvent event);
	public abstract void update(WebGLRenderer renderer);
	public abstract void initialize(WebGLRenderer renderer,int width,int height);
	public abstract void resized(int width,int height);
	public void onModuleLoad() {
		int tabHeight=30;
		tabPanel = new TabLayoutPanel(tabHeight, Unit.PX);
		
		RootLayoutPanel.get().add(tabPanel);
		
		int width=Window.getClientWidth();
		int height=Window.getClientHeight()-tabHeight;
		/**
		 * 
		 * if
			com.google.gwt.core.client.JavaScriptException: (TypeError): Cannot read property 'WebGLRenderer' of undefined
			
			add lines and both js files on same directory with html
			<script type="text/javascript" language="javascript" src="Three.js"></script>
    		<script type="text/javascript" language="javascript" src="stats.js"></script>     
		 */
		renderer = THREE.WebGLRenderer();
		renderer.setSize(width,height);
		
		
		//renderer.setClearColorHex(0x333333, 1);
		
		//RootLayoutPanel.get().setStyleName("transparent");
		
		canvas = new WebGLCanvas(renderer);
		canvas.setClearColorHex(0);
		//final FocusPanel glCanvas=new FocusPanel(canvas);
		
		canvas.addMouseUpHandler(new MouseUpHandler() {
			
			@Override
			public void onMouseUp(MouseUpEvent event) {
			
				TabDemoEntryPoint.this.onMouseUp(event);
			}
		});


		canvas.addMouseWheelHandler(new MouseWheelHandler() {
			@Override
			public void onMouseWheel(MouseWheelEvent event) {
				TabDemoEntryPoint.this.onMouseWheel(event);
			}
		});
		//hpanel.setFocus(true);
		
		
		canvas.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				TabDemoEntryPoint.this.onMouseClick(event);
			}
		});
		
		canvas.addMouseDownHandler(new MouseDownHandler() {
			
			@Override
			public void onMouseDown(MouseDownEvent event) {
				TabDemoEntryPoint.this.onMouseDown(event);
				
			}
		});
		
		canvas.addMouseOutHandler(new MouseOutHandler() {
			
			@Override
			public void onMouseOut(MouseOutEvent event) {
				TabDemoEntryPoint.this.onMouseOut(event);
			}
		});
		
		canvas.addMouseMoveHandler(new MouseMoveHandler(){

			@Override
			public void onMouseMove(MouseMoveEvent event) {
				TabDemoEntryPoint.this.onMouseMove(event);
			}});
		
		//canvas.setStyleName("clear");
		//glCanvas.getElement().getStyle().setBackgroundColor("#fff");
		canvas.setWidth("100%");
		canvas.setHeight("100%");
		
		tabPanel.add(canvas,getTabTitle());
		//tabPanel.add(new Label("hello"),"test");
		//RootLayoutPanel.get().add(canvas);
		
		canvasWidth=width;
		canvasHeight=height;
		initialize(renderer,width,height);
		
		stats = Stats.insertStatsToRootPanel();
		stats.setPosition(0, 30);//for tab header
		timer = new Timer(){
			public void run(){
				update(renderer);
				stats.update();
			}
		};
		
		
		
		if(!GWT.isScript()){
			timer.scheduleRepeating(100);
		}else{
			timer.scheduleRepeating(1000/60);
		}
		
		
		
		dialog = new PopupPanel();
		VerticalPanel dialogRoot=new VerticalPanel();
		dialogRoot.setSpacing(2);
		//dialog.setStyleName("transparent");
		Label label=new Label("Control");
		label.setStyleName("title");
		dialog.add(dialogRoot);
		dialogRoot.add(label);
		main = new VerticalPanel();
		main.setVisible(false);
		
		
		HorizontalPanel hPanel=new HorizontalPanel();
		hPanel.setWidth("100%");
		hPanel.setHorizontalAlignment(HorizontalPanel.ALIGN_RIGHT);
		dialogRoot.add(hPanel);
		hideButton = new Button("Hide Control");
		
		hideButton.setVisible(false);
		hideButton.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				main.setVisible(false);
				hideButton.setVisible(false);
				rightTop(dialog);
			}
		});
		hPanel.add(hideButton);
		
		dialogRoot.add(main);
		
		label.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				showControl();
			}
		});
		
		createControl(main);
		
		dialog.show();
		rightTop(dialog);
		
		Window.addResizeHandler(new ResizeHandler() {
			@Override
			public void onResize(ResizeEvent event) {
				int w=canvas.getOffsetWidth();
				int h=canvas.getOffsetHeight();
				canvasWidth=w;
				canvasHeight=h;
				resized(w,h);
				renderer.setSize(w, h);
				rightTop(dialog);
			}
		});
		HTMLPanel html=new HTMLPanel(getHtml());
		html.setWidth("100%");
		html.setHeight("20px");
		html.setStyleName("text");
		dialog2 = new PopupPanel();
		dialog2.add(html);
		dialog2.setPopupPosition(150, 30);
		dialog2.setWidth("100%");
		dialog2.setStyleName("transparent");
		dialog2.show();
		
	}
	
	protected void showControl(){
		main.setVisible(true);
		hideButton.setVisible(true);
		rightTop(dialog);
	}
	
	protected void hideControl(){
		main.setVisible(false);
		hideButton.setVisible(false);
		
	}
	
	public String getHtml(){
		return "Powerd by <a href='https://github.com/mrdoob/three.js/'>Three.js</a> & <a href='http://code.google.com/intl/en/webtoolkit/'>GWT</a>";
	}
	public abstract void createControl(Panel parent);
	
	public abstract String getTabTitle();
	
	private void rightTop(PopupPanel dialog){
		int w=Window.getClientWidth();
		int h=Window.getScrollTop();
		int dw=dialog.getOffsetWidth();
		//GWT.log(w+"x"+h+" offset="+dialog.getOffsetWidth());
		dialog.setPopupPosition(w-dw-18, h);
	}
	
	protected void leftBottom(PopupPanel dialog){
		int w=Window.getClientWidth();
		int h=Window.getClientHeight();
		int dw=dialog.getOffsetWidth();
		int dh=dialog.getOffsetHeight();
		//GWT.log(w+"x"+h+" offset="+dialog.getOffsetWidth());
		dialog.setPopupPosition(0, h-dh);
	}
	
	public final native void log(JavaScriptObject object)/*-{
	console.log(object);
	}-*/;
	public static final native void log(String object)/*-{
	console.log(object);
	}-*/;
}
