package com.akjava.gwt.bvhtools.client.player.resources;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.TextResource;

public interface Bundles extends ClientBundle {
public static Bundles INSTANCE=GWT.create(Bundles.class);

	@Source("standard_cmu.txt")
	TextResource standard_cmu();
	TextResource small_cmu();
}
