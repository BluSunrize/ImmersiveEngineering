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
import net.minecraft.world.item.ItemStack;

import java.util.function.Consumer;
import java.util.function.Function;

/**
 * @author BluSunrize - 04.07.2015
 */
public class ManualHelper
{
	public static String CAT_GENERAL = "general";
	public static String CAT_CONSTRUCTION = "construction";
	public static String CAT_ENERGY = "energy";
	public static String CAT_MACHINES = "machines";
	public static String CAT_TOOLS = "tools";
	public static String CAT_HEAVYMACHINES = "heavymachines";
	public static String CAT_UPDATE = "update";

	public static final SetRestrictedField<ManualInstance> IE_MANUAL_INSTANCE = SetRestrictedField.client();
	public static final SetRestrictedField<Consumer<Function<String, Object>>> ADD_CONFIG_GETTER = SetRestrictedField.client();
	public static final SetRestrictedField<MultiblockElementConstructor> MAKE_MULTIBLOCK_ELEMENT = SetRestrictedField.client();
	public static final SetRestrictedField<BlueprintElementConstructor> MAKE_BLUEPRINT_ELEMENT = SetRestrictedField.client();

	public static ManualInstance getManual()
	{
		return IE_MANUAL_INSTANCE.getValue();
	}

	/**
	 * Adds a new source of information for the "&lt;config"-syntax in the IE manual
	 *
	 * @param newGetter returns either the object for the given key or null
	 */
	public static void addConfigGetter(Function<String, Object> newGetter)
	{
		ADD_CONFIG_GETTER.getValue().accept(newGetter);
	}

	public interface MultiblockElementConstructor
	{
		SpecialManualElement create(IMultiblock multiblock);
	}

	public interface BlueprintElementConstructor
	{
		SpecialManualElement create(ItemStack... stacks);
	}
}
