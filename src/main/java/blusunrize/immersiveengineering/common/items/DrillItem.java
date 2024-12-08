/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.items;

import blusunrize.immersiveengineering.api.IETags;
import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.api.client.TextUtils;
import blusunrize.immersiveengineering.api.tool.IDrillHead;
import blusunrize.immersiveengineering.api.tool.upgrade.UpgradeEffect;
import blusunrize.immersiveengineering.common.fluids.IEItemFluidHandler;
import blusunrize.immersiveengineering.common.gui.IESlot;
import blusunrize.immersiveengineering.common.util.IESounds;
import blusunrize.immersiveengineering.common.util.Utils;
import com.google.common.collect.ImmutableList;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockUpdatePacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.ClipContext.Fluid;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.HitResult;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.EventBusSubscriber.Bus;
import net.neoforged.neoforge.capabilities.Capabilities.ItemHandler;
import net.neoforged.neoforge.common.CommonHooks;
import net.neoforged.neoforge.common.NeoForgeMod;
import net.neoforged.neoforge.event.entity.player.PlayerEvent.HarvestCheck;
import net.neoforged.neoforge.items.IItemHandler;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Predicate;
import java.util.function.Supplier;

import static blusunrize.immersiveengineering.common.register.IEDataComponents.DRILL_SINGLEBLOCK;

@EventBusSubscriber(modid = Lib.MODID, bus = Bus.GAME)
public class DrillItem extends DieselToolItem
{
	public static final String TYPE = "DRILL";

	public DrillItem()
	{
		super(new Properties().stacksTo(1).component(DRILL_SINGLEBLOCK, false), TYPE, 5);
	}

	/* ------------- WORKBENCH & INVENTORY ------------- */
	@Override
	public Slot[] getWorkbenchSlots(AbstractContainerMenu container, ItemStack stack, Level level, Supplier<Player> getPlayer, IItemHandler toolInventory)
	{
		return new Slot[]{
				new IESlot.WithPredicate(toolInventory, 0, 98, 22, (itemStack) -> itemStack.getItem() instanceof IDrillHead),
				new IESlot.Upgrades(container, toolInventory, 1, 78, 52, TYPE, stack, true, level, getPlayer),
				new IESlot.Upgrades(container, toolInventory, 2, 98, 52, TYPE, stack, true, level, getPlayer),
				new IESlot.Upgrades(container, toolInventory, 3, 118, 52, TYPE, stack, true, level, getPlayer)
		};
	}

	@Override
	public void removeFromWorkbench(Player player, ItemStack stack)
	{
		IItemHandler inv = stack.getCapability(ItemHandler.ITEM);
		if(inv!=null)
		{
			if(!inv.getStackInSlot(0).isEmpty()&&!inv.getStackInSlot(1).isEmpty()&&!inv.getStackInSlot(2).isEmpty()&&!inv.getStackInSlot(3).isEmpty())
				Utils.unlockIEAdvancement(player, "tools/upgrade_drill");
		}
	}


	@Override
	public void finishUpgradeRecalculation(ItemStack stack, RegistryAccess registries)
	{
		super.finishUpgradeRecalculation(stack, registries);
		final var fortune = registries.registryOrThrow(Registries.ENCHANTMENT)
				.getHolder(Enchantments.FORTUNE)
				.orElseThrow();
		final var newEnchantments = new ItemEnchantments.Mutable(
				stack.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY)
		);
		if(getUpgradesStatic(stack).has(UpgradeEffect.FORTUNE))
		{
			newEnchantments.set(fortune, 3);
		}
		else
		{
			newEnchantments.removeIf(Predicate.isEqual(fortune));
		}
		EnchantmentHelper.setEnchantments(stack, newEnchantments.toImmutable());
	}

	@Override
	public final ItemStack getHead(ItemStack drill)
	{
		return getHeadStatic(drill);
	}

	public static ItemStack getHeadStatic(ItemStack drill)
	{
		IItemHandler cap = drill.getCapability(ItemHandler.ITEM);
		if(cap!=null)
		{
			ItemStack head = cap.getStackInSlot(0);
			return !head.isEmpty()&&head.getItem() instanceof IDrillHead?head: ItemStack.EMPTY;
		}
		return ItemStack.EMPTY;
	}

	@Override
	public void setHead(ItemStack drill, ItemStack head)
	{
		setHeadStatic(drill, head);
	}

	public static void setHeadStatic(ItemStack drill, ItemStack head)
	{
		// We do not have caps in datagen, this "should" be drill.getCapability(...)
		makeInternalItemHandler(drill).setStackInSlot(0, head);
	}

	@Override
	public void appendHoverText(ItemStack stack, TooltipContext ctx, List<Component> list, TooltipFlag flag)
	{
		list.add(IEItemFluidHandler.fluidItemInfoFlavor(getFluid(stack), getCapacity(stack, CAPACITY)));
		if(getHead(stack).isEmpty())
			list.add(TextUtils.applyFormat(
					Component.translatable(Lib.DESC_FLAVOUR+"drill.noHead"),
					ChatFormatting.GRAY
			));
		else
		{
			int maxDmg = getMaxHeadDamage(stack);
			int dmg = maxDmg-getHeadDamage(stack);
			float quote = dmg/(float)maxDmg;
			ChatFormatting status = quote < .1?ChatFormatting.RED: quote < .3?ChatFormatting.GOLD: quote < .6?ChatFormatting.YELLOW: ChatFormatting.GREEN;
			list.add(TextUtils.applyFormat(Component.translatable(Lib.DESC_FLAVOUR+"drill.headDamage"), ChatFormatting.GRAY)
					.append(" ")
					.append(TextUtils.applyFormat(
							Component.translatable(Lib.DESC_INFO+"percent", (int)(quote*100)),
							status
					)));
		}
	}

	@Override
	protected double getAttackDamage(ItemStack stack, ItemStack head)
	{
		return ((IDrillHead)head.getItem()).getAttackDamage(head)+getUpgrades(stack).get(UpgradeEffect.DAMAGE);
	}

	/* ------------- DIGGING ------------- */
	public static boolean isSingleBlockMode(ItemStack stack)
	{
		return stack.getOrDefault(DRILL_SINGLEBLOCK, false);
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand)
	{
		ItemStack stack = player.getItemInHand(hand);
		if(player.isShiftKeyDown())
		{
			boolean mode = !isSingleBlockMode(stack);
			stack.set(DRILL_SINGLEBLOCK, mode);
			player.displayClientMessage(
					Component.translatable(Lib.CHAT_INFO+"drill_mode."+(mode?"single": "multi")), true
			);
			return InteractionResultHolder.success(stack);
		}
		return InteractionResultHolder.pass(stack);
	}

	public boolean canToolBeUsed(ItemStack drill)
	{
		return getHeadDamage(drill) < getMaxHeadDamage(drill)&&!getFluid(drill).isEmpty();
	}

	@SubscribeEvent
	public static void handleUnderwaterDrill(HarvestCheck ev)
	{
		ItemStack drill = ev.getEntity().getInventory().getSelected();
		if(!(drill.getItem() instanceof DrillItem drillItem))
			return;
		if(ev.getEntity().isEyeInFluidType(NeoForgeMod.WATER_TYPE.value())&&!drillItem.getUpgrades(drill).has(UpgradeEffect.WATERPROOF))
			ev.setCanHarvest(false);
	}

	@Override
	public int getMaxHeadDamage(ItemStack stack)
	{
		ItemStack head = getHead(stack);
		return !head.isEmpty()?((IDrillHead)head.getItem()).getMaximumHeadDamage(head): 0;
	}

	@Override
	public int getHeadDamage(ItemStack stack)
	{
		ItemStack head = getHead(stack);
		return !head.isEmpty()?((IDrillHead)head.getItem()).getHeadDamage(head): 0;
	}

	@Override
	protected void damageHead(ItemStack head, int amount, LivingEntity living)
	{
		((IDrillHead)head.getItem()).damageHead(head, amount);
	}

	@Override
	public Holder<SoundEvent> getIdleSound(ItemStack stack)
	{
		return IESounds.drill_idle;
	}

	@Override
	public Holder<SoundEvent> getBusySound(ItemStack stack)
	{
		return IESounds.drill_busy;
	}

	@Override
	public Holder<SoundEvent> getFadingSound(ItemStack stack)
	{
		return IESounds.drill_fade;
	}


	@Override
	public Holder<SoundEvent> getAttackSound(ItemStack stack)
	{
		return IESounds.drill_attack;
	}

	@Override
	public Holder<SoundEvent> getHarvestSound(ItemStack stack)
	{
		return IESounds.drill_harvest;
	}

	@Override
	public boolean ableToMakeNoise(ItemStack stack)
	{
		return canToolBeUsed(stack);
	}

	@Override
	public boolean noisySameStack(ItemStack mainStack, ItemStack otherStack)
	{
		return mainStack.getItem() instanceof DrillItem drillItem&&drillItem.equals(otherStack.getItem());
	}

	@Override
	public Tier getHarvestLevel(ItemStack stack, @Nullable Player player)
	{
		ItemStack head = getHead(stack);
		if(!head.isEmpty()&&canToolBeUsed(stack))
			return ((IDrillHead)head.getItem()).getMiningLevel(head);
		return null;
	}

	@Override
	public boolean isEffective(ItemStack stack, BlockState state)
	{
		return state.is(IETags.drillHarvestable);
	}

	@Override
	public float getDestroySpeed(ItemStack stack, BlockState state)
	{
		ItemStack head = getHead(stack);
		if(!head.isEmpty()&&canToolBeUsed(stack))
			return ((IDrillHead)head.getItem()).getMiningSpeed(head)+getUpgrades(stack).get(UpgradeEffect.SPEED);
		return super.getDestroySpeed(stack, state);
	}

	public boolean canBreakExtraBlock(
			Level world, BlockPos pos, BlockState state, Player player, ItemStack drill, ItemStack head
	)
	{
		if(!state.canHarvestBlock(world, pos, player)||!isEffective(drill, state)||!canToolBeUsed(drill))
			return false;
		return !((IDrillHead)head.getItem()).beforeBlockbreak(drill, head, player);
	}


	@Override
	public boolean mineBlock(ItemStack stack, Level world, BlockState centerState, BlockPos centerPos, LivingEntity entity)
	{
		// early exit for client
		if(world.isClientSide||!(entity instanceof ServerPlayer player))
			return false;
		// Damage head for center block
		onBlockMined(centerState, world, centerPos, stack, entity);
		// if sneaking or toggled, don't multi-mine
		if(player.isShiftKeyDown()||isSingleBlockMode(stack))
			return false;
		HitResult mop = getPlayerPOVHitResult(world, player, Fluid.NONE);
		ItemStack head = getHead(stack);
		if(mop==null||head.isEmpty()||!canToolBeUsed(stack))
			return false;
		ImmutableList<BlockPos> additional = ((IDrillHead)head.getItem()).getExtraBlocksDug(head, world, player, mop);
		for(BlockPos pos : additional)
		{
			if(!world.hasChunkAt(pos))
				continue;
			BlockState state = world.getBlockState(pos);
			Block block = state.getBlock();

			if(!state.isAir()&&state.getDestroyProgress(player, world, pos)!=0)
			{
				if(!this.canBreakExtraBlock(world, pos, state, player, stack, head))
					continue;
				var event = CommonHooks.fireBlockBreak(
						world, player.gameMode.getGameModeForPlayer(), player, pos, state
				);
				if(event.isCanceled())
					continue;

				if(player.getAbilities().instabuild)
				{
					if(block.onDestroyedByPlayer(state, world, pos, player, false, state.getFluidState()))
						block.destroy(world, pos, state);
				}
				else
				{
					BlockEntity te = world.getBlockEntity(pos);
					//implicitly damages head
					onBlockMined(state, world, pos, stack, entity);
					if(block.onDestroyedByPlayer(state, world, pos, player, true, state.getFluidState()))
					{
						block.destroy(world, pos, state);
						block.playerDestroy(world, player, pos, state, te, stack);
						// TODO
						//if(world instanceof ServerLevel)
						//	block.popExperience((ServerLevel)world, pos, xpDropEvent);
					}
				}
				world.levelEvent(2001, pos, Block.getId(state));
				player.connection.send(new ClientboundBlockUpdatePacket(world, pos));
			}
		}
		return false;
	}

	private void onBlockMined(BlockState state, Level world, BlockPos pos, ItemStack stack, LivingEntity entity)
	{
		if(state.getDestroySpeed(world, pos)!=0)
		{
			ItemStack head = getHead(stack);
			if(!head.isEmpty())
			{
				if(entity instanceof Player player)
				{
					if(player.getAbilities().instabuild)
						return;
					((IDrillHead)head.getItem()).afterBlockbreak(stack, head, player);
				}
				consumeDurability(stack, world, state, pos, entity);
			}
		}
	}
}
