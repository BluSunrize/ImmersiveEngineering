/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.util;

import com.mojang.authlib.GameProfile;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@EventBusSubscriber
public class FakePlayerUtil
{
	private static GameProfile IE_PROFILE = new GameProfile(UUID.fromString("99562b85-bd1a-4ded-bb1a-c307bf0c0133"), "[ImmersiveEngineering]");
	private static Map<LevelAccessor, FakePlayer> fakePlayerInstances = new HashMap<>();

	public static FakePlayer getFakePlayer(Level world)
	{
		return fakePlayerInstances.computeIfAbsent(world, w -> {
			if(w instanceof ServerLevel)
				return FakePlayerFactory.get((ServerLevel)w, IE_PROFILE);
			else
				return null;
		});
	}

	@SubscribeEvent
	public static void onUnload(WorldEvent.Unload ev)
	{
		LevelAccessor world = ev.getWorld();
		if(world instanceof ServerLevel)
			fakePlayerInstances.remove(world);
	}

}
