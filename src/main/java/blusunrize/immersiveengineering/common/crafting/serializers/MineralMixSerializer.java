/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.crafting.serializers;

import blusunrize.immersiveengineering.api.crafting.IERecipeSerializer;
import blusunrize.immersiveengineering.api.crafting.StackWithChance;
import blusunrize.immersiveengineering.api.excavator.MineralMix;
import blusunrize.immersiveengineering.common.network.PacketUtils;
import blusunrize.immersiveengineering.common.register.IEMultiblockLogic;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.dimension.DimensionType;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class MineralMixSerializer extends IERecipeSerializer<MineralMix>
{
	private static final Codec<MineralMix> CODEC = RecordCodecBuilder.create(
			inst -> inst.group(
					CHANCE_LIST.fieldOf("ores").forGetter(r -> r.outputs),
					CHANCE_LIST.fieldOf("spoils").forGetter(r -> r.outputs),
					Codec.INT.fieldOf("weight").forGetter(r -> r.weight),
					ExtraCodecs.strictOptionalField(Codec.FLOAT, "fail_chance", 0f).forGetter(r -> r.failChance),
					ResourceKey.codec(Registries.DIMENSION_TYPE).listOf().fieldOf("dimensions").forGetter(r -> List.copyOf(r.dimensions)),
					ExtraCodecs.strictOptionalField(BuiltInRegistries.BLOCK.byNameCodec(), "sample_background", Blocks.STONE).forGetter(r -> r.background)
			).apply(inst, (ores, spoils, weight, failChance, dimensions, background) -> {
				double finalTotalChance = ores.stream().mapToDouble(StackWithChance::chance).sum();
				ores = ores.stream().map(stack -> stack.recalculate(finalTotalChance)).toList();
				double finalSpoilChance = spoils.stream().mapToDouble(StackWithChance::chance).sum();
				spoils = spoils.stream().map(stack -> stack.recalculate(finalSpoilChance)).toList();
				return new MineralMix(ores, spoils, weight, failChance, dimensions, background);
			})
	);

	@Override
	public Codec<MineralMix> codec()
	{
		return CODEC;
	}

	@Override
	public ItemStack getIcon()
	{
		return IEMultiblockLogic.CRUSHER.iconStack();
	}

	@Nullable
	@Override
	public MineralMix fromNetwork(FriendlyByteBuf buffer)
	{
		List<StackWithChance> outputs = PacketUtils.readList(buffer, StackWithChance::read);
		List<StackWithChance> spoils = PacketUtils.readList(buffer, StackWithChance::read);
		int weight = buffer.readInt();
		float failChance = buffer.readFloat();
		int count = buffer.readInt();
		List<ResourceKey<DimensionType>> dimensions = new ArrayList<>();
		for(int i = 0; i < count; i++)
			dimensions.add(ResourceKey.create(Registries.DIMENSION_TYPE, buffer.readResourceLocation()));
		Block bg = PacketUtils.readRegistryElement(buffer, BuiltInRegistries.BLOCK);
		return new MineralMix(outputs, spoils, weight, failChance, dimensions, bg);
	}

	@Override
	public void toNetwork(FriendlyByteBuf buffer, MineralMix recipe)
	{
		PacketUtils.writeList(buffer, recipe.outputs, StackWithChance::write);
		PacketUtils.writeList(buffer, recipe.spoils, StackWithChance::write);
		buffer.writeInt(recipe.weight);
		buffer.writeFloat(recipe.failChance);
		buffer.writeInt(recipe.dimensions.size());
		for(ResourceKey<DimensionType> dimension : recipe.dimensions)
			buffer.writeResourceLocation(dimension.location());
		PacketUtils.writeRegistryElement(buffer, BuiltInRegistries.BLOCK, recipe.background);
	}
}
