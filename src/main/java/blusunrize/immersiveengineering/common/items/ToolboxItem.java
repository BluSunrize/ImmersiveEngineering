/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.items;

import blusunrize.immersiveengineering.api.tool.ToolboxHandler.ToolboxCategory;
import blusunrize.immersiveengineering.common.register.IEBlocks.MetalDevices;
import blusunrize.immersiveengineering.common.register.IEMenuTypes;
import blusunrize.immersiveengineering.common.register.IEMenuTypes.ItemContainerTypeNew;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.tooltip.BundleTooltip;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.BundleContents;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.gameevent.GameEvent.Context;
import net.neoforged.neoforge.capabilities.Capabilities.ItemHandler;
import net.neoforged.neoforge.items.IItemHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.AbstractCollection;
import java.util.AbstractList;
import java.util.Arrays;
import java.util.Optional;

public class ToolboxItem extends InternalStorageItem
{
	public static final int SLOT_COUNT = 23;

	public ToolboxItem()
	{
		super(new Properties().stacksTo(1), SLOT_COUNT);
	}

	@Nonnull
	@Override
	public InteractionResultHolder<ItemStack> use(Level world, Player player, @Nonnull InteractionHand hand)
	{
		ItemStack stack = player.getItemInHand(hand);
		if(!world.isClientSide)
			openGui(player, hand);
		return new InteractionResultHolder<>(InteractionResult.SUCCESS, stack);
	}

	@Override
	public boolean overrideOtherStackedOnMe(ItemStack toolbox, ItemStack otherStack, Slot slot, ClickAction action, Player player, SlotAccess slotAccess)
	{
		if(action==ClickAction.SECONDARY&&slot.allowModification(player)&&!otherStack.isEmpty())
		{
			int i = addItem(toolbox, otherStack);
			if(i > 0)
			{
				player.playSound(SoundEvents.BUNDLE_INSERT, 0.8F, 0.8F+player.level().getRandom().nextFloat()*0.4F);
				otherStack.shrink(i);
			}
			return true;
		}
		return false;
	}

	private int addItem(ItemStack toolbox, ItemStack other)
	{
		int[] slots = Arrays.stream(ToolboxCategory.values())
				.filter(cat -> cat.accepts(other))
				.map(ToolboxCategory::getSlots)
				.findFirst()
				.orElse(new int[0]);
		if(slots.length < 1)
			return 0;

		IItemHandler handler = toolbox.getCapability(ItemHandler.ITEM);
		if(handler==null)
			return 0;
		ItemStack remain = other.copy();
		for(int iSlot : slots)
		{
			remain = handler.insertItem(iSlot, remain, false);
			if(remain.isEmpty())
				break;
		}
		return other.getCount()-remain.getCount();
	}

	@Nullable
	@Override
	protected ItemContainerTypeNew<?> getContainerTypeNew()
	{
		return IEMenuTypes.TOOLBOX;
	}

	@Override
	@Nonnull
	public InteractionResult useOn(@Nonnull UseOnContext context)
	{
		BlockPos pos = context.getClickedPos();
		Direction side = context.getClickedFace();
		Level level = context.getLevel();
		BlockState state = level.getBlockState(pos);
		BlockPlaceContext ctx = new BlockPlaceContext(context);
		if(!state.canBeReplaced(ctx))
			pos = pos.relative(side);
		ItemStack stack = context.getItemInHand();
		Player player = context.getPlayer();

		if(player!=null&&player.isShiftKeyDown())
		{
			if(!ctx.canPlace())
				return InteractionResult.FAIL;
			else if(player.mayUseItemAt(pos, side, stack))
			{
				BlockState toolbox = MetalDevices.TOOLBOX.defaultBlockState();
				if(!level.setBlockAndUpdate(pos, toolbox))
					return InteractionResult.FAIL;
				else
				{
					state = level.getBlockState(pos);
					if(state.is(toolbox.getBlock()))
					{
						MetalDevices.TOOLBOX.get().onIEBlockPlacedBy(ctx, toolbox);
						state.getBlock().setPlacedBy(level, pos, state, player, stack);
						if(player instanceof ServerPlayer)
						{
							CriteriaTriggers.PLACED_BLOCK.trigger((ServerPlayer)player, pos, stack);
						}
					}

					SoundType soundtype = state.getSoundType(level, pos, ctx.getPlayer());
					level.playSound(player, pos, soundtype.getPlaceSound(), SoundSource.BLOCKS, (soundtype.getVolume()+1.0F)/2.0F, soundtype.getPitch()*0.8F);
					level.gameEvent(GameEvent.BLOCK_PLACE, pos, Context.of(player, state));
					if(!player.getAbilities().instabuild)
						stack.shrink(1);

					return InteractionResult.SUCCESS;
				}
			}
		}
		return super.useOn(context);
	}

	@Override
	public boolean canFitInsideContainerItems()
	{
		return false;
	}

	@Nonnull
	@Override
	public Optional<TooltipComponent> getTooltipImage(@Nonnull ItemStack stack)
	{
		// cut all empty slots from list
		NonNullList<ItemStack> items = getContainedItems(stack)
				.stream()
				.filter(s -> !s.isEmpty())
				.collect(NonNullList::create, AbstractList::add, AbstractCollection::addAll);
		if(!items.isEmpty())
			return Optional.of(new BundleTooltip(new BundleContents(items)));
		else
			return super.getTooltipImage(stack);
	}
}
