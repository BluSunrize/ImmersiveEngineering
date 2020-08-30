/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraftforge.client.settings.IKeyConflictContext;
import net.minecraftforge.client.settings.KeyConflictContext;

import java.util.function.BiPredicate;

public class ItemKeybindConflictContext implements IKeyConflictContext
{
	private final BiPredicate<ItemStack, PlayerEntity> activePredicate;

	public ItemKeybindConflictContext(BiPredicate<ItemStack, PlayerEntity> activePredicate)
	{
		this.activePredicate = activePredicate;
	}

	@Override
	public boolean isActive()
	{
		if(ClientUtils.mc().currentScreen!=null)
			return false;
		PlayerEntity player = ClientUtils.mc().player;
		if(player!=null)
			for(Hand hand : Hand.values())
			{
				ItemStack held = player.getHeldItem(hand);
				if(this.activePredicate.test(held, player))
					return true;
			}
		return false;
	}

	@Override
	public boolean conflicts(IKeyConflictContext other)
	{
		return other==KeyConflictContext.IN_GAME;
	}
}
