package blusunrize.immersiveengineering.common.items.tools;

import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.IItemTier;
import net.minecraft.item.ItemAxe;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.ToolType;

/**
 * @author BluSunrize - 08.07.2018
 */
public class ItemIEAxe extends ItemToolBase
{
	public ItemIEAxe(IItemTier materialIn, String name, ResourceLocation oreDict)
	{
		super(materialIn, name, ToolType.AXE, oreDict, ItemAxe.EFFECTIVE_ON, 5.5f, -3.1f);
	}

	@Override
	public float getDestroySpeed(ItemStack stack, IBlockState state)
	{
		Material material = state.getMaterial();
		return material!=Material.WOOD&&material!=Material.PLANTS&&material!=Material.VINE?super.getDestroySpeed(stack, state): this.efficiency;
	}
}
