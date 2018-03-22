/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.items;

import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.api.MultiblockHandler;
import blusunrize.immersiveengineering.api.tool.ITool;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces;
import blusunrize.immersiveengineering.common.util.ItemNBTHelper;
import blusunrize.immersiveengineering.common.util.RotationUtil;
import blusunrize.immersiveengineering.common.util.advancements.IEAdvancements;
import com.google.common.collect.ImmutableSet;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Set;

import static blusunrize.immersiveengineering.common.Config.IEConfig.Tools.hammerDurabiliy;

public class ItemHammer extends ItemIEBase implements ITool
{
	public final static String NAME = "hammer";

	//IEConfig.Tools.hammerDurabiliy
	public ItemHammer()
	{
		super(NAME, 1);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack, @Nullable World world, List<String> tooltip, ITooltipFlag flag)
	{
		if (ItemNBTHelper.hasKey(stack, "multiblockPermission"))
		{
			NBTTagList tagList = stack.getTagCompound().getTagList("multiblockPermission", Constants.NBT.TAG_STRING);
			String s = I18n.format(Lib.DESC_INFO + "multiblocksAllowed");
			if (!GuiScreen.isShiftKeyDown())
				tooltip.add(s + " " + I18n.format(Lib.DESC_INFO + "holdShift"));
			else
			{
				tooltip.add(s);
				for (int i = 0; i < tagList.tagCount(); i++)
					tooltip.add(TextFormatting.DARK_GRAY + " " + I18n.format(Lib.DESC_INFO + "multiblock." + tagList.getStringTagAt(i)));
			}
		}
		if (ItemNBTHelper.hasKey(stack, "multiblockInterdiction"))
		{
			NBTTagList tagList = stack.getTagCompound().getTagList("multiblockInterdiction", Constants.NBT.TAG_STRING);
			String s = I18n.format(Lib.DESC_INFO + "multiblockForbidden");
			if (!GuiScreen.isShiftKeyDown())
				tooltip.add(s + " " + I18n.format(Lib.DESC_INFO + "holdShift"));
			else
			{
				tooltip.add(s);
				for (int i = 0; i < tagList.tagCount(); i++)
					tooltip.add(TextFormatting.DARK_GRAY + " " + I18n.format(Lib.DESC_INFO + "multiblock." + tagList.getStringTagAt(i)));
			}
		}
		if (flag.isAdvanced())
		{
			int nbtDamage = ItemNBTHelper.getInt(stack, "hammerDmg");
			tooltip.add("Durability: " + (hammerDurabiliy - nbtDamage) + " / " + hammerDurabiliy);
		}
	}

	@Override
	public boolean hasContainerItem(ItemStack stack)
	{
		return true;
	}

	@Nonnull
	@Override
	public ItemStack getContainerItem(@Nonnull ItemStack stack)
	{
		int nbtDamage = ItemNBTHelper.getInt(stack, "hammerDmg") + 1;
		if (nbtDamage < hammerDurabiliy)
		{
			ItemStack container = stack.copy();
			ItemNBTHelper.setInt(container, "hammerDmg", nbtDamage);
			return container;
		}
		return ItemStack.EMPTY;
	}

	@Nonnull
	@Override
	public EnumActionResult onItemUseFirst(EntityPlayer player, World world, BlockPos pos, EnumFacing side,
										   float hitX, float hitY, float hitZ, EnumHand hand)
	{
		ItemStack stack = player.getHeldItem(hand);
		String[] permittedMultiblocks = null;
		String[] interdictedMultiblocks = null;
		if (ItemNBTHelper.hasKey(stack, "multiblockPermission"))
		{
			NBTTagList list = stack.getTagCompound().getTagList("multiblockPermission", 8);
			permittedMultiblocks = new String[list.tagCount()];
			for (int i = 0; i < permittedMultiblocks.length; i++)
				permittedMultiblocks[i] = list.getStringTagAt(i);
		}
		if (ItemNBTHelper.hasKey(stack, "multiblockInterdiction"))
		{
			NBTTagList list = stack.getTagCompound().getTagList("multiblockInterdiction", 8);
			interdictedMultiblocks = new String[list.tagCount()];
			for (int i = 0; i < interdictedMultiblocks.length; i++)
				interdictedMultiblocks[i] = list.getStringTagAt(i);
		}
		for (MultiblockHandler.IMultiblock mb : MultiblockHandler.getMultiblocks())
			if (mb.isBlockTrigger(world.getBlockState(pos)))
			{
				boolean b = permittedMultiblocks == null;
				if (permittedMultiblocks != null)
					for (String s : permittedMultiblocks)
						if (mb.getUniqueName().equalsIgnoreCase(s))
						{
							b = true;
							break;
						}
				if (!b)
					break;
				if (interdictedMultiblocks != null)
					for (String s : interdictedMultiblocks)
						if (mb.getUniqueName().equalsIgnoreCase(s))
						{
							b = false;
							break;
						}
				if (!b)
					break;
				if (MultiblockHandler.postMultiblockFormationEvent(player, mb, pos, stack).isCanceled())
					continue;
				if (mb.createStructure(world, pos, side, player))
				{
					if (player instanceof EntityPlayerMP)
						IEAdvancements.TRIGGER_MULTIBLOCK.trigger((EntityPlayerMP) player, mb, stack);
					return EnumActionResult.SUCCESS;
				}
			}
		return EnumActionResult.PASS;
	}

	@Nonnull
	@Override
	public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing side,
									  float hitX, float hitY, float hitZ)
	{
		TileEntity tileEntity = world.getTileEntity(pos);
		if (!(tileEntity instanceof IEBlockInterfaces.IDirectionalTile) && !(tileEntity instanceof IEBlockInterfaces.IHammerInteraction) && !(tileEntity instanceof IEBlockInterfaces.IConfigurableSides))
			return RotationUtil.rotateBlock(world, pos, side) ? EnumActionResult.SUCCESS : EnumActionResult.PASS;
		return EnumActionResult.PASS;
	}

	@Override
	public boolean doesSneakBypassUse(ItemStack stack, IBlockAccess world, BlockPos pos, EntityPlayer player)
	{
		return true;
	}

	@Override
	public boolean showDurabilityBar(ItemStack stack)
	{
		return (ItemNBTHelper.getInt(stack, "hammerDmg") > 0);
	}

	@Override
	public double getDurabilityForDisplay(ItemStack stack)
	{
		return ItemNBTHelper.getInt(stack, "hammerDmg") / (double) hammerDurabiliy;
	}

	@Nonnull
	@Override
	public Set<String> getToolClasses(ItemStack stack)
	{
		return ImmutableSet.of(Lib.TOOL_HAMMER);
	}

	@Override
	public boolean isTool(ItemStack item)
	{
		return true;
	}

	@Override
	public int getHarvestLevel(ItemStack stack, @Nonnull String toolClass, @Nullable EntityPlayer player,
							   @Nullable IBlockState blockState)
	{
		if (getToolClasses(stack).contains(toolClass))
			return 2;
		else
			return -1;
	}

	@Override
	public float getDestroySpeed(ItemStack stack, IBlockState state)
	{
		for (String type : this.getToolClasses(stack))
			if (state.getBlock().isToolEffective(type, state))
				return 6;
		return super.getDestroySpeed(stack, state);
	}
}