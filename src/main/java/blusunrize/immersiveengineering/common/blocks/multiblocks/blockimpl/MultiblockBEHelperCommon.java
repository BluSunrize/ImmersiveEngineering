package blusunrize.immersiveengineering.common.blocks.multiblocks.blockimpl;

import blusunrize.immersiveengineering.api.multiblocks.blocks.IMultiblockBEHelper;
import blusunrize.immersiveengineering.api.multiblocks.blocks.IMultiblockContext;
import blusunrize.immersiveengineering.api.multiblocks.blocks.IMultiblockLogic.IMultiblockState;
import blusunrize.immersiveengineering.api.multiblocks.blocks.MultiblockRegistration;
import blusunrize.immersiveengineering.api.multiblocks.blocks.util.CapabilityPosition;
import blusunrize.immersiveengineering.api.multiblocks.blocks.util.MultiblockOrientation;
import blusunrize.immersiveengineering.api.multiblocks.blocks.util.RelativeBlockFace;
import com.google.common.base.Preconditions;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nullable;
import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;

public abstract class MultiblockBEHelperCommon<State extends IMultiblockState> implements IMultiblockBEHelper<State>
{
	protected final BlockEntity be;
	private boolean beingDisassembled = false;
	private final Map<RelativeBlockFace, Integer> cachedRedstoneValues = new EnumMap<>(RelativeBlockFace.class);
	protected final MultiblockRegistration<State> multiblock;
	protected final MultiblockOrientation orientation;

	protected MultiblockBEHelperCommon(BlockEntity be, MultiblockRegistration<State> multiblock, BlockState state)
	{
		this.be = be;
		this.multiblock = multiblock;
		this.orientation = new MultiblockOrientation(state, multiblock.mirrorable());
	}

	@Override
	public VoxelShape getShape(CollisionContext ctx)
	{
		// TODO cache!
		final var logic = multiblock.logic();
		final var relativeShape = logic.shapeGetter().apply(getPositionInMB());
		final var absoluteShape = orientation.transformRelativeShape(relativeShape);
		final var multiblockCtx = getContext();
		if(multiblockCtx!=null)
			return logic.postProcessAbsoluteShape(multiblockCtx, absoluteShape, ctx, getPositionInMB());
		else
			return absoluteShape;
	}

	@Override
	public <T> LazyOptional<T> getCapability(Capability<T> cap, @Nullable Direction side)
	{
		final var ctx = getContext();
		if(ctx==null)
			return LazyOptional.empty();
		final var relativeSide = RelativeBlockFace.from(orientation, side);
		return multiblock.logic().getCapability(ctx, new CapabilityPosition(getPositionInMB(), relativeSide), cap);
	}

	@Override
	public InteractionResult click(Player player, InteractionHand hand, BlockHitResult hit)
	{
		final var ctx = getContext();
		if(ctx==null)
			return InteractionResult.FAIL;
		return multiblock.logic().click(ctx, getPositionInMB(), player, hand, hit, player.level.isClientSide);
	}

	@Override
	public void disassemble()
	{
		if(beingDisassembled)
			return;
		final var ctx = getContextWithChunkloads();
		if(ctx==null)
			// Master BE went missing, can't do anything
			return;
		final var levelWrapper = ctx.getLevel();
		final var absolutePos = levelWrapper.toAbsolute(getPositionInMB());
		final var levelRaw = levelWrapper.getRawLevel();
		getMultiblock().disassemble().disassemble(
				levelRaw, levelWrapper.getAbsoluteOrigin(), levelWrapper.getOrientation()
		);
		levelRaw.removeBlock(absolutePos, false);
	}

	@Override
	public void onEntityCollided(Entity collided)
	{
		getMultiblock().logic().onEntityCollision(getContext(), getPositionInMB(), collided);
	}

	@Nullable
	protected abstract IMultiblockContext<State> getContextWithChunkloads();

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
		final var masterHelper = getMasterHelper();
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
			final var absoluteFace = side.forFront(orientation);
			final var neighbor = be.getBlockPos().relative(absoluteFace);
			return updateRedstoneValue(absoluteFace, neighbor);
		}
	}

	private int updateRedstoneValue(Direction sideAbsolute, BlockPos neighborPos)
	{
		final var level = Objects.requireNonNull(be.getLevel());
		final int rsStrength = level.getSignal(neighborPos, sideAbsolute);
		final var sideRelative = RelativeBlockFace.from(orientation, sideAbsolute);
		cachedRedstoneValues.put(sideRelative, rsStrength);
		return rsStrength;
	}
}
