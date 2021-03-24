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
import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.fml.RegistryObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class MineralMix extends IESerializableRecipe
{
	public static IRecipeType<MineralMix> TYPE = IRecipeType.register(Lib.MODID+":mineral_mix");
	public static RegistryObject<IERecipeSerializer<MineralMix>> SERIALIZER;

	public static Map<ResourceLocation, MineralMix> mineralList = new HashMap<>();

	public final StackWithChance[] outputs;
	public final int weight;
	public final float failChance;
	public final ImmutableSet<RegistryKey<World>> dimensions;
	public final Block background;

	public MineralMix(ResourceLocation id, StackWithChance[] outputs, int weight, float failChance,
					  List<RegistryKey<World>> dimensions, Block background)
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
	public ItemStack getRecipeOutput()
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

	public boolean validDimension(RegistryKey<World> dim)
	{
		if(dimensions!=null&&!dimensions.isEmpty())
			return dimensions.contains(dim);
		return true;
	}
}
