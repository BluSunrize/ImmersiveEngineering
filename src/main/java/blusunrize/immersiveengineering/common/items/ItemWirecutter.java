/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.items;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.api.TargetingInfo;
import blusunrize.immersiveengineering.api.energy.wires.IImmersiveConnectable;
import blusunrize.immersiveengineering.api.energy.wires.old.ImmersiveNetHandler;
import blusunrize.immersiveengineering.api.tool.ITool;
import blusunrize.immersiveengineering.common.IESaveData;
import blusunrize.immersiveengineering.common.blocks.BlockIEBase;
import blusunrize.immersiveengineering.common.items.IEItemInterfaces.IItemDamageableIE;
import blusunrize.immersiveengineering.common.util.ItemNBTHelper;
import blusunrize.immersiveengineering.common.util.Utils;
import com.google.common.collect.ImmutableSet;
import net.minecraft.block.state.IBlockState;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Enchantments;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.ToolType;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Set;

import static blusunrize.immersiveengineering.common.Config.IEConfig.Tools.cutterDurabiliy;

public class ItemWirecutter extends ItemIEBase implements ITool, IItemDamageableIE
{
	public static final ToolType CUTTER_TOOL = ToolType.get(ImmersiveEngineering.MODID+":cutter");

	public ItemWirecutter()
	{
		super("wirecutter", new Properties().maxStackSize(1).setNoRepair());
	}

	@Nonnull
	@Override
	public ItemStack getContainerItem(@Nonnull ItemStack stack)
	{
		ItemStack container = stack.copy();
		this.damageIETool(container, 1, Utils.RAND, null);
		return container;
	}

	@Override
	public boolean isDamageable()
	{
		return true;
	}

	@Override
	public int getMaxDamageIE(ItemStack stack)
	{
		return cutterDurabiliy;
	}

	@Override
	public boolean isEnchantable(@Nonnull ItemStack stack)
	{
		return true;
	}

	@Override
	public int getItemEnchantability()
	{
		return 14;
	}

	@Override
	public boolean canApplyAtEnchantingTable(ItemStack stack, Enchantment enchantment)
	{
		return enchantment==Enchantments.EFFICIENCY||enchantment==Enchantments.UNBREAKING||enchantment==Enchantments.MENDING;
	}

	@Override
	public boolean onBlockStartBreak(ItemStack itemstack, BlockPos pos, EntityPlayer player)
	{
		IBlockState state = player.world.getBlockState(pos);
		boolean effective = false;
		for(ToolType tool : getToolTypes(itemstack))
			if(state.getBlock().isToolEffective(state, tool))
			{
				effective = true;
				break;
			}
		this.damageIETool(itemstack, effective?1: 2, player.getRNG(), player);
		return effective;
	}

	@Override
	public EnumActionResult onItemUse(ItemUseContext context)
	{
		World world = context.getWorld();
		BlockPos pos = context.getPos();
		TileEntity tileEntity = world.getTileEntity(pos);
		TargetingInfo target = new TargetingInfo(context.getFace(), context.getHitX(), context.getHitY(), context.getHitZ());
		ItemStack stack = context.getItem();
		EntityPlayer player = context.getPlayer();
		if(player!=null&&tileEntity instanceof IImmersiveConnectable)
		{
			BlockPos masterPos = ((IImmersiveConnectable)tileEntity).getConnectionMaster(null, target);
			tileEntity = world.getTileEntity(masterPos);
			if(!(tileEntity instanceof IImmersiveConnectable))
				return EnumActionResult.PASS;

			if(!world.isRemote)
			{
				IImmersiveConnectable nodeHere = (IImmersiveConnectable)tileEntity;
				boolean cut = ImmersiveNetHandler.INSTANCE.clearAllConnectionsFor(Utils.toCC(nodeHere), world, target);
				IESaveData.setDirty(world.getDimension());
				if(cut)
				{
					int nbtDamage = ItemNBTHelper.getInt(stack, Lib.NBT_DAMAGE)+1;
					if(nbtDamage < cutterDurabiliy)
						ItemNBTHelper.setInt(stack, Lib.NBT_DAMAGE, nbtDamage);
					else
					{
						player.renderBrokenItemStack(stack);
						player.setItemStackToSlot(EntityEquipmentSlot.MAINHAND, ItemStack.EMPTY);
					}
				}
			}
		}
		return EnumActionResult.SUCCESS;
	}

	@Nonnull
	@Override
	public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, @Nonnull EnumHand hand)
	{
		ItemStack stack = player.getHeldItem(hand);
		if(!world.isRemote)
		{
			//TODO
			//double reachDistance = player.getAttributeMap().getAttributeInstance(EntityPlayer.REACH_DISTANCE).getAttributeValue();
			//Connection target = ApiUtils.getTargetConnection(world, player, null, reachDistance);
			//if(target!=null)
			//	ImmersiveNetHandler.INSTANCE.removeConnectionAndDrop(target, world, player.getPosition());
		}
		return new ActionResult<>(EnumActionResult.SUCCESS, stack);
	}

	@Override
	public int getHarvestLevel(ItemStack stack, @Nonnull ToolType tool, @Nullable EntityPlayer player, @Nullable IBlockState blockState)
	{
		if(getToolTypes(stack).contains(tool))
			return 2;
		else
			return -1;
	}


	@Nonnull
	@Override
	public Set<ToolType> getToolTypes(ItemStack stack)
	{
		return ImmutableSet.of(CUTTER_TOOL);
	}

	@Override
	public float getDestroySpeed(ItemStack stack, IBlockState state)
	{
		for(ToolType type : this.getToolTypes(stack))
			if(state.getBlock().isToolEffective(state, type))
				return 6;
		return super.getDestroySpeed(stack, state);
	}

	@Override
	public boolean canHarvestBlock(ItemStack stack, IBlockState state)
	{
		if(state.getBlock() instanceof BlockIEBase)
		{
			if(((BlockIEBase)state.getBlock()).allowWirecutterHarvest(state))
				return true;
		}
		else if(state.getBlock().isToolEffective(state, CUTTER_TOOL))
			return true;
		return false;
	}

	@Override
	public boolean isTool(ItemStack item)
	{
		return true;
	}
}