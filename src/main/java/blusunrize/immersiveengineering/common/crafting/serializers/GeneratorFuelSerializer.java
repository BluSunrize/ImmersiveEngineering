/*
 * BluSunrize
 * Copyright (c) 2021
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.crafting.serializers;

import blusunrize.immersiveengineering.api.crafting.IERecipeSerializer;
import blusunrize.immersiveengineering.api.energy.GeneratorFuel;
import blusunrize.immersiveengineering.common.network.PacketUtils;
import blusunrize.immersiveengineering.common.register.IEMultiblockLogic;
import com.google.gson.JsonObject;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.common.crafting.conditions.ICondition.IContext;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

import static blusunrize.immersiveengineering.api.crafting.builders.GeneratorFuelBuilder.BURN_TIME_KEY;
import static blusunrize.immersiveengineering.api.crafting.builders.GeneratorFuelBuilder.FLUID_TAG_KEY;

public class GeneratorFuelSerializer extends IERecipeSerializer<GeneratorFuel>
{
	@Override
	public ItemStack getIcon()
	{
		return IEMultiblockLogic.DIESEL_GENERATOR.iconStack();
	}

	@Override
	public GeneratorFuel readFromJson(ResourceLocation recipeId, JsonObject json, IContext context)
	{
		ResourceLocation tagName = new ResourceLocation(json.get(FLUID_TAG_KEY).getAsString());
		TagKey<Fluid> tag = TagKey.create(Registries.FLUID, tagName);
		int amount = json.get(BURN_TIME_KEY).getAsInt();
		return new GeneratorFuel(recipeId, tag, amount);
	}

	@Nullable
	@Override
	public GeneratorFuel fromNetwork(@Nonnull ResourceLocation recipeId, @Nonnull FriendlyByteBuf buffer)
	{
		List<Fluid> fluids = PacketUtils.readList(buffer, buf -> buf.readRegistryIdUnsafe(ForgeRegistries.FLUIDS));
		int burnTime = buffer.readInt();
		return new GeneratorFuel(recipeId, fluids, burnTime);
	}

	@Override
	public void toNetwork(@Nonnull FriendlyByteBuf buffer, @Nonnull GeneratorFuel recipe)
	{
		PacketUtils.writeList(
				buffer, recipe.getFluids(), (f, buf) -> buf.writeRegistryIdUnsafe(ForgeRegistries.FLUIDS, f)
		);
		buffer.writeInt(recipe.getBurnTime());
	}
}
