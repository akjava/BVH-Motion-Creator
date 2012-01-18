package com.akjava.gwt.bvhtools.client.player;

import com.akjava.gwt.three.client.THREE;
import com.akjava.gwt.three.client.cameras.Camera;
import com.akjava.gwt.three.client.renderers.WebGLRenderer;
import com.akjava.gwt.three.client.scenes.Scene;
import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseUpEvent;
import com.google.gwt.event.dom.client.MouseWheelEvent;

public abstract class SimpleDemoEntryPoint extends TabDemoEntryPoint{

	protected Camera camera;
	protected int cameraX,cameraY,cameraZ;
	protected int screenWidth,screenHeight;
	protected long mouseLast;
	protected int tmpZoom;
	protected Scene scene;
	protected int defaultZoom=10;
	protected int minCamera=5;
	@Override
	public void onMouseWheel(MouseWheelEvent event) {
		if(event.isShiftKeyDown()){
			onMouseWheelWithShiftKey(event.getDeltaY());
		}else{
		//TODO make class
		long t=System.currentTimeMillis();
		if(mouseLast+100>t){
			tmpZoom*=2;
		}else{
			tmpZoom=defaultZoom;
		}
		//GWT.log("wheel:"+event.getDeltaY());
		int tmp=cameraZ+event.getDeltaY()*tmpZoom;
		tmp=Math.max(minCamera, tmp);
		tmp=Math.min(4000, tmp);
		cameraZ=tmp;
		mouseLast=t;
		}
		//log(""+cameraZ);
	}
	public  void onMouseWheelWithShiftKey(int deltaY){
		
	}
	
	@Override
	public void initialize(WebGLRenderer renderer, int width, int height) {
		cameraZ=100;
		screenWidth=width;
		screenHeight=height;
		renderer.setClearColorHex(0x333333, 1);
		scene=THREE.Scene();
		createCamera(scene, width, height);
		
		initializeOthers(renderer);
	}
	
	@Override
	public void update(WebGLRenderer renderer) {
		beforeUpdate(renderer);
		camera.getPosition().set(cameraX, cameraY, cameraZ);
		renderer.render(scene, camera);
	}
	
	protected abstract void beforeUpdate(WebGLRenderer renderer);

	protected abstract  void initializeOthers(WebGLRenderer renderer) ;

	private void createCamera(Scene scene,int width,int height){
		if(camera!=null){
			//TODO find update way.
			scene.remove(camera);
		}
		camera = THREE.PerspectiveCamera(35,(double)width/height,1,6000);
		//camera.getPosition().set(0, 0, cameraZ);
		scene.add(camera);
	}
	
	@Override
	public void resized(int width, int height) {
		screenWidth=width;
		screenHeight=height;
		createCamera(scene,width,height);
	}
	
	protected boolean mouseDown;
	
	protected int mouseDownX;
	protected int mouseDownY;
	@Override
	public void onMouseDown(MouseDownEvent event) {
		mouseDown=true;
		mouseDownX=event.getX();
		mouseDownY=event.getY();
	}

	@Override
	public void onMouseUp(MouseUpEvent event) {
		mouseDown=false;
	}
	
	@Override
	public void onMouseOut(MouseOutEvent event) {
		mouseDown=false;
	}
}
