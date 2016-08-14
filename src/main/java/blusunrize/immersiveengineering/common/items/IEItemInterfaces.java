package blusunrize.immersiveengineering.common.items;

import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
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
		default boolean allowFluid(ItemStack container, FluidStack fluid){return true;}
		default FluidStack getFluid(ItemStack container){return FluidUtil.getFluidContained(container);}
	}

	public interface ITextureOverride
	{
		@SideOnly(Side.CLIENT)
		String getModelCacheKey(ItemStack stack);

		@SideOnly(Side.CLIENT)
		List<ResourceLocation> getTextures(ItemStack stack, String key);
	}
}
