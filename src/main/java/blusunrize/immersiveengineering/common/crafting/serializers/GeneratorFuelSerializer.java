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
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;


public class GeneratorFuelSerializer extends IERecipeSerializer<GeneratorFuel>
{
	public static final Codec<GeneratorFuel> CODEC = RecordCodecBuilder.create(inst -> inst.group(
			Codec.mapEither(
					TagKey.codec(Registries.FLUID).fieldOf("fluidTag"),
					BuiltInRegistries.FLUID.byNameCodec().listOf().fieldOf("fluidList")
			).forGetter(f -> f.getFluidsRaw().map(Either::left, Either::right)),
			Codec.INT.fieldOf("burnTime").forGetter(GeneratorFuel::getBurnTime)
	).apply(inst, GeneratorFuel::new));

	@Override
	public Codec<GeneratorFuel> codec()
	{
		return CODEC;
	}

	@Override
	public ItemStack getIcon()
	{
		return IEMultiblockLogic.DIESEL_GENERATOR.iconStack();
	}

	@Nullable
	@Override
	public GeneratorFuel fromNetwork(@Nonnull FriendlyByteBuf buffer)
	{
		List<Fluid> fluids = PacketUtils.readList(
				buffer, buf -> PacketUtils.readRegistryElement(buf, BuiltInRegistries.FLUID)
		);
		int burnTime = buffer.readInt();
		return new GeneratorFuel(fluids, burnTime);
	}

	@Override
	public void toNetwork(@Nonnull FriendlyByteBuf buffer, @Nonnull GeneratorFuel recipe)
	{
		PacketUtils.writeList(
				buffer,
				recipe.getFluids(),
				(f, buf) -> PacketUtils.writeRegistryElement(buf, BuiltInRegistries.FLUID, f)
		);
		buffer.writeInt(recipe.getBurnTime());
	}
}
