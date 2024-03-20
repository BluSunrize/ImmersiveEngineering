/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.api;

import blusunrize.immersiveengineering.api.multiblocks.MultiblockHandler.IMultiblock;
import blusunrize.immersiveengineering.api.utils.SetRestrictedField;
import blusunrize.lib.manual.ManualInstance;
import blusunrize.lib.manual.SpecialManualElement;
import blusunrize.lib.manual.utils.ManualRecipeRef;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * @author BluSunrize - 04.07.2015
 */
public class ManualHelper
{
	public static String CAT_RESOURCES = "resources";
	public static String CAT_WORKBENCHES_STORAGE = "workbenches_storage";
	public static String CAT_CONSTRUCTION = "construction";
	public static String CAT_ELECTRICAL_GRIDS = "electrical_grids";
	public static String CAT_EXPLOSIVES_WEAPONRY = "explosives_weaponry";
	public static String CAT_TOOLS = "tools";
	public static String CAT_SIMPLE_MACHINERY = "simple_machinery";
	public static String CAT_LARGE_FURNACES_OVENS = "large_furnaces_ovens";
	public static String CAT_HEAVY_MACHINERY = "heavy_machinery";
	public static String CAT_UPDATE = "update";

	/**
	 * A map of keys (manual anchors) to suppliers that generate a table.
	 * Example implementations include the Thermoelectric generator and Fermenter / Squeezer
	 */
	public static final Map<String, Supplier<Component[][]>> DYNAMIC_TABLES = new HashMap<>();

	public static final SetRestrictedField<ManualInstance> IE_MANUAL_INSTANCE = SetRestrictedField.client();
	public static final SetRestrictedField<Consumer<Function<String, Object>>> ADD_CONFIG_GETTER = SetRestrictedField.client();
	public static final SetRestrictedField<MultiblockElementConstructor> MAKE_MULTIBLOCK_ELEMENT = SetRestrictedField.client();
	public static final SetRestrictedField<BlueprintElementConstructor> MAKE_BLUEPRINT_ELEMENT = SetRestrictedField.client();
	public static final SetRestrictedField<BlueprintElementConstructorNew> MAKE_BLUEPRINT_ELEMENT_NEW = SetRestrictedField.client();

	public static ManualInstance getManual()
	{
		return IE_MANUAL_INSTANCE.get();
	}

	/**
	 * Adds a new source of information for the "&lt;config"-syntax in the IE manual
	 *
	 * @param newGetter returns either the object for the given key or null
	 */
	public static void addConfigGetter(Function<String, Object> newGetter)
	{
		ADD_CONFIG_GETTER.get().accept(newGetter);
	}

	public interface MultiblockElementConstructor
	{
		SpecialManualElement create(IMultiblock multiblock);
	}

	public interface BlueprintElementConstructor
	{
		SpecialManualElement create(ItemStack... stacks);
	}

	public interface BlueprintElementConstructorNew
	{
		SpecialManualElement create(ManualRecipeRef... stacks);
	}
}
