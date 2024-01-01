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
import blusunrize.immersiveengineering.api.multiblocks.blocks.MultiblockRegistration;
import blusunrize.immersiveengineering.api.multiblocks.blocks.logic.IMultiblockBE;
import blusunrize.immersiveengineering.api.multiblocks.blocks.registry.MultiblockBlockEntityDummy;
import blusunrize.immersiveengineering.api.multiblocks.blocks.registry.MultiblockBlockEntityMaster;
import blusunrize.immersiveengineering.client.utils.BasicClientProperties;
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
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate.StructureBlockInfo;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.function.Consumer;

public abstract class IETemplateMultiblock extends TemplateMultiblock
{
	private final MultiblockRegistration<?> logic;

	public IETemplateMultiblock(
			ResourceLocation loc, BlockPos masterFromOrigin, BlockPos triggerFromOrigin, BlockPos size,
			MultiblockRegistration<?> logic
	)
	{
		super(loc, masterFromOrigin, triggerFromOrigin, size, ImmutableMap.of());
		this.logic = logic;
	}

	@Override
	protected void replaceStructureBlock(StructureBlockInfo info, Level world, BlockPos actualPos, boolean mirrored, Direction clickDirection, Vec3i offsetFromMaster)
	{
		BlockState newState = logic.block().get().defaultBlockState();
		newState = newState.setValue(IEProperties.MULTIBLOCKSLAVE, !offsetFromMaster.equals(Vec3i.ZERO));
		if(newState.hasProperty(IEProperties.MIRRORED))
			newState = newState.setValue(IEProperties.MIRRORED, mirrored);
		if(newState.hasProperty(IEProperties.FACING_HORIZONTAL))
			newState = newState.setValue(IEProperties.FACING_HORIZONTAL, clickDirection.getOpposite());
		final BlockState oldState = world.getBlockState(actualPos);
		world.setBlock(actualPos, newState,0);
		BlockEntity curr = world.getBlockEntity(actualPos);
		if(curr instanceof MultiblockBlockEntityDummy<?> dummy)
			dummy.getHelper().setPositionInMB(info.pos());
		else if(!(curr instanceof MultiblockBlockEntityMaster<?>))
			IELogger.logger.error("Expected MB TE at {} during placement", actualPos);
		final LevelChunk chunk = world.getChunkAt(actualPos);
		world.markAndNotifyBlock(actualPos, chunk, oldState, newState, Block.UPDATE_ALL, 512);
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
		if(be instanceof IMultiblockBE<?> multiblockBE)
			multiblockBE.getHelper().markDisassembling();
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
		return logic.id();
	}

	@Override
	public Component getDisplayName()
	{
		return logic.block().get().getName();
	}

	@Override
	public Block getBlock()
	{
		return logic.block().get();
	}
}
