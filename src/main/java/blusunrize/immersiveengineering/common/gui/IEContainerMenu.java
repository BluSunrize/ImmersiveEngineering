/*
 * BluSunrize
 * Copyright (c) 2023
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.gui;

import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IMultiblockContext;
import blusunrize.immersiveengineering.api.multiblocks.blocks.logic.IMultiblockState;
import blusunrize.immersiveengineering.common.blocks.IEBaseBlockEntity;
import blusunrize.immersiveengineering.common.gui.sync.GenericContainerData;
import blusunrize.immersiveengineering.common.gui.sync.GenericDataSerializers.DataPair;
import blusunrize.immersiveengineering.common.network.MessageContainerData;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.EventBusSubscriber.Bus;
import net.neoforged.neoforge.event.entity.player.PlayerContainerEvent;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

@EventBusSubscriber(modid = Lib.MODID, bus = Bus.GAME)
public abstract class IEContainerMenu extends AbstractContainerMenu implements IScreenMessageReceive
{
	private final List<GenericContainerData<?>> genericData = new ArrayList<>();
	private final List<ServerPlayer> usingPlayers = new ArrayList<>();
	private final Runnable setChanged;
	private final Predicate<Player> isValid;
	public int ownSlotCount;

	protected IEContainerMenu(MenuContext ctx)
	{
		super(ctx.type, ctx.id);
		this.setChanged = ctx.setChanged;
		this.isValid = ctx.isValid;
	}

	public void addGenericData(GenericContainerData<?> newData)
	{
		genericData.add(newData);
	}

	@Override
	public void broadcastChanges()
	{
		super.broadcastChanges();
		List<Pair<Integer, DataPair<?>>> toSync = new ArrayList<>();
		for(int i = 0; i < genericData.size(); i++)
		{
			GenericContainerData<?> data = genericData.get(i);
			if(data.needsUpdate())
				toSync.add(Pair.of(i, data.dataPair()));
		}
		if(!toSync.isEmpty())
			for(ServerPlayer player : usingPlayers)
				player.connection.send(new MessageContainerData(toSync));
	}

	public void receiveSync(List<Pair<Integer, DataPair<?>>> synced)
	{
		for(Pair<Integer, DataPair<?>> syncElement : synced)
			genericData.get(syncElement.getFirst()).processSync(syncElement.getSecond().data());
	}

	@Override
	public void clicked(int id, int dragType, ClickType clickType, Player player)
	{
		Slot slot = id < 0?null: this.slots.get(id);
		if(!(slot instanceof IESlot.ItemHandlerGhost))
		{
			super.clicked(id, dragType, clickType, player);
			return;
		}
		//Spooky Ghost Slots!!!!
		//TODO fix/test
		ItemStack stackSlot = slot.getItem();

		if(dragType==2)
			slot.set(ItemStack.EMPTY);
		else if(dragType==0||dragType==1)
		{
			ItemStack stackHeld = getCarried();
			int amount = Math.min(slot.getMaxStackSize(), stackHeld.getCount());
			if(dragType==1)
				amount = 1;
			if(stackSlot.isEmpty())
			{
				if(!stackHeld.isEmpty()&&slot.mayPlace(stackHeld))
					slot.set(stackHeld.copyWithCount(amount));
			}
			else if(stackHeld.isEmpty())
				slot.set(ItemStack.EMPTY);
			else if(slot.mayPlace(stackHeld))
			{
				if(ItemStack.isSameItem(stackSlot, stackHeld))
					stackSlot.grow(amount);
				else
					slot.set(stackHeld.copyWithCount(amount));
			}
			if(stackSlot.getCount() > slot.getMaxStackSize())
				stackSlot.setCount(slot.getMaxStackSize());
		}
		else if(dragType==5)
		{
			ItemStack stackHeld = getCarried();
			int amount = Math.min(slot.getMaxStackSize(), stackHeld.getCount());
			if(!slot.hasItem())
				slot.set(stackHeld.copyWithCount(amount));
		}
	}

	@Nonnull
	@Override
	public ItemStack quickMoveStack(Player player, int slot)
	{
		ItemStack itemstack = ItemStack.EMPTY;
		Slot slotObject = this.slots.get(slot);
		if(slotObject!=null&&slotObject.hasItem())
		{
			ItemStack itemstack1 = slotObject.getItem();
			itemstack = itemstack1.copy();
			if(slot < ownSlotCount)
			{
				if(!this.moveItemStackTo(itemstack1, ownSlotCount, this.slots.size(), true))
					return ItemStack.EMPTY;
			}
			else if(!this.moveItemStackToWithMayPlace(itemstack1, 0, ownSlotCount, false))
				return ItemStack.EMPTY;

			if(itemstack1.isEmpty())
				slotObject.set(ItemStack.EMPTY);
			else
				slotObject.setChanged();
		}

		return itemstack;
	}

	protected boolean moveItemStackToWithMayPlace(ItemStack pStack, int pStartIndex, int pEndIndex, boolean reverseDirection)
	{
		return moveItemStackToWithMayPlace(slots, this::moveItemStackTo, pStack, pStartIndex, pEndIndex, reverseDirection);
	}

	/**
	 * This function moves the ItemStack pStack to the target container index.
	 * Unlike the regular AbstractContainerMenu::moveItemStackTo it will check mayPlace even when finding existing
	 * stacks of the same item, so players can't place items into the ghost item slot this way.
	 *
	 * @param pStack      The ItemStack being moved
	 * @param pStartIndex the start index of the targeted container slots
	 * @param pEndIndex   the end index of the targeted container slots (excluded)
	 * @return
	 */
	public static boolean moveItemStackToWithMayPlace(
			List<Slot> slots, MoveItemsFunc move, ItemStack pStack, int pStartIndex, int pEndIndex, boolean reverseDirection
	)
	{

		int i;
		final byte step;
		Predicate<Integer> continueLoop;
		if(reverseDirection)
		{
			i = pEndIndex-1;
			step = -1;
			continueLoop = (loop_i) -> loop_i >= pStartIndex;
		}
		else
		{
			i = pStartIndex;
			step = 1;
			continueLoop = (loop_i) -> loop_i < pEndIndex;
		}

		boolean inAllowedRange = true;
		int allowedStart = pStartIndex;

		/* check if there are disallowed slots in the given range.
		 * if a disallowed slot is encountered, use super.moveItemStackTo from the last allowedStart index
		 * up until that slot (might result in a 0 range, which is fine and handled)
		 * then, continue looking through the rest of the indices and see if another allowed slot is encountered,
		 * to start a new range of allowed slots, which will be moved with super.moveItemStackTo */
		while(continueLoop.test(i))
		{
			boolean mayplace = slots.get(i).mayPlace(pStack);
			if(inAllowedRange&&!mayplace)
			{
				if(move.moveItemStackTo(pStack, allowedStart, i, reverseDirection))
					return true;
				inAllowedRange = false;
			}
			else if(!inAllowedRange&&mayplace)
			{
				allowedStart = i;
				inAllowedRange = true;
			}
			i += step;
		}

		return inAllowedRange&&move.moveItemStackTo(pStack, allowedStart, pEndIndex, reverseDirection);
	}

	/**
	 * Tries to place an item in a slot which already contains the item.
	 * If any of those slots are full, try to place it in a slot immediately after, or if those are full, a slot
	 * immediately before the matching slot.
	 * This logic is currently used by the storage shelf.
	 *
	 * @param stack      the stack to be inserted
	 * @param startIndex the first slot to check
	 * @param endIndex   the final slot to check
	 * @return true if the stack was fully consumed
	 */
	public boolean moveToMatchingSlotOrAdjacent(ItemStack stack, int startIndex, int endIndex)
	{
		boolean flag = false;
		int slotIndex = startIndex;

		int lastMatchingSlot = startIndex;

		Slot slot;
		ItemStack itemstack;
		while(!stack.isEmpty())
		{
			if(slotIndex >= endIndex)
				break;
			slot = this.slots.get(slotIndex);
			itemstack = slot.getItem();
			if(!itemstack.isEmpty()&&ItemStack.isSameItemSameComponents(stack, itemstack))
			{
				lastMatchingSlot = slotIndex;
				if(itemstack.isStackable())
				{
					int totalCount = itemstack.getCount()+stack.getCount();
					int maxStackSize = slot.getMaxStackSize(itemstack);
					if(totalCount <= maxStackSize)
					{
						stack.setCount(0);
						itemstack.setCount(totalCount);
						slot.setChanged();
						flag = true;
					}
					else if(itemstack.getCount() < maxStackSize)
					{
						stack.shrink(maxStackSize-itemstack.getCount());
						itemstack.setCount(maxStackSize);
						slot.setChanged();
						flag = true;
					}
				}
			}
			slotIndex++;
		}

		if(!stack.isEmpty())
		{
			slotIndex = lastMatchingSlot;
			while(true)
			{
				if(slotIndex >= endIndex)
					break;
				slot = this.slots.get(slotIndex);
				itemstack = slot.getItem();
				if(itemstack.isEmpty()&&slot.mayPlace(stack))
				{
					int maxStackSize = slot.getMaxStackSize(stack);
					slot.setByPlayer(stack.split(Math.min(stack.getCount(), maxStackSize)));
					slot.setChanged();
					flag = true;
					break;
				}
				slotIndex++;
			}
		}
		if(stack.isEmpty())
			return flag;
		return this.moveItemStackTo(stack, startIndex, Math.max(lastMatchingSlot, endIndex), true);
	}

	@Override
	public void removed(@Nonnull Player player)
	{
		super.removed(player);
		setChanged.run();
	}

	@Override
	public boolean stillValid(@Nonnull Player pPlayer)
	{
		return isValid.test(pPlayer);
	}

	@SubscribeEvent
	public static void onContainerOpened(PlayerContainerEvent.Open ev)
	{
		if(ev.getContainer() instanceof IEContainerMenu ieContainer&&ev.getEntity() instanceof ServerPlayer serverPlayer)
		{
			ieContainer.usingPlayers.add(serverPlayer);
			List<Pair<Integer, DataPair<?>>> list = new ArrayList<>();
			for(int i = 0; i < ieContainer.genericData.size(); i++)
				list.add(Pair.of(i, ieContainer.genericData.get(i).dataPair()));
			serverPlayer.connection.send(new MessageContainerData(list));
		}
	}

	@SubscribeEvent
	public static void onContainerClosed(PlayerContainerEvent.Close ev)
	{
		if(ev.getContainer() instanceof IEContainerMenu ieContainer&&ev.getEntity() instanceof ServerPlayer serverPlayer)
			ieContainer.usingPlayers.remove(serverPlayer);
	}

	public static MenuContext multiblockCtx(
			MenuType<?> pMenuType, int pContainerId, MultiblockMenuContext<?> ctx
	)
	{
		return new MenuContext(pMenuType, pContainerId, ctx.mbContext()::markMasterDirty, p -> {
			if(!ctx.mbContext().isValid().getAsBoolean())
				return false;
			return p.distanceToSqr(Vec3.atCenterOf(ctx.clickedPos)) <= 64.0D;
		});
	}

	public static MenuContext blockCtx(MenuType<?> pMenuType, int pContainerId, BlockEntity be)
	{
		return new MenuContext(pMenuType, pContainerId, () -> {
			be.setChanged();
			if(be instanceof IEBaseBlockEntity ieBE)
				ieBE.markContainingBlockForUpdate(null);
		}, p -> {
			BlockPos pos = be.getBlockPos();
			Level level = be.getLevel();
			if(level==null||level.getBlockEntity(pos)!=be)
				return false;
			else
				return !(p.distanceToSqr(pos.getX()+0.5D, pos.getY()+0.5D, pos.getZ()+0.5D) > 64.0D);
		});
	}

	public static MenuContext itemCtx(
			MenuType<?> pMenuType, int pContainerId, Inventory playerInv, EquipmentSlot slot, ItemStack stack
	)
	{
		return new MenuContext(pMenuType, pContainerId, () -> {
		}, p -> {
			if(p!=playerInv.player)
				return false;
			return ItemStack.isSameItem(p.getItemBySlot(slot), stack);
		});
	}

	public static MenuContext clientCtx(MenuType<?> pMenuType, int pContainerId)
	{
		return new MenuContext(pMenuType, pContainerId, () -> {
		}, $ -> true);
	}

	protected record MenuContext(
			MenuType<?> type, int id, Runnable setChanged, Predicate<Player> isValid
	)
	{
	}

	public record MultiblockMenuContext<S extends IMultiblockState>(IMultiblockContext<S> mbContext,
	                                                                BlockPos clickedPos)
	{
	}

	public interface MoveItemsFunc
	{
		boolean moveItemStackTo(ItemStack stack, int startIndex, int endIndex, boolean reverseDirection);
	}
}
