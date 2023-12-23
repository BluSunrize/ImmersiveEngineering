/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.crafting.serializers;

import blusunrize.immersiveengineering.api.crafting.*;
import blusunrize.immersiveengineering.common.crafting.ArcRecyclingRecipe;
import blusunrize.immersiveengineering.common.network.PacketUtils;
import blusunrize.immersiveengineering.common.register.IEMultiblockLogic;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class ArcFurnaceRecipeSerializer extends IERecipeSerializer<ArcFurnaceRecipe>
{
	public static final Codec<ArcFurnaceRecipe> CODEC = RecordCodecBuilder.create(inst -> inst.group(
			TagOutputList.CODEC.fieldOf("results").forGetter(r -> r.output),
			ExtraCodecs.strictOptionalField(TagOutput.CODEC, "slag", TagOutput.EMPTY).forGetter(r -> r.slag),
			ExtraCodecs.strictOptionalField(CHANCE_LIST, "secondaries", List.of()).forGetter(r -> r.secondaryOutputs),
			Codec.INT.fieldOf("time").forGetter(MultiblockRecipe::getBaseTime),
			Codec.INT.fieldOf("energy").forGetter(MultiblockRecipe::getBaseEnergy),
			IngredientWithSize.CODEC.fieldOf("input").forGetter(r -> r.input),
			IngredientWithSize.CODEC.listOf().fieldOf("additives").forGetter(r -> r.additives)
	).apply(inst, ArcFurnaceRecipe::new));

	@Override
	public Codec<ArcFurnaceRecipe> codec()
	{
		return CODEC;
	}

	@Override
	public ItemStack getIcon()
	{
		return IEMultiblockLogic.ARC_FURNACE.iconStack();
	}

	@Nullable
	@Override
	public ArcFurnaceRecipe fromNetwork(FriendlyByteBuf buffer)
	{
		TagOutputList outputs = new TagOutputList(PacketUtils.readList(buffer, IERecipeSerializer::readLazyStack));
		List<StackWithChance> secondaries = PacketUtils.readList(buffer, StackWithChance::read);
		IngredientWithSize input = IngredientWithSize.read(buffer);
		List<IngredientWithSize> additives = PacketUtils.readList(buffer, IngredientWithSize::read);
		TagOutput slag = readLazyStack(buffer);
		int time = buffer.readInt();
		int energy = buffer.readInt();
		if(!buffer.readBoolean())
			return new ArcFurnaceRecipe(outputs, slag, secondaries, time, energy, input, additives);
		else
		{
			final int numOutputs = buffer.readVarInt();
			List<Pair<TagOutput, Double>> recyclingOutputs = new ArrayList<>(numOutputs);
			for(int i = 0; i < numOutputs; ++i)
				recyclingOutputs.add(Pair.of(readLazyStack(buffer), buffer.readDouble()));
			return new ArcRecyclingRecipe(
					() -> Minecraft.getInstance().getConnection().registryAccess(), recyclingOutputs, input, time, energy
			);
		}
	}

	@Override
	public void toNetwork(FriendlyByteBuf buffer, ArcFurnaceRecipe recipe)
	{
		PacketUtils.writeListReverse(buffer, recipe.output.get(), FriendlyByteBuf::writeItem);
		PacketUtils.writeList(buffer, recipe.secondaryOutputs, StackWithChance::write);
		recipe.input.write(buffer);
		PacketUtils.writeList(buffer, recipe.additives, IngredientWithSize::write);
		buffer.writeItem(recipe.slag.get());
		buffer.writeInt(recipe.getTotalProcessTime());
		buffer.writeInt(recipe.getTotalProcessEnergy());
		buffer.writeBoolean(recipe instanceof ArcRecyclingRecipe);
		if(recipe instanceof ArcRecyclingRecipe recyclingRecipe)
		{
			List<Pair<TagOutput, Double>> outputs = recyclingRecipe.getOutputs();
			buffer.writeVarInt(outputs.size());
			for(Pair<TagOutput, Double> e : outputs)
			{
				buffer.writeItem(e.getFirst().get());
				buffer.writeDouble(e.getSecond());
			}
		}
	}
}
