/*
 *  BluSunrize
 *  Copyright (c) 2021
 *
 *  This code is licensed under "Blu's License of Common Sense"
 *  Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.api.wires;

import net.minecraft.nbt.CompoundTag;

import java.util.List;

/**
 * Implemented by {@link net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate} using Mixins
 */
public interface IConnectionTemplate
{
	/**
	 * @return The connections in this template, in relative coordinates. This value can be modified.
	 */
	List<TemplateConnection> getStoredConnections();

	record TemplateConnection(ConnectionPoint endA, ConnectionPoint endB, WireType type)
	{
		public TemplateConnection(CompoundTag nbt)
		{
			this(
					new ConnectionPoint(nbt.getCompound("endA")),
					new ConnectionPoint(nbt.getCompound("endB")),
					WireType.getValue(nbt.getString("type"))
			);
		}

		public CompoundTag toNBT()
		{
			CompoundTag nbt = new CompoundTag();
			nbt.put("endA", endA.createTag());
			nbt.put("endB", endB.createTag());
			nbt.putString("type", type.getUniqueName());
			return nbt;
		}
	}
}
