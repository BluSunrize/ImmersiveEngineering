/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.gui;

import blusunrize.immersiveengineering.api.energy.MutableEnergyStorage;
import blusunrize.immersiveengineering.common.blocks.multiblocks.logic.arcfurnace.ArcFurnaceLogic;
import blusunrize.immersiveengineering.common.blocks.multiblocks.logic.arcfurnace.ArcFurnaceLogic.State;
import blusunrize.immersiveengineering.common.blocks.multiblocks.logic.arcfurnace.ArcFurnaceProcess;
import blusunrize.immersiveengineering.common.gui.sync.GenericContainerData;
import blusunrize.immersiveengineering.common.gui.sync.GenericDataSerializers;
import blusunrize.immersiveengineering.common.gui.sync.GetterAndSetter;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.level.Level;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;

import java.util.List;

public class ArcFurnaceMenu extends IEContainerMenu
{
	public final IEnergyStorage energy;
	public final GetterAndSetter<List<ProcessSlot>> processes;

	public static ArcFurnaceMenu makeServer(
			MenuType<?> type, int id, Inventory invPlayer, MultiblockMenuContext<State> ctx
	)
	{
		final State state = ctx.mbContext().getState();
		return new ArcFurnaceMenu(
				multiblockCtx(type, id, ctx), invPlayer,
				state.getInventory(), state.getEnergy(),
				GetterAndSetter.getterOnly(() -> state.getProcessQueue().stream()
						.filter(p -> p instanceof ArcFurnaceProcess)
						.map(p -> ProcessSlot.fromCtx((ArcFurnaceProcess)p, ctx.mbContext().getLevel().getRawLevel()))
						.toList()
				)
		);
	}

	public static ArcFurnaceMenu makeClient(MenuType<?> type, int id, Inventory invPlayer)
	{
		return new ArcFurnaceMenu(
				clientCtx(type, id),
				invPlayer,
				new ItemStackHandler(ArcFurnaceLogic.NUM_SLOTS),
				new MutableEnergyStorage(ArcFurnaceLogic.ENERGY_CAPACITY),
				GetterAndSetter.standalone(List.of())
		);
	}

	private ArcFurnaceMenu(
			MenuContext ctx, Inventory inventoryPlayer,
			IItemHandler inv, MutableEnergyStorage energy, GetterAndSetter<List<ProcessSlot>> processes
	)
	{
		super(ctx);
		this.energy = energy;
		this.processes = processes;
		Level level = inventoryPlayer.player.level();
		for(int i = 0; i < 12; i++)
			this.addSlot(new IESlot.ArcInput(inv, i, 10+i%3*21, 34+i/3*18, level));
		for(int i = 0; i < 4; i++)
			this.addSlot(new IESlot.ArcAdditive(inv, 12+i, 114+i%2*18, 34+i/2*18, level));
		for(int i = 0; i < 6; i++)
			this.addSlot(new IESlot.NewOutput(inv, 16+i, 78+i%3*18, 80+i/3*18));
		this.addSlot(new IESlot.NewOutput(inv, 22, 132, 98));

		this.addSlot(new IESlot.ArcElectrode(inv, 23, 62, 10));
		this.addSlot(new IESlot.ArcElectrode(inv, 24, 80, 10));
		this.addSlot(new IESlot.ArcElectrode(inv, 25, 98, 10));

		ownSlotCount = 26;

		for(int i = 0; i < 3; i++)
			for(int j = 0; j < 9; j++)
				addSlot(new Slot(inventoryPlayer, j+i*9+9, 8+j*18, 126+i*18));
		for(int i = 0; i < 9; i++)
			addSlot(new Slot(inventoryPlayer, i, 8+i*18, 184));
		addGenericData(GenericContainerData.energy(energy));
		addGenericData(new GenericContainerData<>(GenericDataSerializers.ARC_PROCESS_SLOTS, processes));
	}

	public record ProcessSlot(int slot, int processStep)
	{
		public static ProcessSlot fromCtx(ArcFurnaceProcess process, Level level)
		{
			float mod = process.processTick/(float)process.getMaxTicks(level);
			int slot = process.getInputSlots()[0];
			int h = (int)Math.max(1, mod*16);
			return new ProcessSlot(slot, h);
		}

		public static ProcessSlot from(FriendlyByteBuf buffer)
		{
			return new ProcessSlot(buffer.readByte(), buffer.readByte());
		}

		public static void writeTo(FriendlyByteBuf out, ProcessSlot slot)
		{
			out.writeByte(slot.slot).writeByte(slot.processStep);
		}
	}
}