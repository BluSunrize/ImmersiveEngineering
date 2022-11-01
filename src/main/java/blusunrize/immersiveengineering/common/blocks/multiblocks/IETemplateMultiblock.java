/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.multiblocks;

import blusunrize.immersiveengineering.api.IEProperties;
import blusunrize.immersiveengineering.api.multiblocks.ClientMultiblocks.MultiblockManualData;
import blusunrize.immersiveengineering.api.multiblocks.TemplateMultiblock;
import blusunrize.immersiveengineering.api.multiblocks.blocks.MultiblockBlockEntityDummy;
import blusunrize.immersiveengineering.api.multiblocks.blocks.MultiblockBlockEntityMaster;
import blusunrize.immersiveengineering.client.utils.BasicClientProperties;
import blusunrize.immersiveengineering.common.blocks.generic.MultiblockPartBlockEntity;
import blusunrize.immersiveengineering.common.register.IEBlocks;
import blusunrize.immersiveengineering.common.util.IELogger;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate.StructureBlockInfo;
import net.minecraftforge.registries.RegistryObject;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.function.Consumer;

public abstract class IETemplateMultiblock extends TemplateMultiblock
{
	private final RegistryObject<? extends Block> baseState;

	public IETemplateMultiblock(
			ResourceLocation loc, BlockPos masterFromOrigin, BlockPos triggerFromOrigin, BlockPos size,
			IEBlocks.BlockEntry<?> baseState
	)
	{
		this(loc, masterFromOrigin, triggerFromOrigin, size, baseState.getRegObject());
	}

	public IETemplateMultiblock(
			ResourceLocation loc, BlockPos masterFromOrigin, BlockPos triggerFromOrigin, BlockPos size,
			RegistryObject<? extends Block> baseState
	)
	{
		super(loc, masterFromOrigin, triggerFromOrigin, size, ImmutableMap.of());
		this.baseState = baseState;
	}

	@Override
	protected void replaceStructureBlock(StructureBlockInfo info, Level world, BlockPos actualPos, boolean mirrored, Direction clickDirection, Vec3i offsetFromMaster)
	{
		BlockState state = baseState.get().defaultBlockState();
		state = state.setValue(IEProperties.MULTIBLOCKSLAVE, !offsetFromMaster.equals(Vec3i.ZERO));
		if(state.hasProperty(IEProperties.MIRRORED))
			state = state.setValue(IEProperties.MIRRORED, mirrored);
		if(state.hasProperty(IEProperties.FACING_HORIZONTAL))
			state = state.setValue(IEProperties.FACING_HORIZONTAL, transformDirection(clickDirection.getOpposite()));
		world.setBlockAndUpdate(actualPos, state);
		BlockEntity curr = world.getBlockEntity(actualPos);
		if(curr instanceof MultiblockPartBlockEntity<?> tile)
		{
			tile.formed = true;
			tile.offsetToMaster = new BlockPos(offsetFromMaster);
			tile.posInMultiblock = info.pos;
			tile.setChanged();
			world.blockEvent(actualPos, world.getBlockState(actualPos).getBlock(), 255, 0);
		}
		else if(curr instanceof MultiblockBlockEntityDummy<?> dummy)
			dummy.getHelper().setPositionInMB(info.pos);
		else if(!(curr instanceof MultiblockBlockEntityMaster<?>))
			IELogger.logger.error("Expected MB TE at {} during placement", actualPos);
	}

	public Direction transformDirection(Direction original)
	{
		return original;
	}

	public Direction untransformDirection(Direction transformed)
	{
		return transformed;
	}

	public BlockPos multiblockToModelPos(BlockPos posInMultiblock, @Nonnull Level level)
	{
		return posInMultiblock.subtract(masterFromOrigin);
	}

	@Override
	public Vec3i getSize(@Nullable Level world)
	{
		return size;
	}

	@Nonnull
	@Override
	public TemplateData getTemplate(@Nonnull Level world)
	{
		TemplateData result = super.getTemplate(world);
		final Vec3i resultSize = result.template().getSize();
		Preconditions.checkState(
				resultSize.equals(size),
				"Wrong template size for multiblock %s, template size: %s",
				getTemplateLocation(), resultSize
		);
		return result;
	}

	@Override
	protected void prepareBlockForDisassembly(Level world, BlockPos pos)
	{
		BlockEntity be = world.getBlockEntity(pos);
		if(be instanceof MultiblockPartBlockEntity<?> multiblockBE)
			multiblockBE.formed = false;
		else if(be!=null)
			IELogger.logger.error("Expected multiblock TE at {}, got {}", pos, be);
	}

	@Override
	public void initializeClient(Consumer<MultiblockManualData> consumer)
	{
		consumer.accept(new BasicClientProperties(this));
	}

	public ResourceLocation getBlockName()
	{
		return baseState.getId();
	}

	@Override
	public Component getDisplayName()
	{
		return baseState.get().getName();
	}

	@Override
	public Block getBlock()
	{
		return baseState.get();
	}
}
