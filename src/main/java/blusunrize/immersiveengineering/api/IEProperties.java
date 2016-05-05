package blusunrize.immersiveengineering.api;

import java.util.Collection;
import java.util.HashMap;
import java.util.Set;

import com.google.common.collect.ImmutableSet;

import blusunrize.immersiveengineering.api.energy.wires.WireType;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.properties.PropertyHelper;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.property.IUnlistedProperty;

public class IEProperties
{
	public static final PropertyDirection FACING_ALL = PropertyDirection.create("facing");
	public static final PropertyDirection FACING_HORIZONTAL = PropertyDirection.create("facing", EnumFacing.Plane.HORIZONTAL);
	public static final PropertyDirection FACING_VERTICAL = PropertyDirection.create("facing", EnumFacing.Plane.VERTICAL);

	public static final PropertyBoolInverted MULTIBLOCKSLAVE = PropertyBoolInverted.create("*multiblockslave");//Name starts with an asterisk to ensure priority when overriding models
	public static final PropertyBoolInverted DYNAMICRENDER = PropertyBoolInverted.create("+dynamicrender");//Name starts with a plus to ensure priority over anything but the multiblockslave property
	public static final PropertySet CONNECTIONS = new PropertySet("conns");
	
	public static final PropertyEnum[] SIDECONFIG = {
			PropertyEnum.create("sideconfig_down", IEEnums.SideConfig.class),
			PropertyEnum.create("sideconfig_up", IEEnums.SideConfig.class),
			PropertyEnum.create("sideconfig_north", IEEnums.SideConfig.class),
			PropertyEnum.create("sideconfig_south", IEEnums.SideConfig.class),
			PropertyEnum.create("sideconfig_west", IEEnums.SideConfig.class),
			PropertyEnum.create("sideconfig_east", IEEnums.SideConfig.class)
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
	public static final PropertyInteger INT_4 = PropertyInteger.create("int_4", 0,3);

	public static final PropertyBoolInverted[] CONVEYORWALLS = {PropertyBoolInverted.create("conveyorwall_left"), PropertyBoolInverted.create("conveyorwall_right")};
	public static final PropertyInteger CONVEYORUPDOWN = PropertyInteger.create("conveyorupdown", 0,2);
//	public static final Property

	public static final IUnlistedProperty<HashMap> OBJ_TEXTURE_REMAP = new IUnlistedProperty<HashMap>()
	{
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
		public Class<HashMap> getType() {
			return HashMap.class;
		}
		@Override
		public String valueToString(HashMap value)
		{
			return value.toString();
		}
	};

	public static class PropertyBoolInverted extends PropertyHelper<Boolean>
	{
		private final ImmutableSet<Boolean> allowedValues = ImmutableSet.<Boolean>of(Boolean.valueOf(false), Boolean.valueOf(true));
		protected PropertyBoolInverted(String name)
		{
			super(name, Boolean.class);
		}
		public Collection<Boolean> getAllowedValues()
		{
			return this.allowedValues;
		}
		public static PropertyBoolInverted create(String name)
		{
			return new PropertyBoolInverted(name);
		}
		public String getName(Boolean value)
		{
			return value.toString();
		}
	}

	@SuppressWarnings("rawtypes")
	public static class PropertySet implements IUnlistedProperty<Set>
	{
		String name;

		public PropertySet(String n)
		{
			name = n;
		}

		@Override
		public String getName()
		{
			return name;
		}

		@Override
		public boolean isValid(Set value)
		{
			if (value == null)
				return false;
			return true;
		}

		@Override
		public Class<Set> getType()
		{
			return Set.class;
		}

		@Override
		public String valueToString(Set value)
		{
			return value.toString();
		}
	}
}