package com.akjava.gwt.bvhtools.client.file;

import com.akjava.gwt.html5.client.file.File;
import com.akjava.gwt.html5.client.file.FileHandler;
import com.akjava.gwt.html5.client.file.FileReader;

public class FileDataContainer implements BVHDataContainer{
	private File file;
	public FileDataContainer(File file){
		this.file=file;
	}
	@Override
	public String getName() {
		return file.getFileName();
	}

	@Override
	public void readText(final BVHDataListener listener) {
		final FileReader reader=FileReader.createFileReader();
		reader.setOnLoad(new FileHandler() {
			@Override
			public void onLoad() {
				listener.dataLoaded(reader.getResultAsString());
			}
		});
		reader.readAsText(file,"utf-8");
	}

}
