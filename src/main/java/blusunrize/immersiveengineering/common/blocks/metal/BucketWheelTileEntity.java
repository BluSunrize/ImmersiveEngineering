/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.metal;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.utils.shapes.CachedShapesWithTransform;
import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.client.models.IOBJModelCallback;
import blusunrize.immersiveengineering.common.IETileTypes;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IBlockBounds;
import blusunrize.immersiveengineering.common.blocks.generic.MultiblockPartTileEntity;
import blusunrize.immersiveengineering.common.blocks.multiblocks.IEMultiblocks;
import blusunrize.immersiveengineering.common.config.IEServerConfig;
import blusunrize.immersiveengineering.common.network.MessageTileSync;
import blusunrize.immersiveengineering.common.util.Utils;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fml.network.PacketDistributor;

import javax.annotation.Nullable;
import java.util.List;

public class BucketWheelTileEntity extends MultiblockPartTileEntity<BucketWheelTileEntity> implements
		IOBJModelCallback<BlockState>, IBlockBounds
{
	public float rotation = 0;
	public final NonNullList<ItemStack> digStacks = NonNullList.withSize(8, ItemStack.EMPTY);
	public boolean active = false;
	public ItemStack particleStack = ItemStack.EMPTY;

	public BucketWheelTileEntity()
	{
		super(IEMultiblocks.BUCKET_WHEEL, IETileTypes.BUCKET_WHEEL.get(), false);
	}

	@Override
	public void readCustomNBT(CompoundNBT nbt, boolean descPacket)
	{
		super.readCustomNBT(nbt, descPacket);
		float nbtRot = nbt.getFloat("rotation");
		rotation = (Math.abs(nbtRot-rotation) > 5*IEServerConfig.MACHINES.excavator_speed.get())?nbtRot: rotation; // avoid stuttering due to packet delays
		ItemStackHelper.loadAllItems(nbt, digStacks);
		active = nbt.getBoolean("active");
		particleStack = nbt.contains("particleStack", NBT.TAG_COMPOUND)?ItemStack.read(nbt.getCompound("particleStack")): ItemStack.EMPTY;
	}

	@Override
	public void writeCustomNBT(CompoundNBT nbt, boolean descPacket)
	{
		super.writeCustomNBT(nbt, descPacket);
		nbt.putFloat("rotation", rotation);
		ItemStackHelper.saveAllItems(nbt, digStacks);
		nbt.putBoolean("active", active);
		if(!particleStack.isEmpty())
			nbt.put("particleStack", particleStack.write(new CompoundNBT()));
	}

	@Override
	protected IFluidTank[] getAccessibleFluidTanks(Direction side)
	{
		return new IFluidTank[0];
	}

	@Override
	protected boolean canFillTankFrom(int iTank, Direction side, FluidStack resources)
	{
		return false;
	}

	@Override
	protected boolean canDrainTankFrom(int iTank, Direction side)
	{
		return false;
	}

	@Override
	public void tick()
	{
		checkForNeedlessTicking();
		if(!formed||!new BlockPos(3, 3, 0).equals(posInMultiblock))
			return;

		if(active)
		{
			rotation += IEServerConfig.MACHINES.excavator_speed.get();
			rotation %= 360;
		}

		if(world.isRemote)
		{
			if(!particleStack.isEmpty())
			{
				//TODO this can be done from the server now
				ImmersiveEngineering.proxy.spawnBucketWheelFX(this, particleStack);
				particleStack = ItemStack.EMPTY;
			}
		}
		else if(active&&world.getGameTime()%20==0)
		{
			CompoundNBT nbt = new CompoundNBT();
			nbt.putFloat("rotation", rotation);
			MessageTileSync sync = new MessageTileSync(this, nbt);
			ImmersiveEngineering.packetHandler.send(PacketDistributor.TRACKING_CHUNK.with(() -> world.getChunkAt(pos)), sync);
		}
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public TextureAtlasSprite getTextureReplacement(BlockState object, String group, String material)
	{
		if(group.startsWith("dig"))
		{
			int index = Integer.parseInt(group.substring(3));
			if(!this.digStacks.get(index).isEmpty())
			{
				ResourceLocation rl = null;
				BlockState state = Utils.getStateFromItemStack(this.digStacks.get(index));
				if(state!=null)
					rl = ClientUtils.getSideTexture(state, Direction.UP);
				else
					rl = ClientUtils.getSideTexture(this.digStacks.get(index), Direction.UP);
				if(rl!=null)
					return ClientUtils.getSprite(rl);
			}
		}
		return null;
	}

	@Override
	public void receiveMessageFromServer(CompoundNBT message)
	{
		synchronized(digStacks)
		{
			if(message.contains("fill", NBT.TAG_INT))
				this.digStacks.set(message.getInt("fill"), ItemStack.read(message.getCompound("fillStack")));
			if(message.contains("empty", NBT.TAG_INT))
			{
				int toRemove = message.getInt("empty");
				particleStack = digStacks.get(toRemove);
				this.digStacks.set(toRemove, ItemStack.EMPTY);
			}
			if(message.contains("rotation", NBT.TAG_INT))
			{
				int packetRotation = message.getInt("rotation");
				if(Math.abs(packetRotation-rotation) > 5*IEServerConfig.MACHINES.excavator_speed.get())
					rotation = packetRotation;
			}
		}
	}

	@Override
	public boolean receiveClientEvent(int id, int arg)
	{
		if(id==0)
			this.active = (arg==1);
		return true;
	}

	@OnlyIn(Dist.CLIENT)
	private AxisAlignedBB renderAABB;

	@Override
	@OnlyIn(Dist.CLIENT)
	public AxisAlignedBB getRenderBoundingBox()
	{
		if(renderAABB==null)
//			if(pos==24)
			renderAABB = new AxisAlignedBB(getPos().add(-(getFacing().getAxis()==Axis.Z?3: 0), -3, -(getFacing().getAxis()==Axis.X?3: 0)), getPos().add((getFacing().getAxis()==Axis.Z?4: 1), 4, (getFacing().getAxis()==Axis.X?4: 1)));
//			else
//				renderAABB = new AxisAlignedBB(getPos(), getPos());
		return renderAABB;
	}

	private static CachedShapesWithTransform<BlockPos, Direction> SHAPES = CachedShapesWithTransform.createDirectional(BucketWheelTileEntity::getBoxes);

	@Override
	public VoxelShape getBlockBounds(@Nullable ISelectionContext ctx)
	{
		return SHAPES.get(posInMultiblock, getFacing());
	}

	private static List<AxisAlignedBB> getBoxes(BlockPos posInMultiblock)
	{
		final AxisAlignedBB ret;
		if(ImmutableSet.of(
				new BlockPos(3, 0, 0),
				new BlockPos(2, 1, 0),
				new BlockPos(4, 1, 0)
		).contains(posInMultiblock))
			ret = new AxisAlignedBB(0, .25f, 0, 1, 1, 1);
		else if(ImmutableSet.of(
				new BlockPos(3, 6, 0),
				new BlockPos(2, 5, 0),
				new BlockPos(4, 5, 0)
		).contains(posInMultiblock))
			ret = new AxisAlignedBB(0, 0, 0, 1, .75f, 1);
		else if(new BlockPos(0, 3, 0).equals(posInMultiblock))
			ret = new AxisAlignedBB(.25f, 0, 0, 1, 1, 1);
		else if(new BlockPos(6, 3, 0).equals(posInMultiblock))
			ret = new AxisAlignedBB(0, 0, 0, .75f, 1, 1);
		else if(ImmutableSet.of(
				new BlockPos(1, 2, 0),
				new BlockPos(1, 4, 0)
		).contains(posInMultiblock))
			ret = new AxisAlignedBB(.25f, 0, 0, 1, 1, 1);
		else if(ImmutableSet.of(
				new BlockPos(5, 2, 0),
				new BlockPos(5, 4, 0)
		).contains(posInMultiblock))
			ret = new AxisAlignedBB(0, 0, 0, .75f, 1, 1);
		else
			ret = new AxisAlignedBB(0, 0, 0, 1, 1, 1);
		return ImmutableList.of(ret);
	}
}