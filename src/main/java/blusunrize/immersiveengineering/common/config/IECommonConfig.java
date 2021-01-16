/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 *
 */

package blusunrize.immersiveengineering.common.config;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.wires.WireLogger;
import blusunrize.immersiveengineering.common.util.compat.IECompatModule;
import com.google.common.collect.ImmutableList;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.BooleanValue;
import net.minecraftforge.common.ForgeConfigSpec.ConfigValue;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.config.ModConfig.ModConfigEvent;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.config.Configurator;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class IECommonConfig
{
	public static final BooleanValue enableWireLogger;
	public static final BooleanValue validateNet;
	public static final BooleanValue enableDebug;
	public static final Map<String, BooleanValue> compat = new HashMap<>();
	public static final ConfigValue<List<? extends String>> preferredOres;

	public static final ForgeConfigSpec CONFIG_SPEC;

	static
	{
		ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
		builder.comment("A list of all mods that IE has integrated compatability for", "Setting any of these to false disables the respective compat")
				.push("compat");
		for(String mod : IECompatModule.moduleClasses.keySet())
			compat.put(mod, builder
					.define(mod, true));
		builder.pop();
		preferredOres = builder
				.comment("A list of preferred Mod IDs that results of IE processes should stem from, aka which mod you want the copper to come from.",
						"This affects the ores dug by the excavator, as well as those crushing recipes that don't have associated IE items. This list is in oreder of priority.")
				.defineList("preferredOres", ImmutableList.of(ImmersiveEngineering.MODID, "minecraft"), obj -> true);
		builder.push(ImmutableList.of("debug", "wires"));
		enableWireLogger = builder
				.comment("Enable detailed logging for the wire network. This can be useful for developers to track"+
						" down issues related to wires.")
				.define("enableWireLogger", false);
		validateNet = builder
				.comment("Run sanity checks on the wire network after every interaction. This will cause a decent "+
						"amount of lag and a lot of log spam if the wire network isn't fully intact. Only enable "+
						"when asked to by an IE developer.")
				.define("validateNets", false);
		builder.pop(1);
		enableDebug = builder
				.comment("A config setting to enable debug features. These features may vary between releases, may cause crashes, and are unsupported. Do not enable unless asked to by a developer of IE.")
				.define("enableDebug", false);
		builder.pop();
		CONFIG_SPEC = builder.build();
	}

	@SubscribeEvent
	public static void onCommonReload(ModConfigEvent ev)
	{
		Level wireLoggerLevel;
		if(enableWireLogger.get())
			wireLoggerLevel = Level.ALL;
		else
			wireLoggerLevel = Level.WARN;
		Configurator.setLevel(WireLogger.logger.getName(), wireLoggerLevel);
	}
}
