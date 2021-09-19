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
import junit.framework.TestCase;
import net.minecraft.resources.ResourceLocation;
import org.junit.Assert;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

public class IEApiTest extends TestCase
{
	public void testGetPreferredElementByMod()
	{
		IEApi.modPreference = ImmutableList.of("b", "a", "c");
		// Ensure that in case of multiple entries with same namespace rank we get the minimum element with that rank
		testGetPreferredElementByMod(rl("a", "a"), rl("a", "b"), rl("a", "a"), rl("c", "0"), rl("d", "a"));
		testGetPreferredElementByMod(rl("d", "a"), rl("e", "c"), rl("d", "b"), rl("d", "a"), rl("e", "a"));
		testGetPreferredElementByMod(rl("b", "a"), rl("b", "b"), rl("b", "a"), rl("a", "0"), rl("d", "a"));
		Assert.assertThrows(NullPointerException.class, () -> IEApi.getPreferredElementbyMod(ImmutableList.of(), Function.identity()));
	}

	private void testGetPreferredElementByMod(ResourceLocation expected, ResourceLocation... input)
	{
		List<ResourceLocation> baseList = Arrays.asList(input);
		// Basic check to make sure order does not matter
		Assert.assertEquals(expected, IEApi.getPreferredElementbyMod(baseList, Function.identity()));
		Assert.assertEquals(expected, IEApi.getPreferredElementbyMod(Lists.reverse(baseList), Function.identity()));
	}

	private ResourceLocation rl(String namespace, String path)
	{
		return new ResourceLocation(namespace, path);
	}
}