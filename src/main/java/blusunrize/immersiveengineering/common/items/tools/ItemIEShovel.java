package blusunrize.immersiveengineering.common.items.tools;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.IItemTier;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.item.ShovelItem;
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
		super(materialIn, name, ToolType.SHOVEL, oreDict, ShovelItem.EFFECTIVE_ON, 1.5f, -3.0f);
	}

	@Override
	public boolean canHarvestBlock(BlockState blockIn)
	{
		Block block = blockIn.getBlock();
		return block==Blocks.SNOW_BLOCK||block==Blocks.SNOW;
	}

	@Override
	public ActionResultType onItemUse(ItemUseContext ctx)
	{
		ItemStack itemstack = ctx.getItem();
		PlayerEntity player = ctx.getPlayer();
		BlockPos pos = ctx.getPos();
		Direction facing = ctx.getFace();
		World world = ctx.getWorld();

		if(player==null||!player.canPlayerEdit(pos.offset(facing), facing, itemstack))
			return ActionResultType.FAIL;
		else
		{
			BlockState iblockstate = world.getBlockState(pos);
			Block block = iblockstate.getBlock();

			if(facing!=Direction.DOWN&&world.getBlockState(pos.up()).getMaterial()==Material.AIR&&block==Blocks.GRASS)
			{
				BlockState iblockstate1 = Blocks.GRASS_PATH.getDefaultState();
				world.playSound(player, pos, SoundEvents.ITEM_SHOVEL_FLATTEN, SoundCategory.BLOCKS, 1.0F, 1.0F);

				if(!world.isRemote)
				{
					world.setBlockState(pos, iblockstate1, 11);
					itemstack.damageItem(1, player);
				}

				return ActionResultType.SUCCESS;
			}
			else
				return ActionResultType.PASS;
		}
	}
}
