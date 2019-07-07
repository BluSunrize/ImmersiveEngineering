package blusunrize.immersiveengineering.common.items.tools;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.material.Material;
import net.minecraft.item.IItemTier;
import net.minecraft.item.ItemStack;
import net.minecraft.item.PickaxeItem;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.ToolType;

/**
 * @author BluSunrize - 08.07.2018
 */
public class ItemIEPickaxe extends ItemToolBase
{
	public ItemIEPickaxe(IItemTier materialIn, String name, ResourceLocation oreDict)
	{
		super(materialIn, name, ToolType.PICKAXE, oreDict, PickaxeItem.EFFECTIVE_ON, 1.0f, -2.8f);
	}

	@Override
	public boolean canHarvestBlock(BlockState blockIn)
	{
		Block block = blockIn.getBlock();
		if(block==Blocks.OBSIDIAN)
			return getTier().getHarvestLevel()==3;
		else if(block!=Blocks.DIAMOND_BLOCK&&block!=Blocks.DIAMOND_ORE)
			if(block!=Blocks.EMERALD_ORE&&block!=Blocks.EMERALD_BLOCK)
				if(block!=Blocks.GOLD_BLOCK&&block!=Blocks.GOLD_ORE)
					if(block!=Blocks.IRON_BLOCK&&block!=Blocks.IRON_ORE)
						if(block!=Blocks.LAPIS_BLOCK&&block!=Blocks.LAPIS_ORE)
							if(block!=Blocks.REDSTONE_ORE)
							{
								Material material = blockIn.getMaterial();
								if(material==Material.ROCK)
									return true;
								else if(material==Material.IRON)
									return true;
								else
									return material==Material.ANVIL;
							}
							else
								return getTier().getHarvestLevel() >= 2;
						else
							return getTier().getHarvestLevel() >= 1;
					else
						return getTier().getHarvestLevel() >= 1;
				else
					return getTier().getHarvestLevel() >= 2;
			else
				return getTier().getHarvestLevel() >= 2;
		else
			return getTier().getHarvestLevel() >= 2;
	}

	@Override
	public float getDestroySpeed(ItemStack stack, BlockState state)
	{
		Material material = state.getMaterial();
		return material!=Material.IRON&&material!=Material.ANVIL&&material!=Material.ROCK?super.getDestroySpeed(stack, state): this.efficiency;
	}
}
