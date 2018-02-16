/*
 * Copyright (C) 2018  KassyLab
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.kassylab;

import android.content.Context;
import android.support.annotation.LayoutRes;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * An easy adapter that creates views defined in an XML file. You can specify
 * the XML file that defines the appearance of the views.
 */
@SuppressWarnings({"unused", "WeakerAccess"})
public abstract class ResourceAdapter<VH extends RecyclerView.ViewHolder> extends RecyclerView.Adapter<VH> {
	
	/**
	 * The resource indicating what views to inflate to display the content of this
	 * array adapter.
	 */
	@LayoutRes
	private final int mResource;
	
	private final LayoutInflater mInflater;
	
	public ResourceAdapter(Context context, @LayoutRes int resource) {
		mResource = resource;
		mInflater = LayoutInflater.from(context);
	}
	
	@Override
	public VH onCreateViewHolder(ViewGroup parent, int viewType) {
		return onCreateViewHolder(parent, mInflater.inflate(mResource, parent, false));
	}
	
	protected abstract VH onCreateViewHolder(ViewGroup parent, View itemView);
}
