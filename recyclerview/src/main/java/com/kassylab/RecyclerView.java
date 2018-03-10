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
import android.support.v4.util.LongSparseArray;
import android.util.AttributeSet;
import android.util.SparseBooleanArray;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SoundEffectConstants;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityEvent;
import android.widget.Checkable;

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
 */
@SuppressWarnings("unused")
public class RecyclerView extends android.support.v7.widget.RecyclerView {
	
	/**
	 * Represents an invalid position. All valid positions are in the range 0 to 1 less than the
	 * number of items in the current adapter.
	 */
	public static final int INVALID_POSITION = -1;
	
	/**
	 * Normal list that does not indicate choices
	 */
	public static final int CHOICE_MODE_NONE = 0;
	
	/**
	 * The list allows up to one choice
	 */
	public static final int CHOICE_MODE_SINGLE = 1;
	
	/**
	 * The list allows multiple choices
	 */
	public static final int CHOICE_MODE_MULTIPLE = 2;
	
	/**
	 * The list allows multiple choices in a modal selection mode
	 */
	public static final int CHOICE_MODE_MULTIPLE_MODAL = 3;
	
	/**
	 * The listener that receives notifications when an item is clicked.
	 */
	OnItemClickListener mOnItemClickListener;
	
	/**
	 * The listener that receives notifications when an item is long clicked.
	 */
	OnItemLongClickListener mOnItemLongClickListener;
	
	/**
	 * The listener that receives notifications when an item is selected.
	 */
	OnItemSelectedListener mOnItemSelectedListener;
	
	/**
	 * Controls if/how the user may choose/check items in the list
	 */
	int mChoiceMode = CHOICE_MODE_NONE;
	
	/**
	 * Controls CHOICE_MODE_MULTIPLE_MODAL. null when inactive.
	 */
	ActionMode mChoiceActionMode;
	
	/**
	 * Wrapper for the multiple choice mode callback; AbsListView needs to perform
	 * a few extra actions around what application code does.
	 */
	MultiChoiceModeWrapper mMultiChoiceModeCallback;
	
	/**
	 * Running count of how many items are currently checked
	 */
	int mCheckedItemCount;
	
	/**
	 * Running state of which positions are currently checked
	 */
	SparseBooleanArray mCheckStates;
	
	/**
	 * Running state of which IDs are currently checked.
	 * If there is a value for a given key, the checked state for that ID is true
	 * and the value holds the last known position in the adapter for that id.
	 */
	LongSparseArray<Integer> mCheckedIdStates;
	
	
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
	
	/**
	 * Interface definition for a callback to be invoked when
	 * an item in this view has been selected.
	 */
	public interface OnItemSelectedListener {
		/**
		 * <p>Callback method to be invoked when an item in this view has been
		 * selected. This callback is invoked only when the newly selected
		 * position is different from the previously selected position or if
		 * there was no selected item.</p>
		 *
		 * Impelmenters can call getItemAtPosition(position) if they need to access the
		 * data associated with the selected item.
		 *
		 * @param parent The RecyclerView where the selection happened
		 * @param view The view within the RecyclerView that was clicked
		 * @param position The position of the view in the adapter
		 * @param id The row id of the item that is selected
		 */
		void onItemSelected(RecyclerView parent, com.kassylab.ViewHolder view, int position, long id);
		
		/**
		 * Callback method to be invoked when the selection disappears from this
		 * view. The selection can disappear for instance when touch is activated
		 * or when the adapter becomes empty.
		 *
		 * @param parent The RecyclerView that now contains no selected item.
		 */
		void onNothingSelected(RecyclerView parent);
	}
	
	
	/**
	 * Register a callback to be invoked when an item in this RecyclerView has
	 * been selected.
	 *
	 * @param listener The callback that will run
	 */
	public void setOnItemSelectedListener(@Nullable OnItemSelectedListener listener) {
		mOnItemSelectedListener = listener;
	}
	
	@Nullable
	public final OnItemSelectedListener getOnItemSelectedListener() {
		return mOnItemSelectedListener;
	}
	
	
	
	
	
	
	
	
	/**
	 * Returns the number of items currently selected. This will only be valid
	 * if the choice mode is not {@link #CHOICE_MODE_NONE} (default).
	 *
	 * <p>To determine the specific items that are currently selected, use one of
	 * the <code>getChecked*</code> methods.
	 *
	 * @return The number of items currently selected
	 *
	 * @see #getCheckedItemPosition()
	 * @see #getCheckedItemPositions()
	 * @see #getCheckedItemIds()
	 */
	public int getCheckedItemCount() {
		return mCheckedItemCount;
	}
	
	/**
	 * Returns the checked state of the specified position. The result is only
	 * valid if the choice mode has been set to {@link #CHOICE_MODE_SINGLE}
	 * or {@link #CHOICE_MODE_MULTIPLE}.
	 *
	 * @param position The item whose checked state to return
	 * @return The item's checked state or <code>false</code> if choice mode
	 *         is invalid
	 *
	 * @see #setChoiceMode(int)
	 */
	public boolean isItemChecked(int position) {
		return mChoiceMode != CHOICE_MODE_NONE && mCheckStates != null && mCheckStates.get(position);
		
	}
	
	/**
	 * Returns the currently checked item. The result is only valid if the choice
	 * mode has been set to {@link #CHOICE_MODE_SINGLE}.
	 *
	 * @return The position of the currently checked item or
	 *         {@link #INVALID_POSITION} if nothing is selected
	 *
	 * @see #setChoiceMode(int)
	 */
	public int getCheckedItemPosition() {
		if (mChoiceMode == CHOICE_MODE_SINGLE && mCheckStates != null && mCheckStates.size() == 1) {
			return mCheckStates.keyAt(0);
		}
		
		return INVALID_POSITION;
	}
	
	/**
	 * Returns the set of checked items in the list. The result is only valid if
	 * the choice mode has not been set to {@link #CHOICE_MODE_NONE}.
	 *
	 * @return  A SparseBooleanArray which will return true for each call to
	 *          get(int position) where position is a checked position in the
	 *          list and false otherwise, or <code>null</code> if the choice
	 *          mode is set to {@link #CHOICE_MODE_NONE}.
	 */
	public SparseBooleanArray getCheckedItemPositions() {
		if (mChoiceMode != CHOICE_MODE_NONE) {
			return mCheckStates;
		}
		return null;
	}
	
	/**
	 * Returns the set of checked items ids. The result is only valid if the
	 * choice mode has not been set to {@link #CHOICE_MODE_NONE} and the adapter
	 * has stable IDs. ({@link android.support.v7.widget.RecyclerView.Adapter#hasStableIds()} == {@code true})
	 *
	 * @return A new array which contains the id of each checked item in the
	 *         list.
	 */
	public long[] getCheckedItemIds() {
		if (mChoiceMode == CHOICE_MODE_NONE || mCheckedIdStates == null || getAdapter() == null) {
			return new long[0];
		}
		
		final LongSparseArray<Integer> idStates = mCheckedIdStates;
		final int count = idStates.size();
		final long[] ids = new long[count];
		
		for (int i = 0; i < count; i++) {
			ids[i] = idStates.keyAt(i);
		}
		
		return ids;
	}
	
	/**
	 * Clear any choices previously set
	 */
	public void clearChoices() {
		if (mCheckStates != null) {
			mCheckStates.clear();
		}
		if (mCheckedIdStates != null) {
			mCheckedIdStates.clear();
		}
		mCheckedItemCount = 0;
	}
	
	/**
	 * Sets the checked state of the specified position. The is only valid if
	 * the choice mode has been set to {@link #CHOICE_MODE_SINGLE} or
	 * {@link #CHOICE_MODE_MULTIPLE}.
	 *
	 * @param position The item whose checked state is to be checked
	 * @param value The new checked state for the item
	 */
	public void setItemChecked(int position, boolean value) {
		if (mChoiceMode == CHOICE_MODE_NONE) {
			return;
		}
		
		// Start selection mode if needed. We don't need to if we're unchecking something.
		if (value && mChoiceMode == CHOICE_MODE_MULTIPLE_MODAL && mChoiceActionMode == null) {
			if (mMultiChoiceModeCallback == null ||
					!mMultiChoiceModeCallback.hasWrappedCallback()) {
				throw new IllegalStateException("RecyclerView: attempted to start selection mode " +
						"for CHOICE_MODE_MULTIPLE_MODAL but no choice mode callback was " +
						"supplied. Call setMultiChoiceModeListener to set a callback.");
			}
			mChoiceActionMode = startActionMode(mMultiChoiceModeCallback);
		}
		
		final boolean itemCheckChanged;
		if (mChoiceMode == CHOICE_MODE_MULTIPLE || mChoiceMode == CHOICE_MODE_MULTIPLE_MODAL) {
			boolean oldValue = mCheckStates.get(position);
			mCheckStates.put(position, value);
			if (mCheckedIdStates != null && getAdapter().hasStableIds()) {
				if (value) {
					mCheckedIdStates.put(getAdapter().getItemId(position), position);
				} else {
					mCheckedIdStates.delete(getAdapter().getItemId(position));
				}
			}
			itemCheckChanged = oldValue != value;
			if (itemCheckChanged) {
				if (value) {
					mCheckedItemCount++;
				} else {
					mCheckedItemCount--;
				}
			}
			if (mChoiceActionMode != null) {
				final long id = getAdapter().getItemId(position);
				mMultiChoiceModeCallback.onItemCheckedStateChanged(mChoiceActionMode,
						position, id, value);
			}
		} else {
			boolean updateIds = mCheckedIdStates != null && getAdapter().hasStableIds();
			// Clear all values if we're checking something, or unchecking the currently
			// selected item
			itemCheckChanged = isItemChecked(position) != value;
			if (value || isItemChecked(position)) {
				mCheckStates.clear();
				if (updateIds) {
					mCheckedIdStates.clear();
				}
			}
			// this may end up selecting the value we just cleared but this way
			// we ensure length of mCheckStates is 1, a fact getCheckedItemPosition relies on
			if (value) {
				mCheckStates.put(position, true);
				if (updateIds) {
					mCheckedIdStates.put(getAdapter().getItemId(position), position);
				}
				mCheckedItemCount = 1;
			} else if (mCheckStates.size() == 0 || !mCheckStates.valueAt(0)) {
				mCheckedItemCount = 0;
			}
		}
		
		// Do not generate a data change while we are in the layout phase or data has not changed
		if (/*!mInLayout && !mBlockLayoutRequests &&*/ itemCheckChanged) {
			/*mDataChanged = true;
			rememberSyncState();*/
			requestLayout();
		}
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
		boolean handled = false;
		boolean dispatchItemClick = true;
		
		if (mChoiceMode != CHOICE_MODE_NONE) {
			handled = true;
			boolean checkedStateChanged = false;
			
			if (mChoiceMode == CHOICE_MODE_MULTIPLE ||
					(mChoiceMode == CHOICE_MODE_MULTIPLE_MODAL && mChoiceActionMode != null)) {
				boolean checked = !mCheckStates.get(position, false);
				mCheckStates.put(position, checked);
				if (mCheckedIdStates != null && getAdapter().hasStableIds()) {
					if (checked) {
						mCheckedIdStates.put(getAdapter().getItemId(position), position);
					} else {
						mCheckedIdStates.delete(getAdapter().getItemId(position));
					}
				}
				if (checked) {
					mCheckedItemCount++;
				} else {
					mCheckedItemCount--;
				}
				if (mChoiceActionMode != null) {
					mMultiChoiceModeCallback.onItemCheckedStateChanged(mChoiceActionMode,
							position, id, checked);
					dispatchItemClick = false;
				}
				checkedStateChanged = true;
			} else if (mChoiceMode == CHOICE_MODE_SINGLE) {
				boolean checked = !mCheckStates.get(position, false);
				if (checked) {
					mCheckStates.clear();
					mCheckStates.put(position, true);
					if (mCheckedIdStates != null && getAdapter().hasStableIds()) {
						mCheckedIdStates.clear();
						mCheckedIdStates.put(getAdapter().getItemId(position), position);
					}
					mCheckedItemCount = 1;
				} else if (mCheckStates.size() == 0 || !mCheckStates.valueAt(0)) {
					mCheckedItemCount = 0;
				}
				checkedStateChanged = true;
			}
			
			if (checkedStateChanged) {
				updateOnScreenCheckedViews(holder, position);
			}
		}
		
		if (dispatchItemClick) {
			handled |= performItemClick2(holder, position, id);
		}
		
		return handled;
	}
	
	private boolean performItemClick2(com.kassylab.ViewHolder holder, int position, long id) {
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
	 * Perform a quick, in-place update of the checked or activated state
	 * on all visible item views. This should only be called when a valid
	 * choice mode is active.
	 */
	private void updateOnScreenCheckedViews(com.kassylab.ViewHolder holder, int position) {
		if (holder.itemView instanceof Checkable) {
			((Checkable) holder.itemView).setChecked(mCheckStates.get(position));
		} else if (getContext().getApplicationInfo().targetSdkVersion
				>= android.os.Build.VERSION_CODES.HONEYCOMB) {
			holder.itemView.setActivated(mCheckStates.get(position));
		}
	}
	
	/**
	 * @see #setChoiceMode(int)
	 *
	 * @return The current choice mode
	 */
	public int getChoiceMode() {
		return mChoiceMode;
	}
	
	/**
	 * Defines the choice behavior for the List. By default, Lists do not have any choice behavior
	 * ({@link #CHOICE_MODE_NONE}). By setting the choiceMode to {@link #CHOICE_MODE_SINGLE}, the
	 * List allows up to one item to  be in a chosen state. By setting the choiceMode to
	 * {@link #CHOICE_MODE_MULTIPLE}, the list allows any number of items to be chosen.
	 *
	 * @param choiceMode One of {@link #CHOICE_MODE_NONE}, {@link #CHOICE_MODE_SINGLE}, or
	 * {@link #CHOICE_MODE_MULTIPLE}
	 */
	public void setChoiceMode(int choiceMode) {
		mChoiceMode = choiceMode;
		if (mChoiceActionMode != null) {
			mChoiceActionMode.finish();
			mChoiceActionMode = null;
		}
		if (mChoiceMode != CHOICE_MODE_NONE) {
			if (mCheckStates == null) {
				mCheckStates = new SparseBooleanArray(0);
			}
			if (mCheckedIdStates == null && getAdapter() != null && getAdapter().hasStableIds()) {
				mCheckedIdStates = new LongSparseArray<>(0);
			}
			// Modal multi-choice mode only has choices when the mode is active. Clear them.
			if (mChoiceMode == CHOICE_MODE_MULTIPLE_MODAL) {
				clearChoices();
				setLongClickable(true);
			}
		}
	}
	
	/**
	 * Set a {@link MultiChoiceModeListener} that will manage the lifecycle of the
	 * selection {@link ActionMode}. Only used when the choice mode is set to
	 * {@link #CHOICE_MODE_MULTIPLE_MODAL}.
	 *
	 * @param listener Listener that will manage the selection mode
	 *
	 * @see #setChoiceMode(int)
	 */
	public void setMultiChoiceModeListener(MultiChoiceModeListener listener) {
		if (mMultiChoiceModeCallback == null) {
			mMultiChoiceModeCallback = new MultiChoiceModeWrapper();
		}
		mMultiChoiceModeCallback.setWrapped(listener);
	}
	
	
	
	
	
	/**
	 * A MultiChoiceModeListener receives events for {@link #CHOICE_MODE_MULTIPLE_MODAL}.
	 * It acts as the {@link ActionMode.Callback} for the selection mode and also receives
	 * {@link #onItemCheckedStateChanged(ActionMode, int, long, boolean)} events when the user
	 * selects and deselects list items.
	 */
	public interface MultiChoiceModeListener extends ActionMode.Callback {
		/**
		 * Called when an item is checked or unchecked during selection mode.
		 *
		 * @param mode The {@link ActionMode} providing the selection mode
		 * @param position Adapter position of the item that was checked or unchecked
		 * @param id Adapter ID of the item that was checked or unchecked
		 * @param checked <code>true</code> if the item is now checked, <code>false</code>
		 *                if the item is now unchecked.
		 */
		void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked);
	}
	
	class MultiChoiceModeWrapper implements MultiChoiceModeListener {
		private MultiChoiceModeListener mWrapped;
		
		void setWrapped(MultiChoiceModeListener wrapped) {
			mWrapped = wrapped;
		}
		
		boolean hasWrappedCallback() {
			return mWrapped != null;
		}
		
		@Override
		public boolean onCreateActionMode(ActionMode mode, Menu menu) {
			if (mWrapped.onCreateActionMode(mode, menu)) {
				// Initialize checked graphic state?
				setLongClickable(false);
				return true;
			}
			return false;
		}
		
		@Override
		public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
			return mWrapped.onPrepareActionMode(mode, menu);
		}
		
		@Override
		public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
			return mWrapped.onActionItemClicked(mode, item);
		}
		
		@Override
		public void onDestroyActionMode(ActionMode mode) {
			mWrapped.onDestroyActionMode(mode);
			mChoiceActionMode = null;
			
			// Ending selection mode means deselecting everything.
			clearChoices();
			
			/*mDataChanged = true;
			rememberSyncState();*/
			requestLayout();
			
			setLongClickable(true);
		}
		
		@Override
		public void onItemCheckedStateChanged(ActionMode mode,
		                                      int position, long id, boolean checked) {
			mWrapped.onItemCheckedStateChanged(mode, position, id, checked);
			
			// If there are no items selected we no longer need the selection mode.
			if (getCheckedItemCount() == 0) {
				mode.finish();
			}
		}
	}
}
