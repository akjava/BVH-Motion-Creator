package com.akjava.gwt.bvhtools.client.tools;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.akjava.bvh.client.BVH;
import com.akjava.bvh.client.BVHMotion;
import com.akjava.bvh.client.BVHNode;
import com.akjava.bvh.client.BVHParser;
import com.akjava.bvh.client.BVHParser.InvalidLineException;
import com.akjava.bvh.client.Channels;
import com.akjava.bvh.client.NameAndChannel;
import com.akjava.gwt.html5.client.file.File;
import com.akjava.gwt.html5.client.file.FileHandler;
import com.akjava.gwt.html5.client.file.FileReader;
import com.akjava.gwt.html5.client.file.FileUtils;
import com.akjava.gwt.lib.client.LogUtils;
import com.akjava.gwt.lib.client.widget.cell.SimpleCellTable;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FileUpload;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.VerticalPanel;

public class CalculateTool {
private VerticalPanel panel;
	public CalculateTool(VerticalPanel panel){
		this.panel=panel;

		
		FileUpload upload=new FileUpload();
		upload.getElement().setAttribute("multiple", "multiple");
		upload.addChangeHandler(new ChangeHandler() {
			
			@Override
			public void onChange(ChangeEvent event) {
				JsArray<File> files=FileUtils.toFile(event.getNativeEvent());
				
				setFile(files);
				
			}
		});
		panel.add(upload);
		
		ScrollPanel scroll=new ScrollPanel();
		scroll.setSize("1020px", "500px");
		panel.add(scroll);
		table = new SimpleCellTable<ResultData>(16) {
			@Override
			public void addColumns(CellTable<ResultData> table) {
				 TextColumn<ResultData> nameColumn = new TextColumn<ResultData>() {
				      public String getValue(ResultData data) {
				        return data.getName();
				      }
				    };
			   table.addColumn(nameColumn,"Bone-Name");
			   
			   TextColumn<ResultData> xmin = new TextColumn<ResultData>() {
				      public String getValue(ResultData data) {
				        return ""+Math.toDegrees(data.getMinX());
				      }
				    };
			   table.addColumn(xmin,"X-Min");
			   TextColumn<ResultData> xmax = new TextColumn<ResultData>() {
				      public String getValue(ResultData data) {
				        return ""+Math.toDegrees(data.getMaxX());
				      }
				    };
			   table.addColumn(xmax,"X-Max");
			   
			   TextColumn<ResultData> ymin = new TextColumn<ResultData>() {
				      public String getValue(ResultData data) {
				        return ""+Math.toDegrees(data.getMinY());
				      }
				    };
			   table.addColumn(ymin,"Y-Min");
			   TextColumn<ResultData> ymax = new TextColumn<ResultData>() {
				      public String getValue(ResultData data) {
				        return ""+Math.toDegrees(data.getMaxY());
				      }
				    };
			   table.addColumn(ymax,"Y-Max");
			   
			   TextColumn<ResultData> zmin = new TextColumn<ResultData>() {
				      public String getValue(ResultData data) {
				        return ""+Math.toDegrees(data.getMinZ());
				      }
				    };
			   table.addColumn(zmin,"Z-Min");
			   TextColumn<ResultData> zmax = new TextColumn<ResultData>() {
				      public String getValue(ResultData data) {
				        return ""+Math.toDegrees(data.getMaxZ());
				      }
				    };
			   table.addColumn(zmax,"Z-Max");
			}
		};
		//panel.add(table);
		scroll.setWidget(table);
		
		//BVH
			//select all
		/*
		HorizontalPanel buttons=new HorizontalPanel();
		panel.add(buttons);
		buttons.setHorizontalAlignment(HorizontalPanel.ALIGN_RIGHT);
		buttons.setWidth("400px");
		Button selectAll=new Button("Select Text");
		buttons.add(selectAll);
		textArea = new TextArea();
		textArea.setSize("400px", "250px");
		textArea.setReadOnly(true);
		panel.add(textArea);
		selectAll.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				textArea.selectAll();
			}
		});
		*/
	}
	

	private class ResultData{
		private String name;
		public ResultData(String name){
			this.name=name;
		}
		public String getName() {
			return name;
		}
		public void setName(String name) {
			this.name = name;
		}
		public double getMinX() {
			return minX;
		}
		public void setMinX(double minX) {
			if(this.minX==null){
				this.minX = minX;
			}else{
				if(minX<this.minX){
					this.minX=minX;
				}
			}
		}
		public double getMaxX() {
			return maxX;
		}
		public void setMaxX(double maxX) {
			if(this.maxX==null){
				this.maxX = maxX;
			}else{
				if(maxX>this.maxX){
					this.maxX=maxX;
				}
			}
		}
		public double getMinY() {
			return minY;
		}
		public void setMinY(double minY) {
			if(this.minY==null){
				this.minY = minY;
			}else{
				if(minY<this.minY){
					this.minY=minY;
				}
			}
			
		}
		public double getMaxY() {
			return maxY;
		}
		public void setMaxY(double maxY) {
			if(this.maxY==null){
				this.maxY = maxY;
			}else{
				if(maxY>this.maxY){
					this.maxY=maxY;
				}
			}
		}
		public double getMinZ() {
			return minZ;
		}
		public void setMinZ(double minZ) {
			if(this.minZ==null){
				this.minZ = minZ;
			}else{
				if(minZ<this.minZ){
					this.minZ=minZ;
				}
			}
		}
		public double getMaxZ() {
			return maxZ;
		}
		public void setMaxZ(double maxZ) {
			if(this.maxZ==null){
				this.maxZ = maxZ;
			}else{
				if(maxZ>this.maxZ){
					this.maxZ=maxZ;
				}
			}
		}
		private Double minX;
		private Double maxX;
		private Double minY;
		private Double maxY;
		private Double minZ;
		private Double maxZ;
	}

	
	private SimpleCellTable<ResultData> table;
	
	
	Map<String,ResultData> resultMap;
	private TextArea textArea;
	private BVH bvhFile;
	private void parseBVH(String bvhText){
		BVHParser parser=new BVHParser();
		try {
			BVH bvh=parser.parse(bvhText);
			GWT.log("parsed");
			//calcurate
			//List<ResultData> datas=new ArrayList<ResultData>();
			
			
			if(resultMap==null){
				resultMap=new LinkedHashMap<String,ResultData>();
				addMap(bvh.getHiearchy());
				bvhFile=bvh;
			}
			
			
			BVHMotion motion=bvh.getMotion();
			GWT.log("motion:"+motion.size());
			for(int i=0;i<motion.size();i++){
				
				doMotion(bvh,motion.getFrameAt(i));
			}
			GWT.log("do motion");
			List<ResultData> result=new ArrayList<ResultData>();
			for(String name:resultMap.keySet()){
				result.add(resultMap.get(name));
			}
			GWT.log("set data");
			table.setData(result);
			
		} catch (InvalidLineException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	private void addMap(BVHNode node){
		resultMap.put(node.getName(), new ResultData(node.getName()));
		for(BVHNode child:node.getJoints()){
			addMap(child);
		}
	}
	private void setFile(JsArray<File> files){
		GWT.log("set-file:"+files.get(0));
		
		
		
		List<File> fileList=new ArrayList<File>();
		for(int i=0;i<files.length();i++){
			fileList.add(files.get(i));
		}
		
		loadFiles(fileList);
	}
	
	private void loadFiles(final List<File> files){
		final FileReader reader=FileReader.createFileReader();
		reader.setOnLoad(new FileHandler() {
			@Override
			public void onLoad() {
				LogUtils.log("load-file");
				parseBVH(reader.getResultAsString());
				if(files.size()>0){
					reader.readAsText(files.remove(0),"utf-8");
				}else{
					updateBVH();
				}
			}
		});
		reader.readAsText(files.remove(0),"utf-8");
	}
	
	

	
	protected void updateBVH() {
		//TODO future
	}
	private void doMotion(BVH bvh,double[] vs){
		GWT.log("domotion:");
		ResultData oldTarget=null;
		String lastOrder=null;
		for(int i=0;i<vs.length;i++){
			NameAndChannel nchannel=bvh.getNameAndChannels().get(i);
			lastOrder=nchannel.getOrder();
			ResultData target=resultMap.get(nchannel.getName());
			GWT.log("target:"+target);
			switch(nchannel.getChannel()){
			case Channels.XROTATION:
				target.setMinX(Math.toRadians(vs[i]));
				target.setMaxX(Math.toRadians(vs[i]));
			break;
			case Channels.YROTATION:
				target.setMinY(Math.toRadians(vs[i]));
				target.setMaxY(Math.toRadians(vs[i]));
			break;
			case Channels.ZROTATION:
				target.setMinZ(Math.toRadians(vs[i]));
				target.setMaxZ(Math.toRadians(vs[i]));
			break;
			case Channels.XPOSITION:
				
				break;
			case Channels.YPOSITION:
				
				break;
			case Channels.ZPOSITION:
				
				break;	
			}
			
			if(oldTarget!=null && oldTarget!=target){
			//	doZYX(oldTarget,lastOrder);
			}
			oldTarget=target;
		}
	//	doZYX(oldTarget,lastOrder);//do last one
	}
}
