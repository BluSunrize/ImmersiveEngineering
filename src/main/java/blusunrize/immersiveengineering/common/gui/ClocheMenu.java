/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.gui;

import blusunrize.immersiveengineering.api.energy.MutableEnergyStorage;
import blusunrize.immersiveengineering.common.blocks.metal.ClocheBlockEntity;
import blusunrize.immersiveengineering.common.gui.IESlot.Cloche;
import blusunrize.immersiveengineering.common.gui.sync.GenericContainerData;
import blusunrize.immersiveengineering.common.gui.sync.GenericDataSerializers;
import blusunrize.immersiveengineering.common.gui.sync.GetterAndSetter;
import blusunrize.immersiveengineering.common.util.Utils;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraftforge.energy.EnergyStorage;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;

import static blusunrize.immersiveengineering.common.blocks.metal.ClocheBlockEntity.*;

public class ClocheMenu extends IEContainerMenu
{
	public final EnergyStorage energyStorage;
	public final FluidTank tank;
	public final GetterAndSetter<Integer> fertilizerAmount;
	public final GetterAndSetter<Float> fertilizerMod;
	public final GetterAndSetter<Float> guiProgress;

	public static ClocheMenu makeServer(
			MenuType<?> type, int id, Inventory invPlayer, ClocheBlockEntity be
	)
	{
		return new ClocheMenu(
				blockCtx(type, id, be), invPlayer, new ItemStackHandler(be.getInventory()),
				be.energyStorage, be.tank,
				GetterAndSetter.getterOnly(() -> be.fertilizerAmount),
				GetterAndSetter.getterOnly(() -> be.fertilizerMod),
				GetterAndSetter.getterOnly(be::getGuiProgress)
		);
	}

	public static ClocheMenu makeClient(MenuType<?> type, int id, Inventory invPlayer)
	{
		return new ClocheMenu(
				clientCtx(type, id), invPlayer, new ItemStackHandler(NUM_SLOTS),
				new MutableEnergyStorage(ENERGY_CAPACITY), new FluidTank(TANK_CAPACITY),
				GetterAndSetter.standalone(0), GetterAndSetter.standalone(0f),
				GetterAndSetter.standalone(0f)
		);
	}

	private ClocheMenu(
			MenuContext ctx, Inventory inventoryPlayer, IItemHandler inv,
			MutableEnergyStorage energyStorage, FluidTank tank,
			GetterAndSetter<Integer> fertilizerAmount, GetterAndSetter<Float> fertilizerMod,
			GetterAndSetter<Float> guiProgress
	)
	{
		super(ctx);
		this.energyStorage = energyStorage;
		this.tank = tank;
		this.fertilizerAmount = fertilizerAmount;
		this.fertilizerMod = fertilizerMod;
		this.guiProgress = guiProgress;
		Level level = inventoryPlayer.player.level();
		this.addSlot(new Cloche(Cloche.Type.SOIL, inv, SLOT_SOIL, 62, 54, level));
		this.addSlot(new Cloche(Cloche.Type.SEED, inv, SLOT_SEED, 62, 34, level));
		this.addSlot(new Cloche(Cloche.Type.FERTILIZER, inv, SLOT_FERTILIZER, 8, 59, level));

		for(int i = 0; i < 4; i++)
			this.addSlot(new IESlot.NewOutput(inv, 3+i, 116+i%2*18, 34+i/2*18)
			{
				@Override
				public void onTake(Player pPlayer, ItemStack pStack)
				{
					super.onTake(pPlayer, pStack);
					if(pStack.getItem()==Items.CHORUS_FRUIT)
						Utils.unlockIEAdvancement(pPlayer, "main/chorus_cloche");
				}
			});

		this.ownSlotCount = 7;

		for(int i = 0; i < 3; i++)
			for(int j = 0; j < 9; j++)
				addSlot(new Slot(inventoryPlayer, j+i*9+9, 8+j*18, 85+i*18));
		for(int i = 0; i < 9; i++)
			addSlot(new Slot(inventoryPlayer, i, 8+i*18, 143));
		addGenericData(GenericContainerData.energy(energyStorage));
		addGenericData(GenericContainerData.fluid(tank));
		addGenericData(new GenericContainerData<>(GenericDataSerializers.INT32, fertilizerAmount));
		addGenericData(new GenericContainerData<>(GenericDataSerializers.FLOAT, fertilizerMod));
		addGenericData(new GenericContainerData<>(GenericDataSerializers.FLOAT, guiProgress));
	}
}