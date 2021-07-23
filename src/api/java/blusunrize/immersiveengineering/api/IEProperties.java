/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.api;

import blusunrize.immersiveengineering.api.IEEnums.IOSideConfig;
import blusunrize.immersiveengineering.api.excavator.MineralMix;
import blusunrize.immersiveengineering.api.utils.DirectionUtils;
import blusunrize.immersiveengineering.api.wires.Connection;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.mojang.math.Transformation;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraftforge.client.model.data.ModelProperty;

import java.util.*;

public class IEProperties
{
	public static final DirectionProperty FACING_ALL = DirectionProperty.create("facing", DirectionUtils.VALUES);
	public static final DirectionProperty FACING_HORIZONTAL = DirectionProperty.create("facing", Direction.Plane.HORIZONTAL);
	public static final DirectionProperty FACING_TOP_DOWN = DirectionProperty.create("facing", Direction.UP, Direction.DOWN);

	public static final PropertyBoolInverted MULTIBLOCKSLAVE = PropertyBoolInverted.create("multiblockslave");
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
		public Collection<Boolean> getPossibleValues()
		{
			return ALLOWED_VALUES;
		}

		@Override
		public Optional<Boolean> getValue(String value)
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

	public static class VisibilityList
	{
		private final Set<String> selected;
		private final boolean showSelected;

		private VisibilityList(Collection<String> selected, boolean show)
		{
			this.selected = new HashSet<>(selected);
			this.showSelected = show;
		}

		public static VisibilityList show(String... visible)
		{
			return show(Arrays.asList(visible));
		}

		public static VisibilityList show(Collection<String> visible)
		{
			return new VisibilityList(visible, true);
		}

		public static VisibilityList hide(Collection<String> hidden)
		{
			return new VisibilityList(hidden, false);
		}

		public static VisibilityList showAll()
		{
			return hide(ImmutableSet.of());
		}

		public static VisibilityList hideAll()
		{
			return show(ImmutableList.of());
		}

		public boolean isVisible(String group)
		{
			return showSelected==selected.contains(group);
		}

		@Override
		public boolean equals(Object o)
		{
			if(this==o) return true;
			if(o==null||getClass()!=o.getClass()) return false;
			VisibilityList that = (VisibilityList)o;
			return showSelected==that.showSelected&&
					selected.equals(that.selected);
		}

		@Override
		public int hashCode()
		{
			return Objects.hash(selected, showSelected);
		}
	}

	public static class IEObjState
	{
		public final VisibilityList visibility;
		public final Transformation transform;

		public IEObjState(VisibilityList visibility)
		{
			this(visibility, Transformation.identity());
		}

		public IEObjState(VisibilityList visibility, Transformation transform)
		{
			this.visibility = visibility;
			this.transform = transform;
		}

		@Override
		public boolean equals(Object o)
		{
			if(this==o) return true;
			if(o==null||getClass()!=o.getClass()) return false;
			IEObjState that = (IEObjState)o;
			return visibility.equals(that.visibility)&&
					transform.equals(that.transform);
		}

		@Override
		public int hashCode()
		{
			return Objects.hash(visibility, transform);
		}
	}

	public static class Model
	{
		public static final ModelProperty<IEObjState> IE_OBJ_STATE = new ModelProperty<>();
		public static final ModelProperty<Map<String, String>> TEXTURE_REMAP = new ModelProperty<>();
		public static final ModelProperty<ConnectionModelData> CONNECTIONS = new ModelProperty<>();
		public static final ModelProperty<MineralMix[]> MINERAL = new ModelProperty<>();
		public static final ModelProperty<Map<Direction, IOSideConfig>> SIDECONFIG = new ModelProperty<>();
		public static final ModelProperty<BlockPos> SUBMODEL_OFFSET = new ModelProperty<>();
	}
}