/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.items;

import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;

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

	public interface IGuiItem
	{
		int getGuiID(ItemStack stack);
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
			return FluidUtil.getFluidContained(container);
		}
	}

	public interface ITextureOverride
	{
		@SideOnly(Side.CLIENT)
		String getModelCacheKey(ItemStack stack);

		@SideOnly(Side.CLIENT)
		List<ResourceLocation> getTextures(ItemStack stack, String key);
	}

	public interface IBulletContainer
	{
		NonNullList<ItemStack> getBullets(ItemStack container, boolean remote);

		default NonNullList<ItemStack> getBullets(ItemStack container)
		{
			return getBullets(container, FMLCommonHandler.instance().getEffectiveSide()==Side.CLIENT);
		}

		int getBulletCount(ItemStack container);
	}

	public interface IItemDamageableIE
	{

		int getMaxDamageIE(ItemStack stack);

		int getItemDamageIE(ItemStack stack);
	}
}
