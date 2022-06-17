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
import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.common.wires.IEWireTypes.IEWireType;
import com.google.common.collect.ImmutableList;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.BooleanValue;
import net.minecraftforge.common.ForgeConfigSpec.ConfigValue;
import net.minecraftforge.common.ForgeConfigSpec.DoubleValue;
import net.minecraftforge.common.ForgeConfigSpec.IntValue;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.fml.event.config.ModConfigEvent;

import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@EventBusSubscriber(value = Dist.CLIENT, modid = Lib.MODID, bus = Bus.MOD)
public class IEClientConfig
{
	public final static BooleanValue showUpdateNews;
	public final static BooleanValue fancyItemHolding;
	public final static BooleanValue stencilBufferEnabled;
	public final static ConfigValue<List<? extends String>> earDefenders_SoundBlacklist;
	public final static BooleanValue enableVBOs;
	public final static BooleanValue disableFancyTESR;
	public final static BooleanValue showTextOverlay;
	public final static BooleanValue nixietubeFont;
	public final static IntValue manualGuiScale;
	public final static BooleanValue badEyesight;
	public static boolean lastBadEyesight;
	public final static BooleanValue tagTooltips;
	public final static DoubleValue increasedTileRenderdistance;
	public final static Map<IEWireType, IntValue> wireColors = new EnumMap<>(IEWireType.class);

	public static final ForgeConfigSpec CONFIG_SPEC;

	private static void addColor(ForgeConfigSpec.Builder builder, IEWireType type, int defaultColor)
	{
		wireColors.put(type, builder.defineInRange(type.name().toLowerCase(Locale.ENGLISH), defaultColor,
				Integer.MIN_VALUE, Integer.MAX_VALUE));
	}

	static
	{
		ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
		disableFancyTESR = builder
				.comment("Disables most lighting code for certain models that are rendered dynamically (TESR). May improve FPS.",
						"Affects turrets and garden cloches")
				.define("disableFancyTESR", false);
		showTextOverlay = builder
				.comment("Show the text overlay for various blocks, such as the configuration of capacitors or pumps")
				.define("showTextOverlay", true);
		nixietubeFont = builder
				.comment("Set this to false to disable the super awesome looking nixie tube front for the voltmeter and other things")
				.define("nixietubeFont", true);
		manualGuiScale = builder
				.comment("Set the GUI scale of the Engineer's Manual. This uses the same numbers as Vanilla's GUI Scale and is therefor limited to the maximum value available ingame.")
				.defineInRange("manualGuiScale", 4, 1, 32);
		badEyesight = builder
				.comment("Set this to true if you suffer from bad eyesight. The Engineer's manual will be switched to a bold and darker text to improve readability.")
				.define("badEyesight", false);
		tagTooltips = builder
				.comment("Controls if item tooltips should contain the tags names of items. These tooltips are only visible in advanced tooltip mode (F3+H)")
				.define("tagTooltips", true);
		increasedTileRenderdistance = builder
				.comment("Increase the distance at which certain TileEntities (specifically windmills) are still visible. This is a modifier, so set it to 1 for default render distance, to 2 for doubled distance and so on.")
				.defineInRange("increasedTileRenderdistance", 1.5, 0, Double.MAX_VALUE);
		showUpdateNews = builder
				.comment("Set this to false to hide the update news in the manual")
				.define("showUpdateNews", true);
		fancyItemHolding = builder
				.comment("Allows revolvers and other IE items to look properly held in 3rd person. This uses a coremod. Can be disabled in case of conflicts with other animation mods.")
				.define("fancyItemHolding", true);
		stencilBufferEnabled = builder
				.comment("Set to false to disable the stencil buffer. This may be necessary on older GPUs.")
				.define("stencilBufferEnabled", true);
		earDefenders_SoundBlacklist = builder
				.comment("A list of sounds that should not be muffled by the Ear Defenders. Adding to this list requires knowledge of the correct sound resource names.")
				.defineList("earDefenders_SoundBlacklist", ImmutableList.of(), obj -> true);
		enableVBOs = builder
				.comment("Use VBOs to render certain blocks. This is significantly faster than the usual rendering,",
						"but may not work correctly with visual effects from other mods")
				.define("enableVBO", true);
		builder.comment("Options to set the RGB color of all IE wire types")
				.push("wire_colors");
		addColor(builder, IEWireType.COPPER, 0xb36c3f);
		addColor(builder, IEWireType.ELECTRUM, 0xeda045);
		addColor(builder, IEWireType.STEEL, 0x6f6f6f);
		addColor(builder, IEWireType.STRUCTURE_ROPE, 0x967e6d);
		addColor(builder, IEWireType.STRUCTURE_STEEL, 0x6f6f6f);
		addColor(builder, IEWireType.REDSTONE, 0xff2f2f);
		addColor(builder, IEWireType.COPPER_INSULATED, 0xfaf1de);
		addColor(builder, IEWireType.ELECTRUM_INSULATED, 0x9d857a);
		builder.pop();
		CONFIG_SPEC = builder.build();
	}

	@SubscribeEvent
	public static void onConfigChange(ModConfigEvent ev)
	{
		if(CONFIG_SPEC==ev.getConfig().getSpec())
		{
			lastBadEyesight = badEyesight.get();
			ImmersiveEngineering.proxy.resetManual();
		}
	}
}
