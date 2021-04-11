/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.api.wires.utils;

import blusunrize.immersiveengineering.api.utils.ItemUtils;
import blusunrize.immersiveengineering.api.utils.SetRestrictedField;
import blusunrize.immersiveengineering.api.wires.IWireCoil;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants.NBT;

public class WirecoilUtils
{
	public static final SetRestrictedField<UseCallback> COIL_USE = SetRestrictedField.common();

	public static ActionResultType doCoilUse(
			IWireCoil coil, PlayerEntity player, World world, BlockPos pos, Hand hand, Direction side,
			float hitX, float hitY, float hitZ
	)
	{
		return COIL_USE.getValue().doCoilUse(coil, player, world, pos, hand, side, hitX, hitY, hitZ);
	}

	public static void clearWireLink(ItemStack stack)
	{
		ItemUtils.removeTag(stack, "linkingDim");
		ItemUtils.removeTag(stack, "linkingPos");
		ItemUtils.removeTag(stack, "linkingOffset");
		ItemUtils.removeTag(stack, "linkingTarget");
	}

	public static boolean hasWireLink(ItemStack stack)
	{
		return ItemUtils.hasTag(stack, "linkingDim", NBT.TAG_STRING);
	}

	public interface UseCallback
	{
		ActionResultType doCoilUse(
				IWireCoil coil, PlayerEntity player, World world, BlockPos pos, Hand hand, Direction side,
				float hitX, float hitY, float hitZ
		);
	}
}
