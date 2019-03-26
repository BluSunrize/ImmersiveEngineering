/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.api;

import blusunrize.immersiveengineering.api.IEEnums.SideConfig;
import blusunrize.immersiveengineering.api.energy.wires.Connection;
import com.google.common.collect.ImmutableList;
import net.minecraft.state.AbstractProperty;
import net.minecraft.state.DirectionProperty;
import net.minecraft.state.IntegerProperty;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.property.IUnlistedProperty;

import java.util.Collection;
import java.util.HashMap;
import java.util.Optional;
import java.util.Set;

public class IEProperties
{
	public static final DirectionProperty FACING_ALL = DirectionProperty.create("facing");
	public static final DirectionProperty FACING_HORIZONTAL = DirectionProperty.create("facing", EnumFacing.Plane.HORIZONTAL);

	public static final PropertyBoolInverted MULTIBLOCKSLAVE = PropertyBoolInverted.create("_0multiblockslave");//Name starts with '_0' to ensure priority when overriding models
	public static final PropertyBoolInverted DYNAMICRENDER = PropertyBoolInverted.create("_1dynamicrender");//Name starts with '_1' to ensure priority over anything but the multiblockslave property
	public static final PropertyConnections CONNECTIONS = new PropertyConnections("conns");

	//	public static final PropertyEnum[] SIDECONFIG = {
//			PropertyEnum.create("sideconfig_down", IEEnums.SideConfig.class),
//			PropertyEnum.create("sideconfig_up", IEEnums.SideConfig.class),
//			PropertyEnum.create("sideconfig_north", IEEnums.SideConfig.class),
//			PropertyEnum.create("sideconfig_south", IEEnums.SideConfig.class),
//			PropertyEnum.create("sideconfig_west", IEEnums.SideConfig.class),
//			PropertyEnum.create("sideconfig_east", IEEnums.SideConfig.class)
//	};
	public static final ProperySideConfig[] SIDECONFIG = {
			new ProperySideConfig("sideconfig_down"),
			new ProperySideConfig("sideconfig_up"),
			new ProperySideConfig("sideconfig_north"),
			new ProperySideConfig("sideconfig_south"),
			new ProperySideConfig("sideconfig_west"),
			new ProperySideConfig("sideconfig_east")
	};
	public static final PropertyBoolInverted[] SIDECONNECTION = {
			PropertyBoolInverted.create("sideconnection_down"),
			PropertyBoolInverted.create("sideconnection_up"),
			PropertyBoolInverted.create("sideconnection_north"),
			PropertyBoolInverted.create("sideconnection_south"),
			PropertyBoolInverted.create("sideconnection_west"),
			PropertyBoolInverted.create("sideconnection_east")
	};

	//An array of non-descript booleans for mirroring, active textures, etc.
	public static final PropertyBoolInverted[] BOOLEANS = {
			PropertyBoolInverted.create("boolean0"),
			PropertyBoolInverted.create("boolean1"),
			PropertyBoolInverted.create("boolean2")
	};
	public static final IntegerProperty INT_4 = IntegerProperty.create("int_4", 0, 3);
	public static final IntegerProperty INT_16 = IntegerProperty.create("int_16", 0, 15);

	public static class ProperySideConfig implements IUnlistedProperty<SideConfig>
	{
		final String name;

		public ProperySideConfig(String name)
		{
			this.name = name;
		}

		@Override
		public String getName()
		{
			return name;
		}

		@Override
		public boolean isValid(SideConfig value)
		{
			return true;
		}

		@Override
		public Class<SideConfig> getType()
		{
			return IEEnums.SideConfig.class;
		}

		@Override
		public String valueToString(SideConfig value)
		{
			return value.toString();
		}
	}

	public static final IUnlistedProperty<HashMap> OBJ_TEXTURE_REMAP = new IUnlistedProperty<HashMap>()
	{
		@Override
		public String getName()
		{
			return "obj_texture_remap";
		}

		@Override
		public boolean isValid(HashMap value)
		{
			return true;
		}

		@Override
		public Class<HashMap> getType()
		{
			return HashMap.class;
		}

		@Override
		public String valueToString(HashMap value)
		{
			return value.toString();
		}
	};

	public static class PropertyBoolInverted extends AbstractProperty<Boolean>
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

	public static class PropertyConnections implements IUnlistedProperty<ConnectionModelData>
	{
		String name;

		public PropertyConnections(String n)
		{
			name = n;
		}

		@Override
		public String getName()
		{
			return name;
		}

		@Override
		public boolean isValid(ConnectionModelData value)
		{
			return value!=null;
		}

		@Override
		public Class<ConnectionModelData> getType()
		{
			return ConnectionModelData.class;
		}

		@Override
		public String valueToString(ConnectionModelData value)
		{
			return value.toString();
		}
	}

	public static final IUnlistedProperty<TileEntity> TILEENTITY_PASSTHROUGH = new IUnlistedProperty<TileEntity>()
	{
		@Override
		public String getName()
		{
			return "tileentity_passthrough";
		}

		@Override
		public boolean isValid(TileEntity value)
		{
			return true;
		}

		@Override
		public Class<TileEntity> getType()
		{
			return TileEntity.class;
		}

		@Override
		public String valueToString(TileEntity value)
		{
			return value.toString();
		}
	};
}