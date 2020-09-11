/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 *
 */

package blusunrize.immersiveengineering.common.config;

import blusunrize.immersiveengineering.common.wires.IEWireTypes.IEWireType;
import com.google.common.collect.ImmutableList;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.BooleanValue;
import net.minecraftforge.common.ForgeConfigSpec.ConfigValue;
import net.minecraftforge.common.ForgeConfigSpec.IntValue;

import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class IEClientConfig
{
	public final static BooleanValue showUpdateNews;
	public final static BooleanValue fancyItemHolding;
	public final static BooleanValue stencilBufferEnabled;
	public final static ConfigValue<List<? extends String>> earDefenders_SoundBlacklist;
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
}
