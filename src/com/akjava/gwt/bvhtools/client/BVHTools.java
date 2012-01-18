package com.akjava.gwt.bvhtools.client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.akjava.bvh.client.BVH;
import com.akjava.bvh.client.BVHNode;
import com.akjava.bvh.client.BVHParser;
import com.akjava.bvh.client.BVHParser.ParserListener;
import com.akjava.bvh.client.Channels;
import com.akjava.bvh.client.NameAndChannel;
import com.akjava.bvh.client.Vec3;
import com.akjava.bvh.client.gwt.BoxData;
import com.akjava.bvh.client.gwt.BoxDataParser;
import com.akjava.gwt.bvhtools.client.player.SimpleDemoEntryPoint;
import com.akjava.gwt.bvhtools.client.player.list.BVHFileWidget;
import com.akjava.gwt.bvhtools.client.player.list.DataListCell;
import com.akjava.gwt.bvhtools.client.player.list.DataListCell.ChangeSelectionListener;
import com.akjava.gwt.bvhtools.client.player.list.DataListCell.DataListRenderer;
import com.akjava.gwt.bvhtools.client.player.resources.Bundles;
import com.akjava.gwt.bvhtools.client.tools.CalculateTool;
import com.akjava.gwt.bvhtools.client.tools.MergeTool;
import com.akjava.gwt.bvhtools.client.tools.MixTool;
import com.akjava.gwt.bvhtools.client.tools.StripTool;
import com.akjava.gwt.bvhtools.client.tools.ThinTool;
import com.akjava.gwt.html5.client.HTML5InputRange;
import com.akjava.gwt.html5.client.extra.HTML5Builder;
import com.akjava.gwt.html5.client.file.File;
import com.akjava.gwt.html5.client.file.FileHandler;
import com.akjava.gwt.html5.client.file.FileReader;
import com.akjava.gwt.html5.client.file.FileUtils;
import com.akjava.gwt.lib.client.LogUtils;
import com.akjava.gwt.lib.client.widget.cell.util.Benchmark;
import com.akjava.gwt.three.client.THREE;
import com.akjava.gwt.three.client.core.Geometry;
import com.akjava.gwt.three.client.core.Intersect;
import com.akjava.gwt.three.client.core.Matrix4;
import com.akjava.gwt.three.client.core.Object3D;
import com.akjava.gwt.three.client.core.Projector;
import com.akjava.gwt.three.client.core.Vector3;
import com.akjava.gwt.three.client.gwt.Clock;
import com.akjava.gwt.three.client.gwt.Object3DUtils;
import com.akjava.gwt.three.client.lights.Light;
import com.akjava.gwt.three.client.objects.Mesh;
import com.akjava.gwt.three.client.renderers.WebGLRenderer;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.MouseMoveEvent;
import com.google.gwt.event.dom.client.MouseUpEvent;
import com.google.gwt.event.dom.client.MouseUpHandler;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import com.google.gwt.http.client.URL;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.FileUpload;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class BVHTools extends SimpleDemoEntryPoint {

	final Projector projector=THREE.Projector();
	@Override
	public void onMouseClick(ClickEvent event) {
		int x=event.getX();
		int y=event.getY();
		/*
		if(inEdge(x,y)){
			screenMove(x,y);
			return;
		}*/
		
		JsArray<Intersect> intersects=projector.gwtPickIntersects(event.getX(), event.getY(), screenWidth, screenHeight, camera,scene);
		
		for(int i=0;i<intersects.length();i++){
			Intersect sect=intersects.get(i);
			
			
			select(sect.getObject());
			break;
		}
		
	}


	Object3D selectedObject;
	
	private void select(Object3D selected){
		selectedObject=selected;
		meshScaleX.setValue((int) (selectedObject.getScale().getX()*10));
		meshScaleY.setValue((int) (selectedObject.getScale().getY()*10));
		meshScaleZ.setValue((int) (selectedObject.getScale().getZ()*10));
		
		positionX.setValue((int) (selectedObject.getPosition().getX()*10));
		positionY.setValue((int) (selectedObject.getPosition().getY()*10));
		positionZ.setValue((int) (selectedObject.getPosition().getZ()*10));
	}

	@Override
	public void onMouseMove(MouseMoveEvent event) {
		if(mouseDown){
			int diffX=event.getX()-mouseDownX;
			int diffY=event.getY()-mouseDownY;
			mouseDownX=event.getX();
			mouseDownY=event.getY();
			
			rotationRange.setValue(rotationRange.getValue()+diffY);
			rotationYRange.setValue(rotationYRange.getValue()+diffX);
		}
	}
	

	private int defaultZ=200;
	
	Map<String,Vector3> boneSizeMap=new HashMap<String,Vector3>();

	private Object3D rootGroup,boneContainer,backgroundContainer;
	
	private Map<String,BoxData> boxDatas;
	@Override
	public void initializeOthers(WebGLRenderer renderer) {
		cameraY=10;
		defaultZoom=5;
		canvas.setClearColorHex(0xcccccc);
		
		
		boxDatas=new BoxDataParser().parse(Bundles.INSTANCE.boxsize().getText());
		
		scene.add(THREE.AmbientLight(0x888888));
		Light pointLight = THREE.PointLight(0xffffff);
		pointLight.setPosition(0, 10, 300);
		scene.add(pointLight);
		
		doLoad("14_08");
		
		rootGroup=THREE.Object3D();
		scene.add(rootGroup);
		
		backgroundContainer=THREE.Object3D();
		rootGroup.add(backgroundContainer);
		
		Geometry geo=THREE.PlaneGeometry(100, 100,20,20);
		Mesh mesh=THREE.Mesh(geo, THREE.MeshBasicMaterial().color(0x666666).wireFrame(true).build());
		//mesh.setPosition(0, -17, 0);
		mesh.setRotation(Math.toRadians(-90), 0, 0);
		backgroundContainer.add(mesh);
		
		boneContainer=THREE.Object3D();
		rootGroup.add(boneContainer);
		/*
		BVHParser parser=new BVHParser();
		//String singleBvh=Bundles.INSTANCE.single_basic().getText();
		//String singleBvh=Bundles.INSTANCE.twolink_n_rz().getText();
		//String singleBvh=Bundles.INSTANCE.twolink_full().getText();
		//String singleBvh=Bundles.INSTANCE.fourlink_branch().getText();
		String singleBvh=Bundles.INSTANCE.twelve01().getText();
		try {
			jointMap=new HashMap<String,Object3D>();
		bvh = parser.parse(singleBvh);
		BVHNode node=bvh.getHiearchy();
		
		root=THREE.Object3D();
		scene.add(root);
		doLog(root,node);
		
		//  
		//jointMap.get("Hips").setRotation(Math.toRadians(7.1338),Math.toRadians(-1.8542),Math.toRadians( -7.8190));
		
		//doPose(bvh,"-0.3899 17.5076 7.8007 0 0 0 0 0 0 -21 0 0 0 0 0 0 0 0 0 0 0 0 0 0 21 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 -16 0 0 21 0 0 11 0 0 0 -8 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 8 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0");
		//doPose(bvh,"-0.3899 17.5076 7.8007 -7.8190 -1.8542 7.1338 0.0000 0.0000 0.0000 -20.0095 -34.8717 23.1818 0.0543 -1.5040 -4.1368 -5.1851 39.6153 -34.2618 0.1274 2.3012 6.3387 0.0000 0.0000 0.0000 27.9815 23.6282 8.6217 -0.0094 -0.6245 1.7162 0.4741 -16.6281 -18.3131 -0.0041 -0.4123 1.1330 4.5929 -0.1445 3.0131 1.7537 0.3513 -8.5234 -0.7284 -0.1282 -10.7842 -14.7495 -15.2891 1.4491 15.1151 -23.2999 16.3175 5.0944 -11.3477 8.3590 -0.0000 -0.0000 0.0000 -91.7499 11.2503 9.6316 11.7870 -19.4845 -2.0307 -0.0000 -0.0000 23.6415 17.7243 -7.2146 -2.2985 -7.1250 0.0000 -0.0000 -6.6264 22.4256 3.6210 -0.0000 -0.0000 0.0000 92.9366 -10.7837 -16.0742 -9.0830 15.2927 -1.2219 0.0000 0.0000 58.8395 3.1909 -29.5170 -1.5733 7.1250 -0.0000 -0.0000 36.2824 -55.5617 -10.3909");
		//doPose(bvh,"-0.5011 17.6282 7.6163 -6.2586 -15.2051 5.4723 0.0000 0.0000 0.0000 -21.3981 -26.0192 22.4790 0.0292 -1.1029 -3.0320 -3.3535 38.5451 -41.4272 0.6169 5.0265 13.9840 0.0000 0.0000 0.0000 24.7397 22.1958 5.0034 -0.0966 -2.0046 5.5183 -0.3873 -3.4522 -13.5373 -0.2228 3.0387 -8.3865 3.1625 -2.9263 2.2948 2.9237 -3.5238 -7.9690 1.7270 -1.8496 -9.7328 3.3763 2.5058 -7.4595 13.0244 0.5160 17.4961 3.6704 1.4696 6.5682 -0.0000 0.0000 0.0000 -42.7751 -56.6315 -8.6834 50.4538 -53.1769 -26.5370 -0.0000 0.0000 -77.6575 14.7878 7.4808 1.9684 -7.1250 0.0000 -0.0000 -10.2345 37.5343 1.9047 -0.0000 0.0000 0.0000 76.3877 59.4476 93.5538 -141.2974 47.2822 -102.5201 0.0000 -0.0000 -31.1956 -12.8820 -58.8974 11.0797 7.1250 -0.0000 -0.0000 91.0580 -76.5215 -77.2227");
	doPose(bvh,bvh.getMotion().getMotions().get(0));
		Matrix4 mx=THREE.Matrix4();
		mx.setRotationFromEuler(THREE.Vector3(Math.toRadians(23.1818),Math.toRadians(-34.8717),Math.toRadians(-20.0095)), "ZYX");
		Vector3 rot=THREE.Vector3(0, 0, 0);
		rot.setRotationFromMatrix(mx);
		double x=Math.toDegrees(rot.getX());
		double y=Math.toDegrees(rot.getY());
		double z=Math.toDegrees(rot.getZ());
		GWT.log(x+","+y+","+z);
		//jointMap.get("LeftUpLeg").setRotation(rot.getX(), rot.getY(), rot.getZ());
		//jointMap.get("LeftUpLeg").setRotation(Math.toRadians(23.1818),Math.toRadians(-34.8717),Math.toRadians(-20.0095));
		//jointMap.get("RightUpLeg").getRotation(Math.toRadians(23.1818),Math.toRadians(-34.8717),Math.toRadians(-20.0095));
		} catch (InvalidLineException e) {
			log(e.getMessage());
			e.printStackTrace();
		}
		*/
	//	ctime=System.currentTimeMillis();
		
		tabPanel.addSelectionHandler(new SelectionHandler<Integer>() {
			
			@Override
			public void onSelection(SelectionEvent<Integer> event) {
				int selection=event.getSelectedItem();
				if(selection==0){
					stats.setVisible(true);
					showControl();
					dialog2.setVisible(true);
				}else{
				stats.setVisible(false);
				hideControl();
				dialog2.setVisible(false);
				}
			}
		});
		new MergeTool(createTabVerticalPanel("Merge"));	
		new ThinTool(createTabVerticalPanel("ThinOut"));	
		new StripTool(createTabVerticalPanel("Strip"));	
		new MixTool(createTabVerticalPanel("Mix"));	
		new CalculateTool(createTabVerticalPanel("Calculate"));	
	}
	
	private VerticalPanel createTabVerticalPanel(String name){
		VerticalPanel panel=new VerticalPanel();
		tabPanel.add(panel,name);
		return panel;
	}
	
	private void doZYX(Object3D target,String lastOrder){
		Vector3 vec=target.getRotation();
		Matrix4 mx=THREE.Matrix4();
		mx.setRotationFromEuler(vec, lastOrder);
		
		vec.setRotationFromMatrix(mx);
	}
	/*
	private void doPose(BVH bvh,String line){
		String[] tmp=line.split(" ");
		double[] vs=BVHParser.toDouble(tmp);
		Object3D oldTarget=null;
		String lastOrder=null;
		for(int i=0;i<vs.length;i++){
			NameAndChannel nchannel=bvh.getNameAndChannels().get(i);
			lastOrder=nchannel.getOrder();
			Object3D target=jointMap.get(nchannel.getName());
			switch(nchannel.getChannel()){
			case Channels.XROTATION:
				target.getRotation().setX(Math.toRadians(vs[i]));
				
			break;
			case Channels.YROTATION:
				target.getRotation().setY(Math.toRadians(vs[i]));
			break;
			case Channels.ZROTATION:
				target.getRotation().setZ(Math.toRadians(vs[i]));
			break;
			}
			
			if(oldTarget!=null && oldTarget!=target){
				doZYX(oldTarget,lastOrder);
			}
			oldTarget=target;
		}
		doZYX(oldTarget,lastOrder);//do last one
	}*/
	
	private void doPose(BVH bvh,double[] vs){
		Object3D oldTarget=null;
		String lastOrder=null;
		for(int i=0;i<vs.length;i++){
			NameAndChannel nchannel=bvh.getNameAndChannels().get(i);
			lastOrder=nchannel.getOrder();
			Object3D target=jointMap.get(nchannel.getName());
			switch(nchannel.getChannel()){
			case Channels.XROTATION:
				target.getRotation().setX(Math.toRadians(vs[i]));
				
			break;
			case Channels.YROTATION:
				target.getRotation().setY(Math.toRadians(vs[i]));
			break;
			case Channels.ZROTATION:
				target.getRotation().setZ(Math.toRadians(vs[i]));
			break;
			case Channels.XPOSITION:
				if(translatePosition.getValue()){
				target.getPosition().setX(vs[i]);
				}else{
					target.getPosition().setX(0);	
				}
				break;
			case Channels.YPOSITION:
				if(translatePosition.getValue()){
				target.getPosition().setY(vs[i]);
				}else{
					target.getPosition().setY(0);	
				}
				break;
			case Channels.ZPOSITION:
				if(translatePosition.getValue()){
				target.getPosition().setZ(vs[i]);
				}else{
					target.getPosition().setZ(0);	
				}
				break;	
			}
			
			if(oldTarget!=null && oldTarget!=target){
				doZYX(oldTarget,lastOrder);
			}
			oldTarget=target;
		}
		doZYX(oldTarget,lastOrder);//do last one
	}
	
	private Map<String,Object3D> jointMap;
	
	public Mesh createLine(Vec3 from,Vec3 to){
		Geometry lineG = THREE.Geometry();
		lineG.vertices().push(THREE.Vertex(THREE.Vector3(from.getX(), from.getY(), from.getY())));
		lineG.vertices().push(THREE.Vertex(THREE.Vector3(to.getX(), to.getY(), to.getZ())));
		Mesh line=THREE.Line(lineG, THREE.LineBasicMaterial().color(0).build());
		return line;
	}
	
	private List<Object3D> bodyMeshs=new ArrayList<Object3D>();
	String tmp="";
	public void doJoint(Object3D parent,BVHNode pNode,BVHNode node){
		GWT.log(node.getName()+","+node.getOffset()+",endsite:"+node.getEndSite());
		GWT.log(node.getChannels().toString());
		
		Object3D group=THREE.Object3D();
		Mesh mesh=THREE.Mesh(THREE.CubeGeometry(.4,.4, .4), THREE.MeshLambertMaterial().color(0x00ff00).build());
		group.add(mesh);
		mesh.setName(node.getName());
		
		//initial position
		group.setPosition(THREE.Vector3(node.getOffset().getX(), node.getOffset().getY(), node.getOffset().getZ()));
		jointMap.put(node.getName(), group);
		
		//create half
		Vector3 half=group.getPosition().clone();
		if(half.getX()!=0 || half.getY()!=0 || half.getY()!=0){
		half.divideScalar(2);
		//Mesh hmesh=THREE.Mesh(THREE.CubeGeometry(.2,.2,.2), THREE.MeshLambertMaterial().color(0xffffff).build());
		Mesh hmesh=THREE.Mesh(THREE.CylinderGeometry(.1,.1,.2,6), THREE.MeshLambertMaterial().color(0xffffff).build());
		
		hmesh.setPosition(half);
		parent.add(hmesh);
		bodyMeshs.add(hmesh);
		
		
		BoxData data=boxDatas.get(pNode.getName());
		if(data!=null){
			hmesh.setScale(data.getScaleX(), data.getScaleY(), data.getScaleZ());
			hmesh.getRotation().setZ(Math.toRadians(data.getRotateZ()));
		}
		
		if(pNode!=null){//TODO remove
			tmp+=pNode.getName()+",1,1,1\n";
		}
		
		}
		
		//line
		Mesh l1=createLine(new Vec3(),node.getOffset());
		parent.add(l1);
		
		if(node.getEndSite()!=null){
			Mesh end=THREE.Mesh(THREE.CubeGeometry(.1, .1, .1), THREE.MeshBasicMaterial().color(0x008800).build());
			end.setPosition(THREE.Vector3(node.getEndSite().getX(), node.getEndSite().getY(), node.getEndSite().getZ()));
			group.add(end);
			Geometry lineG = THREE.Geometry();
			lineG.vertices().push(THREE.Vertex(THREE.Vector3(0, 0, 0)));
			lineG.vertices().push(THREE.Vertex(THREE.Vector3(node.getEndSite().getX(), node.getEndSite().getY(), node.getEndSite().getZ())));
			Mesh line=THREE.Line(lineG, THREE.LineBasicMaterial().color(0).build());
			group.add(line);
			
			Vector3 half2=end.getPosition().clone();
			if(half2.getX()!=0 || half2.getY()!=0 || half2.getY()!=0){
			half2.divideScalar(2);
			//Mesh hmesh=THREE.Mesh(THREE.CubeGeometry(.1,.1,.1), THREE.MeshLambertMaterial().color(0xffffff).build());
			Mesh hmesh=THREE.Mesh(THREE.CylinderGeometry(.1,.1,.2,6), THREE.MeshLambertMaterial().color(0xffffff).build());
			
			hmesh.setPosition(half2);
			group.add(hmesh);
			tmp+=node.getName()+",1,1,1\n";
			
			BoxData data=boxDatas.get(node.getName());
			if(data!=null){
				hmesh.setScale(data.getScaleX(), data.getScaleY(), data.getScaleZ());
			}
			if(node.getName().equals("Head")){
				hmesh.getPosition().setZ(hmesh.getPosition().getZ()+0.5);
			}
			bodyMeshs.add(hmesh);
			
			}
		}
		parent.add(group);
		List<BVHNode> joints=node.getJoints();
		if(joints!=null){
			for(BVHNode joint:joints){
			doJoint(group,node,joint);
			}
		}
		
	}
	

	private Label loadingLabel=new Label();
	private CheckBox translatePosition;
	private HTML5InputRange positionYRange;
	private HTML5InputRange meshScaleX;
	private HTML5InputRange meshScaleY;
	private HTML5InputRange meshScaleZ;
	private HTML5InputRange positionX;
	private HTML5InputRange positionY;
	private HTML5InputRange positionZ;
	private PopupPanel bottomPanel;
	protected boolean playing;
	private HTML5InputRange positionXRange;

	private HTML5InputRange positionZRange;

	private CheckBox drawBackground;

	private void loadBVH(String path){
		Benchmark.start("load");
		RequestBuilder builder = new RequestBuilder(RequestBuilder.GET, URL.encode(path));
		loadingLabel.setText("loading-data");
			try {
				builder.sendRequest(null, new RequestCallback() {
					
					@Override
					public void onResponseReceived(Request request, Response response) {
						
						String bvhText=response.getText();
						//log("loaded:"+Benchmark.end("load"));
						//useless spend allmost time with request and spliting.
						parseBVH(bvhText);
						loadingLabel.setText("");
					}
					
					
					

@Override
public void onError(Request request, Throwable exception) {
				Window.alert("load faild:");
}
				});
			} catch (RequestException e) {
				log(e.getMessage());
				e.printStackTrace();
			}
	}
	
	private void setEmptyBone(){
		if(boneRoot!=null){
			boneContainer.remove(boneRoot);
		}
		boneRoot=null;
		bvh=null;
	}
	private void parseBVH(String bvhText){
		final BVHParser parser=new BVHParser();
		jointMap=new HashMap<String,Object3D>();
		
		parser.parseAsync(bvhText, new ParserListener() {
			
			@Override
			public void onSuccess(BVH bv) {
				bvh=bv;
				bvh.setSkips(skipFrames);
				
				BVHNode node=bvh.getHiearchy();
				
				if(boneRoot!=null){
					boneContainer.remove(boneRoot);
				}
				boneRoot=THREE.Object3D();
				boneContainer.add(boneRoot);
				doJoint(boneRoot,null,node);
				GWT.log(tmp);
				int poseIndex=0;
				if(ignoreFirst.getValue()){
					poseIndex=1;
				}
				
				clock.update();
				updatePoseIndex(poseIndex);
				doPose(bvh,bvh.getFrameAt(poseIndex));
				currentFrameRange.setMax(bvh.getFrames()-1);
			}
			
			@Override
			public void onFaild(String message) {
				log(message);
			}
		});
	}
	
	/* timer style
	 * parser.initialize();
						bvhText=bvhText.replace("\r", "");
						final String lines[]=bvhText.split("\n");
						final int pLine=lines.length/20;
Timer timer=new Timer(){
							int index=0;
							boolean parsing;
							@Override
							public synchronized void  run() {
								if(parsing){
									return;
								}
								parsing=true;
								loadingLabel.setText(index+"/"+lines.length);
								GWT.log("called:"+index+","+pLine);
							try {
								parser.parseLines(lines, index, index+pLine);
								if(index>=lines.length){
									//done
									bvh=parser.getBvh();
									BVHNode node=bvh.getHiearchy();
									
									if(root!=null){
										scene.remove(root);
									}
									root=THREE.Object3D();
									scene.add(root);
									doLog(root,node);
									
									doPose(bvh,bvh.getMotion().getMotions().get(0));
									poseIndex=0;
									cancel();
								}else{
									index+=pLine;
								}
							} catch (InvalidLineException e) {
								log(e.getMessage());
							}
							
							parsing=false;
						
							
							}
							
					};
						timer.scheduleRepeating(20);
						*/
	
	private long getFrameTime(int index){
		long time=(long) (bvh.getFrameTime()*index*1000);
		return time;
	}

	private double getPlaySpeed(){
		String v=speedBox.getItemText(speedBox.getSelectedIndex());
		double r=1;
		try{
			r=Double.parseDouble(v.substring(2));
		}catch(Exception e){}
		return r;
	}
	
	private double playSpeed=1;
	private void createBottomPanel(){
		bottomPanel = new PopupPanel();
		bottomPanel.setVisible(true);
		bottomPanel.setSize("650px", "40px");
		VerticalPanel main=new VerticalPanel();
		bottomPanel.add(main);
		bottomPanel.show();
		
		
		HorizontalPanel upperPanel=new HorizontalPanel();
		upperPanel.setVerticalAlignment(HorizontalPanel.ALIGN_MIDDLE);
		main.add(upperPanel);
		upperPanel.add(new Label("Speed"));
		speedBox = new ListBox();
		speedBox.addItem("x 0.25");
		speedBox.addItem("x 0.5");
		speedBox.addItem("x 1");
		speedBox.addItem("x 2");
		speedBox.addItem("x 4");
		speedBox.addItem("x 10");
		speedBox.setSelectedIndex(2);
		speedBox.addChangeHandler(new ChangeHandler() {
			
			@Override
			public void onChange(ChangeEvent event) {
				playSpeed=getPlaySpeed();
			}
		});
		upperPanel.add(speedBox);
		
		
		abLoopCheck = new CheckBox("A/B Loop");
		upperPanel.add(abLoopCheck);
		
		
		
		final Button asA=new Button("A:");
		asA.setWidth("60px");
		upperPanel.add(asA);
		asA.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				aFrame=currentFrameRange.getValue();
				asA.setText("A:"+(aFrame+1));
			}
		});
		
		final Button asB=new Button("B:");
		asB.setWidth("60px");
		upperPanel.add(asB);
		asB.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				bFrame=currentFrameRange.getValue();
				asB.setText("B:"+(bFrame+1));
			}
		});
		
		upperPanel.add(new Label("Skip every frame:"));
		final TextBox skipFrameBox=new TextBox();
		skipFrameBox.setWidth("40px");
		upperPanel.add(skipFrameBox);
		
		Button updateSkipBt=new Button("Update skips");
		upperPanel.add(updateSkipBt);
		updateSkipBt.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				String v=skipFrameBox.getValue();
				
				int sp=0;
				try{
				sp=Integer.parseInt(v);
				}catch(Exception e){	
				}
				setBvhSkips(sp);
			}
		});
		
		
		HorizontalPanel pPanel=new HorizontalPanel();
		main.add(pPanel);
		pPanel.setVerticalAlignment(HorizontalPanel.ALIGN_MIDDLE);
		
		
		
		
		
		
		playing=true;
		final Button playButton=new Button("Play");
		playButton.setEnabled(false);
		final Button stopButton=new Button("Stop");
		
		playButton.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				clock.update();
				//ctime=System.currentTimeMillis()-getFrameTime();
				playing=true;
				playButton.setEnabled(false);
				stopButton.setEnabled(true);
				
			}
		});
		pPanel.add(playButton);
		
		
		stopButton.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				playing=false;
				playButton.setEnabled(true);
				stopButton.setEnabled(false);
			}
		});
		
		pPanel.add(stopButton);
		
		final Button prevButton=new Button("Prev");
		
		prevButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				int c=currentFrameRange.getValue();
				c--;
				/*
				if(c==0 && ignoreFirst.getValue()){
					c=1;
				}*/
				
				if(c<0){
					c=0;
				}
				currentFrameRange.setValue(c);
				updatePoseIndex(currentFrameRange.getValue());
			}
		});
		
		pPanel.add(prevButton);
		
		final Button nextButton=new Button("Next");
		nextButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				int c=currentFrameRange.getValue();
				c++;
				if(c>=bvh.getFrames()){
					c=bvh.getFrames()-1;
				}
				currentFrameRange.setValue(c);
				updatePoseIndex(currentFrameRange.getValue());
			}
		});
		
		pPanel.add(nextButton);
		
		
		
		
		currentFrameRange = new HTML5InputRange(0,1000,0);
		currentFrameRange.setWidth("420px");
		pPanel.add(currentFrameRange);
		
		currentFrameRange.addMouseUpHandler(new MouseUpHandler() {
			
			@Override
			public void onMouseUp(MouseUpEvent event) {
				
				updatePoseIndex(currentFrameRange.getValue());
			}
		});
		
		currentFrameLabel = new Label();
		pPanel.add(currentFrameLabel);
		
		super.leftBottom(bottomPanel);
	}
	private int aFrame;
	private int bFrame;
	
	
	private int skipFrames;

	private CheckBox drawMesh;
	private void setBvhSkips(int skips){
		//TODO
		//set global for newload
		skipFrames=skips;
		//set current bvh
		bvh.setSkips(skips);
		
		updatePoseIndex(0);
		currentFrameRange.setMax(bvh.getFrames()-1);
		//update labels
	}
	private JsArray<File> lastSelectedFiles;
	@Override
	public void createControl(Panel parent) {
		
		parent.add(loadingLabel);
		

		parent.add(new Label("Load BVH File"));
		FileUpload file=new FileUpload();
		
		file.setHeight("50px");
		file.getElement().setAttribute("multiple", "multiple");
		
		
		file.addChangeHandler(new ChangeHandler() {
			
			

			@Override
			public void onChange(ChangeEvent event) {
				//Benchmark.start("load");
				
				lastSelectedFiles = FileUtils.toFile(event.getNativeEvent());
				
				for(int i=0;i<lastSelectedFiles.length();i++){
					bvhFileList.add(lastSelectedFiles.get(i));
				}
				
				dataListCell.setDatas(bvhFileList);
				dataListCell.setSelection(lastSelectedFiles.get(0));
				//bvhCellList.setRowCount(bvhFileList.size(), true);
				//bvhCellList.setRowData(bvhFileList);
				
				//fileSelectionModel.setSelected(files.get(0), true);
				
				/*
				log("length:"+files.length());
				GWT.log(files.get(0).getFileName());
				GWT.log(files.get(0).getFileType());
				GWT.log(""+files.get(0).getFileSize());
				log(event.getNativeEvent());
				final FileReader reader=FileReader.createFileReader();
				reader.setOnLoad(new FileHandler() {
					@Override
					public void onLoad() {
						//log("load:"+Benchmark.end("load"));
						//GWT.log(reader.getResultAsString());
						parseBVH(reader.getResultAsString());
					}
				});
				reader.readAsText(files.get(0),"utf-8");
				*/
				
			}
		});
		parent.add(file);
		HorizontalPanel fileControl=new HorizontalPanel();
		fileControl.setVerticalAlignment(HorizontalPanel.ALIGN_MIDDLE);
		parent.add(fileControl);
		Button prevBt=new Button("Prev");
		prevBt.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				if(bvhFileList.size()==0){
					return;
				}
				File file=dataListCell.getSelection();
				int index=bvhFileList.indexOf(file);
				index--;
				if(index<0){
					index=bvhFileList.size()-1;
				}
				dataListCell.setSelection(bvhFileList.get(index));
			}
		});
		fileControl.add(prevBt);
		
		Button nextBt=new Button("Next");
		nextBt.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				doNextMotion();
			}
		});
		fileControl.add(nextBt);
		
		fileControl.add(new Label("Auto Play"));
		loopTime = new ListBox();
		loopTime.addItem("None");
		loopTime.addItem("1");
		loopTime.addItem("3");
		loopTime.addItem("10");
		loopTime.setSelectedIndex(0);
		fileControl.add(loopTime);
		
		
		file.setStylePrimaryName("fileborder");
		/*
		meshScaleX = new HTML5InputRange(0,150,3);
		parent.add(HTML5Builder.createRangeLabel("Scale-x", meshScaleX));
		parent.add(meshScaleX);
		
		meshScaleY = new HTML5InputRange(0,150,3);
		parent.add(HTML5Builder.createRangeLabel("Scale-y", meshScaleY));
		parent.add(meshScaleY);
		
		meshScaleZ = new HTML5InputRange(0,150,3);
		parent.add(HTML5Builder.createRangeLabel("Scale-z", meshScaleZ));
		parent.add(meshScaleZ);
		
		positionX = new HTML5InputRange(-150,150,0);
		parent.add(HTML5Builder.createRangeLabel("Position-x", positionX));
		parent.add(positionX);
		
		positionY = new HTML5InputRange(-150,150,0);
		parent.add(HTML5Builder.createRangeLabel("Position-y", positionY));
		parent.add(positionY);
		
		positionZ = new HTML5InputRange(-150,150,0);
		parent.add(HTML5Builder.createRangeLabel("Position-z", positionZ));
		parent.add(positionZ);
		*/
		
		
		dataListCell = new DataListCell<File>(new DataListRenderer<File>(){
			@Override
			public Widget createWidget(File data,DataListCell<File> dataList) {

				return new BVHFileWidget(data,dataList);
			}});
		dataListCell.setHeight("60px");
		parent.add(dataListCell);
		dataListCell.setListener(new ChangeSelectionListener<File>() {
			@Override
			public void onChangeSelection(File data) {
				final FileReader reader=FileReader.createFileReader();
				reader.setOnLoad(new FileHandler() {
					@Override
					public void onLoad() {
						parseBVH(reader.getResultAsString());
					}
				});
				reader.readAsText(data,"utf-8");
			}
		});
		HorizontalPanel dataControls=new HorizontalPanel();
		parent.add(dataControls);
		Button remove=new Button("Remove");
		remove.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				if(bvhFileList.size()==0){
					return;
				}
				File file=dataListCell.getSelection();
				int index=bvhFileList.indexOf(file);
				bvhFileList.remove(file);
				if(index>=bvhFileList.size()){
					index=0;
				}
				dataListCell.setDatas(bvhFileList);
				if(bvhFileList.size()!=0){
				dataListCell.setSelection(bvhFileList.get(index));
				}else{
					setEmptyBone();
				}
			}
		});
		dataControls.add(remove);
		
		Button removeAll=new Button("Remove All");
		removeAll.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				if(bvhFileList.size()==0){
					return;
				}
				bvhFileList.clear();
				dataListCell.setDatas(bvhFileList);
				setEmptyBone();
				
			}
		});
		dataControls.add(removeAll);
		
		Button reload=new Button("Reload");
		reload.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				
				doReload();
				
			}
		});
		dataControls.add(reload);
		
		
		
		drawMesh = new CheckBox("Draw Body Mesh");
		parent.add(drawMesh);
		drawMesh.setValue(true);
		drawMesh.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				doChangeVisibleBodyMesh();
			}
		});
		
		drawBackground = new CheckBox("Draw Background");
		parent.add(drawBackground);
		drawBackground.setValue(true);
		
		
		translatePosition = new CheckBox("Translate Position");
		parent.add(translatePosition);
		translatePosition.setValue(true);
		translatePosition.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				updatePoseIndex(currentFrameRange.getValue());
			}
		});
		
		ignoreFirst = new CheckBox("Ignore First Frame(Usually Pose)");
		ignoreFirst.setValue(true);
		parent.add(ignoreFirst);
		
		
		
		HorizontalPanel h1=new HorizontalPanel();
		
		rotationRange = new HTML5InputRange(-180,180,0);
		parent.add(HTML5Builder.createRangeLabel("X-Rotate:", rotationRange));
		parent.add(h1);
		h1.add(rotationRange);
		Button reset=new Button("Reset");
		reset.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				rotationRange.setValue(0);
			}
		});
		h1.add(reset);
		
		HorizontalPanel h2=new HorizontalPanel();
		
		rotationYRange = new HTML5InputRange(-180,180,0);
		parent.add(HTML5Builder.createRangeLabel("Y-Rotate:", rotationYRange));
		parent.add(h2);
		h2.add(rotationYRange);
		Button reset2=new Button("Reset");
		reset2.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				rotationYRange.setValue(0);
			}
		});
		h2.add(reset2);
		
		
		HorizontalPanel h3=new HorizontalPanel();
		rotationZRange = new HTML5InputRange(-180,180,0);
		parent.add(HTML5Builder.createRangeLabel("Z-Rotate:", rotationZRange));
		parent.add(h3);
		h3.add(rotationZRange);
		Button reset3=new Button("Reset");
		reset3.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				rotationZRange.setValue(0);
			}
		});
		h3.add(reset3);
		
		HorizontalPanel h4=new HorizontalPanel();
		positionXRange = new HTML5InputRange(-50,50,0);
		parent.add(HTML5Builder.createRangeLabel("X-Position:", positionXRange));
		parent.add(h4);
		h4.add(positionXRange);
		Button reset4=new Button("Reset");
		reset4.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				positionXRange.setValue(0);
			}
		});
		h4.add(reset4);
		
		HorizontalPanel h5=new HorizontalPanel();
		positionYRange = new HTML5InputRange(-50,50,0);
		parent.add(HTML5Builder.createRangeLabel("Y-Position:", positionYRange));
		parent.add(h5);
		h5.add(positionYRange);
		Button reset5=new Button("Reset");
		reset5.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				positionYRange.setValue(0);
			}
		});
		h5.add(reset5);
		
		HorizontalPanel h6=new HorizontalPanel();
		positionZRange = new HTML5InputRange(-50,50,0);
		parent.add(HTML5Builder.createRangeLabel("Z-Position:", positionZRange));
		parent.add(h6);
		h6.add(positionZRange);
		Button reset6=new Button("Reset");
		reset6.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				positionZRange.setValue(0);
			}
		});
		h6.add(reset6);
		
		
		createBottomPanel();
		showControl();
	}

	protected void doReload() {
		LogUtils.log(lastSelectedFiles);
		if(lastSelectedFiles==null){
			return;
		}
		//bvhFileList.clear();
		for(int i=0;i<lastSelectedFiles.length();i++){
			bvhFileList.add(lastSelectedFiles.get(i));
		}
		
		dataListCell.setDatas(bvhFileList);
		dataListCell.setSelection(lastSelectedFiles.get(0));
	}

	protected void doChangeVisibleBodyMesh() {
		for(Object3D object:bodyMeshs){
			object.setVisible(drawMesh.getValue());
		}
	}

	private void doNextMotion(){
		if(bvhFileList.size()==0){
			return;
		}
		File file=dataListCell.getSelection();
		int index=bvhFileList.indexOf(file);
		index++;
		if(index>bvhFileList.size()-1){
			index=0;
		}
		dataListCell.setSelection(bvhFileList.get(index));
	}
	
	protected void doLoad(String itemText) {
		String[] g_n=itemText.split("_");
		loadBVH("bvhs/"+g_n[0]+"/"+itemText+".bvh");
	}

	private HTML5InputRange rotationRange;
	private HTML5InputRange rotationYRange;
	private HTML5InputRange rotationZRange;

	Object3D boneRoot;
	
	
	private BVH bvh;
	private CheckBox ignoreFirst;
	
	//private long ctime;
	private HTML5InputRange currentFrameRange;
	private Label currentFrameLabel;
	//private int poseIndex;
	
	private void updatePoseIndex(int index){
		//poseIndex=index;
		currentFrameRange.setValue(index);
		currentFrameLabel.setText((index+1)+"/"+bvh.getFrames());
		doPose(bvh,bvh.getFrameAt(index));
	}
	
	Clock clock=new Clock();
	private List<File> bvhFileList=new ArrayList<File>();
	//private CellList<File> bvhCellList;
	//private SingleSelectionModel<File> fileSelectionModel;
	private DataListCell<File> dataListCell;

	
	private int currentLoop=0;
	private ListBox loopTime;

	private ListBox speedBox;
	private long remainTime;

	private CheckBox abLoopCheck;
	@Override
	protected void beforeUpdate(WebGLRenderer renderer) {
		
		
		//camera.getPosition().incrementX((mouseX - camera.getPosition().getX() ) * 0.05);
		//camera.getPosition().incrementY((-mouseY - camera.getPosition().getY() ) * 0.05);
		
		//validate ab-check
		boolean abLoop=abLoopCheck.getValue();
		if(abLoop && bvh!=null){
			if(aFrame>=bFrame){
				abLoopCheck.setValue(false);
			}
			if(aFrame>bvh.getFrames()-1 || bFrame>bvh.getFrames()-1){
				abLoopCheck.setValue(false);
			}
		}
		
		
		Object3DUtils.setVisibleAll(backgroundContainer, drawBackground.getValue());
		//backgroundContainer.setVisible();
		
		boneContainer.setPosition(positionXRange.getValue(), positionYRange.getValue(), positionZRange.getValue());
		
		if(rootGroup!=null){
		rootGroup.getRotation().set(Math.toRadians(rotationRange.getValue()),Math.toRadians(rotationYRange.getValue()),Math.toRadians(rotationZRange.getValue()));
		}
		
		if(bvh!=null){
			if(playing){
				long last=clock.delta()+remainTime;
				double ftime=(bvh.getFrameTime()*1000/playSpeed);
				
				if(ftime==0){
					return; //somehow frame become strange
				}
				int frame=(int) (last/ftime);
				remainTime=(long) (last-(ftime*frame));
			//	log(""+frame);
				//GWT.log(ftime+","+frame+","+remainTime);
			int minFrame=0;
			int maxFrame=bvh.getFrames();
			
			if(abLoop){
				minFrame=aFrame;
				maxFrame=bFrame;
			}
			
			if(frame>0){
			int index=currentFrameRange.getValue()+frame;
			
			
			
			boolean overLoop=index>=maxFrame;
			
			index=(index-minFrame)%(maxFrame-minFrame);
			index+=minFrame;
			
			if(ignoreFirst.getValue() && index==0 &&minFrame==0){
				index=1;
			}
			
			updatePoseIndex(index);	
			
			
			if(overLoop && bvhFileList.size()>1 && !abLoop){
				//next Frame
				try{
				int maxLoop=Integer.parseInt(loopTime.getItemText(loopTime.getSelectedIndex()));
				//log("maxloop:"+maxLoop);
				if(maxLoop>0){
					currentLoop++;
					if(currentLoop>=maxLoop){
						currentLoop=0;
						doNextMotion();
					}
				}
				
				}catch(Exception e){}
			}
			
			
			
			
				
			}
		/*		
		double delta=(double)(System.currentTimeMillis()-ctime)/1000;
		delta%=bvh.getMotion().getDuration();
		poseIndex = (int) (delta/bvh.getMotion().getFrameTime());
		*/
			}
	
		}
	}
	
	@Override
	public void resized(int width, int height) {
		super.resized(width, height);
		leftBottom(bottomPanel);
	}
	@Override
	public String getHtml(){
		return super.getHtml()+". Sample BVH File from <a href='https://sites.google.com/a/cgspeed.com/cgspeed/motion-capture/cmu-bvh-conversion'>CMU Graphics Lab Motion Capture Database.</a><br/> More Infomation click <a href='http://webgl.akjava.com'>webgl.akjava.com</a>";
	}

	@Override
	public String getTabTitle() {
		return "BVH M-Creator";
	}
}