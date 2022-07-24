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
import blusunrize.immersiveengineering.api.IEApi;
import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.api.wires.WireLogger;
import blusunrize.immersiveengineering.common.util.compat.IECompatModules;
import com.google.common.collect.ImmutableList;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.BooleanValue;
import net.minecraftforge.common.ForgeConfigSpec.ConfigValue;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.fml.event.config.ModConfigEvent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@EventBusSubscriber(modid = Lib.MODID, bus = Bus.MOD)
public class IECommonConfig
{
	public static final BooleanValue enableWireLogger;
	public static final BooleanValue validateNet;
	public static final Map<String, BooleanValue> compat = new HashMap<>();
	public static final ConfigValue<List<? extends String>> preferredOres;

	public static final ForgeConfigSpec CONFIG_SPEC;

	static
	{
		ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
		builder.comment(
				"IMPORTANT NOTICE:",
				"THIS IS ONLY THE COMMON CONFIG. It does not contain all the values adjustable for IE.",
				"All modifiers for machines, all ore gen, the retrogen features and most other adjustable values have been moved to immersiveengineering-server.toml.",
				"That file is PER WORLD, meaning you have to go into 'saves/<world name>/serverconfig' to adjust it. Those changes will then only apply for THAT WORLD.",
				"You can then take that config file and put it in the 'defaultconfigs' folder to make it apply automatically to all NEW worlds you generate FROM THERE ON.",
				"This may appear confusing to many of you, but it is a new sensible way to handle configuration, because the server configuration is synced when playing multiplayer."
		).define("importantInfo", true);

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
		builder.pop(2);
		builder.comment("A list of all mods that IE has integrated compatability for", "Setting any of these to false disables the respective compat")
				.push("compat");
		for(String mod : IECompatModules.getAvailableModules())
			compat.put(mod, builder.define(mod, true));
		builder.pop();
		CONFIG_SPEC = builder.build();
	}

	@SubscribeEvent
	public static void onCommonReload(ModConfigEvent ev)
	{
		if(CONFIG_SPEC==ev.getConfig().getSpec())
		{
			WireLogger.logger.setEnabled(enableWireLogger.get());
			IEApi.modPreference = preferredOres.get();
		}
	}
}
