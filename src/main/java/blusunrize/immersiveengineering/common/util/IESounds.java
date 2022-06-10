/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.util;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import net.minecraft.core.Registry;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;

import static blusunrize.immersiveengineering.ImmersiveEngineering.MODID;

public class IESounds
{
	private static final DeferredRegister<SoundEvent> REGISTER = DeferredRegister.create(
			Registry.SOUND_EVENT_REGISTRY, MODID
	);
	public static final SoundEvent metalpress_piston = registerSound("metal_press_piston");
	public static final SoundEvent metalpress_smash = registerSound("metal_press_smash");
	public static final SoundEvent birthdayParty = registerSound("birthday_party");
	public static final SoundEvent revolverFire = registerSound("revolver_fire");
	public static final SoundEvent revolverFireThump = registerSound("revolver_fire_thump");
	public static final SoundEvent revolverReload = registerSound("revolver_reload");
	public static final SoundEvent spray = registerSound("spray");
	public static final SoundEvent sprayFire = registerSound("spray_fire");
	public static final SoundEvent chargeFast = registerSound("charge_fast");
	public static final SoundEvent chargeSlow = registerSound("charge_slow");
	public static final SoundEvent spark = registerSound("spark");
	public static final SoundEvent railgunFire = registerSound("railgun_fire");
	public static final SoundEvent tesla = registerSound("tesla");
	public static final SoundEvent crusher = registerSound("crusher");
	public static final SoundEvent dieselGenerator = registerSound("diesel_generator");
	public static final SoundEvent direSwitch = registerSound("dire_switch");
	public static final SoundEvent chute = registerSound("chute");

	public static void init()
	{
		REGISTER.register(FMLJavaModLoadingContext.get().getModEventBus());
	}

	private static SoundEvent registerSound(String name)
	{
		// This is not the intended way of using deferred registers, but the constructor of SoundEvent doesn't actually
		// do anything interesting, and we need the instances to be available when registering villager professions
		SoundEvent event = new SoundEvent(ImmersiveEngineering.rl(name));
		REGISTER.register(name, () -> event);
		return event;
	}
}
