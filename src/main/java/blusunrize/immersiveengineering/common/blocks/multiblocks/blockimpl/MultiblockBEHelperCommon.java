/*
 * BluSunrize
 * Copyright (c) 2023
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.multiblocks.blockimpl;

import blusunrize.immersiveengineering.api.multiblocks.blocks.MultiblockRegistration;
import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IMultiblockBEHelper;
import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IMultiblockBEHelperMaster;
import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IMultiblockContext;
import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IMultiblockLevel;
import blusunrize.immersiveengineering.api.multiblocks.blocks.logic.IMultiblockLogic;
import blusunrize.immersiveengineering.api.multiblocks.blocks.logic.IMultiblockState;
import blusunrize.immersiveengineering.api.multiblocks.blocks.util.CapabilityPosition;
import blusunrize.immersiveengineering.api.multiblocks.blocks.util.MultiblockOrientation;
import blusunrize.immersiveengineering.api.multiblocks.blocks.util.RelativeBlockFace;
import blusunrize.immersiveengineering.api.multiblocks.blocks.util.ShapeType;
import blusunrize.immersiveengineering.common.blocks.multiblocks.logic.interfaces.MBMemorizeStructure;
import blusunrize.immersiveengineering.common.util.Utils;
import com.google.common.base.Preconditions;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate.StructureBlockInfo;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nullable;
import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiFunction;

public abstract class MultiblockBEHelperCommon<State extends IMultiblockState> implements IMultiblockBEHelper<State>
{
	protected final BlockEntity be;
	private boolean beingDisassembled = false;
	private final Map<RelativeBlockFace, Integer> cachedRedstoneValues = new EnumMap<>(RelativeBlockFace.class);
	protected final MultiblockRegistration<State> multiblock;
	protected final MultiblockOrientation orientation;
	private IMultiblockBEHelperMaster<State> masterHelperDuringDisassembly;
	private final EnumMap<ShapeType, CachedValue<BlockPos, MultiblockOrientation, VoxelShape>> cachedShape;

	protected MultiblockBEHelperCommon(BlockEntity be, MultiblockRegistration<State> multiblock, BlockState state)
	{
		this.be = be;
		this.multiblock = multiblock;
		this.orientation = new MultiblockOrientation(state, multiblock.mirrorable());
		this.cachedShape = new EnumMap<>(ShapeType.class);
		for(final ShapeType type : ShapeType.values())
			this.cachedShape.put(type, new CachedValue<>((pos, orientation) -> {
				final VoxelShape relative = multiblock.logic().shapeGetter(type).apply(pos);
				return orientation.transformRelativeShape(relative);
			}));
	}

	@Override
	public VoxelShape getShape(@Nullable CollisionContext ctx, ShapeType type)
	{
		final BlockPos posInMB = getPositionInMB();
		final IMultiblockLogic<State> logic = multiblock.logic();
		final VoxelShape absoluteShape = cachedShape.get(type).get(posInMB, orientation);
		if(ctx!=null&&multiblock.postProcessesShape())
		{
			final IMultiblockContext<State> multiblockCtx = getContext();
			if(multiblockCtx!=null)
				return logic.postProcessAbsoluteShape(multiblockCtx, absoluteShape, ctx, posInMB, type);
		}
		return absoluteShape;
	}

	@Override
	public CapabilityPosition getCapabilityPosition(@Nullable Direction side)
	{
		final RelativeBlockFace relativeSide = RelativeBlockFace.from(orientation, side);
		return new CapabilityPosition(getPositionInMB(), relativeSide);
	}

	@Override
	public ItemInteractionResult click(Player player, InteractionHand hand, BlockHitResult hit)
	{
		final MultiblockBEHelperMaster<State> helper = getMasterHelper();
		if(helper==null)
			return ItemInteractionResult.FAIL;
		final MultiblockContext<State> ctx = helper.getContext();
		for(final ComponentInstance<?> component : helper.getComponentInstances())
		{
			final ItemInteractionResult componentResult = component.click(
					getPositionInMB(), player, hand, hit, player.level().isClientSide
			);
			if(componentResult!=ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION)
				return componentResult;
		}
		return multiblock.logic().click(ctx, getPositionInMB(), player, hand, hit, player.level().isClientSide);
	}

	@Override
	public void disassemble()
	{
		if(beingDisassembled)
			return;
		this.masterHelperDuringDisassembly = getMasterHelperWithChunkloads();
		if(masterHelperDuringDisassembly==null)
			// Master BE went missing, can't do anything
			return;
		final IMultiblockContext<State> ctx = masterHelperDuringDisassembly.getContext();
		final IMultiblockLevel levelWrapper = ctx.getLevel();
		final BlockPos absolutePos = levelWrapper.toAbsolute(getPositionInMB());
		final Level levelRaw = levelWrapper.getRawLevel();
		getMultiblock().disassemble().disassemble(
				levelRaw, levelWrapper.getAbsoluteOrigin(), levelWrapper.getOrientation()
		);
		levelRaw.removeBlock(absolutePos, false);
	}

	@Override
	public void onEntityCollided(Entity collided)
	{
		final MultiblockBEHelperMaster<State> helper = getMasterHelper();
		if(helper==null)
			return;
		getMultiblock().logic().onEntityCollision(helper.getContext(), getPositionInMB(), collided);
		for(final ComponentInstance<?> component : helper.getComponentInstances())
			component.onEntityCollision(getPositionInMB(), collided);
	}

	@Nullable
	protected abstract IMultiblockBEHelperMaster<State> getMasterHelperWithChunkloads();

	@Nullable
	protected abstract MultiblockBEHelperMaster<State> getMasterHelper();

	@Override
	public void markDisassembling()
	{
		beingDisassembled = true;
	}

	@Override
	public int getComparatorValue()
	{
		if(!multiblock.hasComparatorOutput())
			return 0;
		// TODO cache locally?
		final MultiblockBEHelperMaster<State> masterHelper = getMasterHelper();
		if(masterHelper==null)
			return 0;
		return masterHelper.getCurrentComparatorOutputs().getInt(getPositionInMB());
	}

	@Override
	public void onNeighborChanged(BlockPos fromPos)
	{
		BlockPos delta = fromPos.subtract(be.getBlockPos());
		Direction sideAbsolute = Direction.getNearest(delta.getX(), delta.getY(), delta.getZ());
		Preconditions.checkNotNull(sideAbsolute);
		updateRedstoneValue(sideAbsolute, fromPos);
	}

	@Override
	public int getRedstoneInput(RelativeBlockFace side)
	{
		if(cachedRedstoneValues.containsKey(side))
			return cachedRedstoneValues.get(side);
		else
		{
			final Direction absoluteFace = side.forFront(orientation);
			final BlockPos neighbor = be.getBlockPos().relative(absoluteFace);
			return updateRedstoneValue(absoluteFace, neighbor);
		}
	}

	@Override
	public BlockState getOriginalBlock(Level level)
	{
		IMultiblockLogic<State> logic = getMultiblock().logic();
		State state = getState();
		if(state==null && this.masterHelperDuringDisassembly!=null)
			state = this.masterHelperDuringDisassembly.getState();
		if(logic instanceof MBMemorizeStructure<State> memo && state!=null)
		{
			BlockState memorized = memo.getMemorizedBlockState(state, getPositionInMB());
			if(memorized!=null)
				return memorized;
		}
		for(StructureBlockInfo block : multiblock.getStructure().apply(level))
			if(block.pos().equals(getPositionInMB()))
				return block.state();
		return Blocks.AIR.defaultBlockState();
	}

	@Override
	public ItemStack getPickBlock()
	{
		final Level level = be.getLevel();
		if(level==null)
			return ItemStack.EMPTY;
		return Utils.getPickBlock(getOriginalBlock(level));
	}

	@Nullable
	public IMultiblockBEHelperMaster<State> getMasterHelperDuringDisassembly()
	{
		if(masterHelperDuringDisassembly!=null)
			return masterHelperDuringDisassembly;
		else
			return getMasterHelperWithChunkloads();
	}

	private int updateRedstoneValue(Direction sideAbsolute, BlockPos neighborPos)
	{
		final Level level = Objects.requireNonNull(be.getLevel());
		final int rsStrength = level.getSignal(neighborPos, sideAbsolute);
		final RelativeBlockFace sideRelative = RelativeBlockFace.from(orientation, sideAbsolute);
		cachedRedstoneValues.put(sideRelative, rsStrength);
		return rsStrength;
	}

	protected static class CachedValue<K1, K2, T>
	{
		private final BiFunction<K1, K2, T> computeValue;
		@Nullable
		private T value;
		private K1 key1;
		private K2 key2;

		protected CachedValue(BiFunction<K1, K2, T> computeValue)
		{
			this.computeValue = computeValue;
		}

		public T get(K1 first, K2 second)
		{
			if(value==null||!Objects.equals(first, key1)||!Objects.equals(second, key2))
			{
				value = computeValue.apply(first, second);
				this.key1 = first;
				this.key2 = second;
			}
			return value;
		}
	}
}
