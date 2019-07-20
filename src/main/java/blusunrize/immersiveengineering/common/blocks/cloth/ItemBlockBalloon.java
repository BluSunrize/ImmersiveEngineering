/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.cloth;

import blusunrize.immersiveengineering.common.IEContent;
import blusunrize.immersiveengineering.common.blocks.ItemBlockIEBase;
import net.minecraft.block.Block;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;

public class ItemBlockBalloon extends ItemBlockIEBase
{

	public ItemBlockBalloon(Block b)
	{
		super(b);
	}

	@Override
	public ActionResult<ItemStack> onItemRightClick(World worldIn, PlayerEntity playerIn, Hand hand)
	{
		ItemStack itemStackIn = playerIn.getHeldItem(hand);
		if(playerIn.isSneaking())
			increaseOffset(itemStackIn);
		else
		{
			Vec3d pos = playerIn.getPositionVector().add(0, playerIn.getEyeHeight(), 0).add(playerIn.getLookVec());
			BlockPos bPos = new BlockPos(pos);
			CompoundNBT nbt = itemStackIn.getOrCreateTag();
			int offset = nbt.getByte("offset");
			bPos = bPos.up(offset);
			if(worldIn.isAirBlock(bPos))
			{
				if(!worldIn.isRemote)
				{
					worldIn.setBlockState(bPos, IEContent.blockBalloon.getDefaultState());
					itemStackIn.shrink(1);
					if(itemStackIn.getCount() <= 0)
						playerIn.setHeldItem(hand, ItemStack.EMPTY);
				}
				return new ActionResult<>(ActionResultType.SUCCESS, itemStackIn);
			}
		}
		return new ActionResult<>(ActionResultType.PASS, itemStackIn);
	}

	@Override
	public ActionResultType onItemUse(PlayerEntity playerIn, World worldIn, BlockPos pos, Hand hand, Direction side, float hitX, float hitY, float hitZ)
	{
		ItemStack stack = playerIn.getHeldItem(hand);
		if(playerIn.isSneaking())
		{
			increaseOffset(stack);
			return ActionResultType.SUCCESS;
		}
		return super.onItemUse(playerIn, worldIn, pos, hand, side, hitX, hitY, hitZ);
	}

	@Override
	public ITextComponent getDisplayName(ItemStack stack)
	{
		ITextComponent ret = super.getDisplayName(stack);
		CompoundNBT nbt = stack.getOrCreateTag();
		if(nbt.getByte("offset")!=0)
			ret.appendText(" (+"+nbt.getByte("offset")+")");
		return ret;
	}

	private void increaseOffset(ItemStack s)
	{
		CompoundNBT tag = s.getOrCreateTag();
		byte offset = tag.getByte("offset");
		tag.putByte("offset", (byte)((offset+1)%5));
	}
}
