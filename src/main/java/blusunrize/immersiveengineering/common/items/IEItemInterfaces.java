/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.items;

import blusunrize.immersiveengineering.api.fluid.FluidUtils;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.common.thread.EffectiveSide;

import java.util.List;
import java.util.Optional;

public class IEItemInterfaces
{
	public interface IColouredItem
	{
		default boolean hasCustomItemColours()
		{
			return false;
		}

		default int getColourForIEItem(ItemStack stack, int pass)
		{
			return 16777215;
		}
	}

	public interface IAdvancedFluidItem
	{
		int getCapacity(ItemStack stack, int baseCapacity);

		default boolean allowFluid(ItemStack container, FluidStack fluid)
		{
			return true;
		}

		default FluidStack getFluid(ItemStack container)
		{
			Optional<FluidStack> optional = FluidUtils.getFluidContained(container);
			if(optional.isPresent())
				return optional.orElseThrow(RuntimeException::new);
			else
				return null;
		}
	}

	public interface ITextureOverride
	{
		@OnlyIn(Dist.CLIENT)
		String getModelCacheKey(ItemStack stack);

		@OnlyIn(Dist.CLIENT)
		List<ResourceLocation> getTextures(ItemStack stack, String key);
	}

	public interface IBulletContainer
	{
		NonNullList<ItemStack> getBullets(ItemStack container, boolean remote);

		default NonNullList<ItemStack> getBullets(ItemStack container)
		{
			return getBullets(container, EffectiveSide.get()==LogicalSide.CLIENT);
		}

		int getBulletCount(ItemStack container);
	}

	public interface IScrollwheel
	{
		void onScrollwheel(ItemStack stack, PlayerEntity playerEntity, boolean forward);
	}
}
