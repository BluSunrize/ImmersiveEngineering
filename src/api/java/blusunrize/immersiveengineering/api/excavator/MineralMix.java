/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 *
 */

package blusunrize.immersiveengineering.api.excavator;

import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.api.crafting.IERecipeSerializer;
import blusunrize.immersiveengineering.api.crafting.IESerializableRecipe;
import blusunrize.immersiveengineering.api.crafting.StackWithChance;
import com.google.common.collect.ImmutableSet;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.fml.RegistryObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class MineralMix extends IESerializableRecipe
{
	public static RecipeType<MineralMix> TYPE;
	public static RegistryObject<IERecipeSerializer<MineralMix>> SERIALIZER;

	public static Map<ResourceLocation, MineralMix> mineralList = new HashMap<>();

	public final StackWithChance[] outputs;
	public final int weight;
	public final float failChance;
	public final ImmutableSet<ResourceKey<Level>> dimensions;
	public final Block background;

	public MineralMix(ResourceLocation id, StackWithChance[] outputs, int weight, float failChance,
					  List<ResourceKey<Level>> dimensions, Block background)
	{
		super(ItemStack.EMPTY, TYPE, id);
		this.weight = weight;
		this.failChance = failChance;
		this.outputs = outputs;
		this.dimensions = ImmutableSet.copyOf(dimensions);
		this.background = background;
	}

	@Override
	protected IERecipeSerializer<MineralMix> getIESerializer()
	{
		return SERIALIZER.get();
	}

	@Override
	public ItemStack getResultItem()
	{
		return ItemStack.EMPTY;
	}

	public String getPlainName()
	{
		String path = getId().getPath();
		return path.substring(path.lastIndexOf("/")+1);
	}

	public String getTranslationKey()
	{
		return Lib.DESC_INFO+"mineral."+getPlainName();
	}

	public ItemStack getRandomOre(Random rand)
	{
		float r = rand.nextFloat();
		for(StackWithChance o : outputs)
			if(o.getChance() >= 0)
			{
				r -= o.getChance();
				if(r < 0)
					return o.getStack();
			}
		return ItemStack.EMPTY;
	}

	public boolean validDimension(ResourceKey<Level> dim)
	{
		if(dimensions!=null&&!dimensions.isEmpty())
			return dimensions.contains(dim);
		return true;
	}
}
