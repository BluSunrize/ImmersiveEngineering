/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.util;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import net.minecraft.core.registries.Registries;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

import static blusunrize.immersiveengineering.ImmersiveEngineering.MODID;

public class IESounds
{
	private static final DeferredRegister<SoundEvent> REGISTER = DeferredRegister.create(
			Registries.SOUND_EVENT, MODID
	);
	public static final RegistryObject<SoundEvent> metalpress_piston = registerSound("metal_press_piston");
	public static final RegistryObject<SoundEvent> metalpress_smash = registerSound("metal_press_smash");
	public static final RegistryObject<SoundEvent> birthdayParty = registerSound("birthday_party");
	public static final RegistryObject<SoundEvent> revolverFire = registerSound("revolver_fire");
	public static final RegistryObject<SoundEvent> revolverFireThump = registerSound("revolver_fire_thump");
	public static final RegistryObject<SoundEvent> revolverReload = registerSound("revolver_reload");
	public static final RegistryObject<SoundEvent> spray = registerSound("spray");
	public static final RegistryObject<SoundEvent> sprayFire = registerSound("spray_fire");
	public static final RegistryObject<SoundEvent> chargeFast = registerSound("charge_fast");
	public static final RegistryObject<SoundEvent> chargeSlow = registerSound("charge_slow");
	public static final RegistryObject<SoundEvent> spark = registerSound("spark");
	public static final RegistryObject<SoundEvent> railgunFire = registerSound("railgun_fire");
	public static final RegistryObject<SoundEvent> tesla = registerSound("tesla");
	public static final RegistryObject<SoundEvent> crusher = registerSound("crusher");
	public static final RegistryObject<SoundEvent> dieselGenerator = registerSound("diesel_generator");
	public static final RegistryObject<SoundEvent> direSwitch = registerSound("dire_switch");
	public static final RegistryObject<SoundEvent> chute = registerSound("chute");
	public static RegistryObject<SoundEvent> glider = registerSound("glider");
	public static final RegistryObject<SoundEvent> assembler = registerSound("assembler");
	public static final RegistryObject<SoundEvent> refinery = registerSound("refinery");
	public static final RegistryObject<SoundEvent> bottling = registerSound("bottling");
	public static final RegistryObject<SoundEvent> saw_startup = registerSound("saw_startup");
	public static final RegistryObject<SoundEvent> saw_empty = registerSound("saw_empty");
	public static final RegistryObject<SoundEvent> saw_full = registerSound("saw_full");
	public static final RegistryObject<SoundEvent> saw_shutdown = registerSound("saw_shutdown");
	public static final RegistryObject<SoundEvent> mixer = registerSound("mixer");
	public static final RegistryObject<SoundEvent> fermenter = registerSound("fermenter");
	public static final RegistryObject<SoundEvent> preheater = registerSound("preheater");
	public static final RegistryObject<SoundEvent> arcFurnace = registerSound("arc_furnace");
	public static final RegistryObject<SoundEvent> oreConveyor = registerSound("ore_conveyor");
	public static final RegistryObject<SoundEvent> oreDump = registerSound("ore_dump");
	public static final RegistryObject<SoundEvent> process1 = registerSound("process_1");
	public static final RegistryObject<SoundEvent> process1Lift = registerSound("process_1_lift");
	public static final RegistryObject<SoundEvent> process2 = registerSound("process_2");
	public static final RegistryObject<SoundEvent> process2Lift = registerSound("process_2_lift");
	public static final RegistryObject<SoundEvent> electromagnet = registerSound("electromagnet");


	public static void init()
	{
		REGISTER.register(FMLJavaModLoadingContext.get().getModEventBus());
	}

	private static RegistryObject<SoundEvent> registerSound(String name)
	{
		return REGISTER.register(name, () -> SoundEvent.createVariableRangeEvent(ImmersiveEngineering.rl(name)));
	}
}
