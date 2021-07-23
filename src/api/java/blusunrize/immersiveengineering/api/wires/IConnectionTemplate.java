/*
 *  BluSunrize
 *  Copyright (c) 2021
 *
 *  This code is licensed under "Blu's License of Common Sense"
 *  Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.api.wires;

import java.util.List;

/**
 * Implemented by {@link net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate} using Mixins
 */
public interface IConnectionTemplate
{
	/**
	 * @return The connections in this template, in relative coordinates. This value can be modified.
	 */
	List<Connection> getStoredConnections();
}
