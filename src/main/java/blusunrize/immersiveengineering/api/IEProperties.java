package blusunrize.immersiveengineering.api;

import blusunrize.immersiveengineering.client.models.IOBJModelCallback;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableSet;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.properties.PropertyHelper;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.property.IUnlistedProperty;

import java.util.Collection;
import java.util.HashMap;
import java.util.Set;

public class IEProperties
{
	public static final PropertyDirection FACING_ALL = PropertyDirection.create("facing");
	public static final PropertyDirection FACING_HORIZONTAL = PropertyDirection.create("facing", EnumFacing.Plane.HORIZONTAL);
	public static final PropertyDirection FACING_VERTICAL = PropertyDirection.create("facing", EnumFacing.Plane.VERTICAL);

	public static final PropertyBoolInverted MULTIBLOCKSLAVE = PropertyBoolInverted.create("_0multiblockslave");//Name starts with '_0' to ensure priority when overriding models
	public static final PropertyBoolInverted DYNAMICRENDER = PropertyBoolInverted.create("_1dynamicrender");//Name starts with '_1' to ensure priority over anything but the multiblockslave property
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
		public Class<HashMap> getType() {
			return HashMap.class;
		}
		@Override
		public String valueToString(HashMap value)
		{
			return value.toString();
		}
	};
	public static final IUnlistedProperty<IOBJModelCallback> OBJ_MODEL_CALLBACK = new IUnlistedProperty<IOBJModelCallback>()
	{
		@Override
		public String getName()
		{
			return "obj_model_callback";
		}
		@Override
		public boolean isValid(IOBJModelCallback value)
		{
			return true;
		}
		@Override
		public Class<IOBJModelCallback> getType() {
			return IOBJModelCallback.class;
		}
		@Override
		public String valueToString(IOBJModelCallback value)
		{
			return value.toString();
		}
	};
	
	public static class PropertyBoolInverted extends PropertyHelper<Boolean>
	{
		private final ImmutableSet<Boolean> allowedValues = ImmutableSet.of(Boolean.valueOf(false), Boolean.valueOf(true));
		protected PropertyBoolInverted(String name)
		{
			super(name, Boolean.class);
		}
		@Override
		public Collection<Boolean> getAllowedValues()
		{
			return this.allowedValues;
		}
		@Override
		public Optional<Boolean> parseValue(String value)
		{
			return Optional.of(Boolean.getBoolean(value));
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
			return value != null;
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