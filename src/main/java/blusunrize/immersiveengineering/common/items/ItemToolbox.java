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
import blusunrize.immersiveengineering.common.blocks.metal.BlockTypes_MetalDecoration2;
import blusunrize.immersiveengineering.common.items.IEItemInterfaces.IGuiItem;
import com.google.common.collect.Sets;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Set;
import java.util.function.BiPredicate;
import java.util.function.Predicate;

public class ItemToolbox extends ItemInternalStorage implements IGuiItem
{
	public static final int SLOT_COUNT = 23;

	public ItemToolbox()
	{
		super("toolbox", 1);
		ToolboxHandler.addToolType(new Predicate<ItemStack>()
		{
			final Set<String> set = Sets.newHashSet(Config.IEConfig.Tools.toolbox_tools);

			@Override
			public boolean test(ItemStack stack)
			{
				return set.contains(stack.getItem().getRegistryName().toString());
			}
		});
		ToolboxHandler.addFoodType(new Predicate<ItemStack>()
		{
			final Set<String> set = Sets.newHashSet(Config.IEConfig.Tools.toolbox_foods);

			@Override
			public boolean test(ItemStack stack)
			{
				return set.contains(stack.getItem().getRegistryName().toString());
			}
		});
		ToolboxHandler.addWiringType(new BiPredicate<ItemStack, World>()
		{
			final Set<String> set = Sets.newHashSet(Config.IEConfig.Tools.toolbox_wiring);

			@Override
			public boolean test(ItemStack stack, World world)
			{
				return set.contains(stack.getItem().getRegistryName().toString());
			}
		});
	}

	@Override
	public int getGuiID(ItemStack stack)
	{
		return Lib.GUIID_Toolbox;
	}

	@Override
	public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand)
	{
		ItemStack stack = player.getHeldItem(hand);
		if(!world.isRemote)
			CommonProxy.openGuiForItem(player, hand==EnumHand.MAIN_HAND?EntityEquipmentSlot.MAINHAND: EntityEquipmentSlot.OFFHAND);
		return new ActionResult<>(EnumActionResult.SUCCESS, stack);
	}

	@Override
	public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ)
	{
		ItemStack stack = player.getHeldItem(hand);
		if(player.isSneaking())
		{
			IBlockState state = world.getBlockState(pos);
			Block block = state.getBlock();
			if(!block.isReplaceable(world, pos))
				pos = pos.offset(side);

			if(stack.getCount()!=0&&player.canPlayerEdit(pos, side, stack)&&world.mayPlace(IEContent.blockMetalDecoration2, pos, false, side, null))
			{
				IBlockState toolbox = IEContent.blockMetalDecoration2.getStateFromMeta(BlockTypes_MetalDecoration2.TOOLBOX.getMeta());
				if(world.setBlockState(pos, toolbox, 3))
				{
					IEContent.blockMetalDecoration2.onIEBlockPlacedBy(world, pos, toolbox, side, hitX, hitY, hitZ, player, stack);

					SoundType soundtype = world.getBlockState(pos).getBlock().getSoundType(world.getBlockState(pos), world, pos, player);
					world.playSound(player, pos, soundtype.getPlaceSound(), SoundCategory.BLOCKS, (soundtype.getVolume()+1.0F)/2.0F, soundtype.getPitch()*0.8F);
					stack.shrink(1);
				}
				return EnumActionResult.SUCCESS;
			}
			else
				return EnumActionResult.FAIL;
		}
		return super.onItemUse(player, world, pos, hand, side, hitX, hitY, hitZ);
	}

	@Override
	public int getSlotCount(ItemStack stack)
	{
		return SLOT_COUNT;
	}
}
