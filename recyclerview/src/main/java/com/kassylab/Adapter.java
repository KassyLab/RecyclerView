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

import android.util.SparseBooleanArray;
import android.widget.Checkable;

/**
 * Base class for an Adapter
 *
 * <p>Adapters provide a binding from an app-specific data set to views that are displayed
 * within a {@link RecyclerView}.</p>
 *
 * @param <VH> A class that extends {@link ViewHolder} that will be used by the adapter.
 */

public abstract class Adapter<VH extends ViewHolder> extends RecyclerView.Adapter<VH> {
	
	@Override
	public void onBindViewHolder(VH holder, int position) {
		if (holder.itemView instanceof Checkable && holder.mOwnerRecyclerView != null
				&& holder.mOwnerRecyclerView.get() != null) {
			SparseBooleanArray checkedItemPositions =
					holder.mOwnerRecyclerView.get().getCheckedItemPositions();
			((Checkable) holder.itemView).setChecked(checkedItemPositions.get(position));
		}
	}
}
