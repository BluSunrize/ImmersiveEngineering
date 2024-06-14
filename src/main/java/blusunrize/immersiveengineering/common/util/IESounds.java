/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.util;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import static blusunrize.immersiveengineering.ImmersiveEngineering.MODID;

public class IESounds
{
	private static final DeferredRegister<SoundEvent> REGISTER = DeferredRegister.create(
			Registries.SOUND_EVENT, MODID
	);
	public static final Holder<SoundEvent> metalpress_piston = registerSound("metal_press_piston");
	public static final Holder<SoundEvent> metalpress_smash = registerSound("metal_press_smash");
	public static final Holder<SoundEvent> birthdayParty = registerSound("birthday_party");
	public static final Holder<SoundEvent> revolverFire = registerSound("revolver_fire");
	public static final Holder<SoundEvent> revolverFireThump = registerSound("revolver_fire_thump");
	public static final Holder<SoundEvent> revolverReload = registerSound("revolver_reload");
	public static final Holder<SoundEvent> spray = registerSound("spray");
	public static final Holder<SoundEvent> sprayFire = registerSound("spray_fire");
	public static final Holder<SoundEvent> chargeFast = registerSound("charge_fast");
	public static final Holder<SoundEvent> chargeSlow = registerSound("charge_slow");
	public static final Holder<SoundEvent> spark = registerSound("spark");
	public static final Holder<SoundEvent> railgunFire = registerSound("railgun_fire");
	public static final Holder<SoundEvent> tesla = registerSound("tesla");
	public static final Holder<SoundEvent> crusher = registerSound("crusher");
	public static final Holder<SoundEvent> dieselGenerator = registerSound("diesel_generator");
	public static final Holder<SoundEvent> direSwitch = registerSound("dire_switch");
	public static final Holder<SoundEvent> chute = registerSound("chute");
	public static Holder<SoundEvent> glider = registerSound("glider");
	public static final Holder<SoundEvent> assembler = registerSound("assembler");
	public static final Holder<SoundEvent> refinery = registerSound("refinery");
	public static final Holder<SoundEvent> bottling = registerSound("bottling");
	public static final Holder<SoundEvent> saw_startup = registerSound("saw_startup");
	public static final Holder<SoundEvent> saw_empty = registerSound("saw_empty");
	public static final Holder<SoundEvent> saw_full = registerSound("saw_full");
	public static final Holder<SoundEvent> saw_shutdown = registerSound("saw_shutdown");
	public static final Holder<SoundEvent> mixer = registerSound("mixer");
	public static final Holder<SoundEvent> fermenter = registerSound("fermenter");
	public static final Holder<SoundEvent> preheater = registerSound("preheater");
	public static final Holder<SoundEvent> arcFurnace = registerSound("arc_furnace");
	public static final Holder<SoundEvent> oreConveyor = registerSound("ore_conveyor");
	public static final Holder<SoundEvent> oreDump = registerSound("ore_dump");
	public static final Holder<SoundEvent> process1 = registerSound("process_1");
	public static final Holder<SoundEvent> process1Lift = registerSound("process_1_lift");
	public static final Holder<SoundEvent> process2 = registerSound("process_2");
	public static final Holder<SoundEvent> process2Lift = registerSound("process_2_lift");
	public static final Holder<SoundEvent> wooden_creaking = registerSound("wooden_creaking");
	public static final Holder<SoundEvent> windmill = registerSound("windmill");
	public static final Holder<SoundEvent> electromagnet = registerSound("electromagnet");


	public static void init(IEventBus modBus)
	{
		REGISTER.register(modBus);
	}

	private static Holder<SoundEvent> registerSound(String name)
	{
		return REGISTER.register(name, () -> SoundEvent.createVariableRangeEvent(ImmersiveEngineering.rl(name)));
	}
}
