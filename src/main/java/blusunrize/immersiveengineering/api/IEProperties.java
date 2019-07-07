/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.api;

import blusunrize.immersiveengineering.api.energy.wires.Connection;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import net.minecraft.state.DirectionProperty;
import net.minecraft.state.IntegerProperty;
import net.minecraft.state.Property;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.client.model.data.ModelProperty;
import net.minecraftforge.client.model.obj.OBJModel.OBJState;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class IEProperties
{
	public static final DirectionProperty FACING_ALL = DirectionProperty.create("facing");
	public static final DirectionProperty FACING_HORIZONTAL = DirectionProperty.create("facing", Direction.Plane.HORIZONTAL);

	public static final PropertyBoolInverted MULTIBLOCKSLAVE = PropertyBoolInverted.create("_0multiblockslave");//Name starts with '_0' to ensure priority when overriding models
	public static final PropertyBoolInverted DYNAMICRENDER = PropertyBoolInverted.create("_1dynamicrender");//Name starts with '_1' to ensure priority over anything but the multiblockslave property
	public static final PropertyBoolInverted ACTIVE = PropertyBoolInverted.create("active");
	public static final PropertyBoolInverted IS_SECOND_STATE = PropertyBoolInverted.create("issecondstate");
	public static final PropertyBoolInverted MIRRORED = PropertyBoolInverted.create("mirrored");

	public static final Map<Direction, PropertyBoolInverted> SIDECONNECTION =
			ImmutableMap.<Direction, PropertyBoolInverted>builder()
					.put(Direction.DOWN, PropertyBoolInverted.create("sideconnection_down"))
					.put(Direction.UP, PropertyBoolInverted.create("sideconnection_up"))
					.put(Direction.NORTH, PropertyBoolInverted.create("sideconnection_north"))
					.put(Direction.SOUTH, PropertyBoolInverted.create("sideconnection_south"))
					.put(Direction.WEST, PropertyBoolInverted.create("sideconnection_west"))
					.put(Direction.EAST, PropertyBoolInverted.create("sideconnection_east"))
					.build();

	
	public static final IntegerProperty INT_4 = IntegerProperty.create("int_4", 0, 3);
	public static final IntegerProperty INT_16 = IntegerProperty.create("int_16", 0, 15);


	public static class PropertyBoolInverted extends Property<Boolean>
	{
		private static final ImmutableList<Boolean> ALLOWED_VALUES = ImmutableList.of(false, true);

		protected PropertyBoolInverted(String name)
		{
			super(name, Boolean.class);
		}

		@Override
		public Collection<Boolean> getAllowedValues()
		{
			return ALLOWED_VALUES;
		}

		@Override
		public Optional<Boolean> parseValue(String value)
		{
			return Optional.of(Boolean.parseBoolean(value));
		}

		public static PropertyBoolInverted create(String name)
		{
			return new PropertyBoolInverted(name);
		}

		@Override
		public String getName(Boolean value)
		{
			return value.toString();
		}
	}

	public static class ConnectionModelData
	{
		public final Set<Connection> connections;
		public final BlockPos here;

		public ConnectionModelData(Set<Connection> connections, BlockPos here)
		{
			this.connections = connections;
			this.here = here;
		}

		@Override
		public String toString()
		{
			return connections+" at "+here;
		}
	}

	public static class Model
	{
		public static final ModelProperty<OBJState> objState = new ModelProperty<>();
	}
}