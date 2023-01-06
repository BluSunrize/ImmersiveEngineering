/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.gui;

import blusunrize.immersiveengineering.api.crafting.MixerRecipe;
import blusunrize.immersiveengineering.api.energy.IMutableEnergyStorage;
import blusunrize.immersiveengineering.api.energy.MutableEnergyStorage;
import blusunrize.immersiveengineering.common.blocks.metal.MixerBlockEntity;
import blusunrize.immersiveengineering.common.blocks.multiblocks.process_old.MultiblockProcess;
import blusunrize.immersiveengineering.common.blocks.multiblocks.process_old.MultiblockProcessInMachine;
import blusunrize.immersiveengineering.common.gui.sync.GenericContainerData;
import blusunrize.immersiveengineering.common.gui.sync.GenericDataSerializers;
import blusunrize.immersiveengineering.common.gui.sync.GetterAndSetter;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;

import java.util.ArrayList;
import java.util.List;

//TODO custom subclass of ItemStackHandler for markDirty etc
public class MixerMenu extends IEContainerMenu implements IESlot.ICallbackContainer
{
	public static MixerMenu makeServer(
			MenuType<?> type, int id, Inventory invPlayer, MixerBlockEntity be
	)
	{
		final GetterAndSetter<List<SlotProgress>> progress = GetterAndSetter.getterOnly(() -> {
			List<SlotProgress> result = new ArrayList<>();
			for(final MultiblockProcess<MixerRecipe> process : be.processQueue)
				if(process instanceof MultiblockProcessInMachine<MixerRecipe> inMachine)
				{
					final float mod = 1-(process.processTick/(float)process.getMaxTicks(be.getLevel()));
					for(final int inputSlot : inMachine.getInputSlots())
						result.add(new SlotProgress(inputSlot, mod));
				}
			return result;
		});
		return new MixerMenu(
				blockCtx(type, id, be), invPlayer, new ItemStackHandler(be.inventory), be.energyStorage, progress,
				GetterAndSetter.getterOnly(() -> be.tank.fluids),
				new GetterAndSetter<>(() -> be.outputAll, b -> be.outputAll = b)
		);
	}

	public static MixerMenu makeClient(MenuType<?> type, int id, Inventory invPlayer)
	{
		return new MixerMenu(
				clientCtx(type, id),
				invPlayer,
				new ItemStackHandler(MixerBlockEntity.NUM_SLOTS),
				new MutableEnergyStorage(MixerBlockEntity.ENERGY_CAPACITY),
				GetterAndSetter.standalone(List.of()),
				GetterAndSetter.standalone(List.of()),
				GetterAndSetter.standalone(false)
		);
	}

	public final IEnergyStorage energy;
	public final GetterAndSetter<List<SlotProgress>> progress;
	public final GetterAndSetter<List<FluidStack>> tankContents;
	public final GetterAndSetter<Boolean> outputAll;

	private MixerMenu(
			MenuContext ctx, Inventory inventoryPlayer, IItemHandler inv, IMutableEnergyStorage energy,
			GetterAndSetter<List<SlotProgress>> progress, GetterAndSetter<List<FluidStack>> tankContents,
			GetterAndSetter<Boolean> outputAll
	)
	{
		super(ctx);
		this.energy = energy;
		this.progress = progress;
		this.tankContents = tankContents;
		this.outputAll = outputAll;

		for(int i = 0; i < 8; i++)
			this.addSlot(new IESlot.ContainerCallback(this, inv, i, 7+(i%2)*21, 7+(i/2)*18));
		ownSlotCount = 8;

		for(int i = 0; i < 3; i++)
			for(int j = 0; j < 9; j++)
				addSlot(new Slot(inventoryPlayer, j+i*9+9, 8+j*18, 86+i*18));
		for(int i = 0; i < 9; i++)
			addSlot(new Slot(inventoryPlayer, i, 8+i*18, 144));
		addGenericData(GenericContainerData.energy(energy));
		addGenericData(new GenericContainerData<>(GenericDataSerializers.MIXER_SLOTS, progress));
		addGenericData(new GenericContainerData<>(GenericDataSerializers.FLUID_STACKS, tankContents));
		addGenericData(new GenericContainerData<>(GenericDataSerializers.BOOLEAN, outputAll));
	}

	@Override
	public boolean canInsert(ItemStack stack, int slotNumber, Slot slotObject)
	{
		for(final var progress : this.progress.get())
			if(progress.slot==slotNumber)
				return false;
		return true;
	}

	@Override
	public boolean canTake(ItemStack stack, int slotNumber, Slot slotObject)
	{
		return canInsert(stack, slotNumber, slotObject);
	}

	@Override
	public void receiveMessageFromScreen(CompoundTag nbt)
	{
		if(nbt.contains("outputAll", Tag.TAG_BYTE))
			outputAll.set(nbt.getBoolean("outputAll"));
	}

	public record SlotProgress(int slot, float progress)
	{
		public SlotProgress(FriendlyByteBuf buf)
		{
			this(buf.readVarInt(), buf.readFloat());
		}

		public static void write(FriendlyByteBuf buf, SlotProgress progress)
		{
			buf.writeVarInt(progress.slot).writeFloat(progress.progress);
		}
	}
}