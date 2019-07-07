/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.items;

import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.api.tool.ToolboxHandler;
import blusunrize.immersiveengineering.common.CommonProxy;
import blusunrize.immersiveengineering.common.Config;
import blusunrize.immersiveengineering.common.IEContent;
import blusunrize.immersiveengineering.common.blocks.BlockIEBase;
import blusunrize.immersiveengineering.common.items.IEItemInterfaces.IGuiItem;
import com.google.common.collect.Sets;
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
import java.util.Set;

public class ItemToolbox extends ItemInternalStorage implements IGuiItem
{
	public static final int SLOT_COUNT = 23;

	public ItemToolbox()
	{
		super("toolbox", new Properties().maxStackSize(1));
		final Set<String> configTools = Sets.newHashSet(Config.IEConfig.Tools.toolbox_tools);
		final Set<String> configFood = Sets.newHashSet(Config.IEConfig.Tools.toolbox_foods);
		final Set<String> configWires = Sets.newHashSet(Config.IEConfig.Tools.toolbox_wiring);
		ToolboxHandler.addToolType(stack -> configTools.contains(stack.getItem().getRegistryName().toString()));
		ToolboxHandler.addFoodType(stack -> configFood.contains(stack.getItem().getRegistryName().toString()));
		ToolboxHandler.addWiringType((stack, world) -> configWires.contains(stack.getItem().getRegistryName().toString()));
	}

	@Override
	public int getGuiID(ItemStack stack)
	{
		return Lib.GUIID_Toolbox;
	}

	@Nonnull
	@Override
	public ActionResult<ItemStack> onItemRightClick(World world, PlayerEntity player, @Nonnull Hand hand)
	{
		ItemStack stack = player.getHeldItem(hand);
		if(!world.isRemote)
			CommonProxy.openGuiForItem(player, hand==Hand.MAIN_HAND?EquipmentSlotType.MAINHAND: EquipmentSlotType.OFFHAND);
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
				BlockState toolbox = IEContent.blockToolbox.getDefaultState();
				if(world.setBlockState(pos, toolbox, 3))
				{
					((BlockIEBase)IEContent.blockToolbox).onIEBlockPlacedBy(new BlockItemUseContext(ctx), toolbox);

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
	public int getSlotCount(ItemStack stack)
	{
		return SLOT_COUNT;
	}
}
