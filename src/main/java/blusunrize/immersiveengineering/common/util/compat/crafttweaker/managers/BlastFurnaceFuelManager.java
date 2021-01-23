/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */
package blusunrize.immersiveengineering.common.util.compat.crafttweaker.managers;

import blusunrize.immersiveengineering.api.crafting.BlastFurnaceFuel;
import blusunrize.immersiveengineering.common.util.compat.crafttweaker.actions.AbstractActionGenericRemoveRecipe;
import blusunrize.immersiveengineering.common.util.compat.crafttweaker.actions.ActionAddRecipeCustomOutput;
import com.blamejared.crafttweaker.api.CraftTweakerAPI;
import com.blamejared.crafttweaker.api.annotations.ZenRegister;
import com.blamejared.crafttweaker.api.item.IIngredient;
import com.blamejared.crafttweaker.api.item.IItemStack;
import com.blamejared.crafttweaker.api.managers.IRecipeManager;
import com.blamejared.crafttweaker_annotations.annotations.Document;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.util.ResourceLocation;
import org.openzen.zencode.java.ZenCodeType;
import org.openzen.zencode.java.ZenCodeType.Method;

/**
 * Allows you to add or remove Blast Furnace fuel items.
 * <p>
 *
 * @docParam this <recipetype:immersiveengineering:blast_furnace_fuel>
 */
@ZenRegister
@Document("mods/immersiveengineering/BlastFurnaceFuel")
@ZenCodeType.Name("mods.immersiveengineering.BlastFurnaceFuel")
public class BlastFurnaceFuelManager implements IRecipeManager
{

	@Override
	public IRecipeType<BlastFurnaceFuel> getRecipeType()
	{
		return BlastFurnaceFuel.TYPE;
	}

	/**
	 * Adds a fuel to the Blast Furnace
	 *
	 * @param recipePath The recipe name, without the resource location
	 * @param fuel       The fuel to be added
	 * @param burnTime   The fuel's burntime
	 * @docParam recipePath "the_sungods_sword_can_burn"
	 * @docParam fuel <item:minecraft:golden_sword>.withTag({RepairCost: 0 as int, Damage: 0 as int, display: {Name: "{\"text\":\"Sword of the Sungod\"}" as string}})
	 * @docParam burnTime 100000
	 */
	@ZenCodeType.Method
	public void addFuel(String recipePath, IIngredient fuel, int burnTime)
	{
		final ResourceLocation resourceLocation = new ResourceLocation("crafttweaker", recipePath);
		final BlastFurnaceFuel recipe = new BlastFurnaceFuel(resourceLocation, fuel.asVanillaIngredient(), burnTime);
		CraftTweakerAPI.apply(new ActionAddRecipeCustomOutput(this, recipe, fuel));
	}

	@Override
	public void removeRecipe(IItemStack output)
	{
		removeFuel(output);
	}

	/**
	 * Removes the fuel value for this item
	 *
	 * @param fuel The fuel to remove
	 * @docParam fuel <item:minecraft:charcoal>
	 */
	@Method
	public void removeFuel(IItemStack fuel)
	{
		CraftTweakerAPI.apply(new AbstractActionGenericRemoveRecipe<BlastFurnaceFuel>(this, fuel)
		{
			@Override
			public boolean shouldRemove(BlastFurnaceFuel recipe)
			{
				return recipe.input.test(fuel.getInternal());
			}
		});
	}
}
