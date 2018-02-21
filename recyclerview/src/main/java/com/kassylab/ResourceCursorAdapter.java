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
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * An easy adapter that creates views defined in an XML file. You can specify
 * the XML file that defines the appearance of the views.
 */
@SuppressWarnings({"unused", "WeakerAccess"})
public abstract class ResourceCursorAdapter<VH extends RecyclerView.ViewHolder> extends CursorAdapter<VH> {
	
	private int mResource;
	
	private LayoutInflater mInflater;
	
	/**
	 * Constructor the enables auto-requery.
	 *
	 * @deprecated This option is discouraged, as it results in Cursor queries
	 * being performed on the application's UI thread and thus can cause poor
	 * responsiveness or even Application Not Responding errors.  As an alternative,
	 * use {@link android.app.LoaderManager} with a {@link android.content.CursorLoader}.
	 *
	 * @param context The context where the ListView associated with this adapter is running
	 * @param layout resource identifier of a layout file that defines the views
	 *            for this list item.  Unless you override them later, this will
	 *            define both the item views and the drop down views.
	 * @param c The cursor from which to get the data.
	 */
	@Deprecated
	public ResourceCursorAdapter(Context context, int layout, Cursor c) {
		super(context, c);
		mResource = layout;
		mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}
	
	/**
	 * Constructor with default behavior as per
	 * {@link CursorAdapter#CursorAdapter(Context, Cursor, boolean)}; it is recommended
	 * you not use this, but instead {@link #ResourceCursorAdapter(Context, int, Cursor, int)}.
	 * When using this constructor, {@link #FLAG_REGISTER_CONTENT_OBSERVER}
	 * will always be set.
	 *
	 * @param context The context where the ListView associated with this adapter is running
	 * @param layout resource identifier of a layout file that defines the views
	 *            for this list item.  Unless you override them later, this will
	 *            define both the item views and the drop down views.
	 * @param c The cursor from which to get the data.
	 * @param autoRequery If true the adapter will call requery() on the
	 *                    cursor whenever it changes so the most recent
	 *                    data is always displayed.  Using true here is discouraged.
	 */
	public ResourceCursorAdapter(Context context, int layout, Cursor c, boolean autoRequery) {
		super(context, c, autoRequery);
		mResource = layout;
		mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}
	
	/**
	 * Standard constructor.
	 *
	 * @param context The context where the ListView associated with this adapter is running
	 * @param layout Resource identifier of a layout file that defines the views
	 *            for this list item.  Unless you override them later, this will
	 *            define both the item views and the drop down views.
	 * @param c The cursor from which to get the data.
	 * @param flags Flags used to determine the behavior of the adapter,
	 * as per {@link CursorAdapter#CursorAdapter(Context, Cursor, int)}.
	 */
	public ResourceCursorAdapter(Context context, int layout, Cursor c, int flags) {
		super(context, c, flags);
		mResource = layout;
		mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}
	
	@Override
	public VH onCreateViewHolder(ViewGroup parent, int viewType) {
		return onCreateViewHolder(parent, mInflater.inflate(mResource, parent, false));
	}
	
	protected abstract VH onCreateViewHolder(ViewGroup parent, View itemView);
}
