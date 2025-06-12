/*
 * BluSunrize
 * Copyright (c) 2025
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.util.loot;

import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.advancements.critereon.StatePropertiesPredicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nonnull;
import java.util.Optional;

public record LootBlockStateFromLocationPredicate(Holder<Block> block,
												  Optional<StatePropertiesPredicate> properties) implements LootItemCondition
{
	public static final MapCodec<LootBlockStateFromLocationPredicate> CODEC = RecordCodecBuilder.<LootBlockStateFromLocationPredicate>mapCodec(
					kind -> kind.group(
									BuiltInRegistries.BLOCK.holderByNameCodec().fieldOf("block").forGetter(LootBlockStateFromLocationPredicate::block),
									StatePropertiesPredicate.CODEC.optionalFieldOf("properties").forGetter(LootBlockStateFromLocationPredicate::properties)
							)
							.apply(kind, LootBlockStateFromLocationPredicate::new)
			)
			.validate(LootBlockStateFromLocationPredicate::validate);

	private static DataResult<LootBlockStateFromLocationPredicate> validate(LootBlockStateFromLocationPredicate condition)
	{
		return condition.properties()
				.flatMap(predicate -> predicate.checkState(condition.block().value().getStateDefinition()))
				.map(property -> DataResult.<LootBlockStateFromLocationPredicate>error(() -> "Block "+condition.block()+" has no property"+property))
				.orElse(DataResult.success(condition));
	}

	@Nonnull
	@Override
	public LootItemConditionType getType()
	{
		return IELootFunctions.BLOCKSTATE.value();
	}

	@Override
	public boolean test(LootContext lootContext)
	{
		Vec3 pos = lootContext.getParamOrNull(LootContextParams.ORIGIN);
		if(pos==null)
			return false;
		BlockState blockstate = lootContext.getLevel().getBlockState(BlockPos.containing(pos));
		return blockstate.is(this.block)&&(this.properties.isEmpty()||this.properties.get().matches(blockstate));
	}


	public static class Builder implements LootItemCondition.Builder {
		private final Holder<Block> block;
		private Optional<StatePropertiesPredicate> properties = Optional.empty();

		public Builder(Block block) {
			this.block = block.builtInRegistryHolder();
		}

		public LootBlockStateFromLocationPredicate.Builder setProperties(StatePropertiesPredicate.Builder statePredicateBuilder) {
			this.properties = statePredicateBuilder.build();
			return this;
		}

		public LootItemCondition build() {
			return new LootBlockStateFromLocationPredicate(this.block, this.properties);
		}
	}
}
