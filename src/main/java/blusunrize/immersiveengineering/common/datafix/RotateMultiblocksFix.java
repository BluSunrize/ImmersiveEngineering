/*
 * BluSunrize
 * Copyright (c) 2023
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.datafix;

import blusunrize.immersiveengineering.api.IEProperties;
import blusunrize.immersiveengineering.common.register.IEMultiblockLogic;
import com.google.common.base.Preconditions;
import com.mojang.datafixers.*;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.serialization.Dynamic;
import net.minecraft.core.Direction;
import net.minecraft.util.datafix.fixes.References;

import java.util.Optional;
import java.util.Set;
import java.util.function.UnaryOperator;

public class RotateMultiblocksFix extends DataFix
{
	private static final String PROPERTIES_KEY = "Properties";
	private static final String FACING_KEY = IEProperties.FACING_HORIZONTAL.getName();
	private static final Set<String> STONE_MULTIBLOCKS = Set.of(
			IEMultiblockLogic.ALLOY_SMELTER.block().getId().toString(),
			IEMultiblockLogic.ADV_BLAST_FURNACE.block().getId().toString(),
			IEMultiblockLogic.BLAST_FURNACE.block().getId().toString(),
			IEMultiblockLogic.COKE_OVEN.block().getId().toString()
	);
	private static final String METAL_PRESS = IEMultiblockLogic.METAL_PRESS.block().getId().toString();

	public static void registerFix(Schema newSchema, DataFixerBuilder builder)
	{
		Preconditions.checkState(newSchema.getVersionKey()==DataFixUtils.makeKey(3214, 0));
		builder.addFixer(new RotateMultiblocksFix(newSchema));
	}

	public RotateMultiblocksFix(Schema outputSchema)
	{
		super(outputSchema, false);
	}

	@Override
	protected TypeRewriteRule makeRule()
	{
		final Type<?> inputBlockStateType = this.getInputSchema().getType(References.BLOCK_STATE);
		return this.fixTypeEverywhereTyped("Rotate IE multiblocks", inputBlockStateType, this::fixBlockstate);
	}

	private Typed<?> fixBlockstate(Typed<?> typed)
	{
		return typed.update(DSL.remainderFinder(), blockstateDynamic -> {
			Optional<String> maybeName = blockstateDynamic.get("Name").asString().result();
			if(maybeName.isEmpty())
				return blockstateDynamic;
			if(STONE_MULTIBLOCKS.contains(maybeName.get()))
				return fixDirection(blockstateDynamic, Direction::getOpposite);
			else if(METAL_PRESS.equals(maybeName.get()))
				return fixDirection(blockstateDynamic, Direction::getCounterClockWise);
			else
				return blockstateDynamic;
		});
	}

	private Dynamic<?> fixDirection(Dynamic<?> blockState, UnaryOperator<Direction> directionFix)
	{
		final Optional<? extends Dynamic<?>> maybeProperties = blockState.get(PROPERTIES_KEY).get().result();
		if(maybeProperties.isEmpty())
			return blockState;
		final Dynamic<?> properties = maybeProperties.get();
		final Optional<String> maybeFacing = properties.get(FACING_KEY).asString().result();
		if(maybeFacing.isEmpty())
			return blockState;
		final Direction facing = Direction.byName(maybeFacing.get());
		if(facing==null)
			return blockState;
		final String newFacingName = directionFix.apply(facing).getSerializedName();
		final Dynamic<?> newProperties = properties.set(FACING_KEY, properties.createString(newFacingName));
		return blockState.set(PROPERTIES_KEY, newProperties);
	}
}
