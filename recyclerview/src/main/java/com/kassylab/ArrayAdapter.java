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
import android.support.annotation.ArrayRes;
import android.support.annotation.IdRes;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * You can use this adapter to provide views for a {@link RecyclerView},
 * Returns a view for each object in a collection of data objects you
 * provide, and can be used with {@link RecyclerView}.
 * <p>
 * By default, the array adapter creates a view by calling {@link Object#toString()} on each
 * data object in the collection you provide, and places the result in a TextView.
 * You may also customize what type of view is used for the data object in the collection.
 * To customize what type of view is used for the data object,
 * override {@link #bindViewHolder(RecyclerView.ViewHolder, int)}.
 * </p>
 */
@SuppressWarnings({"unused", "WeakerAccess"})
public class ArrayAdapter<T> extends ResourceAdapter<ArrayAdapter.ViewHolder> implements Filterable {
	
	/**
	 * Lock used to modify the content of {@link #mObjects}. Any write operation
	 * performed on the array should be synchronized on this lock. This lock is also
	 * used by the filter (see {@link #getFilter()} to make a synchronized copy of
	 * the original array of data.
	 */
	private final Object mLock = new Object();
	
	private final Context mContext;
	
	/**
	 * Contains the list of objects that represent the data of this ArrayAdapter.
	 * The content of this list is referred to as "the array" in the documentation.
	 */
	private List<T> mObjects;
	
	/**
	 * If the inflated resource is not a TextView, {@code mFieldId} is used to find
	 * a TextView inside the inflated views hierarchy. This field must contain the
	 * identifier that matches the one defined in the resource file.
	 */
	private int mFieldId = 0;
	
	/**
	 * Indicates whether or not {@link #notifyDataSetChanged()} must be called whenever
	 * {@link #mObjects} is modified.
	 */
	private boolean mNotifyOnChange = true;
	
	// A copy of the original mObjects array, initialized from and then used instead as soon as
	// the mFilter ArrayFilter is used. mObjects will then only contain the filtered values.
	private ArrayList<T> mOriginalValues;
	private ArrayFilter mFilter;
	
	/**
	 * Constructor
	 *
	 * @param context The current context.
	 * @param resource The resource ID for a layout file containing a TextView to use when
	 *                 instantiating views.
	 */
	public ArrayAdapter(@NonNull Context context, @LayoutRes int resource) {
		this(context, resource, 0, new ArrayList<T>());
	}
	
	/**
	 * Constructor
	 *
	 * @param context The current context.
	 * @param resource The resource ID for a layout file containing a layout to use when
	 *                 instantiating views.
	 * @param textViewResourceId The id of the TextView within the layout resource to be populated
	 */
	public ArrayAdapter(@NonNull Context context, @LayoutRes int resource,
	                    @IdRes int textViewResourceId) {
		this(context, resource, textViewResourceId, new ArrayList<T>());
	}
	
	/**
	 * Constructor
	 *
	 * @param context The current context.
	 * @param resource The resource ID for a layout file containing a TextView to use when
	 *                 instantiating views.
	 * @param objects The objects to represent in the ListView.
	 */
	public ArrayAdapter(@NonNull Context context, @LayoutRes int resource, @NonNull T[] objects) {
		this(context, resource, 0, Arrays.asList(objects));
	}
	
	/**
	 * Constructor
	 *
	 * @param context The current context.
	 * @param resource The resource ID for a layout file containing a layout to use when
	 *                 instantiating views.
	 * @param textViewResourceId The id of the TextView within the layout resource to be populated
	 * @param objects The objects to represent in the ListView.
	 */
	public ArrayAdapter(@NonNull Context context, @LayoutRes int resource,
	                    @IdRes int textViewResourceId, @NonNull T[] objects) {
		this(context, resource, textViewResourceId, Arrays.asList(objects));
	}
	
	/**
	 * Constructor
	 *
	 * @param context The current context.
	 * @param resource The resource ID for a layout file containing a TextView to use when
	 *                 instantiating views.
	 * @param objects The objects to represent in the ListView.
	 */
	public ArrayAdapter(@NonNull Context context, @LayoutRes int resource,
	                    @NonNull List<T> objects) {
		this(context, resource, 0, objects);
	}
	
	/**
	 * Constructor
	 *
	 * @param context The current context.
	 * @param resource The resource ID for a layout file containing a layout to use when
	 *                 instantiating views.
	 * @param textViewResourceId The id of the TextView within the layout resource to be populated
	 * @param objects The objects to represent in the ListView.
	 */
	public ArrayAdapter(@NonNull Context context, @LayoutRes int resource,
	                    @IdRes int textViewResourceId, @NonNull List<T> objects) {
		super(context, resource);
		
		mContext = context;
		mObjects = objects;
		mFieldId = textViewResourceId;
	}
	
	
	
	/**
	 * Adds the specified object at the end of the array.
	 *
	 * @param object The object to add at the end of the array.
	 */
	public void add(@Nullable T object) {
		synchronized (mLock) {
			if (mOriginalValues != null) {
				mOriginalValues.add(object);
			} else {
				mObjects.add(object);
			}
		}
		if (mNotifyOnChange) notifyDataSetChanged();
	}
	
	/**
	 * Adds the specified Collection at the end of the array.
	 *
	 * @param collection The Collection to add at the end of the array.
	 * @throws UnsupportedOperationException if the <tt>addAll</tt> operation
	 *         is not supported by this list
	 * @throws ClassCastException if the class of an element of the specified
	 *         collection prevents it from being added to this list
	 * @throws NullPointerException if the specified collection contains one
	 *         or more null elements and this list does not permit null
	 *         elements, or if the specified collection is null
	 * @throws IllegalArgumentException if some property of an element of the
	 *         specified collection prevents it from being added to this list
	 */
	public void addAll(@NonNull Collection<? extends T> collection) {
		synchronized (mLock) {
			if (mOriginalValues != null) {
				mOriginalValues.addAll(collection);
			} else {
				mObjects.addAll(collection);
			}
		}
		if (mNotifyOnChange) notifyDataSetChanged();
	}
	
	/**
	 * Adds the specified items at the end of the array.
	 *
	 * @param items The items to add at the end of the array.
	 */
	@SuppressWarnings("unchecked")
	public void addAll(T ... items) {
		synchronized (mLock) {
			if (mOriginalValues != null) {
				Collections.addAll(mOriginalValues, items);
			} else {
				Collections.addAll(mObjects, items);
			}
		}
		if (mNotifyOnChange) notifyDataSetChanged();
	}
	
	/**
	 * Inserts the specified object at the specified index in the array.
	 *
	 * @param object The object to insert into the array.
	 * @param index The index at which the object must be inserted.
	 */
	public void insert(@Nullable T object, int index) {
		synchronized (mLock) {
			if (mOriginalValues != null) {
				mOriginalValues.add(index, object);
			} else {
				mObjects.add(index, object);
			}
		}
		if (mNotifyOnChange) notifyDataSetChanged();
	}
	
	/**
	 * Removes the specified object from the array.
	 *
	 * @param object The object to remove.
	 */
	public void remove(@Nullable T object) {
		synchronized (mLock) {
			if (mOriginalValues != null) {
				mOriginalValues.remove(object);
			} else {
				mObjects.remove(object);
			}
		}
		if (mNotifyOnChange) notifyDataSetChanged();
	}
	
	/**
	 * Remove all elements from the list.
	 */
	public void clear() {
		synchronized (mLock) {
			if (mOriginalValues != null) {
				mOriginalValues.clear();
			} else {
				mObjects.clear();
			}
		}
		if (mNotifyOnChange) notifyDataSetChanged();
	}
	
	/**
	 * Sorts the content of this adapter using the specified comparator.
	 *
	 * @param comparator The comparator used to sort the objects contained
	 *        in this adapter.
	 */
	public void sort(@NonNull Comparator<? super T> comparator) {
		synchronized (mLock) {
			if (mOriginalValues != null) {
				Collections.sort(mOriginalValues, comparator);
			} else {
				Collections.sort(mObjects, comparator);
			}
		}
		if (mNotifyOnChange) notifyDataSetChanged();
	}
	
	/**
	 * Control whether methods that change the list ({@link #add}, {@link #addAll(Collection)},
	 * {@link #addAll(Object[])}, {@link #insert}, {@link #remove}, {@link #clear},
	 * {@link #sort(Comparator)}) automatically call {@link #notifyDataSetChanged}.  If set to
	 * false, caller must manually call notifyDataSetChanged() to have the changes
	 * reflected in the attached view.
	 *
	 * The default is true, and calling notifyDataSetChanged()
	 * resets the flag to true.
	 *
	 * @param notifyOnChange if true, modifications to the list will
	 *                       automatically call {@link
	 *                       #notifyDataSetChanged}
	 */
	public void setNotifyOnChange(boolean notifyOnChange) {
		mNotifyOnChange = notifyOnChange;
	}
	
	/**
	 * Returns the context associated with this array adapter. The context is used
	 * to create views from the resource passed to the constructor.
	 *
	 * @return The Context associated with this adapter.
	 */
	public @NonNull Context getContext() {
		return mContext;
	}
	
	@Override
	protected ViewHolder onCreateViewHolder(ViewGroup parent, View itemView) {
		return new ViewHolder(parent, itemView);
	}
	
	@Override
	public void onBindViewHolder(ArrayAdapter.ViewHolder holder, int position) {
		super.onBindViewHolder(holder, position);
		
		final T item = mObjects.get(position);
		if (item instanceof CharSequence) {
			holder.text.setText((CharSequence) item);
		} else {
			holder.text.setText(item.toString());
		}
	}
	
	@Override
	public int getItemCount() {
		return mObjects.size();
	}
	
	public class ViewHolder extends com.kassylab.ViewHolder {
		
		final TextView text;
		
		ViewHolder(ViewGroup parent, View itemView) {
			super(parent, itemView);
			
			try {
				if (mFieldId == 0) {
					//  If no custom field is assigned, assume the whole resource is a TextView
					text = (TextView) itemView;
				} else {
					//  Otherwise, find the TextView field within the layout
					text = itemView.findViewById(mFieldId);
					
					if (text == null) {
						throw new RuntimeException("Failed to find view with ID "
								+ mContext.getResources().getResourceName(mFieldId)
								+ " in item layout");
					}
				}
			} catch (ClassCastException e) {
				Log.e("ArrayAdapter", "You must supply a resource ID for a TextView");
				throw new IllegalStateException(
						"ArrayAdapter requires the resource ID to be a TextView", e);
			}
		}
	}
	
	/**
	 * Creates a new ArrayAdapter from external resources. The content of the array is
	 * obtained through {@link android.content.res.Resources#getTextArray(int)}.
	 *
	 * @param context The application's environment.
	 * @param textArrayResId The identifier of the array to use as the data source.
	 * @param textViewResId The identifier of the layout used to create views.
	 *
	 * @return An {@code ArrayAdapter<CharSequence>}.
	 */
	public static @NonNull
	ArrayAdapter<CharSequence> createFromResource(@NonNull Context context,
	                                              @ArrayRes int textArrayResId, @LayoutRes int textViewResId) {
		final CharSequence[] strings = context.getResources().getTextArray(textArrayResId);
		return new ArrayAdapter<>(context, textViewResId, 0, Arrays.asList(strings));
	}
	
	@Override
	public @NonNull Filter getFilter() {
		if (mFilter == null) {
			mFilter = new ArrayFilter();
		}
		return mFilter;
	}
	
	/**
	 * <p>An array filter constrains the content of the array adapter with
	 * a prefix. Each item that does not start with the supplied prefix
	 * is removed from the list.</p>
	 */
	private class ArrayFilter extends Filter {
		@Override
		protected FilterResults performFiltering(CharSequence prefix) {
			final FilterResults results = new FilterResults();
			
			if (mOriginalValues == null) {
				synchronized (mLock) {
					mOriginalValues = new ArrayList<>(mObjects);
				}
			}
			
			if (prefix == null || prefix.length() == 0) {
				final ArrayList<T> list;
				synchronized (mLock) {
					list = new ArrayList<>(mOriginalValues);
				}
				results.values = list;
				results.count = list.size();
			} else {
				final String prefixString = prefix.toString().toLowerCase();
				
				final ArrayList<T> values;
				synchronized (mLock) {
					values = new ArrayList<>(mOriginalValues);
				}
				
				final ArrayList<T> newValues = new ArrayList<>();
				
				for (T value : values) {
					final String valueText = value.toString().toLowerCase();
					
					// First match against the whole, non-splitted value
					if (valueText.startsWith(prefixString)) {
						newValues.add(value);
					} else {
						final String[] words = valueText.split(" ");
						for (String word : words) {
							if (word.startsWith(prefixString)) {
								newValues.add(value);
								break;
							}
						}
					}
				}
				
				results.values = newValues;
				results.count = newValues.size();
			}
			
			return results;
		}
		
		@Override
		protected void publishResults(CharSequence constraint, FilterResults results) {
			//noinspection unchecked
			mObjects = (List<T>) results.values;
			notifyDataSetChanged();
		}
	}
}
