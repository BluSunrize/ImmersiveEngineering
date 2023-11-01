/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.api.crafting;

import blusunrize.immersiveengineering.api.IEApi;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.NonNullList;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipeCodecs;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.neoforged.neoforge.common.conditions.ConditionalOps;
import net.neoforged.neoforge.common.util.Lazy;
import net.neoforged.neoforge.fluids.FluidStack;

import java.util.List;
import java.util.function.Function;

// TODO codecs
public abstract class IERecipeSerializer<R extends Recipe<?>> implements RecipeSerializer<R>
{
	public static final Codec<Lazy<ItemStack>> LAZY_OUTPUT_CODEC = Codec.either(
			CraftingRecipeCodecs.ITEMSTACK_OBJECT_CODEC, IngredientWithSize.CODEC
	).xmap(
			e -> Lazy.of(() -> e.map(
					Function.identity(),
					outgredient -> IEApi.getPreferredStackbyMod(outgredient.getMatchingStacks())
			)),
			// TODO be a bit more careful about resolving the Lazy here?
			i -> Either.left(i.get())
	);
	public static final Codec<List<Lazy<ItemStack>>> OUTER_LAZY_OUTPUTS_CODEC = ConditionalOps.decodeListWithElementConditions(
			LAZY_OUTPUT_CODEC, "conditions"
	);
	public static final Codec<Lazy<NonNullList<ItemStack>>> LAZY_OUTPUTS_CODEC = OUTER_LAZY_OUTPUTS_CODEC.xmap(
			IERecipeSerializer::combineLazies,
			nnl -> nnl.get().stream().map(i -> Lazy.of(() -> i)).toList()
	);
	public static final Codec<StackWithChance> CHANCE_STACK_CODEC = RecordCodecBuilder.create(
			inst -> inst.group(
					LAZY_OUTPUT_CODEC.fieldOf("stack").forGetter(StackWithChance::stack),
					Codec.FLOAT.fieldOf("chance").forGetter(StackWithChance::chance)
			).apply(inst, StackWithChance::new)
	);
	public static final Codec<List<StackWithChance>> CHANCE_LIST = ConditionalOps.decodeListWithElementConditions(
			CHANCE_STACK_CODEC, "conditions"
	);

	public static MapCodec<Lazy<ItemStack>> optionalItemOutput(String name)
	{
		return LAZY_OUTPUT_CODEC.optionalFieldOf(name, IESerializableRecipe.LAZY_EMPTY);
	}

	public static MapCodec<FluidStack> optionalFluidOutput(String name)
	{
		return FluidStack.CODEC.optionalFieldOf(name, FluidStack.EMPTY);
	}

	public static Lazy<NonNullList<ItemStack>> combineLazies(List<Lazy<ItemStack>> stacks)
	{
		return Lazy.of(() -> {
			NonNullList<ItemStack> list = NonNullList.create();
			for(Lazy<ItemStack> itemStackLazy : stacks)
				if(!itemStackLazy.get().isEmpty())
					list.add(itemStackLazy.get());
			return list;
		});
	}

	public abstract ItemStack getIcon();

	protected static Lazy<ItemStack> readLazyStack(FriendlyByteBuf buf)
	{
		ItemStack stack = buf.readItem();
		return Lazy.of(() -> stack);
	}

	protected static void writeLazyStack(FriendlyByteBuf buf, Lazy<ItemStack> stack)
	{
		buf.writeItem(stack.get());
	}
}
