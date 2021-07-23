/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.util;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;

import java.util.HashSet;
import java.util.Set;

import static blusunrize.immersiveengineering.ImmersiveEngineering.MODID;

/**
 * @author BluSunrize - 03.07.2016
 */
@Mod.EventBusSubscriber(modid = MODID, bus = Bus.MOD)
public class IESounds
{
	static Set<SoundEvent> registeredEvents = new HashSet<>();
	public static SoundEvent metalpress_piston = registerSound("metal_press_piston");
	public static SoundEvent metalpress_smash = registerSound("metal_press_smash");
	public static SoundEvent birthdayParty = registerSound("birthday_party");
	public static SoundEvent revolverFire = registerSound("revolver_fire");
	public static SoundEvent revolverFireThump = registerSound("revolver_fire_thump");
	public static SoundEvent revolverReload = registerSound("revolver_reload");
	public static SoundEvent spray = registerSound("spray");
	public static SoundEvent sprayFire = registerSound("spray_fire");
	public static SoundEvent chargeFast = registerSound("charge_fast");
	public static SoundEvent chargeSlow = registerSound("charge_slow");
	public static SoundEvent spark = registerSound("spark");
	public static SoundEvent railgunFire = registerSound("railgun_fire");
	public static SoundEvent tesla = registerSound("tesla");
	public static SoundEvent crusher = registerSound("crusher");
	public static SoundEvent dieselGenerator = registerSound("diesel_generator");
	public static SoundEvent direSwitch = registerSound("dire_switch");
	public static SoundEvent chute = registerSound("chute");

	private static SoundEvent registerSound(String name)
	{
		ResourceLocation location = new ResourceLocation(ImmersiveEngineering.MODID, name);
		SoundEvent event = new SoundEvent(location);
		registeredEvents.add(event.setRegistryName(location));
		return event;
	}

	@SubscribeEvent
	public static void registerSounds(RegistryEvent.Register<SoundEvent> evt)
	{
		for(SoundEvent event : registeredEvents)
			evt.getRegistry().register(event);
	}

	public static void PlaySoundForPlayer(Entity player, SoundEvent sound, float volume, float pitch)
	{
		if(player instanceof ServerPlayer)
			((ServerPlayer)player).connection.send(new ClientboundSoundPacket(sound, player.getSoundSource(),
					player.getX(), player.getY(), player.getZ(), volume, pitch));
	}
}
