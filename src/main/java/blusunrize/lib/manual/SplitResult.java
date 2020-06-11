/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.lib.manual;

import com.google.common.collect.ImmutableList;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMaps;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntMaps;
import it.unimi.dsi.fastutil.objects.Object2IntMaps.UnmodifiableMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class SplitResult
{
	public final List<List<String>> entry;
	public final Object2IntMap<String> pageByAnchor;
	public final Int2ObjectMap<SpecialManualElement> specialByPage;

	SplitResult(List<List<String>> entry, Object2IntMap<String> pageByAnchor, Int2ObjectMap<SpecialManualElement> specialByPage)
	{
		this.entry = ImmutableList.copyOf(
				entry.stream()
						.map(ImmutableList::copyOf)
						.collect(Collectors.toList())
		);
		this.pageByAnchor = Object2IntMaps.unmodifiable(pageByAnchor);
		this.specialByPage = Int2ObjectMaps.unmodifiable(specialByPage);
	}
}
