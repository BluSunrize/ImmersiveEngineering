/*
 * BluSunrize
 * Copyright (c) 2022
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.util.compat.computers.generic;

import blusunrize.immersiveengineering.api.utils.ComputerControlState;

import java.util.stream.Stream;

public interface ComputerControllable
{
	Stream<ComputerControlState> getAllComputerControlStates();
}
