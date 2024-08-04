/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.api.wires.utils;

import blusunrize.immersiveengineering.api.utils.SetRestrictedField;
import blusunrize.immersiveengineering.api.wires.IWireCoil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public class WirecoilUtils
{
	public static final SetRestrictedField<UseCallback> COIL_USE = SetRestrictedField.common();

	public static InteractionResult doCoilUse(
			IWireCoil coil, Player player, Level world, BlockPos pos, InteractionHand hand, Direction side,
			float hitX, float hitY, float hitZ
	)
	{
		return COIL_USE.get().doCoilUse(coil, player, world, pos, hand, side, hitX, hitY, hitZ);
	}

	public interface UseCallback
	{
		InteractionResult doCoilUse(
				IWireCoil coil, Player player, Level world, BlockPos pos, InteractionHand hand, Direction side,
				float hitX, float hitY, float hitZ
		);
	}
}
