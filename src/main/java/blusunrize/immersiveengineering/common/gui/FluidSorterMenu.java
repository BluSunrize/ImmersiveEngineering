/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.gui;

import blusunrize.immersiveengineering.api.utils.DirectionUtils;
import blusunrize.immersiveengineering.common.blocks.wooden.FluidSorterBlockEntity;
import blusunrize.immersiveengineering.common.gui.sync.GenericContainerData;
import blusunrize.immersiveengineering.common.gui.sync.GenericDataSerializers;
import blusunrize.immersiveengineering.common.gui.sync.GetterAndSetter;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.List;

public class FluidSorterMenu extends IEContainerMenu
{
	public final GetterAndSetter<byte[]> sortWithNBT;
	public final List<List<GetterAndSetter<FluidStack>>> filters;

	public static FluidSorterMenu makeServer(
			MenuType<?> type, int id, Inventory invPlayer, FluidSorterBlockEntity be
	)
	{
		return new FluidSorterMenu(
				blockCtx(type, id, be), invPlayer,
				GetterAndSetter.getterOnly(() -> be.sortWithNBT),
				be.filters
		);
	}

	public static FluidSorterMenu makeClient(MenuType<?> type, int id, Inventory invPlayer)
	{
		return new FluidSorterMenu(
				clientCtx(type, id), invPlayer,
				GetterAndSetter.standalone(new byte[DirectionUtils.VALUES.length]),
				FluidSorterBlockEntity.makeFilterArray()
		);
	}

	private FluidSorterMenu(
			MenuContext ctx, Inventory inventoryPlayer, GetterAndSetter<byte[]> sortWithNBT, FluidStack[][] filters
	)
	{
		super(ctx);
		this.sortWithNBT = sortWithNBT;
		this.filters = Arrays.stream(filters)
				.map(GetterAndSetter::forArray)
				.toList();
		for(int i = 0; i < 3; i++)
			for(int j = 0; j < 9; j++)
				addSlot(new Slot(inventoryPlayer, j+i*9+9, 8+j*18, 163+i*18));
		for(int i = 0; i < 9; i++)
			addSlot(new Slot(inventoryPlayer, i, 8+i*18, 221));
		for(List<GetterAndSetter<FluidStack>> sideFilter : this.filters)
			for(GetterAndSetter<FluidStack> filter : sideFilter)
				addGenericData(new GenericContainerData<>(GenericDataSerializers.FLUID_STACK, filter));
		addGenericData(new GenericContainerData<>(GenericDataSerializers.BYTE_ARRAY, sortWithNBT));
	}

	@Override
	public void receiveMessageFromScreen(CompoundTag message)
	{
		if(message.contains("useNBT", Tag.TAG_INT))
		{
			byte config = message.getByte("useNBT");
			int side = message.getInt("side");
			this.sortWithNBT.get()[side] = config;
		}
		if(message.contains("filter_side", Tag.TAG_INT))
		{
			var currentServer = ServerLifecycleHooks.getCurrentServer();
			if(null == currentServer) return;
			int side = message.getInt("filter_side");
			int slot = message.getInt("filter_slot");
			FluidStack newFilter = FluidStack.parseOptional(currentServer.registryAccess(), message.getCompound("filter"));
			if(!newFilter.isEmpty())
				newFilter.setAmount(1); // Not strictly necessary, but also doesn't hurt
			this.filters.get(side).get(slot).set(newFilter);
		}
	}

	@Nonnull
	@Override
	public ItemStack quickMoveStack(Player player, int slot)
	{
		return ItemStack.EMPTY;
	}

	public FluidStack getFilter(int side, int slot)
	{
		return this.filters.get(side).get(slot).get();
	}
}
