/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.items;

import blusunrize.immersiveengineering.api.ApiUtils;
import blusunrize.immersiveengineering.api.IETags;
import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.api.TargetingInfo;
import blusunrize.immersiveengineering.api.wires.Connection;
import blusunrize.immersiveengineering.api.wires.GlobalWireNetwork;
import blusunrize.immersiveengineering.api.wires.IImmersiveConnectable;
import blusunrize.immersiveengineering.api.wires.utils.WireUtils;
import blusunrize.immersiveengineering.common.config.IEServerConfig;
import blusunrize.immersiveengineering.common.util.ItemNBTHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.common.Tags;
import net.minecraftforge.common.ToolAction;

import javax.annotation.Nonnull;
import java.util.concurrent.atomic.AtomicBoolean;

public class WirecutterItem extends IEBaseItem
{
	public WirecutterItem()
	{
		super(new Properties().defaultDurability(100));
	}

	@Override
	public int getMaxDamage(ItemStack stack)
	{
		return IEServerConfig.getOrDefault(IEServerConfig.TOOLS.cutterDurabiliy);
	}

	@Nonnull
	@Override
	public ItemStack getCraftingRemainingItem(@Nonnull ItemStack stack)
	{
		ItemStack container = stack.copy();
		if(container.hurt(1, ApiUtils.RANDOM_SOURCE, null))
			return ItemStack.EMPTY;
		else
			return container;
	}

	@Override
	public boolean hasCraftingRemainingItem(@Nonnull ItemStack stack)
	{
		return true;
	}

	@Override
	public boolean canBeDepleted()
	{
		return true;
	}

	@Override
	public boolean isEnchantable(@Nonnull ItemStack stack)
	{
		return true;
	}

	@Override
	public int getEnchantmentValue()
	{
		return 14;
	}

	@Override
	public boolean canApplyAtEnchantingTable(ItemStack stack, Enchantment enchantment)
	{
		return enchantment==Enchantments.BLOCK_EFFICIENCY||enchantment==Enchantments.UNBREAKING||enchantment==Enchantments.MENDING;
	}

	@Override
	public boolean isValidRepairItem(ItemStack stack, ItemStack repairCandidate)
	{
		return repairCandidate.is(Tags.Items.INGOTS_IRON);
	}

	// Block breaking
	@Override
	public boolean mineBlock(ItemStack itemstack, Level pLevel, BlockState state, BlockPos pPos, LivingEntity pEntityLiving)
	{
		boolean effective = state.is(IETags.wirecutterHarvestable);
		itemstack.hurt(1, ApiUtils.RANDOM_SOURCE, null);
		return effective;
	}

	@Override
	public float getDestroySpeed(ItemStack stack, BlockState state)
	{
		if(isCorrectToolForDrops(stack, state))
			return 6;
		return super.getDestroySpeed(stack, state);
	}

	@Override
	public boolean isCorrectToolForDrops(ItemStack stack, BlockState state)
	{
		return state.is(IETags.wirecutterHarvestable);
	}

	@Override
	public boolean canPerformAction(ItemStack stack, ToolAction toolAction)
	{
		return toolAction==Lib.WIRECUTTER_DIG;
	}

	// Wire breaking
	@Override
	public InteractionResult useOn(UseOnContext context)
	{
		Level world = context.getLevel();
		BlockPos pos = context.getClickedPos();
		BlockEntity tileEntity = world.getBlockEntity(pos);
		TargetingInfo target = new TargetingInfo(context.getClickedFace(),
				(float)context.getClickLocation().x,
				(float)context.getClickLocation().y,
				(float)context.getClickLocation().z);
		ItemStack stack = context.getItemInHand();
		Player player = context.getPlayer();
		if(player!=null&&tileEntity instanceof IImmersiveConnectable)
		{
			BlockPos masterPos = ((IImmersiveConnectable)tileEntity).getConnectionMaster(null, target);
			tileEntity = world.getBlockEntity(masterPos);
			if(!(tileEntity instanceof IImmersiveConnectable))
				return InteractionResult.PASS;

			if(!world.isClientSide)
			{
				IImmersiveConnectable nodeHere = (IImmersiveConnectable)tileEntity;
				GlobalWireNetwork net = GlobalWireNetwork.getNetwork(world);
				AtomicBoolean cut = new AtomicBoolean(false);
				net.removeAllConnectionsAt(nodeHere, conn -> {
					ItemStack coil = conn.type.getWireCoil(conn);
					if (world.getGameRules().getBoolean(GameRules.RULE_DOBLOCKDROPS))
					    world.addFreshEntity(new ItemEntity(world, player.getX(), player.getY(), player.getZ(), coil, 0, 0, 0));
					cut.set(true);
				});
				if(cut.get())
					damageStack(stack, player, context.getHand());
			}
		}
		else if(player!=null)
		{
			return use(world, player, context.getHand()).getResult();
		}
		return InteractionResult.SUCCESS;
	}

	@Nonnull
	@Override
	public InteractionResultHolder<ItemStack> use(Level world, Player player, @Nonnull InteractionHand hand)
	{
		ItemStack stack = player.getItemInHand(hand);
		if(!world.isClientSide)
		{
			double reachDistance = player.getAttribute(ForgeMod.BLOCK_REACH.get()).getValue();
			Connection target = WireUtils.getTargetConnection(world, player, null, reachDistance);
			if(target!=null)
			{
				GlobalWireNetwork.getNetwork(world).removeInsertAndDropConnection(target, player, world);
				damageStack(stack, player, hand);
			}
		}
		return new InteractionResultHolder<>(InteractionResult.SUCCESS, stack);
	}

	private void damageStack(ItemStack stack, Player player, InteractionHand hand)
	{
		int nbtDamage = ItemNBTHelper.getInt(stack, Lib.NBT_DAMAGE)+1;
		if(nbtDamage < IEServerConfig.TOOLS.cutterDurabiliy.get())
			ItemNBTHelper.putInt(stack, Lib.NBT_DAMAGE, nbtDamage);
		else
		{
			player.broadcastBreakEvent(hand);
			player.setItemInHand(hand, ItemStack.EMPTY);
		}
	}
}