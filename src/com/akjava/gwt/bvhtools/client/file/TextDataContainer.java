package com.akjava.gwt.bvhtools.client.file;


public class TextDataContainer implements BVHDataContainer{
	private String  name;
	private String json;
	public TextDataContainer(String name,String json){
		this.name=name;
		this.json=json;
	}
	@Override
	public String getName() {
		return name;
	}

	@Override
	public void readText(BVHDataListener listener) {
		listener.dataLoaded(json);
	}

}
