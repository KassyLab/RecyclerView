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

import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

/**
 * A ViewHolder describes an item view and metadata about its place within the RecyclerView.
 *
 * <p>{@link RecyclerView.Adapter} implementations should subclass ViewHolder and add fields for caching
 * potentially expensive {@link View#findViewById(int)} results.</p>
 *
 * <p>While {@link RecyclerView.LayoutParams} belong to the {@link RecyclerView.LayoutManager},
 * {@link ViewHolder ViewHolders} belong to the adapter. Adapters should feel free to use
 * their own custom ViewHolder implementations to store data that makes binding view contents
 * easier. Implementations should assume that individual item views will hold strong references
 * to <code>ViewHolder</code> objects and that <code>RecyclerView</code> instances may hold
 * strong references to extra off-screen item views for caching purposes</p>
 */
@SuppressWarnings({"unused", "WeakerAccess"})
public abstract class ViewHolder extends RecyclerView.ViewHolder {
	
	public Uri itemUri;
	
	/**
	 * Is set when VH is bound from the adapter.
	 */
	private com.kassylab.RecyclerView mOwnerRecyclerView;
	
	public ViewHolder(View itemView) {
		this(null, itemView);
	}
	
	public ViewHolder(ViewGroup parent, View itemView) {
		super(itemView);
		
		if (parent != null && parent instanceof com.kassylab.RecyclerView) {
			mOwnerRecyclerView = (com.kassylab.RecyclerView) parent;
		}
		
		itemView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mOwnerRecyclerView != null) {
					mOwnerRecyclerView.performItemClick(ViewHolder.this, getAdapterPosition(), getItemId());
				}
			}
		});
	}
}
