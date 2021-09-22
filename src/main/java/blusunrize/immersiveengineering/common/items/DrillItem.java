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
import blusunrize.immersiveengineering.client.render.IEOBJItemRenderer;
import blusunrize.immersiveengineering.common.fluids.IEItemFluidHandler;
import blusunrize.immersiveengineering.common.gui.IESlot;
import blusunrize.immersiveengineering.common.util.Utils;
import com.google.common.collect.ImmutableList;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.network.protocol.game.ClientboundBlockUpdatePacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.ClipContext.Fluid;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.client.IItemRenderProperties;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.entity.player.PlayerEvent.HarvestCheck;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

@EventBusSubscriber(modid = Lib.MODID, bus = Bus.FORGE)
public class DrillItem extends DieselToolItem
{
	public static Material[] validMaterials = {Material.HEAVY_METAL, Material.CLAY, Material.GLASS, Material.GRASS, Material.DIRT,
			Material.ICE, Material.METAL, Material.ICE_SOLID, Material.PISTON, Material.STONE, Material.SAND, Material.TOP_SNOW};

	public DrillItem()
	{
		super(new Properties().stacksTo(1), "DRILL");
	}

	@Override
	public void initializeClient(@Nonnull Consumer<IItemRenderProperties> consumer)
	{
		super.initializeClient(consumer);
		consumer.accept(IEOBJItemRenderer.USE_IEOBJ_RENDER);
	}

	/* ------------- WORKBENCH & INVENTORY ------------- */
	@Override
	public int getSlotCount()
	{
		return 5;
	}

	@Override
	public Slot[] getWorkbenchSlots(AbstractContainerMenu container, ItemStack stack, Level level, Supplier<Player> getPlayer, IItemHandler toolInventory)
	{
		return new Slot[]{
				new IESlot.WithPredicate(toolInventory, 0, 98, 22, (itemStack) -> itemStack.getItem() instanceof IDrillHead),
				new IESlot.Upgrades(container, toolInventory, 1, 78, 52, "DRILL", stack, true, level, getPlayer),
				new IESlot.Upgrades(container, toolInventory, 2, 98, 52, "DRILL", stack, true, level, getPlayer),
				new IESlot.Upgrades(container, toolInventory, 3, 118, 52, "DRILL", stack, true, level, getPlayer)
		};
	}

	@Override
	public void removeFromWorkbench(Player player, ItemStack stack)
	{
		LazyOptional<IItemHandler> invCap = stack.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
		invCap.ifPresent(inv -> {
			if(!inv.getStackInSlot(0).isEmpty()&&!inv.getStackInSlot(1).isEmpty()&&!inv.getStackInSlot(2).isEmpty()&&!inv.getStackInSlot(3).isEmpty())
				Utils.unlockIEAdvancement(player, "main/upgrade_drill");
		});
	}

	@Override
	public final ItemStack getHead(ItemStack drill)
	{
		return getHeadStatic(drill);
	}

	public static ItemStack getHeadStatic(ItemStack drill)
	{
		if(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY==null)
			return ItemStack.EMPTY;
		LazyOptional<IItemHandler> cap = drill.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY);
		if(cap.isPresent())
		{
			ItemStack head = cap.map(handler -> handler.getStackInSlot(0)).orElse(ItemStack.EMPTY);
			return !head.isEmpty()&&head.getItem() instanceof IDrillHead?head: ItemStack.EMPTY;
		}
		return ItemStack.EMPTY;
	}

	@Override
	public void setHead(ItemStack drill, ItemStack head)
	{
		IItemHandler inv = drill.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).orElseThrow(RuntimeException::new);
		((IItemHandlerModifiable)inv).setStackInSlot(0, head);
	}

	@Override
	public void appendHoverText(ItemStack stack, @Nullable Level world, List<Component> list, TooltipFlag flag)
	{
		list.add(IEItemFluidHandler.fluidItemInfoFlavor(getFluid(stack), getCapacity(stack, CAPACITY)));
		if(getHead(stack).isEmpty())
			list.add(TextUtils.applyFormat(
					new TranslatableComponent(Lib.DESC_FLAVOUR+"drill.noHead"),
					ChatFormatting.GRAY
			));
		else
		{
			int maxDmg = getMaxHeadDamage(stack);
			int dmg = maxDmg-getHeadDamage(stack);
			float quote = dmg/(float)maxDmg;
			ChatFormatting status = quote < .1?ChatFormatting.RED: quote < .3?ChatFormatting.GOLD: quote < .6?ChatFormatting.YELLOW: ChatFormatting.GREEN;
			list.add(TextUtils.applyFormat(new TranslatableComponent(Lib.DESC_FLAVOUR+"drill.headDamage"), ChatFormatting.GRAY)
					.append(" ")
					.append(TextUtils.applyFormat(
							new TranslatableComponent(Lib.DESC_INFO+"percent", (int)(quote*100)),
							status
					)));
		}
	}

	@Override
	protected double getAttackDamage(ItemStack stack, ItemStack head)
	{
		return ((IDrillHead)head.getItem()).getAttackDamage(head)+getUpgrades(stack).getInt("damage");
	}

	/* ------------- DIGGING ------------- */
	public boolean canToolBeUsed(ItemStack drill)
	{
		return getHeadDamage(drill) < getMaxHeadDamage(drill)&&!getFluid(drill).isEmpty();
	}

	@SubscribeEvent
	public static void handleUnderwaterDrill(HarvestCheck ev)
	{
		if(!(ev.getEntityLiving() instanceof Player player))
			return;
		ItemStack drill = player.getInventory().getSelected();
		if(!(drill.getItem() instanceof DrillItem drillItem))
			return;
		if(player.isEyeInFluid(FluidTags.WATER)&&!drillItem.getUpgrades(drill).getBoolean("waterproof"))
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
	public boolean mineBlock(ItemStack stack, Level world, BlockState state, BlockPos pos, LivingEntity living)
	{
		if(state.getDestroySpeed(world, pos)!=0)
		{
			ItemStack head = getHead(stack);
			if(!head.isEmpty())
			{
				if(living instanceof Player)
				{
					if(((Player)living).getAbilities().instabuild)
						return true;
					((IDrillHead)head.getItem()).afterBlockbreak(stack, head, (Player)living);
				}
				consumeDurability(stack, world, state, pos, living);
			}
		}

		return true;
	}

	@Override
	protected void damageHead(ItemStack head, int amount, LivingEntity living)
	{
		((IDrillHead)head.getItem()).damageHead(head, amount);
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
			return ((IDrillHead)head.getItem()).getMiningSpeed(head)+getUpgrades(stack).getFloat("speed");
		return super.getDestroySpeed(stack, state);
	}

	public boolean canBreakExtraBlock(Level world, Block block, BlockPos pos, BlockState state, Player player, ItemStack drill, ItemStack head, boolean inWorld)
	{
		if(block.canHarvestBlock(state, world, pos, player)&&isEffective(drill, state)&&canToolBeUsed(drill))
		{
			if(inWorld)
				return !((IDrillHead)head.getItem()).beforeBlockbreak(drill, head, player);
			else
				return true;
		}
		return false;
	}

	@Override
	public boolean onBlockStartBreak(ItemStack stack, BlockPos iPos, Player player)
	{
		Level world = player.level;
		if(player.isShiftKeyDown()||world.isClientSide||!(player instanceof ServerPlayer))
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
				if(!this.canBreakExtraBlock(world, block, pos, state, player, stack, head, true))
					continue;
				int xpDropEvent = ForgeHooks.onBlockBreakEvent(world, ((ServerPlayer)player).gameMode.getGameModeForPlayer(), (ServerPlayer)player, pos);
				if(xpDropEvent < 0)
					continue;

				if(player.getAbilities().instabuild)
				{
					block.playerWillDestroy(world, pos, state, player);
					if(block.removedByPlayer(state, world, pos, player, false, state.getFluidState()))
						block.destroy(world, pos, state);
				}
				else
				{
					block.playerWillDestroy(world, pos, state, player);
					BlockEntity te = world.getBlockEntity(pos);
					//implicitly damages head
					stack.mineBlock(world, state, pos, player);
					if(block.removedByPlayer(state, world, pos, player, true, state.getFluidState()))
					{
						block.destroy(world, pos, state);
						block.playerDestroy(world, player, pos, state, te, stack);
						if(world instanceof ServerLevel)
							block.popExperience((ServerLevel)world, pos, xpDropEvent);
					}
				}
				world.levelEvent(2001, pos, Block.getId(state));
				((ServerPlayer)player).connection.send(new ClientboundBlockUpdatePacket(world, pos));
			}
		}
		return false;
	}
}
