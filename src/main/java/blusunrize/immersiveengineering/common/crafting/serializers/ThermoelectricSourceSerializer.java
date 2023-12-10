/*
 * BluSunrize
 * Copyright (c) 2021
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.crafting.serializers;

import blusunrize.immersiveengineering.api.crafting.IERecipeSerializer;
import blusunrize.immersiveengineering.api.energy.ThermoelectricSource;
import blusunrize.immersiveengineering.common.network.PacketUtils;
import blusunrize.immersiveengineering.common.register.IEBlocks.MetalDevices;
import com.google.common.base.Preconditions;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.tags.TagKey;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class ThermoelectricSourceSerializer extends IERecipeSerializer<ThermoelectricSource>
{
	public static final Codec<ThermoelectricSource> CODEC = RecordCodecBuilder.create(inst -> inst.group(
			Codec.INT.fieldOf("tempKelvin").forGetter(r -> r.temperature),
			ExtraCodecs.strictOptionalField(TagKey.codec(Registries.BLOCK), "blockTag").forGetter(r -> r.blocks.leftOptional()),
			maybeListOrSingle(BuiltInRegistries.BLOCK.byNameCodec(), "singleBlock").forGetter(r -> r.blocks.rightOptional())
	).apply(inst, (temperature, tag, fixedBlocks) -> {
		Preconditions.checkState(tag.isPresent()!=fixedBlocks.isPresent());
		if(tag.isPresent())
			return new ThermoelectricSource(tag.get(), temperature);
		else
			return new ThermoelectricSource(fixedBlocks.get(), temperature);
	}));

	@Override
	public Codec<ThermoelectricSource> codec()
	{
		return CODEC;
	}

	@Override
	public ItemStack getIcon()
	{
		return new ItemStack(MetalDevices.THERMOELECTRIC_GEN);
	}

	@Nullable
	@Override
	public ThermoelectricSource fromNetwork(@Nonnull FriendlyByteBuf buffer)
	{
		List<Block> blocks = PacketUtils.readList(
				buffer, buf -> PacketUtils.readRegistryElement(buffer, BuiltInRegistries.BLOCK)
		);
		int temperature = buffer.readInt();
		return new ThermoelectricSource(blocks, temperature);
	}

	@Override
	public void toNetwork(@Nonnull FriendlyByteBuf buffer, @Nonnull ThermoelectricSource recipe)
	{
		PacketUtils.writeList(
				buffer,
				recipe.getMatchingBlocks(),
				(b, buf) -> PacketUtils.writeRegistryElement(buf, BuiltInRegistries.BLOCK, b)
		);
		buffer.writeInt(recipe.getTemperature());
	}
}
