package blusunrize.immersiveengineering.common.items.tools;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.IItemTier;
import net.minecraft.item.ItemSpade;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.ToolType;

/**
 * @author BluSunrize - 08.07.2018
 */
public class ItemIEShovel extends ItemToolBase
{
	public ItemIEShovel(IItemTier materialIn, String name, ResourceLocation oreDict)
	{
		super(materialIn, name, ToolType.SHOVEL, oreDict, ItemSpade.EFFECTIVE_ON, 1.5f, -3.0f);
	}

	@Override
	public boolean canHarvestBlock(IBlockState blockIn)
	{
		Block block = blockIn.getBlock();
		return block==Blocks.SNOW_BLOCK||block==Blocks.SNOW;
	}

	@Override
	public EnumActionResult onItemUse(ItemUseContext ctx)
	{
		ItemStack itemstack = ctx.getItem();
		EntityPlayer player = ctx.getPlayer();
		BlockPos pos = ctx.getPos();
		EnumFacing facing = ctx.getFace();
		World world = ctx.getWorld();

		if(player==null||!player.canPlayerEdit(pos.offset(facing), facing, itemstack))
			return EnumActionResult.FAIL;
		else
		{
			IBlockState iblockstate = world.getBlockState(pos);
			Block block = iblockstate.getBlock();

			if(facing!=EnumFacing.DOWN&&world.getBlockState(pos.up()).getMaterial()==Material.AIR&&block==Blocks.GRASS)
			{
				IBlockState iblockstate1 = Blocks.GRASS_PATH.getDefaultState();
				world.playSound(player, pos, SoundEvents.ITEM_SHOVEL_FLATTEN, SoundCategory.BLOCKS, 1.0F, 1.0F);

				if(!world.isRemote)
				{
					world.setBlockState(pos, iblockstate1, 11);
					itemstack.damageItem(1, player);
				}

				return EnumActionResult.SUCCESS;
			}
			else
				return EnumActionResult.PASS;
		}
	}
}
