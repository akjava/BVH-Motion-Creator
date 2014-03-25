package com.akjava.gwt.bvhtools.client;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.akjava.bvh.client.BVH;
import com.akjava.bvh.client.BVHMotion;
import com.akjava.bvh.client.BVHNode;
import com.akjava.bvh.client.BVHParser;
import com.akjava.bvh.client.BVHParser.InvalidLineException;
import com.akjava.bvh.client.BVHParser.ParserListener;
import com.akjava.bvh.client.BVHWriter;
import com.akjava.bvh.client.Channels;
import com.akjava.bvh.client.NameAndChannel;
import com.akjava.bvh.client.Vec3;
import com.akjava.gwt.bvh.client.BoxData;
import com.akjava.gwt.bvh.client.BoxDataParser;
import com.akjava.gwt.bvh.client.poseframe.PoseEditorData;
import com.akjava.gwt.bvh.client.poseframe.PoseFrameData;
import com.akjava.gwt.bvh.client.threejs.AnimationBoneConverter;
import com.akjava.gwt.bvh.client.threejs.BVHConverter;
import com.akjava.gwt.bvhtools.client.file.BVHDataContainer;
import com.akjava.gwt.bvhtools.client.file.BVHDataListener;
import com.akjava.gwt.bvhtools.client.file.FileDataContainer;
import com.akjava.gwt.bvhtools.client.file.TextDataContainer;
import com.akjava.gwt.bvhtools.client.player.list.BVHFileWidget;
import com.akjava.gwt.bvhtools.client.player.list.DataListCell;
import com.akjava.gwt.bvhtools.client.player.list.DataListCell.ChangeSelectionListener;
import com.akjava.gwt.bvhtools.client.player.list.DataListCell.DataListRenderer;
import com.akjava.gwt.bvhtools.client.player.resources.Bundles;
import com.akjava.gwt.bvhtools.client.tools.CalculateTool;
import com.akjava.gwt.bvhtools.client.tools.MergeTool;
import com.akjava.gwt.bvhtools.client.tools.MixTool;
import com.akjava.gwt.bvhtools.client.tools.StripTool;
import com.akjava.gwt.bvhtools.client.tools.TextTool;
import com.akjava.gwt.bvhtools.client.tools.ThinTool;
import com.akjava.gwt.html5.client.InputRangeWidget;
import com.akjava.gwt.html5.client.extra.HTML5Builder;
import com.akjava.gwt.html5.client.file.File;
import com.akjava.gwt.html5.client.file.FileUploadForm;
import com.akjava.gwt.html5.client.file.FileUtils;
import com.akjava.gwt.html5.client.file.ui.DropVerticalPanelBase;
import com.akjava.gwt.lib.client.IStorageControler;
import com.akjava.gwt.lib.client.LogUtils;
import com.akjava.gwt.lib.client.StorageControler;
import com.akjava.gwt.lib.client.StorageException;
import com.akjava.gwt.three.client.gwt.animation.AnimationBone;
import com.akjava.gwt.three.client.gwt.animation.AnimationBonesData;
import com.akjava.gwt.three.client.gwt.core.Intersect;
import com.akjava.gwt.three.client.gwt.materials.LineBasicMaterialParameter;
import com.akjava.gwt.three.client.gwt.materials.MeshBasicMaterialParameter;
import com.akjava.gwt.three.client.gwt.materials.MeshLambertMaterialParameter;
import com.akjava.gwt.three.client.java.JClock;
import com.akjava.gwt.three.client.java.ui.SimpleTabDemoEntryPoint;
import com.akjava.gwt.three.client.java.utils.Object3DUtils;
import com.akjava.gwt.three.client.js.THREE;
import com.akjava.gwt.three.client.js.core.Geometry;
import com.akjava.gwt.three.client.js.core.Object3D;
import com.akjava.gwt.three.client.js.core.Projector;
import com.akjava.gwt.three.client.js.lights.Light;
import com.akjava.gwt.three.client.js.math.Euler;
import com.akjava.gwt.three.client.js.math.Vector3;
import com.akjava.gwt.three.client.js.objects.Line;
import com.akjava.gwt.three.client.js.objects.Mesh;
import com.akjava.gwt.three.client.js.renderers.WebGLRenderer;
import com.akjava.lib.common.utils.Benchmark;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.DoubleClickEvent;
import com.google.gwt.event.dom.client.DropEvent;
import com.google.gwt.event.dom.client.DropHandler;
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
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Frame;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.TabLayoutPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */

public class BVHTools extends SimpleTabDemoEntryPoint {
	private String version="5.0.1(for r66)";
	public static DateTimeFormat dateFormat=DateTimeFormat.getFormat("yy/MM/dd HH:mm");
	private static BVHTools bvhTools;
	public static BVHTools getInstance(){
		return bvhTools;
	}
	final Projector projector=THREE.Projector();
	@Override
	public void onMouseClick(ClickEvent event) {
		/* TODO future
		int x=event.getX();
		int y=event.getY();
		
		
		JsArray<Intersect> intersects=projector.gwtPickIntersects(event.getX(), event.getY(), screenWidth, screenHeight, camera,scene);
		
		for(int i=0;i<intersects.length();i++){
			Intersect sect=intersects.get(i);
			break;
		}
		*/
		
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
	private VerticalPanel datasPanel;
	private IStorageControler storageControler;
	@Override
	public void initializeOthers(WebGLRenderer renderer) {
		LogUtils.log("BVHTools version:"+version);
		loadDefaultBVH("pose.bvh"); //i forgot what is this?maybe for some slow network
		storageControler = new StorageControler();
		bvhTools=this;
		cameraY=10;
		defaultZoom=5;
		canvas.setClearColor(0xcccccc);
		
		
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
		mesh.getRotation().set(Math.toRadians(-90), 0, 0,Euler.XYZ);
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
		
		toolsPanel = new TabLayoutPanel(24,Unit.PX);
		tabPanel.add(toolsPanel,"Tools");
		
		createDataPanel();
		createExperimentalPanel();
		
		new MergeTool(createTabVerticalPanel("Merge"));	
		new ThinTool(createTabVerticalPanel("ThinOut"));	
		new StripTool(createTabVerticalPanel("Strip"));	
		new MixTool(createTabVerticalPanel("Mix"));	
		new CalculateTool(createTabVerticalPanel("Calculate"));	
		new TextTool(createTabVerticalPanel("Text"));	
		
		updateDatasPanel();
	}
	
	private void createExperimentalPanel(){
		VerticalPanel expRoot=new VerticalPanel();
		tabPanel.add(expRoot,"Experimental");
		
		
		Frame doc1=new Frame("pose_help.html");
		doc1.setSize("500px", "200px");
		Button bt1=new Button("Open Pose Editor");
		bt1.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				Window.open("pose.html", "posetool", null);
			}
		});
		expRoot.add(doc1);
		expRoot.add(bt1);
		
		Frame doc=new Frame("weight_help.html");
		doc.setSize("500px", "200px");
		Button bt=new Button("Open Model Weight Tool");
		bt.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				Window.open("weight.html", "weighttool", null);
			}
		});
		expRoot.add(doc);
		expRoot.add(bt);
	}
	
	private void createDataPanel(){
		VerticalPanel datasRoot=new VerticalPanel();
		
		tabPanel.add(datasRoot,"Datas");
datasPanel = new VerticalPanel();
		
		//datasPanel.setStyleName("debug");
		ScrollPanel scroll=new ScrollPanel(datasPanel);
		scroll.setSize("550px", "400px");
		
		HorizontalPanel control=new HorizontalPanel();
		datasRoot.add(control);
		Button load=new Button("Load Checked Datas");
		control.add(load);
		load.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				List<BVHDataContainer> datas=new ArrayList<BVHDataContainer>();
				int size=datasPanel.getWidgetCount();
				for(int i=0;i<size;i++){
					Widget w=datasPanel.getWidget(i);
					if(w instanceof DataPanel){
						DataPanel panel=(DataPanel)w;
						if(panel.isChecked()){
						TextDataContainer dataContainer=new TextDataContainer(panel.getName(), convertPoseEditorDataToBVH(panel.getPoseEditorData(),bvhForData));
						datas.add(dataContainer);
						}
					}
				}
				
				addBVHDatas(datas);
			}
		});
		
		Button check=new Button("Check All");
		control.add(check);
		check.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				int size=datasPanel.getWidgetCount();
				for(int i=0;i<size;i++){
					Widget w=datasPanel.getWidget(i);
					if(w instanceof DataPanel){
						DataPanel panel=(DataPanel)w;
						panel.setChecked(true);
					}
				}
			}
		});
		
		Button uncheck=new Button("Uncheck All");
		control.add(uncheck);
		uncheck.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				int size=datasPanel.getWidgetCount();
				for(int i=0;i<size;i++){
					Widget w=datasPanel.getWidget(i);
					if(w instanceof DataPanel){
						DataPanel panel=(DataPanel)w;
						panel.setChecked(false);
					}
				}
			}
		});
		
		Button updateList=new Button("Update List");
		control.add(updateList);
		updateList.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				updateDatasPanel();	
			}
		});
		datasRoot.add(scroll);
	}
	

	
	private void updateDatasPanel(){
		try{
		datasPanel.clear();
		int index=storageControler.getValue(PoseEditorData.KEY_INDEX, 0);
		for(int i=index;i>=0;i--){
			String b64=storageControler.getValue(PoseEditorData.KEY_IMAGE+i,null);
			String json=storageControler.getValue(PoseEditorData.KEY_DATA+i,null);
			String head=storageControler.getValue(PoseEditorData.KEY_HEAD+i,null);
			if(b64!=null && json!=null){
			DataPanel dp=new DataPanel(i,head,b64,json);
			//dp.setSize("200px", "200px");
			datasPanel.add(dp);
			}
		}
		}catch (StorageException e) {
			Window.alert("faild getValue:"+e.getMessage());
		}
	}
	
	public class DataPanel extends HorizontalPanel{
		private int index;
		private String name;
		private long cdate;
		private String json;
		private CheckBox check;
		public DataPanel(final int ind,String head,String base64, String text){
			json=text;
			this.index=ind;
			Image img=new Image();
			img.setUrl(base64);
			
			
			String name_cdate[]=head.split("\t");
			name=name_cdate[0];
			cdate=(long)(Double.parseDouble(name_cdate[1]));
			
			String dlabel=dateFormat.format(new Date(cdate));
			add(new Label(dlabel));
			add(img);
			
			final Label nameLabel=new Label(name);
			nameLabel.setWidth("160px");
			add(nameLabel);
			
			check = new CheckBox();
			add(check);
			
			Button loadBt=new Button("Load");
			add(loadBt);
			loadBt.addClickHandler(new ClickHandler() {
				
				@Override
				public void onClick(ClickEvent event) {
					
					PoseEditorData ped=getPoseEditorData();
					
					
					if(ped!=null){
					doLoadPoseEditorData(ped);
					}else{
						//TODO error catch
						Window.alert("load faild");
					}
				}
			});
			
		
			
			
			
			
			
		}
		public void setChecked(boolean bool){
			check.setValue(bool);
		}
		public PoseEditorData getPoseEditorData(){
			PoseEditorData ped=PoseEditorData.readData(json);
			if(ped!=null){
				ped.setFileId(index);
			}
			return ped;
		}
		public boolean isChecked(){
			return check.getValue();
		}
		
		public String getJson(){
			return json;
		}
		public String getName(){
			return name;
		}
		
	
		protected void doLoadPoseEditorData(PoseEditorData ped) {
			
			
			
			//parseBVH();
			
			TextDataContainer dataContainer=new TextDataContainer(name, convertPoseEditorDataToBVH(ped,bvhForData));
			addBVHData(dataContainer);
			
		}
		
		
	}
	
	private VerticalPanel createTabVerticalPanel(String name){
		VerticalPanel panel=new VerticalPanel();
		toolsPanel.add(panel,name);
		return panel;
	}
	
	private void doRotation(Object3D target,String lastOrder){
		//log(target.getName()+",order="+lastOrder+" "+ThreeLog.get(GWTThreeUtils.radiantToDegree(target.getRotation())));
		target.getRotation().setOrder(lastOrder);
		/*
		Matrix4 mx=THREE.Matrix4();
		mx.setRotationFromEuler(vec, lastOrder);
		vec.setRotationFromMatrix(mx);//in this here,miss rotation because of over 90?
		*/
		
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
	
	public static String  convertPoseEditorDataToBVH(PoseEditorData ped,BVH bvh){
		AnimationBoneConverter boneConverter=new AnimationBoneConverter();
		JsArray<AnimationBone> bones=boneConverter.convertJsonBone(bvh);
		AnimationBonesData ab=new AnimationBonesData(bones, null);
		ped.updateMatrix(ab);//current-bone
		
		BVH exportBVH=new BVH();
		
		BVHConverter converter=new BVHConverter();
		BVHNode node=converter.convertBVHNode(bones);
		
		exportBVH.setHiearchy(node);
		
		converter.setChannels(node,0,"XYZ");	//TODO support other order
		
		
		BVHMotion motion=new BVHMotion();
		motion.setFrameTime(.25);
		
		for(PoseFrameData pose:ped.getPoseFrameDatas()){
			double[] values=converter.angleAndMatrixsToMotion(pose.getAngleAndMatrixs(),BVHConverter.ROOT_POSITION_ROTATE_ONLY,"XYZ");
			motion.add(values);
		}
		motion.setFrames(motion.getMotions().size());//
		
		exportBVH.setMotion(motion);
		//log("frames:"+exportBVH.getFrames());
		BVHWriter writer=new BVHWriter();
		
		String bvhText=writer.writeToString(exportBVH);
		return bvhText;
	}
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
				doRotation(oldTarget,lastOrder);
			}
			oldTarget=target;
		}
		doRotation(oldTarget,lastOrder);//do last one
	}
	
	private Map<String,Object3D> jointMap;
	
	public Line createLine(Vec3 from,Vec3 to){
		Geometry lineG = THREE.Geometry();
		lineG.vertices().push(THREE.Vector3(from.getX(), from.getY(), from.getY()));
		lineG.vertices().push(THREE.Vector3(to.getX(), to.getY(), to.getZ()));
		Line line=THREE.Line(lineG, THREE.LineBasicMaterial(LineBasicMaterialParameter.create().color(0)));
		return line;
	}
	
	private List<Object3D> bodyMeshs=new ArrayList<Object3D>();
	String tmp="";
	public void doJoint(Object3D parent,BVHNode pNode,BVHNode node){
		GWT.log(node.getName()+","+node.getOffset()+",endsite:"+node.getEndSite());
		GWT.log(node.getChannels().toString());
		
		Object3D group=THREE.Object3D();
		Mesh mesh=THREE.Mesh(THREE.CubeGeometry(.4,.4, .4), THREE.MeshLambertMaterial(MeshLambertMaterialParameter.create().color(0x00ff00)));
		group.add(mesh);
		mesh.setName(node.getName());
		group.setName(node.getName());
		//initial position
		group.setPosition(THREE.Vector3(node.getOffset().getX(), node.getOffset().getY(), node.getOffset().getZ()));
		jointMap.put(node.getName(), group);
		
		//create half
		Vector3 half=group.getPosition().clone();
		if(half.getX()!=0 || half.getY()!=0 || half.getY()!=0){
		half.divideScalar(2);
		//Mesh hmesh=THREE.Mesh(THREE.CubeGeometry(.2,.2,.2), THREE.MeshLambertMaterial().color(0xffffff).build());
		Mesh hmesh=THREE.Mesh(THREE.CylinderGeometry(.1,.1,.2,6), THREE.MeshLambertMaterial(MeshLambertMaterialParameter.create().color(0xffffff)));
		
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
		Line l1=createLine(new Vec3(),node.getOffset());
		parent.add(l1);
		
		if(node.getEndSite()!=null){
			Mesh end=THREE.Mesh(THREE.CubeGeometry(.1, .1, .1), THREE.MeshBasicMaterial(MeshBasicMaterialParameter.create().color(0x008800)));
			end.setPosition(THREE.Vector3(node.getEndSite().getX(), node.getEndSite().getY(), node.getEndSite().getZ()));
			group.add(end);
			Geometry lineG = THREE.Geometry();
			lineG.vertices().push(THREE.Vector3(0, 0, 0));
			lineG.vertices().push(THREE.Vector3(node.getEndSite().getX(), node.getEndSite().getY(), node.getEndSite().getZ()));
			Line line=THREE.Line(lineG, THREE.LineBasicMaterial(LineBasicMaterialParameter.create().color(0)));
			group.add(line);
			
			Vector3 half2=end.getPosition().clone();
			if(half2.getX()!=0 || half2.getY()!=0 || half2.getY()!=0){
			half2.divideScalar(2);
			//Mesh hmesh=THREE.Mesh(THREE.CubeGeometry(.1,.1,.1), THREE.MeshLambertMaterial().color(0xffffff).build());
			Mesh hmesh=THREE.Mesh(THREE.CylinderGeometry(.1,.1,.2,6), THREE.MeshLambertMaterial(MeshLambertMaterialParameter.create().color(0xffffff)));
			
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
	private InputRangeWidget positionYRange;
	private InputRangeWidget meshScaleX;
	private InputRangeWidget meshScaleY;
	private InputRangeWidget meshScaleZ;
	private InputRangeWidget positionX;
	private InputRangeWidget positionY;
	private InputRangeWidget positionZ;
	private PopupPanel bottomPanel;
	protected boolean playing;
	private InputRangeWidget positionXRange;

	private InputRangeWidget positionZRange;

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
				LogUtils.log(e.getMessage());
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
	
	//TODO can choose in preference
	private BVH bvhForData;
	private void loadDefaultBVH(final String path){
		RequestBuilder builder = new RequestBuilder(RequestBuilder.GET, URL.encode(path));
			try {
				builder.sendRequest(null, new RequestCallback() {
					
					@Override
					public void onResponseReceived(Request request, Response response) {
						
						String bvhText=response.getText();
						BVHParser parser=new BVHParser();
						try {
							bvhForData=	parser.parse(bvhText);
						} catch (InvalidLineException e) {
							LogUtils.log("invalid bvh:"+e.getMessage());
							e.printStackTrace();
						}
					}
					
					
					

@Override
public void onError(Request request, Throwable exception) {
				Window.alert("load faild:"+path);
}
				});
			} catch (RequestException e) {
				LogUtils.log(e.getMessage());
				e.printStackTrace();
			}
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
				//GWT.log(tmp);
				int poseIndex=0;
				GWT.log("f-size:"+bvh.getFrames());
				if(ignoreFirst.getValue() && bvh.getFrames()>1){
					poseIndex=1;
				}
				
				
				clock.update();
				updatePoseIndex(poseIndex);
				doPose(bvh,bvh.getFrameAt(poseIndex));
				currentFrameRange.setMax(bvh.getFrames()-1);
			}
			
			@Override
			public void onFaild(String message) {
				LogUtils.log(message);
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
		
		
		
		
		currentFrameRange = InputRangeWidget.createInputRange(0,1000,0);
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
	
	private BVHDataListener bvhDataListener;
	@Override
	public void createControl(DropVerticalPanelBase parent) {
		bvhDataListener=new BVHDataListener() {
			
			@Override
			public void dataLoaded(String text) {
				parseBVH(text);
			}
		};
		parent.add(loadingLabel);
		
		
		
		
		

		parent.add(new Label("Load BVH File"));
		final FileUploadForm file=new FileUploadForm();
		
		file.getFileUpload().setHeight("50px");
		file.getFileUpload().getElement().setAttribute("multiple", "multiple");
		
		
		parent.addDropHandler(new DropHandler() {
			@Override
			public void onDrop(DropEvent event) {
				event.preventDefault();
				//final FileReader reader=FileReader.createFileReader();
				lastSelectedFiles=FileUtils.transferToFile(event.getNativeEvent());
				
				FileDataContainer firstOne=null;
				for(int i=0;i<lastSelectedFiles.length();i++){
					FileDataContainer container=new FileDataContainer(lastSelectedFiles.get(i));
					if(i==0){
						firstOne=container;
					}
					bvhFileList.add(container);
				}
				
				dataListCell.setDatas(bvhFileList);
				dataListCell.setSelection(firstOne);
				file.reset();
			}
		});
		
		file.getFileUpload().addChangeHandler(new ChangeHandler() {
			@Override
			public void onChange(ChangeEvent event) {
				//Benchmark.start("load");
				
				lastSelectedFiles = FileUtils.toFile(event.getNativeEvent());
				
				FileDataContainer firstOne=null;
				for(int i=0;i<lastSelectedFiles.length();i++){
					FileDataContainer container=new FileDataContainer(lastSelectedFiles.get(i));
					if(i==0){
						firstOne=container;
					}
					bvhFileList.add(container);
				}
				
				dataListCell.setDatas(bvhFileList);
				dataListCell.setSelection(firstOne);
				file.reset();
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
				BVHDataContainer file=dataListCell.getSelection();
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
		
		
		dataListCell = new DataListCell<BVHDataContainer>(new DataListRenderer<BVHDataContainer>(){
			@Override
			public Widget createWidget(BVHDataContainer data,DataListCell<BVHDataContainer> dataList) {

				return new BVHFileWidget(data,dataList);
			}});
		dataListCell.setHeight("60px");
		parent.add(dataListCell);
		dataListCell.setListener(new ChangeSelectionListener<BVHDataContainer>() {
			@Override
			public void onChangeSelection(BVHDataContainer data) {
				
				/*
				final FileReader reader=FileReader.createFileReader();
				reader.setOnLoad(new FileHandler() {
					@Override
					public void onLoad() {
						parseBVH(reader.getResultAsString());
					}
				});
				reader.readAsText(data,"utf-8");
				*/
				data.readText(bvhDataListener);
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
				BVHDataContainer file=dataListCell.getSelection();
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
		
		rotationRange = InputRangeWidget.createInputRange(-180,180,0);
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
		
		rotationYRange = InputRangeWidget.createInputRange(-180,180,0);
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
		rotationZRange = InputRangeWidget.createInputRange(-180,180,0);
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
		positionXRange = InputRangeWidget.createInputRange(-50,50,0);
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
		positionYRange = InputRangeWidget.createInputRange(-50,50,0);
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
		positionZRange = InputRangeWidget.createInputRange(-50,50,0);
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
		
		Button launchPose=new Button("Launch Pose Editor");
		parent.add(launchPose);
		launchPose.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				Window.open("pose.html", "posetool", null);
			}
		});
		
		createBottomPanel();
		showControl();
	}

	protected void doReload() {
		LogUtils.log(lastSelectedFiles);
		if(lastSelectedFiles==null){
			return;
		}
		//bvhFileList.clear();
		FileDataContainer firstOne=null;
		for(int i=0;i<lastSelectedFiles.length();i++){
			FileDataContainer container=new FileDataContainer(lastSelectedFiles.get(i));
			if(i==0){
				firstOne=container;
			}
			bvhFileList.add(container);
		}
		
		dataListCell.setDatas(bvhFileList);
		dataListCell.setSelection(firstOne);
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
		BVHDataContainer file=dataListCell.getSelection();
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

	private InputRangeWidget rotationRange;
	private InputRangeWidget rotationYRange;
	private InputRangeWidget rotationZRange;

	Object3D boneRoot;
	
	
	private BVH bvh;
	private CheckBox ignoreFirst;
	
	//private long ctime;
	private InputRangeWidget currentFrameRange;
	private Label currentFrameLabel;
	//private int poseIndex;
	
	private void updatePoseIndex(int index){
		if(index>=bvh.getFrames()){
			if(bvh.getFrames()!=1){//1 frame pose usually happen
				LogUtils.log("invalid frame at:"+index+" of "+bvh.getFrames());
			}
			return;
		}
		//poseIndex=index;
		currentFrameRange.setValue(index);
		currentFrameLabel.setText((index+1)+"/"+bvh.getFrames());
		doPose(bvh,bvh.getFrameAt(index));
	}
	
	JClock clock=new JClock();
	private List<BVHDataContainer> bvhFileList=new ArrayList<BVHDataContainer>();
	//private CellList<File> bvhCellList;
	//private SingleSelectionModel<File> fileSelectionModel;
	private DataListCell<BVHDataContainer> dataListCell;

	
	private int currentLoop=0;
	private ListBox loopTime;

	private ListBox speedBox;
	private long remainTime;

	private CheckBox abLoopCheck;

	private TabLayoutPanel toolsPanel;
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
			String lastOrder=rootGroup.getRotation().getOrder();
		rootGroup.getRotation().set(Math.toRadians(rotationRange.getValue()),Math.toRadians(rotationYRange.getValue()),Math.toRadians(rotationZRange.getValue()),lastOrder);
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
	
	public void addBVHData(BVHDataContainer dataContainer){
		
		if(!existsBVHData(dataContainer)){
			bvhFileList.add(dataContainer);
		}
		
		dataListCell.setDatas(bvhFileList);
		dataListCell.setSelection(dataContainer);
		tabPanel.selectTab(0);
	}
	
public void addBVHDatas(List<BVHDataContainer> dataContainers){
		
		for(BVHDataContainer dataContainer:dataContainers){
		if(!existsBVHData(dataContainer)){
			bvhFileList.add(dataContainer);
		}
		}
		
		dataListCell.setDatas(bvhFileList);
		dataListCell.setSelection(dataContainers.get(0));
		tabPanel.selectTab(0);
	}
	private boolean existsBVHData(BVHDataContainer dataContainer){
		for(BVHDataContainer container:bvhFileList){
			if(container.getName().equals(dataContainer.getName())){
				return true;
			}
		}
		return false;
	}
	
	@Override
	public void resized(int width, int height) {
		super.resized(width, height);
		leftBottom(bottomPanel);
	}
	@Override
	public String getHtml(){
		return "BVH Motion Creator ver."+version+"<br/>"+super.getHtml()+". Sample BVH File from <a href='https://sites.google.com/a/cgspeed.com/cgspeed/motion-capture/cmu-bvh-conversion'>CMU Graphics Lab Motion Capture Database.</a><br/> More Infomation click <a href='http://webgl.akjava.com'>webgl.akjava.com</a>";
	}

	@Override
	public String getTabTitle() {
		return "BVH Player";
	}




	@Override
	public void onDoubleClick(DoubleClickEvent event) {
		// TODO Auto-generated method stub
		
	}





}