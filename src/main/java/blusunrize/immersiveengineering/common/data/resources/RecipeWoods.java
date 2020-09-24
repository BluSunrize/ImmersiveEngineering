/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 *
 */

package blusunrize.immersiveengineering.common.data.resources;

import com.google.common.base.Preconditions;
import net.minecraft.item.Items;
import net.minecraft.util.IItemProvider;

/**
 * An Enum of  non-metal ores from Vanilla, IE and other mods. Used for generating Crusher recipes
 */
public enum RecipeWoods
{
	// Vanilla Logs
	OAK_LOGS("oak", true, Items.OAK_PLANKS, Items.OAK_LOG, Items.STRIPPED_OAK_LOG,
			Items.OAK_WOOD, Items.OAK_DOOR, Items.OAK_STAIRS),
	SPRUCE_LOGS("spruce", true, Items.SPRUCE_PLANKS, Items.SPRUCE_LOG, Items.STRIPPED_SPRUCE_LOG,
			Items.SPRUCE_WOOD, Items.SPRUCE_DOOR, Items.SPRUCE_STAIRS),
	BIRCH_LOGS("birch", true, Items.BIRCH_PLANKS, Items.BIRCH_LOG, Items.STRIPPED_BIRCH_LOG,
			Items.BIRCH_WOOD, Items.BIRCH_DOOR, Items.BIRCH_STAIRS),
	JUNGLE_LOGS("jungle", true, Items.JUNGLE_PLANKS, Items.JUNGLE_LOG, Items.STRIPPED_JUNGLE_LOG,
			Items.JUNGLE_WOOD, Items.JUNGLE_DOOR, Items.JUNGLE_STAIRS),
	ACACIA_LOGS("acacia", true, Items.ACACIA_PLANKS, Items.ACACIA_LOG, Items.STRIPPED_ACACIA_LOG,
			Items.ACACIA_WOOD, Items.ACACIA_DOOR, Items.ACACIA_STAIRS),
	DARK_OAK_LOGS("dark_oak", true, Items.DARK_OAK_PLANKS, Items.DARK_OAK_LOG, Items.STRIPPED_DARK_OAK_LOG,
			Items.DARK_OAK_WOOD, Items.DARK_OAK_DOOR, Items.DARK_OAK_STAIRS);

	private final String name;
	private final boolean produceSawdust;
	private final IItemProvider plank;
	private final IItemProvider log;
	private final IItemProvider stripped;
	private final IItemProvider wood;
	private final IItemProvider door;
	private final IItemProvider stairs;

	RecipeWoods(String name, boolean produceSawdust, IItemProvider plank, IItemProvider log, IItemProvider stripped,
				IItemProvider wood, IItemProvider door, IItemProvider stairs)
	{
		Preconditions.checkNotNull(name);
		Preconditions.checkNotNull(plank);
		this.name = name;
		this.produceSawdust = produceSawdust;
		this.plank = plank;
		this.log = log;
		this.stripped = stripped;
		this.wood = wood;
		this.door = door;
		this.stairs = stairs;
	}

	public String getName()
	{
		return name;
	}

	public boolean produceSawdust()
	{
		return produceSawdust;
	}

	public IItemProvider getLog()
	{
		return log;
	}

	public IItemProvider getStripped()
	{
		return stripped;
	}

	public IItemProvider getPlank()
	{
		return plank;
	}

	public IItemProvider getWood()
	{
		return wood;
	}

	public IItemProvider getDoor()
	{
		return door;
	}

	public IItemProvider getStairs()
	{
		return stairs;
	}
}
