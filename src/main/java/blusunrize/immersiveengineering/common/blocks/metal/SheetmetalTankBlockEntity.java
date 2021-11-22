/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.metal;

import blusunrize.immersiveengineering.api.fluid.FluidUtils;
import blusunrize.immersiveengineering.api.utils.CapabilityReference;
import blusunrize.immersiveengineering.api.utils.DirectionUtils;
import blusunrize.immersiveengineering.client.utils.TextUtils;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IBlockBounds;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IBlockOverlayText;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IComparatorOverride;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IPlayerInteraction;
import blusunrize.immersiveengineering.common.blocks.generic.MultiblockPartBlockEntity;
import blusunrize.immersiveengineering.common.blocks.multiblocks.IEMultiblocks;
import blusunrize.immersiveengineering.common.util.LayeredComparatorOutput;
import blusunrize.immersiveengineering.common.util.MultiblockCapability;
import blusunrize.immersiveengineering.common.util.Utils;
import com.google.common.collect.ImmutableSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidAttributes;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;
import net.minecraftforge.fluids.capability.templates.FluidTank;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class SheetmetalTankBlockEntity extends MultiblockPartBlockEntity<SheetmetalTankBlockEntity>
		implements IBlockOverlayText, IPlayerInteraction, IComparatorOverride, IBlockBounds
{
	public FluidTank tank = new FluidTank(512*FluidAttributes.BUCKET_VOLUME);
	private final LayeredComparatorOutput comparatorHelper = new LayeredComparatorOutput(
			tank.getCapacity(),
			4,
			() -> level.updateNeighborsAt(getBlockPos(), getBlockState().getBlock()),
			layer -> {
				BlockPos masterPos = worldPosition.subtract(offsetToMaster);
				for(int x = -1; x <= 1; x++)
					for(int z = -1; z <= 1; z++)
					{
						BlockPos pos = masterPos.offset(x, layer+1, z);
						level.updateNeighborsAt(pos, level.getBlockState(pos).getBlock());
					}
			}
	);
	private final List<CapabilityReference<IFluidHandler>> fluidNeighbors = new ArrayList<>();

	public SheetmetalTankBlockEntity(BlockEntityType<SheetmetalTankBlockEntity> type, BlockPos pos, BlockState state)
	{
		super(IEMultiblocks.SHEETMETAL_TANK, type, true, pos, state);
		// Tanks should not output by default
		this.redstoneControlInverted = true;
		for(Direction f : DirectionUtils.VALUES)
			if(f!=Direction.UP)
				fluidNeighbors.add(
						CapabilityReference.forNeighbor(this, CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, f)
				);
	}

	@Override
	public Component[] getOverlayText(Player player, HitResult mop, boolean hammer)
	{
		if(Utils.isFluidRelatedItemStack(player.getItemInHand(InteractionHand.MAIN_HAND)))
		{
			SheetmetalTankBlockEntity master = master();
			FluidStack fs = master!=null?master.tank.getFluid(): this.tank.getFluid();
			return new Component[]{TextUtils.formatFluidStack(fs)};
		}
		return null;
	}

	@Override
	public boolean useNixieFont(Player player, HitResult mop)
	{
		return false;
	}

	@Override
	public void tickServer()
	{
		if(!isRSDisabled())
			for(CapabilityReference<IFluidHandler> outputRef : fluidNeighbors)
				if(tank.getFluidAmount() > 0)
				{
					int outSize = Math.min(144, tank.getFluidAmount());
					FluidStack out = Utils.copyFluidStackWithAmount(tank.getFluid(), outSize, false);
					IFluidHandler output = outputRef.getNullable();
					if(output!=null)
					{
						int accepted = output.fill(out, FluidAction.SIMULATE);
						if(accepted > 0)
						{
							int drained = output.fill(Utils.copyFluidStackWithAmount(out, Math.min(out.getAmount(), accepted), false), FluidAction.EXECUTE);
							this.tank.drain(drained, FluidAction.EXECUTE);
							this.markContainingBlockForUpdate(null);
						}
					}
				}
		comparatorHelper.update(tank.getFluidAmount());
	}

	@Override
	public Set<BlockPos> getRedstonePos()
	{
		return ImmutableSet.of(
				new BlockPos(1, 0, 1)
		);
	}

	@Override
	public void readCustomNBT(CompoundTag nbt, boolean descPacket)
	{
		super.readCustomNBT(nbt, descPacket);
		tank.readFromNBT(nbt.getCompound("tank"));
	}

	@Override
	public void writeCustomNBT(CompoundTag nbt, boolean descPacket)
	{
		super.writeCustomNBT(nbt, descPacket);
		CompoundTag tankTag = tank.writeToNBT(new CompoundTag());
		nbt.put("tank", tankTag);
	}

	@Override
	public VoxelShape getBlockBounds(@Nullable CollisionContext ctx)
	{
		if(posInMultiblock.getX()%2==0&&posInMultiblock.getY()==0&&posInMultiblock.getZ()%2==0)
			return Shapes.box(.375f, 0, .375f, .625f, 1, .625f);
		return Shapes.block();
	}

	private static final BlockPos ioTopOffset = new BlockPos(1, 4, 1);
	private static final BlockPos ioBottomOffset = new BlockPos(1, 0, 1);
	private final MultiblockCapability<IFluidHandler> fluidInput = MultiblockCapability.make(
			this, be -> be.fluidInput, SheetmetalTankBlockEntity::master, registerFluidInput(tank)
	);
	private final MultiblockCapability<IFluidHandler> fluidIO = MultiblockCapability.make(
			this, be -> be.fluidInput, SheetmetalTankBlockEntity::master, registerFluidHandler(tank)
	);

	@Nonnull
	@Override
	public <C> LazyOptional<C> getCapability(@Nonnull Capability<C> capability, @Nullable Direction facing)
	{
		if(capability==CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY)
		{
			if(ioBottomOffset.equals(posInMultiblock))
				return fluidIO.getAndCast();
			else if(ioTopOffset.equals(posInMultiblock))
				return fluidInput.getAndCast();
		}
		return super.getCapability(capability, facing);
	}

	@Override
	public boolean interact(Direction side, Player player, InteractionHand hand, ItemStack heldItem, float hitX, float hitY, float hitZ)
	{
		SheetmetalTankBlockEntity master = this.master();
		if(master!=null)
		{
			if(FluidUtils.interactWithFluidHandler(player, hand, master.tank))
			{
				this.updateMasterBlock(null, true);
				return true;
			}
		}
		return false;
	}

	private AABB renderAABB;

	@Override
	public AABB getRenderBoundingBox()
	{
		if(renderAABB==null)
		{
			if(offsetToMaster.equals(BlockPos.ZERO))
				renderAABB = new AABB(getBlockPos().offset(-1, 0, -1), getBlockPos().offset(2, 5, 2));
			else
				renderAABB = new AABB(getBlockPos(), getBlockPos());
		}
		return renderAABB;
	}

	@Override
	public int getComparatorInputOverride()
	{
		if(ioBottomOffset.equals(posInMultiblock))
			return comparatorHelper.getCurrentMasterOutput();
		SheetmetalTankBlockEntity master = master();
		if(offsetToMaster.getY() >= 1&&master!=null)//4 layers of storage
			return master.comparatorHelper.getLayerOutput(offsetToMaster.getY()-1);
		return 0;
	}
}