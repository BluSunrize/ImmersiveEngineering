/*
 *  BluSunrize
 *  Copyright (c) 2021
 *
 *  This code is licensed under "Blu's License of Common Sense"
 *  Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.api;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class IEApiTest
{
	@Test
	public void testGetPreferredElementByMod()
	{
		IEApi.modPreference = ImmutableList.of("b", "a", "c");
		// Ensure that in case of multiple entries with same namespace rank we get the minimum element with that rank
		testGetPreferredElementByMod(rl("a", "a"), rl("a", "b"), rl("a", "a"), rl("c", "0"), rl("d", "a"));
		testGetPreferredElementByMod(rl("d", "a"), rl("e", "c"), rl("d", "b"), rl("d", "a"), rl("e", "a"));
		testGetPreferredElementByMod(rl("b", "a"), rl("b", "b"), rl("b", "a"), rl("a", "0"), rl("d", "a"));
		assertTrue(IEApi.getPreferredElementbyMod(Stream.empty(), Function.identity()).isEmpty());
	}

	private void testGetPreferredElementByMod(ResourceLocation expected, ResourceLocation... input)
	{
		List<ResourceLocation> baseList = Arrays.asList(input);
		// Basic check to make sure order does not matter
		assertEquals(Optional.of(expected), IEApi.getPreferredElementbyMod(baseList.stream(), Function.identity()));
		assertEquals(Optional.of(expected), IEApi.getPreferredElementbyMod(Lists.reverse(baseList).stream(), Function.identity()));
	}

	private ResourceLocation rl(String namespace, String path)
	{
		return ResourceLocation.fromNamespaceAndPath(namespace, path);
	}
}