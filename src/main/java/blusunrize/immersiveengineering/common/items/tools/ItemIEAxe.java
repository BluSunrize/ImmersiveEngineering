package blusunrize.immersiveengineering.common.items.tools;

import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;

/**
 * @author BluSunrize - 08.07.2018
 */
public class ItemIEAxe extends ItemToolBase
{
	public ItemIEAxe(ToolMaterial materialIn, String name, String toolclass, String oreDict)
	{
		super(materialIn, name, toolclass, oreDict, AXE_EFFECTIVE, 5.5f, -3.1f);
	}

	@Override
	public float getDestroySpeed(ItemStack stack, IBlockState state)
	{
		Material material = state.getMaterial();
		return material!=Material.WOOD&&material!=Material.PLANTS&&material!=Material.VINE?super.getDestroySpeed(stack, state): this.efficiency;
	}
}
