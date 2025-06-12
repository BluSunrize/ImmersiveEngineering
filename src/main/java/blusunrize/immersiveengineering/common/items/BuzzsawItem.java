/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.items;

import blusunrize.immersiveengineering.api.ApiUtils;
import blusunrize.immersiveengineering.api.IETags;
import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.api.client.TextUtils;
import blusunrize.immersiveengineering.api.tool.upgrade.UpgradeEffect;
import blusunrize.immersiveengineering.common.fluids.IEItemFluidHandler;
import blusunrize.immersiveengineering.common.gui.IESlot;
import blusunrize.immersiveengineering.common.items.IEItemInterfaces.IScrollwheel;
import blusunrize.immersiveengineering.common.items.upgrades.ToolUpgrade;
import blusunrize.immersiveengineering.common.register.IEDataComponents;
import blusunrize.immersiveengineering.common.register.IEItems.Misc;
import blusunrize.immersiveengineering.common.register.IEItems.Tools;
import blusunrize.immersiveengineering.common.util.IESounds;
import blusunrize.immersiveengineering.common.util.Utils;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.Holder.Reference;
import net.minecraft.core.HolderLookup.RegistryLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockUpdatePacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.item.enchantment.ItemEnchantments.Mutable;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.capabilities.Capabilities.ItemHandler;
import net.neoforged.neoforge.common.CommonHooks;
import net.neoforged.neoforge.common.ItemAbilities;
import net.neoforged.neoforge.common.ItemAbility;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.IItemHandlerModifiable;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class BuzzsawItem extends DieselToolItem implements IScrollwheel
{
	public static final Collection<SawbladeItem> SAWBLADES = new ArrayList<>(2);
	public static final String TYPE = "BUZZSAW";

	public BuzzsawItem()
	{
		super(new Properties().stacksTo(1), TYPE, 5);
	}

	/* ------------- WORKBENCH & INVENTORY ------------- */
	@Override
	public Slot[] getWorkbenchSlots(AbstractContainerMenu container, ItemStack stack, Level level, Supplier<Player> getPlayer, IItemHandler toolInventory)
	{
		final boolean hasQuiver = hasQuiverUpgrade(stack);
		final int mainHeadX = hasQuiver?88: 98;
		List<Slot> slots = new ArrayList<>(5);
		slots.add(new IESlot.WithPredicate(
				toolInventory, 0, mainHeadX, 22, BuzzsawItem::isSawblade, newBlade -> setHead(stack, newBlade)
		));
		slots.add(new IESlot.Upgrades(container, toolInventory, 1, 88, 52, TYPE, stack, true, level, getPlayer));
		slots.add(new IESlot.Upgrades(container, toolInventory, 2, 108, 52, TYPE, stack, true, level, getPlayer));
		if(hasQuiverUpgrade(stack))
		{
			slots.add(new IESlot.WithPredicate(toolInventory, 3, 108, 12, BuzzsawItem::isSawblade));
			slots.add(new IESlot.WithPredicate(toolInventory, 4, 108, 32, BuzzsawItem::isSawblade));
		}
		return slots.toArray(new Slot[0]);
	}

	@Override
	public ItemStack getUpgradeAfterRemoval(ItemStack stack, ItemStack upgrade)
	{
		if(isSpareBladeUpgrade(upgrade))
		{
			List<ItemStack> spares = new ArrayList<>();
			forEachSpareBlade(stack, (i, blade) -> spares.add(blade));
			upgrade.set(IEDataComponents.GENERIC_ITEMS, ItemContainerContents.fromItems(spares));
		}
		return upgrade;
	}

	@Override
	public void removeUpgrade(ItemStack stack, Player player, ItemStack upgrade)
	{
		if(isSpareBladeUpgrade(upgrade))
			forEachSpareBlade(stack, (i, $) -> setSawblade(stack, ItemStack.EMPTY, i));
	}

	private void forEachSpareBlade(ItemStack stack, BiConsumer<Integer, ItemStack> onBlade)
	{
		for(int i = 1; i <= 2; i++)
			onBlade.accept(i, getSawblade(stack, i));
	}

	@Override
	public void removeFromWorkbench(Player player, ItemStack stack)
	{
		IItemHandler inv = stack.getCapability(ItemHandler.ITEM);
		if(inv!=null&&!inv.getStackInSlot(0).isEmpty()&&!inv.getStackInSlot(1).isEmpty()&&!inv.getStackInSlot(2).isEmpty())
			Utils.unlockIEAdvancement(player, "tools/upgrade_buzzsaw");
	}

	@Override
	public void recalculateUpgrades(ItemStack stack, Level w, Player player)
	{
		super.recalculateUpgrades(stack, w, player);
		IItemHandler inv = stack.getCapability(ItemHandler.ITEM);
		if(inv instanceof IItemHandlerModifiable modifiable)
			for(int iUpgrade = 1; iUpgrade <= 2; iUpgrade++)
			{
				ItemStack upgrade = inv.getStackInSlot(iUpgrade).copy();
				if(!isSpareBladeUpgrade(upgrade))
					continue;
				var upgradeInventory = upgrade.remove(IEDataComponents.GENERIC_ITEMS);
				if(upgradeInventory==null)
					continue;
				for(int i = 1; i <= upgradeInventory.getSlots(); i++)
				{
					var blade = upgradeInventory.getStackInSlot(i-1);
					if(!blade.isEmpty())
						setSawblade(stack, blade, i);
				}
				modifiable.setStackInSlot(iUpgrade, upgrade);
			}
	}

	@Override
	public ItemStack getHead(ItemStack itemStack)
	{
		return getSawblade(itemStack, 0);
	}

	public static ItemStack getSawblade(ItemStack itemStack, int spare)
	{
		IItemHandler cap = itemStack.getCapability(ItemHandler.ITEM);
		if(cap==null)
			return ItemStack.EMPTY;
		// handle spares
		int slot = spare==0?0: 2+spare;
		ItemStack sawblade = cap.getStackInSlot(slot);
		return !sawblade.isEmpty()&&isSawblade(sawblade)?sawblade: ItemStack.EMPTY;
	}

	@Override
	public void setHead(ItemStack buzzsaw, ItemStack sawblade)
	{
		setSawblade(buzzsaw, sawblade, 0);
	}

	public static void setSawblade(ItemStack buzzsaw, ItemStack sawblade, int spare)
	{
		int slot = spare==0?0: 2+spare;
		makeInternalItemHandler(buzzsaw).setStackInSlot(slot, sawblade);
	}

	@Override
	public int getEnchantmentLevel(ItemStack stack, Holder<Enchantment> enchantment)
	{
		ItemStack sawblade = getSawblade(stack, 0);
		if(sawblade.getItem() instanceof SawbladeItem blade)
		{
			final Integer levelOverride = blade.getExtraEnchantments().get(enchantment.unwrapKey().orElseThrow());
			if(levelOverride!=null)
				return levelOverride;
		}
		return super.getEnchantmentLevel(stack, enchantment);
	}

	@Override
	public ItemEnchantments getAllEnchantments(ItemStack stack, RegistryLookup<Enchantment> lookup)
	{
		ItemStack sawblade = getSawblade(stack, 0);
		ItemEnchantments superEnchants = super.getAllEnchantments(stack, lookup);
		if(sawblade.getItem() instanceof SawbladeItem blade)
		{
			ItemEnchantments.Mutable mutable = new Mutable(superEnchants);
			blade.getExtraEnchantments().forEach((enchantmentResourceKey, integer) ->
					lookup.get(enchantmentResourceKey).ifPresent(enchantmentReference -> mutable.upgrade(enchantmentReference, integer))
			);
			return mutable.toImmutable();
		}
		else
			return superEnchants;
	}

	@Override
	public void appendHoverText(ItemStack stack, TooltipContext ctx, List<Component> list, TooltipFlag flag)
	{
		list.add(IEItemFluidHandler.fluidItemInfoFlavor(getFluid(stack), getCapacity(stack, CAPACITY)));
		if(getHead(stack).isEmpty())
			list.add(TextUtils.applyFormat(
					Component.translatable(Lib.DESC_FLAVOUR+"buzzsaw.noBlade"),
					ChatFormatting.GRAY
			));
		else
		{
			int maxDmg = getMaxHeadDamage(stack);
			int dmg = maxDmg-getHeadDamage(stack);
			float quote = dmg/(float)maxDmg;
			ChatFormatting status = (quote < .1?ChatFormatting.RED: quote < .3?ChatFormatting.GOLD: quote < .6?ChatFormatting.YELLOW: ChatFormatting.GREEN);
			list.add(TextUtils.applyFormat(Component.translatable(Lib.DESC_FLAVOUR+"buzzsaw.bladeDamage"), ChatFormatting.GRAY)
					.append(" ")
					.append(TextUtils.applyFormat(
							Component.translatable(Lib.DESC_INFO+"percent", (int)(quote*100)),
							status
					)));
		}
	}

	@Override
	protected double getAttackDamage(ItemStack stack, ItemStack sawblade)
	{
		return ((SawbladeItem)sawblade.getItem()).getSawbladeDamage();
	}

	@Override
	public void onScrollwheel(ItemStack stack, Player playerEntity, boolean forward)
	{
		if(hasQuiverUpgrade(stack))
		{
			ItemStack sawblade = getHead(stack);
			ItemStack spare1 = getSawblade(stack, 1);
			ItemStack spare2 = getSawblade(stack, 2);
			if(forward)
			{
				setHead(stack, spare2);
				setSawblade(stack, sawblade, 1);
				setSawblade(stack, spare1, 2);
			}
			else
			{
				setHead(stack, spare1);
				setSawblade(stack, spare2, 1);
				setSawblade(stack, sawblade, 2);
			}
		}
	}

	/* ------------- DIGGING ------------- */

	@Override
	public boolean canToolBeUsed(ItemStack stack)
	{
		if(getHeadDamage(stack) >= getMaxHeadDamage(stack))
			return false;
		return !getFluid(stack).isEmpty();
	}

	@Override
	public int getMaxHeadDamage(ItemStack stack)
	{
		ItemStack sawblade = getHead(stack);
		return !sawblade.isEmpty()?sawblade.getMaxDamage(): 0;
	}

	@Override
	public int getHeadDamage(ItemStack stack)
	{
		ItemStack sawblade = getHead(stack);
		return !sawblade.isEmpty()?sawblade.getDamageValue(): 0;
	}

	@Override
	public Holder<SoundEvent> getIdleSound(ItemStack stack)
	{
		return IESounds.buzzsaw_idle;
	}

	@Override
	public Holder<SoundEvent> getBusySound(ItemStack stack)
	{
		return IESounds.buzzsaw_busy;
	}

	@Override
	public Holder<SoundEvent> getFadingSound(ItemStack stack)
	{
		return IESounds.buzzsaw_fade;
	}

	@Override
	public Holder<SoundEvent> getAttackSound(ItemStack stack)
	{
		return IESounds.buzzsaw_attack;
	}

	@Override
	public Holder<SoundEvent> getHarvestSound(ItemStack stack)
	{
		Item headitem = getHead(stack).getItem();
		if(headitem instanceof GrindingDiskItem||headitem instanceof RockcutterItem)
			return IESounds.buzzsaw_harvest_grind;
		return IESounds.buzzsaw_harvest_saw;
	}

	@Override
	public boolean ableToMakeNoise(ItemStack stack)
	{
		return canToolBeUsed(stack);
	}

	@Override
	public boolean noisySameStack(ItemStack mainStack, ItemStack otherStack)
	{
		return mainStack.getItem() instanceof BuzzsawItem buzzsawItem&&buzzsawItem.equals(otherStack.getItem())&&getHead(mainStack).getItem().equals(getHead(otherStack).getItem());
	}

	@Override
	public boolean mineBlock(ItemStack stack, Level world, BlockState state, BlockPos pos, LivingEntity living)
	{
		consumeDurability(stack, world, state, pos, living);
		if(!world.isClientSide&&!living.isShiftKeyDown()&&living instanceof ServerPlayer)
			if(canFellTree(stack)&&canToolBeUsed(stack)&&isTree(world, pos)&&!state.is(IETags.buzzsawTreeBlacklist))
				fellTree(world, pos, (ServerPlayer)living, stack);
		return true;
	}

	@Override
	protected int getToolDamageFromBlock(ItemStack stack, @Nullable BlockState state)
	{
		ItemStack sawblade = getHead(stack);
		if(sawblade.getItem() instanceof SawbladeItem sawbladeItem)
			return sawbladeItem.getSawbladeDamageFromBlock(state==null||isEffective(stack, state));
		return 0;
	}

	@Override
	protected void damageHead(ItemStack head, int amount, LivingEntity living)
	{
		head.hurtAndBreak(amount, living, EquipmentSlot.MAINHAND);
	}

	@Override
	protected void consumeDurability(ItemStack stack, Level world, @Nullable BlockState state, @Nullable BlockPos pos, LivingEntity living)
	{
		if(state==null||!state.is(BlockTags.LEAVES)||ApiUtils.RANDOM.nextInt(10)==0)
			super.consumeDurability(stack, world, state, pos, living);
	}

	@Override
	public Tier getHarvestLevel(ItemStack stack, @Nullable Player player)
	{
		ItemStack sawblade = getHead(stack);
		if(!sawblade.isEmpty())
			return Tiers.DIAMOND;
		return null;
	}

	@Override
	public boolean isEffective(ItemStack stack, BlockState state)
	{
		Predicate<BlockState> mineable = null;
		ItemStack sawblade = getHead(stack);
		if(sawblade.getItem() instanceof SawbladeItem)
			mineable = ((SawbladeItem)sawblade.getItem()).getSawbladeMaterials();

		return mineable!=null&&mineable.test(state);
	}

	@Override
	public float getDestroySpeed(ItemStack stack, BlockState state)
	{
		if(isEffective(stack, state))
		{
			ItemStack sawblade = getHead(stack);
			if(!sawblade.isEmpty()&&canToolBeUsed(stack))
				return ((SawbladeItem)sawblade.getItem()).getSawbladeSpeed();
		}
		return super.getDestroySpeed(stack, state);
	}

	/* ------------- Tool Actions ------------- */

	@Override
	public boolean canPerformAction(ItemStack stack, ItemAbility toolAction)
	{
		ItemStack sawblade = getHead(stack);
		if(sawblade.getItem() instanceof SawbladeItem)
			return ((SawbladeItem)sawblade.getItem()).getItemAbilities().contains(toolAction);
		return false;
	}

	private static final Map<ItemAbility, SoundEvent> ACTION_SOUNDS = new HashMap<>();

	static
	{
		ACTION_SOUNDS.put(ItemAbilities.AXE_STRIP, SoundEvents.AXE_STRIP);
		ACTION_SOUNDS.put(ItemAbilities.AXE_SCRAPE, SoundEvents.AXE_SCRAPE);
		ACTION_SOUNDS.put(ItemAbilities.AXE_WAX_OFF, SoundEvents.AXE_WAX_OFF);
		ACTION_SOUNDS.put(ItemAbilities.SHEARS_CARVE, SoundEvents.PUMPKIN_CARVE);
	}

	@Override
	public InteractionResult useOn(UseOnContext context)
	{
		ItemStack head = getHead(context.getItemInHand());
		if(!(head.getItem() instanceof SawbladeItem sawblade))
			return InteractionResult.PASS;

		Level level = context.getLevel();
		BlockPos pos = context.getClickedPos();
		BlockState state = level.getBlockState(pos);

		Set<ItemAbility> toolActions = sawblade.getItemAbilities();
		for(ItemAbility action : toolActions)
		{
			BlockState transformed = state.getToolModifiedState(context, action, false);
			if(transformed!=null)
			{
				SoundEvent sound = ACTION_SOUNDS.get(action);
				if(sound!=null)
					level.playSound(context.getPlayer(), pos, sound, SoundSource.BLOCKS, 1.0F, 1.0F);
				if(!level.isClientSide)
				{
					level.setBlock(pos, transformed, 11);
					if(context.getPlayer()!=null)
						this.damageHead(head, 1, context.getPlayer());
				}
				return InteractionResult.sidedSuccess(level.isClientSide);
			}
		}
		return InteractionResult.PASS;
	}

	/**
	 * Check if there is a tree sprouting from the given position.
	 * We define a tree as a vertical stack of logs, up to 32 blocks tall
	 * which can go diagonal by one block per level (acacia)
	 * and with a leaf block at its top
	 *
	 * @param world
	 * @param initialPos
	 * @return
	 */
	private boolean isTree(Level world, BlockPos initialPos)
	{
		int logs = 0;
		boolean leafTop = false;
		BlockPos pos = initialPos;
		for(int y = 0; y < 32; y++)
		{
			pos = pos.above();
			BlockState state = world.getBlockState(pos);
			if(state.is(BlockTags.LOGS))
				logs++;
			else
			{
				if(state.is(BlockTags.LEAVES))
					leafTop = true;
				boolean foundLog = false;
				if(!leafTop)
				{
					// Yay, Acacia trees grow diagonally >_>
					boolean loop = true;
					for(int z = -1; z <= 1&&loop; z++)
						for(int x = -1; x <= 1&&loop; x++)
						{
							state = world.getBlockState(pos.offset(x, 0, z));
							if(state.is(BlockTags.LOGS))
							{
								pos = pos.offset(x, 0, z);
								foundLog = true;
								logs++;
								loop = false;
							}
						}
				}
				// If there is no diagonal growth, the tree ends
				if(!foundLog)
					break;
			}
		}
		return logs >= 3&&leafTop;
	}

	private boolean canFellTree(ItemStack stack)
	{
		ItemStack sawblade = getHead(stack);
		if(sawblade.getItem() instanceof SawbladeItem)
			return ((SawbladeItem)sawblade.getItem()).canSawbladeFellTree();
		return false;
	}

	/**
	 * The max distance a block can be from the initial hit
	 * to still be considered part of the tree
	 * This is based on the largest vanilla Jungle Trees
	 */
	private static final int MAX_HORIZONTAL_DISTANCE = 7;

	private boolean fellTree(Level world, BlockPos initialPos, ServerPlayer player, ItemStack stack)
	{
		int logs = 0;
		Deque<BlockPos> openList = new ArrayDeque<>();
		Deque<BlockPos> closedList = new ArrayDeque<>();
		openList.add(initialPos);
		while(!openList.isEmpty()&&closedList.size() < 512&&logs < 256)
		{
			BlockPos next = openList.pollFirst();

			// Ignore blocks too far away
			if(Math.abs(next.getX()-initialPos.getX()) > MAX_HORIZONTAL_DISTANCE
					||Math.abs(next.getZ()-initialPos.getZ()) > MAX_HORIZONTAL_DISTANCE)
				continue;

			if(!closedList.contains(next))
			{
				BlockState state = world.getBlockState(next);
				if(state.is(BlockTags.LOGS))
				{
					closedList.add(next);
					logs++;
					// Find all at same level or above, including diagonals
					for(int y = 0; y <= 1; y++)
						for(int z = -1; z <= 1; z++)
							for(int x = -1; x <= 1; x++)
								openList.add(next.offset(x, y, z));
				}
				else if(state.is(BlockTags.LEAVES))
				{
					closedList.add(next);
					int trunkDist = state.getBlock() instanceof LeavesBlock?state.getValue(LeavesBlock.DISTANCE): 0;
					// Leaves only propagate in cardinal directions, and only to other leaves
					for(Direction dir : new Direction[]{Direction.UP, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST})
					{
						BlockPos adj = next.relative(dir);
						BlockState adjState = world.getBlockState(adj);
						if(adjState.is(BlockTags.LEAVES))
						{
							int adjDist = adjState.getBlock() instanceof LeavesBlock?adjState.getValue(LeavesBlock.DISTANCE): 0;
							if(adjDist < trunkDist) // We don't want to get closer
								continue;
						}
						openList.add(adj);
					}
				}
			}
		}

		if(closedList.size()==0)
			return false;
		// Register a Tick Handler to break the blocks, 5 at a time
		NeoForge.EVENT_BUS.register(new Object()
		{
			@SubscribeEvent
			public void onTick(LevelTickEvent.Pre event)
			{
				if(event.getLevel()==world)
				{
					breakFromList(closedList, 5, world, player, stack);
					if(closedList.isEmpty())
						NeoForge.EVENT_BUS.unregister(this);
				}
			}
		});
		return true;
	}

	private void breakFromList(Deque<BlockPos> closedList, int maxAmount, Level world, ServerPlayer player, ItemStack stack)
	{
		int count = 0;
		while(count++ < maxAmount&&!closedList.isEmpty())
		{
			BlockPos pos = closedList.pollFirst();

			// Roughly mirror ServerPlayerGameMode.destroyBlock here
			BlockState state = world.getBlockState(pos);

			var eventResult = CommonHooks.fireBlockBreak(world, player.gameMode.getGameModeForPlayer(), player, pos, state);
			if(eventResult.isCanceled())
				continue;

			Block block = state.getBlock();

			if(!state.isAir()&&state.getDestroyProgress(player, world, pos)!=0)
			{
				if(player.getAbilities().instabuild)
				{
					if(block.onDestroyedByPlayer(state, world, pos, player, false, state.getFluidState()))
						block.destroy(world, pos, state);
				}
				else
				{
					BlockEntity te = world.getBlockEntity(pos);
					consumeDurability(stack, world, state, pos, player);
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
	}

	public static boolean hasQuiverUpgrade(ItemStack stack)
	{
		return Tools.BUZZSAW.get().getUpgrades(stack).has(UpgradeEffect.SPAREBLADES);
	}

	public static boolean isSawblade(ItemStack stack)
	{
		Item item = stack.getItem();
		return item instanceof SawbladeItem&&SAWBLADES.contains(item);
	}

	private static boolean isSpareBladeUpgrade(ItemStack upgrade)
	{
		return upgrade.getItem()!=Misc.TOOL_UPGRADES.get(ToolUpgrade.BUZZSAW_SPAREBLADES).asItem();
	}
}
