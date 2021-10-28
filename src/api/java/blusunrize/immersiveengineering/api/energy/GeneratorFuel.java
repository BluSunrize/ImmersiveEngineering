/*
 * BluSunrize
 * Copyright (c) 2021
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 *
 */

package blusunrize.immersiveengineering.api.energy;

import blusunrize.immersiveengineering.api.crafting.IERecipeSerializer;
import blusunrize.immersiveengineering.api.crafting.IESerializableRecipe;
import blusunrize.immersiveengineering.api.utils.FastEither;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.Tag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fml.RegistryObject;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;

public class GeneratorFuel extends IESerializableRecipe
{
	public static RecipeType<GeneratorFuel> TYPE;
	public static RegistryObject<IERecipeSerializer<GeneratorFuel>> SERIALIZER;

	public static Collection<GeneratorFuel> ALL_FUELS = new ArrayList<>();

	final FastEither<Tag<Fluid>, List<Fluid>> fluids;
	private final int burnTime;

	public GeneratorFuel(ResourceLocation id, Tag<Fluid> fluids, int burnTime)
	{
		super(ItemStack.EMPTY, TYPE, id);
		this.fluids = FastEither.left(fluids);
		this.burnTime = burnTime;
	}

	public GeneratorFuel(ResourceLocation id, List<Fluid> fluids, int burnTime)
	{
		super(ItemStack.EMPTY, TYPE, id);
		this.fluids = FastEither.right(fluids);
		this.burnTime = burnTime;
	}

	public List<Fluid> getFluids()
	{
		return fluids.map(Tag::getValues, Function.identity());
	}

	public int getBurnTime()
	{
		return burnTime;
	}

	@Override
	protected IERecipeSerializer<?> getIESerializer()
	{
		return SERIALIZER.get();
	}

	@Nonnull
	@Override
	public ItemStack getResultItem()
	{
		return ItemStack.EMPTY;
	}

	public boolean matches(Fluid in)
	{
		if(this.fluids.isLeft())
			return in.is(this.fluids.leftNonnull());
		else
			return this.fluids.rightNonnull().contains(in);
	}

	public static GeneratorFuel getRecipeFor(Fluid in, @Nullable GeneratorFuel hint)
	{
		if(hint!=null&&hint.matches(in))
			return hint;
		for(GeneratorFuel fuel : ALL_FUELS)
			if(fuel.matches(in))
				return fuel;
		return null;
	}
}
