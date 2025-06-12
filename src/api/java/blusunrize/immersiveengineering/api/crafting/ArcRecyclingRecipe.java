/*
 * BluSunrize
 * Copyright (c) 2025
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.api.crafting;

import blusunrize.immersiveengineering.api.IEApi;
import blusunrize.immersiveengineering.api.IETags;
import blusunrize.immersiveengineering.api.utils.TagUtils;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.util.Lazy;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class ArcRecyclingRecipe extends ArcFurnaceRecipe
{
	private final Supplier<RegistryAccess> tags;
	private final List<Pair<TagOutput, Double>> outputs;
	private final Lazy<TagOutputList> defaultOutputs;

	public static final String SPECIAL_TYPE = "recycling";

	public ArcRecyclingRecipe(Supplier<RegistryAccess> tags, List<Pair<TagOutput, Double>> outputs, IngredientWithSize input, int time, int energyPerTick)
	{
		super(
				buildOutputList(tags, outputs),
				TagOutput.EMPTY,
				List.of(),
				time,
				energyPerTick,
				input,
				List.of(),
				SPECIAL_TYPE
		);
		this.tags = tags;
		this.outputs = outputs;
		this.defaultOutputs = Lazy.of(() -> buildOutputList(this.tags, this.outputs));
	}

	private static TagOutputList buildOutputList(Supplier<RegistryAccess> tags, List<Pair<TagOutput, Double>> outputs) {
		List<TagOutput> ret = new ArrayList<>();
		for(Pair<TagOutput, Double> e : outputs)
		{
			if(e.getSecond() >= 1)
				ret.add(new TagOutput(e.getFirst().get().copyWithCount((int)(e.getSecond().doubleValue()))));
			String[] type = TagUtils.getMatchingPrefixAndRemaining(tags.get(), e.getFirst().get(), "ingots");
			if(type!=null&&((e.getSecond()%1) > 0.11))
			{
				ret.add(new TagOutput(TagUtils.createItemWrapper(IETags.getNugget(type[1])), (int)(9*(e.getSecond()%1))));
			}
		}
		return new TagOutputList(ret);
	}

	@Override
	public NonNullList<ItemStack> generateActualOutput(ItemStack input, NonNullList<ItemStack> additives, long seed)
	{
		if(outputs==null)
			return NonNullList.create();
		float mod;
		if(!input.isDamageableItem())
			mod = 1;
		else
			mod = (input.getMaxDamage()-input.getDamageValue())/(float)input.getMaxDamage();
		NonNullList<ItemStack> outs = NonNullList.create();
		for(Pair<TagOutput, Double> e : outputs)
		{
			double scaledOut = mod*e.getSecond();
			addOutputToList(scaledOut, outs, e);
		}
		return outs;
	}

	@Override
	public NonNullList<ItemStack> getBaseOutputs()
	{
		return defaultOutputs.get().get();
	}

	@Override
	public NonNullList<ItemStack> getItemOutputs()
	{
		return defaultOutputs.get().get();
	}

	private void addOutputToList(double scaledOut, NonNullList<ItemStack> outs, Pair<TagOutput, Double> e)
	{
		//Noone likes nuggets anyway >_>
		if(scaledOut >= 1)
			outs.add(e.getFirst().get().copyWithCount((int)scaledOut));
		int nuggetOut = (int)((scaledOut-(int)scaledOut)*9);
		if(nuggetOut > 0)
		{
			String[] type = TagUtils.getMatchingPrefixAndRemaining(tags.get(), e.getFirst().get(), "ingots");
			if(type!=null)
			{
				ItemStack nuggets = IEApi.getPreferredTagStack(tags.get(), TagUtils.createItemWrapper(IETags.getNugget(type[1])));
				if(!nuggets.isEmpty())
					outs.add(nuggets.copyWithCount(nuggetOut));
			}
		}
	}

	@Override
	public boolean matches(ItemStack input, NonNullList<ItemStack> additives)
	{
		return !input.isEmpty()&&this.input.test(input);
	}

	public List<Pair<TagOutput, Double>> getOutputs()
	{
		return outputs;
	}
}
