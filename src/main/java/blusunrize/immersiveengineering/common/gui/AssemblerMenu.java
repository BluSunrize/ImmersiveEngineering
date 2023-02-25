/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.gui;

import blusunrize.immersiveengineering.api.energy.MutableEnergyStorage;
import blusunrize.immersiveengineering.common.blocks.metal.CrafterPatternInventory;
import blusunrize.immersiveengineering.common.blocks.multiblocks.logic.AssemblerLogic;
import blusunrize.immersiveengineering.common.blocks.multiblocks.logic.AssemblerLogic.State;
import blusunrize.immersiveengineering.common.gui.sync.GenericContainerData;
import blusunrize.immersiveengineering.common.gui.sync.GenericDataSerializers;
import blusunrize.immersiveengineering.common.gui.sync.GetterAndSetter;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.energy.EnergyStorage;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.SlotItemHandler;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class AssemblerMenu extends IEContainerMenu
{
	public final List<IItemHandlerModifiable> patterns;
	public final IItemHandler inv;
	public final FluidTank[] tanks;
	public final EnergyStorage energy;
	public final GetterAndSetter<Boolean> recursiveIngredients;

	public static AssemblerMenu makeServer(
			MenuType<?> type, int id, Inventory invPlayer, MultiblockMenuContext<State> ctx
	)
	{
		final State state = ctx.mbContext().getState();
		List<IItemHandlerModifiable> patterns = new ArrayList<>(AssemblerLogic.NUM_PATTERNS);
		for(final CrafterPatternInventory pattern : state.patterns)
			patterns.add(new ItemStackHandler(pattern.inv)
			{
				@Override
				protected void onContentsChanged(int slot)
				{
					pattern.recalculateOutput(ctx.mbContext().getLevel().getRawLevel());
				}

				@Override
				public int getSlotLimit(int slot)
				{
					return 1;
				}
			});
		return new AssemblerMenu(
				multiblockCtx(type, id, ctx), invPlayer,
				patterns, state.inventory, state.tanks, state.energy,
				new GetterAndSetter<>(() -> state.recursiveIngredients, b -> state.recursiveIngredients = b)
		);
	}

	public static AssemblerMenu makeClient(MenuType<?> type, int id, Inventory invPlayer)
	{
		List<IItemHandlerModifiable> patterns = new ArrayList<>(AssemblerLogic.NUM_PATTERNS);
		for(int i = 0; i < AssemblerLogic.NUM_PATTERNS; ++i)
			patterns.add(new ItemStackHandler(10));
		FluidTank[] tanks = new FluidTank[AssemblerLogic.NUM_TANKS];
		for(int i = 0; i < tanks.length; ++i)
			tanks[i] = new FluidTank(AssemblerLogic.TANK_CAPACITY);
		return new AssemblerMenu(
				clientCtx(type, id), invPlayer,
				patterns, new ItemStackHandler(AssemblerLogic.INVENTORY_SIZE), tanks,
				new MutableEnergyStorage(AssemblerLogic.ENERGY_CAPACITY), GetterAndSetter.standalone(false)
		);
	}

	private AssemblerMenu(
			MenuContext ctx, Inventory inventoryPlayer,
			List<IItemHandlerModifiable> patterns, IItemHandler inv,
			FluidTank[] tanks, MutableEnergyStorage energy, GetterAndSetter<Boolean> recursiveIngredients
	)
	{
		super(ctx);
		this.patterns = patterns;
		this.inv = inv;
		this.tanks = tanks;
		this.energy = energy;
		this.recursiveIngredients = recursiveIngredients;
		for(int i = 0; i < AssemblerLogic.NUM_PATTERNS; i++)
		{
			IItemHandler itemHandler = patterns.get(i);
			for(int j = 0; j < 9; j++)
			{
				int x = 9+i*58+(j%3)*18;
				int y = 7+(j/3)*18;
				this.addSlot(new IESlot.ItemHandlerGhost(itemHandler, j, x, y));
			}
			this.addSlot(new IESlot.NewOutput(inv, 18+i, 27+i*58, 64));
		}
		for(int i = 0; i < 18; i++)
			this.addSlot(new SlotItemHandler(inv, i, 13+(i%9)*18, 87+(i/9)*18));
		ownSlotCount = 21;

		for(int i = 0; i < 3; i++)
			for(int j = 0; j < 9; j++)
				addSlot(new Slot(inventoryPlayer, j+i*9+9, 13+j*18, 137+i*18));
		for(int i = 0; i < 9; i++)
			addSlot(new Slot(inventoryPlayer, i, 13+i*18, 195));
		addGenericData(GenericContainerData.energy(energy));
		for(int i = 0; i < AssemblerLogic.NUM_TANKS; ++i)
			addGenericData(GenericContainerData.fluid(tanks[i]));
		for(IItemHandlerModifiable pattern : patterns)
			addGenericData(new GenericContainerData<>(
					GenericDataSerializers.ITEM_STACK, () -> pattern.getStackInSlot(9), s -> pattern.setStackInSlot(9, s)
			));
		addGenericData(new GenericContainerData<>(GenericDataSerializers.BOOLEAN, recursiveIngredients));
	}

	@Nonnull
	@Override
	public ItemStack quickMoveStack(Player player, int slot)
	{
		ItemStack stack = ItemStack.EMPTY;
		Slot slotObject = slots.get(slot);

		if(slotObject!=null&&slotObject.hasItem()&&!(slotObject instanceof IESlot.ItemHandlerGhost))
		{
			ItemStack stackInSlot = slotObject.getItem();
			stack = stackInSlot.copy();
			if(slot < 48)
			{
				if(!this.moveItemStackTo(stackInSlot, 48, (48+36), true))
					return ItemStack.EMPTY;
			}
			else
			{
				if(!this.moveItemStackTo(stackInSlot, 30, 48, false))
					return ItemStack.EMPTY;
			}

			if(stackInSlot.getCount()==0)
				slotObject.set(ItemStack.EMPTY);
			else
				slotObject.setChanged();

			if(stackInSlot.getCount()==stack.getCount())
				return ItemStack.EMPTY;
			slotObject.onTake(player, stackInSlot);
		}
		return stack;
	}

	@Override
	public void receiveMessageFromScreen(CompoundTag nbt)
	{
		super.receiveMessageFromScreen(nbt);
		if(nbt.contains("buttonID", Tag.TAG_INT))
		{
			int id = nbt.getInt("buttonID");
			if(id >= 0&&id < patterns.size())
			{
				IItemHandlerModifiable pattern = patterns.get(id);
				for(int i = 0; i < pattern.getSlots(); i++)
					pattern.setStackInSlot(i, ItemStack.EMPTY);
			}
			else if(id==3)
				recursiveIngredients.set(!recursiveIngredients.get());
		}
		else if(nbt.contains("patternSync", Tag.TAG_INT))
		{
			int r = nbt.getInt("recipe");
			ListTag list = nbt.getList("patternSync", 10);
			IItemHandlerModifiable pattern = patterns.get(r);
			for(int i = 0; i < list.size(); i++)
			{
				CompoundTag itemTag = list.getCompound(i);
				pattern.setStackInSlot(itemTag.getInt("slot"), ItemStack.of(itemTag));
			}
		}
	}
}
