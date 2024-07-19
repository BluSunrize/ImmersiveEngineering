/*
 * BluSunrize
 * Copyright (c) 2024
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.gui;

import blusunrize.immersiveengineering.api.energy.MutableEnergyStorage;
import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IMultiblockContext;
import blusunrize.immersiveengineering.common.blocks.multiblocks.logic.RadioTowerLogic;
import blusunrize.immersiveengineering.common.blocks.multiblocks.logic.RadioTowerLogic.State;
import blusunrize.immersiveengineering.common.gui.sync.GenericContainerData;
import blusunrize.immersiveengineering.common.gui.sync.GenericDataSerializers;
import blusunrize.immersiveengineering.common.gui.sync.GetterAndSetter;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.energy.IEnergyStorage;

import java.util.Comparator;
import java.util.List;

public class RadioTowerMenu extends IEContainerMenu
{
	public final IEnergyStorage energy;
	public final GetterAndSetter<Integer> frequency;
	public final GetterAndSetter<int[]> savedFrequencies;
	public final GetterAndSetter<Integer> range;
	public final GetterAndSetter<NearbyComponents> otherComponents;

	public static RadioTowerMenu makeServer(
			MenuType<?> type, int id, Inventory invPlayer, MultiblockMenuContext<State> ctx
	)
	{
		final State state = ctx.mbContext().getState();
		return new RadioTowerMenu(
				multiblockCtx(type, id, ctx), invPlayer,
				state.energy,
				new GetterAndSetter<>(state::getFrequency, state::setFrequency),
				new GetterAndSetter<>(state::getSavedFrequencies, state::setSavedFrequencies),
				GetterAndSetter.getterOnly(state::getChunkRange),
				GetterAndSetter.getterOnly(() -> NearbyComponents.fromCtx(ctx.mbContext()))
		);
	}

	public static RadioTowerMenu makeClient(MenuType<?> type, int id, Inventory invPlayer)
	{
		return new RadioTowerMenu(
				clientCtx(type, id),
				invPlayer,
				new MutableEnergyStorage(RadioTowerLogic.ENERGY_CAPACITY),
				GetterAndSetter.standalone(0),
				GetterAndSetter.standalone(new int[0]),
				GetterAndSetter.standalone(0),
				GetterAndSetter.standalone(new NearbyComponents(List.of()))
		);
	}

	private RadioTowerMenu(
			MenuContext ctx, Inventory inventoryPlayer, MutableEnergyStorage energy,
			GetterAndSetter<Integer> frequency, GetterAndSetter<int[]> savedFrequencies,
			GetterAndSetter<Integer> range, GetterAndSetter<NearbyComponents> otherComponents
	)
	{
		super(ctx);
		this.energy = energy;
		this.frequency = frequency;
		this.savedFrequencies = savedFrequencies;
		this.range = range;
		this.otherComponents = otherComponents;

		addGenericData(GenericContainerData.energy(energy));
		addGenericData(new GenericContainerData<>(GenericDataSerializers.INT32, frequency));
		addGenericData(new GenericContainerData<>(GenericDataSerializers.INT_ARRAY, savedFrequencies));
		addGenericData(new GenericContainerData<>(GenericDataSerializers.INT32, range));
		addGenericData(new GenericContainerData<>(GenericDataSerializers.RADIO_TOWER_NEARBY, otherComponents));
	}


	@Override
	public void receiveMessageFromScreen(CompoundTag nbt)
	{
		if(nbt.contains("frequency", Tag.TAG_INT))
			this.frequency.set(nbt.getInt("frequency"));
		if(nbt.contains("savedFrequencies", Tag.TAG_INT_ARRAY))
			this.savedFrequencies.set(nbt.getIntArray("savedFrequencies"));
	}


	public record NearbyComponents(List<Vec3> positions)
	{
		public static NearbyComponents fromCtx(IMultiblockContext<RadioTowerLogic.State> ctx)
		{
			final State state = ctx.getState();
			List<Vec3> list = state.getRelativeComponentsInRange(ctx);
			final double distMod = 1/list.stream().max(
					Comparator.comparingDouble(Vec3::lengthSqr)
			).orElse(Vec3.ZERO).length();
			return new NearbyComponents(list.stream().map(vec3 -> vec3.scale(distMod)).toList());
		}

		public static NearbyComponents from(FriendlyByteBuf buffer)
		{
			return new NearbyComponents(buffer.readList(FriendlyByteBuf::readVec3));
		}

		public static void writeTo(FriendlyByteBuf out, NearbyComponents nearby)
		{
			out.writeCollection(nearby.positions(), FriendlyByteBuf::writeVec3);
		}
	}
}