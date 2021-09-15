/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.cloth;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.common.blocks.BlockItemIE;
import blusunrize.immersiveengineering.common.register.IEBlocks.Cloth;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class BlockItemBalloon extends BlockItemIE
{
	public BlockItemBalloon(Block b)
	{
		super(b, new Item.Properties().tab(ImmersiveEngineering.ITEM_GROUP));
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level worldIn, Player playerIn, InteractionHand hand)
	{
		ItemStack itemStackIn = playerIn.getItemInHand(hand);
		if(playerIn.isShiftKeyDown())
			increaseOffset(itemStackIn);
		else
		{
			Vec3 pos = playerIn.position().add(0, playerIn.getEyeHeight(), 0).add(playerIn.getLookAngle());
			BlockPos bPos = new BlockPos(pos);
			int offset = getOffset(itemStackIn);
			bPos = bPos.above(offset);
			if(worldIn.isEmptyBlock(bPos))
			{
				if(!worldIn.isClientSide)
				{
					worldIn.setBlockAndUpdate(bPos, Cloth.BALLOON.defaultBlockState());
					itemStackIn.shrink(1);
					if(itemStackIn.getCount() <= 0)
						playerIn.setItemInHand(hand, ItemStack.EMPTY);
				}
				return new InteractionResultHolder<>(InteractionResult.SUCCESS, itemStackIn);
			}
		}
		return new InteractionResultHolder<>(InteractionResult.PASS, itemStackIn);
	}

	@Override
	public InteractionResult useOn(UseOnContext context)
	{
		Player player = context.getPlayer();
		if(player!=null&&player.isShiftKeyDown())
		{
			ItemStack stack = player.getItemInHand(context.getHand());
			increaseOffset(stack);
			return InteractionResult.SUCCESS;
		}
		return super.useOn(context);
	}

	@Override
	protected boolean placeBlock(BlockPlaceContext context, BlockState newState)
	{
		int offset = getOffset(context.getItemInHand());
		context = BlockPlaceContext.at(context, context.getClickedPos().above(offset), context.getClickedFace());
		return super.placeBlock(context, newState);
	}

	@Override
	public Component getName(ItemStack stack)
	{
		MutableComponent ret = super.getName(stack).copy();
		CompoundTag nbt = stack.getOrCreateTag();
		if(nbt.getByte("offset")!=0)
			ret.append(" (+"+nbt.getByte("offset")+")");
		return ret;
	}

	private void increaseOffset(ItemStack s)
	{
		CompoundTag tag = s.getOrCreateTag();
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
