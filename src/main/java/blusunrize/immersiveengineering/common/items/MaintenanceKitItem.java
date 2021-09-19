/*
 * BluSunrize
 * Copyright (c) 2018
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.items;

import javax.annotation.Nonnull;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class MaintenanceKitItem extends IEBaseItem
{
	public MaintenanceKitItem()
	{
		super("maintenance_kit", new Properties().stacksTo(1).defaultDurability(50));
	}

	@Nonnull
	@Override
	public InteractionResultHolder<ItemStack> use(Level world, Player player, @Nonnull InteractionHand hand)
	{
		if(!world.isClientSide)
			openGui(player, hand==InteractionHand.MAIN_HAND?EquipmentSlot.MAINHAND: EquipmentSlot.OFFHAND);
		ItemStack stack = player.getItemInHand(hand);
		return new InteractionResultHolder<>(InteractionResult.SUCCESS, stack);
	}
}
