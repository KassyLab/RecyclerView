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
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.SoundEffectConstants;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityEvent;

/**
 * A flexible view for providing a limited window into a large data set.
 *
 * <h3>Glossary of terms:</h3>
 *
 * <ul>
 *     <li><em>Adapter:</em> A subclass of {@link Adapter} responsible for providing views
 *     that represent items in a data set.</li>
 *     <li><em>Position:</em> The position of a data item within an <em>Adapter</em>.</li>
 *     <li><em>Index:</em> The index of an attached child view as used in a call to
 *     {@link ViewGroup#getChildAt}. Contrast with <em>Position.</em></li>
 *     <li><em>Binding:</em> The process of preparing a child view to display data corresponding
 *     to a <em>position</em> within the adapter.</li>
 *     <li><em>Recycle (view):</em> A view previously used to display data for a specific adapter
 *     position may be placed in a cache for later reuse to display the same type of data again
 *     later. This can drastically improve performance by skipping initial layout inflation
 *     or construction.</li>
 *     <li><em>Scrap (view):</em> A child view that has entered into a temporarily detached
 *     state during layout. Scrap views may be reused without becoming fully detached
 *     from the parent RecyclerView, either unmodified if no rebinding is required or modified
 *     by the adapter if the view was considered <em>dirty</em>.</li>
 *     <li><em>Dirty (view):</em> A child view that must be rebound by the adapter before
 *     being displayed.</li>
 * </ul>
 *
 * <h4>Positions in RecyclerView:</h4>
 * <p>
 * RecyclerView introduces an additional level of abstraction between the {@link Adapter} and
 * {@link LayoutManager} to be able to detect data set changes in batches during a layout
 * calculation. This saves LayoutManager from tracking adapter changes to calculate animations.
 * It also helps with performance because all view bindings happen at the same time and unnecessary
 * bindings are avoided.
 * <p>
 * For this reason, there are two types of <code>position</code> related methods in RecyclerView:
 * <ul>
 *     <li>layout position: Position of an item in the latest layout calculation. This is the
 *     position from the LayoutManager's perspective.</li>
 *     <li>adapter position: Position of an item in the adapter. This is the position from
 *     the Adapter's perspective.</li>
 * </ul>
 * <p>
 * These two positions are the same except the time between dispatching <code>adapter.notify*
 * </code> events and calculating the updated layout.
 * <p>
 * Methods that return or receive <code>*LayoutPosition*</code> use position as of the latest
 * layout calculation (e.g. {@link ViewHolder#getLayoutPosition()},
 * {@link #findViewHolderForLayoutPosition(int)}). These positions include all changes until the
 * last layout calculation. You can rely on these positions to be consistent with what user is
 * currently seeing on the screen. For example, if you have a list of items on the screen and user
 * asks for the 5<sup>th</sup> element, you should use these methods as they'll match what user
 * is seeing.
 * <p>
 * The other set of position related methods are in the form of
 * <code>*AdapterPosition*</code>. (e.g. {@link ViewHolder#getAdapterPosition()},
 * {@link #findViewHolderForAdapterPosition(int)}) You should use these methods when you need to
 * work with up-to-date adapter positions even if they may not have been reflected to layout yet.
 * For example, if you want to access the item in the adapter on a ViewHolder click, you should use
 * {@link ViewHolder#getAdapterPosition()}. Beware that these methods may not be able to calculate
 * adapter positions if {@link Adapter#notifyDataSetChanged()} has been called and new layout has
 * not yet been calculated. For this reasons, you should carefully handle {@link #NO_POSITION} or
 * <code>null</code> results from these methods.
 * <p>
 * When writing a {@link LayoutManager} you almost always want to use layout positions whereas when
 * writing an {@link Adapter}, you probably want to use adapter positions.
 *
 * @attr ref android.support.v7.recyclerview.R.styleable#RecyclerView_layoutManager
 */
@SuppressWarnings("unused")
public class RecyclerView extends android.support.v7.widget.RecyclerView {
	
	/**
	 * The listener that receives notifications when an item is clicked.
	 */
	OnItemClickListener mOnItemClickListener;
	
	/**
	 * The listener that receives notifications when an item is long clicked.
	 */
	OnItemLongClickListener mOnItemLongClickListener;
	
	public RecyclerView(Context context) {
		super(context);
	}
	
	public RecyclerView(Context context, @Nullable AttributeSet attrs) {
		super(context, attrs);
	}
	
	public RecyclerView(Context context, @Nullable AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}
	
	/**
	 * Interface definition for a callback to be invoked when an item in this
	 * AdapterView has been clicked.
	 */
	public interface OnItemClickListener {
		
		/**
		 * Callback method to be invoked when an item in this RecyclerView has
		 * been clicked.
		 *
		 * @param parent The AdapterView where the click happened.
		 * @param holder The view within the AdapterView that was clicked (this
		 *            will be a view provided by the adapter)
		 * @param position The position of the view in the adapter.
		 * @param id The row id of the item that was clicked.
		 */
		void onItemClick(RecyclerView parent, com.kassylab.ViewHolder holder, int position, long id);
	}
	
	/**
	 * Register a callback to be invoked when an item in this AdapterView has
	 * been clicked.
	 *
	 * @param listener The callback that will be invoked.
	 */
	public void setOnItemClickListener(@Nullable OnItemClickListener listener) {
		mOnItemClickListener = listener;
	}
	
	/**
	 * @return The callback to be invoked with an item in this AdapterView has
	 *         been clicked, or null id no callback has been set.
	 */
	@Nullable
	public final OnItemClickListener getOnItemClickListener() {
		return mOnItemClickListener;
	}
	
	/**
	 * Call the OnItemClickListener, if it is defined. Performs all normal
	 * actions associated with clicking: reporting accessibility event, playing
	 * a sound, etc.
	 *
	 * @param holder The view within the AdapterView that was clicked.
	 * @param position The position of the view in the adapter.
	 * @param id The row id of the item that was clicked.
	 * @return True if there was an assigned OnItemClickListener that was
	 *         called, false otherwise is returned.
	 */
	
	@SuppressWarnings("UnusedReturnValue")
	public boolean performItemClick(com.kassylab.ViewHolder holder, int position, long id) {
		final boolean result;
		if (mOnItemClickListener != null) {
			playSoundEffect(SoundEffectConstants.CLICK);
			mOnItemClickListener.onItemClick(this, holder, position, id);
			result = true;
		} else {
			result = false;
		}
		
		if (holder != null) {
			holder.itemView.sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_CLICKED);
		}
		return result;
	}
	
	/**
	 * Interface definition for a callback to be invoked when an item in this
	 * view has been clicked and held.
	 */
	public interface OnItemLongClickListener {
		/**
		 * Callback method to be invoked when an item in this view has been
		 * clicked and held.
		 *
		 * Implementers can call getItemAtPosition(position) if they need to access
		 * the data associated with the selected item.
		 *
		 * @param parent The AbsListView where the click happened
		 * @param holder The view within the AbsListView that was clicked
		 * @param position The position of the view in the list
		 * @param id The row id of the item that was clicked
		 *
		 * @return true if the callback consumed the long click, false otherwise
		 */
		boolean onItemLongClick(RecyclerView parent, com.kassylab.ViewHolder holder, int position, long id);
	}
	
	
	/**
	 * Register a callback to be invoked when an item in this AdapterView has
	 * been clicked and held
	 *
	 * @param listener The callback that will run
	 */
	public void setOnItemLongClickListener(OnItemLongClickListener listener) {
		if (!isLongClickable()) {
			setLongClickable(true);
		}
		mOnItemLongClickListener = listener;
	}
	
	/**
	 * @return The callback to be invoked with an item in this AdapterView has
	 *         been clicked and held, or null id no callback as been set.
	 */
	public final OnItemLongClickListener getOnItemLongClickListener() {
		return mOnItemLongClickListener;
	}
}
