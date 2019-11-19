/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.items;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.ApiUtils;
import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.api.TargetingInfo;
import blusunrize.immersiveengineering.api.tool.ITool;
import blusunrize.immersiveengineering.api.wires.Connection;
import blusunrize.immersiveengineering.api.wires.GlobalWireNetwork;
import blusunrize.immersiveengineering.api.wires.IImmersiveConnectable;
import blusunrize.immersiveengineering.common.IEConfig;
import blusunrize.immersiveengineering.common.blocks.IEBaseBlock;
import blusunrize.immersiveengineering.common.util.ItemNBTHelper;
import blusunrize.immersiveengineering.common.util.Utils;
import com.google.common.collect.ImmutableSet;
import net.minecraft.block.BlockState;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.ToolType;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

public class WirecutterItem extends IEBaseItem implements ITool
{
	public static final ToolType CUTTER_TOOL = ToolType.get(ImmersiveEngineering.MODID+"_cutter");

	public WirecutterItem()
	{
		super("wirecutter", new Properties().defaultMaxDamage(IEConfig.TOOLS.cutterDurabiliy.get()).setNoRepair());
	}

	@Nonnull
	@Override
	public ItemStack getContainerItem(@Nonnull ItemStack stack)
	{
		ItemStack container = stack.copy();
		container.attemptDamageItem(1, Utils.RAND, null);
		return container;
	}

	@Override
	public boolean hasContainerItem(@Nonnull ItemStack stack)
	{
		return true;
	}

	@Override
	public boolean isDamageable()
	{
		return true;
	}

//	@Override
//	public int getMaxDamageIE(ItemStack stack)
//	{
//		return IEConfig.TOOLS.cutterDurabiliy.get();
//	}

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
	public boolean onBlockStartBreak(ItemStack itemstack, BlockPos pos, PlayerEntity player)
	{
		BlockState state = player.world.getBlockState(pos);
		boolean effective = false;
		for(ToolType tool : getToolTypes(itemstack))
			if(state.getBlock().isToolEffective(state, tool))
			{
				effective = true;
				break;
			}
		itemstack.attemptDamageItem(1, Utils.RAND, null);
		return effective;
	}

	@Override
	public ActionResultType onItemUse(ItemUseContext context)
	{
		World world = context.getWorld();
		BlockPos pos = context.getPos();
		TileEntity tileEntity = world.getTileEntity(pos);
		TargetingInfo target = new TargetingInfo(context.getFace(),
				(float)context.getHitVec().x,
				(float)context.getHitVec().y,
				(float)context.getHitVec().z);
		ItemStack stack = context.getItem();
		PlayerEntity player = context.getPlayer();
		if(player!=null&&tileEntity instanceof IImmersiveConnectable)
		{
			BlockPos masterPos = ((IImmersiveConnectable)tileEntity).getConnectionMaster(null, target);
			tileEntity = world.getTileEntity(masterPos);
			if(!(tileEntity instanceof IImmersiveConnectable))
				return ActionResultType.PASS;

			if(!world.isRemote)
			{
				IImmersiveConnectable nodeHere = (IImmersiveConnectable)tileEntity;
				GlobalWireNetwork net = GlobalWireNetwork.getNetwork(world);
				AtomicBoolean cut = new AtomicBoolean(false);
				net.removeAllConnectionsAt(nodeHere, conn -> {
					ItemStack coil = conn.type.getWireCoil(conn);
					world.addEntity(new ItemEntity(world, pos.getX(), pos.getY(), pos.getZ(), coil));
					cut.set(true);
				});
				if(cut.get())
					damageStack(stack, player, context.getHand());
			}
		}
		return ActionResultType.SUCCESS;
	}

	private void damageStack(ItemStack stack, PlayerEntity player, Hand hand)
	{
		int nbtDamage = ItemNBTHelper.getInt(stack, Lib.NBT_DAMAGE)+1;
		if(nbtDamage < IEConfig.TOOLS.cutterDurabiliy.get())
			ItemNBTHelper.putInt(stack, Lib.NBT_DAMAGE, nbtDamage);
		else
		{
			player.renderBrokenItemStack(stack);
			player.setHeldItem(hand, ItemStack.EMPTY);
		}
	}

	@Nonnull
	@Override
	public ActionResult<ItemStack> onItemRightClick(World world, PlayerEntity player, @Nonnull Hand hand)
	{
		ItemStack stack = player.getHeldItem(hand);
		if(!world.isRemote)
		{
			double reachDistance = player.getAttribute(PlayerEntity.REACH_DISTANCE).getValue();
			Connection target = ApiUtils.getTargetConnection(world, player, null, reachDistance);
			if(target!=null)
			{
				GlobalWireNetwork.getNetwork(world).removeAndDropConnection(target, player.getPosition());
				damageStack(stack, player, hand);
			}
		}
		return new ActionResult<>(ActionResultType.SUCCESS, stack);
	}

	@Override
	public int getHarvestLevel(ItemStack stack, @Nonnull ToolType tool, @Nullable PlayerEntity player, @Nullable BlockState blockState)
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
	public float getDestroySpeed(ItemStack stack, BlockState state)
	{
		for(ToolType type : this.getToolTypes(stack))
			if(state.getBlock().isToolEffective(state, type))
				return 6;
		return super.getDestroySpeed(stack, state);
	}

	@Override
	public boolean canHarvestBlock(ItemStack stack, BlockState state)
	{
		if(state.getBlock() instanceof IEBaseBlock)
		{
			return ((IEBaseBlock)state.getBlock()).allowWirecutterHarvest(state);
		}
		else return state.getBlock().isToolEffective(state, CUTTER_TOOL);
	}

	@Override
	public boolean isTool(ItemStack item)
	{
		return true;
	}
}