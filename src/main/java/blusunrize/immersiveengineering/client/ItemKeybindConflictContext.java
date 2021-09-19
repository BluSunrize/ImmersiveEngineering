/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.settings.IKeyConflictContext;
import net.minecraftforge.client.settings.KeyConflictContext;

import java.util.function.BiPredicate;

public class ItemKeybindConflictContext implements IKeyConflictContext
{
	private final BiPredicate<ItemStack, Player> activePredicate;

	public ItemKeybindConflictContext(BiPredicate<ItemStack, Player> activePredicate)
	{
		this.activePredicate = activePredicate;
	}

	@Override
	public boolean isActive()
	{
		if(ClientUtils.mc().screen!=null)
			return false;
		Player player = ClientUtils.mc().player;
		if(player!=null)
			for(InteractionHand hand : InteractionHand.values())
			{
				ItemStack held = player.getItemInHand(hand);
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
