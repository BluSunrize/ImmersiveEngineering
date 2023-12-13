/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.crafting.serializers;

import blusunrize.immersiveengineering.api.crafting.BlueprintCraftingRecipe;
import blusunrize.immersiveengineering.api.crafting.IERecipeSerializer;
import blusunrize.immersiveengineering.api.crafting.IngredientWithSize;
import blusunrize.immersiveengineering.api.crafting.TagOutput;
import blusunrize.immersiveengineering.common.register.IEBlocks.WoodenDevices;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class BlueprintCraftingRecipeSerializer extends IERecipeSerializer<BlueprintCraftingRecipe>
{
	public static final Codec<BlueprintCraftingRecipe> CODEC = RecordCodecBuilder.create(inst -> inst.group(
			Codec.STRING.fieldOf("category").forGetter(r -> r.blueprintCategory),
			TagOutput.CODEC.fieldOf("result").forGetter(r -> r.output),
			IngredientWithSize.CODEC.listOf().fieldOf("inputs").forGetter(r -> r.inputs)
	).apply(inst, BlueprintCraftingRecipe::new));

	@Override
	public Codec<BlueprintCraftingRecipe> codec()
	{
		return CODEC;
	}

	@Override
	public ItemStack getIcon()
	{
		return new ItemStack(WoodenDevices.WORKBENCH);
	}

	@Nullable
	@Override
	public BlueprintCraftingRecipe fromNetwork(FriendlyByteBuf buffer)
	{
		String category = buffer.readUtf();
		TagOutput output = readLazyStack(buffer);
		int inputCount = buffer.readInt();
		List<IngredientWithSize> ingredients = new ArrayList<>();
		for(int i = 0; i < inputCount; i++)
			ingredients.add(IngredientWithSize.read(buffer));
		return new BlueprintCraftingRecipe(category, output, ingredients);
	}

	@Override
	public void toNetwork(FriendlyByteBuf buffer, BlueprintCraftingRecipe recipe)
	{
		buffer.writeUtf(recipe.blueprintCategory);
		writeLazyStack(buffer, recipe.output);
		buffer.writeInt(recipe.inputs.size());
		for(IngredientWithSize ingredient : recipe.inputs)
			ingredient.write(buffer);
	}
}
