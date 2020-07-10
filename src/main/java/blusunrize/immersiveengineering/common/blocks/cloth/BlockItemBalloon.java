/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.cloth;

import blusunrize.immersiveengineering.common.blocks.BlockItemIE;
import blusunrize.immersiveengineering.common.blocks.IEBlocks.Cloth;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;

public class BlockItemBalloon extends BlockItemIE
{
	public BlockItemBalloon(Block b, Item.Properties props)
	{
		super(b, props);
	}

	@Override
	public ActionResult<ItemStack> onItemRightClick(World worldIn, PlayerEntity playerIn, Hand hand)
	{
		ItemStack itemStackIn = playerIn.getHeldItem(hand);
		if(playerIn.isSneaking())
			increaseOffset(itemStackIn);
		else
		{
			Vector3d pos = playerIn.getPositionVec().add(0, playerIn.getEyeHeight(), 0).add(playerIn.getLookVec());
			BlockPos bPos = new BlockPos(pos);
			int offset = getOffset(itemStackIn);
			bPos = bPos.up(offset);
			if(worldIn.isAirBlock(bPos))
			{
				if(!worldIn.isRemote)
				{
					worldIn.setBlockState(bPos, Cloth.balloon.getDefaultState());
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
	public ActionResultType onItemUse(ItemUseContext context)
	{
		PlayerEntity player = context.getPlayer();
		if(player!=null&&player.isSneaking())
		{
			ItemStack stack = player.getHeldItem(context.getHand());
			increaseOffset(stack);
			return ActionResultType.SUCCESS;
		}
		return super.onItemUse(context);
	}

	@Override
	protected boolean placeBlock(BlockItemUseContext context, BlockState newState)
	{
		int offset = getOffset(context.getItem());
		context = BlockItemUseContext.func_221536_a(context, context.getPos().up(offset), context.getFace());
		return super.placeBlock(context, newState);
	}

	@Override
	public ITextComponent getDisplayName(ItemStack stack)
	{
		IFormattableTextComponent ret = super.getDisplayName(stack).deepCopy();
		CompoundNBT nbt = stack.getOrCreateTag();
		if(nbt.getByte("offset")!=0)
			ret.func_240702_b_(" (+"+nbt.getByte("offset")+")");
		return ret;
	}

	private void increaseOffset(ItemStack s)
	{
		CompoundNBT tag = s.getOrCreateTag();
		tag.putByte("offset", (byte)((getOffset(s)+1)%5));
	}

	private byte getOffset(ItemStack stack)
	{
		if(stack.hasTag())
			return stack.getOrCreateTag().getByte("offset");
		else
			return 0;
	}
}
