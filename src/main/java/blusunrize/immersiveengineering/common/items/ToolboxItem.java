/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.items;

import blusunrize.immersiveengineering.api.tool.ToolboxHandler;
import blusunrize.immersiveengineering.common.blocks.IEBlocks.MetalDevices;
import blusunrize.immersiveengineering.common.config.IEServerConfig;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nonnull;

public class ToolboxItem extends InternalStorageItem
{
	public static final int SLOT_COUNT = 23;

	public ToolboxItem()
	{
		super("toolbox", new Properties().maxStackSize(1));
		ToolboxHandler.addToolType(stack -> IEServerConfig.TOOLS.toolbox_tools.get().contains(stack.getItem().getRegistryName().toString()));
		ToolboxHandler.addFoodType(stack -> IEServerConfig.TOOLS.toolbox_foods.get().contains(stack.getItem().getRegistryName().toString()));
		ToolboxHandler.addWiringType((stack, world) -> IEServerConfig.TOOLS.toolbox_wiring.get().contains(stack.getItem().getRegistryName().toString()));
	}

	@Nonnull
	@Override
	public ActionResult<ItemStack> onItemRightClick(World world, PlayerEntity player, @Nonnull Hand hand)
	{
		ItemStack stack = player.getHeldItem(hand);
		if(!world.isRemote)
			openGui(player, hand==Hand.MAIN_HAND?EquipmentSlotType.MAINHAND: EquipmentSlotType.OFFHAND);
		return new ActionResult<>(ActionResultType.SUCCESS, stack);
	}

	@Override
	public ActionResultType onItemUse(ItemUseContext ctx)
	{
		ItemStack stack = ctx.getItem();
		PlayerEntity player = ctx.getPlayer();
		if(player!=null && player.isSneaking())
		{
			World world = ctx.getWorld();
			BlockPos pos = ctx.getPos();
			Direction side = ctx.getFace();
			BlockState state = world.getBlockState(pos);
			Block block = state.getBlock();
			if(!block.isReplaceable(state, new BlockItemUseContext(ctx)))
				pos = pos.offset(side);

			if(stack.getCount()!=0&&player.canPlayerEdit(pos, side, stack))//TODO &&world.mayPlace(IEContent.blockToolbox, pos, false, side, null))
			{
				BlockState toolbox = MetalDevices.toolbox.getDefaultState();
				if(world.setBlockState(pos, toolbox, 3))
				{
					MetalDevices.toolbox.get().onIEBlockPlacedBy(new BlockItemUseContext(ctx), toolbox);

					SoundType soundtype = world.getBlockState(pos).getBlock().getSoundType(world.getBlockState(pos), world, pos, player);
					world.playSound(player, pos, soundtype.getPlaceSound(), SoundCategory.BLOCKS, (soundtype.getVolume()+1.0F)/2.0F, soundtype.getPitch()*0.8F);
					stack.shrink(1);
				}
				return ActionResultType.SUCCESS;
			}
			else
				return ActionResultType.FAIL;
		}
		return super.onItemUse(ctx);
	}

	@Override
	public int getSlotCount()
	{
		return SLOT_COUNT;
	}
}
