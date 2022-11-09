package blusunrize.immersiveengineering.common.blocks.multiblocks.blockimpl;

import blusunrize.immersiveengineering.api.multiblocks.blocks.*;
import blusunrize.immersiveengineering.api.multiblocks.blocks.IMultiblockLogic.IMultiblockState;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nullable;

public abstract class MultiblockBEHelperCommon<State extends IMultiblockState> implements IMultiblockBEHelper<State>
{
	private boolean beingDisassembled = false;
	protected final MultiblockRegistration<State> multiblock;
	protected final MultiblockOrientation orientation;

	protected MultiblockBEHelperCommon(MultiblockRegistration<State> multiblock, BlockState state)
	{
		this.multiblock = multiblock;
		this.orientation = new MultiblockOrientation(state, multiblock.mirrorable());
	}

	@Override
	public VoxelShape getShape()
	{
		// TODO cache!
		return orientation.transformRelativeShape(multiblock.logic().shapeGetter().apply(getPositionInMB()));
	}

	@Override
	public <T> LazyOptional<T> getCapability(Capability<T> cap, @Nullable Direction side)
	{
		final var ctx = getContext();
		if(ctx==null)
			return LazyOptional.empty();
		final var relativeSide = RelativeBlockFace.from(orientation, side);
		return multiblock.logic().getCapability(ctx, getPositionInMB(), relativeSide, cap);
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

	@Override
	public void markDisassembling()
	{
		beingDisassembled = true;
	}
}
