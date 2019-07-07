/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.metal;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.ApiUtils;
import blusunrize.immersiveengineering.common.Config.IEConfig;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IDynamicTexture;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IHasObjProperty;
import blusunrize.immersiveengineering.common.blocks.generic.TileEntityMultiblockPart;
import blusunrize.immersiveengineering.common.blocks.multiblocks.MultiblockBucketWheel;
import blusunrize.immersiveengineering.common.network.MessageTileSync;
import blusunrize.immersiveengineering.common.util.Utils;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fml.network.PacketDistributor;

import java.util.ArrayList;
import java.util.HashMap;

public class TileEntityBucketWheel extends TileEntityMultiblockPart<TileEntityBucketWheel> implements IHasObjProperty, IDynamicTexture
{
	public static TileEntityType<TileEntityBucketWheel> TYPE;

	public float rotation = 0;
	public NonNullList<ItemStack> digStacks = NonNullList.withSize(8, ItemStack.EMPTY);
	public boolean active = false;
	public ItemStack particleStack = ItemStack.EMPTY;

	public TileEntityBucketWheel()
	{
		super(MultiblockBucketWheel.instance, TYPE, false);
	}

	@Override
	public void readCustomNBT(CompoundNBT nbt, boolean descPacket)
	{
		super.readCustomNBT(nbt, descPacket);
		float nbtRot = nbt.getFloat("rotation");
		rotation = (Math.abs(nbtRot-rotation) > 5*IEConfig.Machines.excavator_speed)?nbtRot: rotation; // avoid stuttering due to packet delays
		digStacks = Utils.readInventory(nbt.getList("digStacks", 10), 8);
		active = nbt.getBoolean("active");
		particleStack = nbt.hasKey("particleStack")?ItemStack.read(nbt.getCompound("particleStack")): ItemStack.EMPTY;
	}

	@Override
	public void writeCustomNBT(CompoundNBT nbt, boolean descPacket)
	{
		super.writeCustomNBT(nbt, descPacket);
		nbt.setFloat("rotation", rotation);
		nbt.setTag("digStacks", Utils.writeInventory(digStacks));
		nbt.setBoolean("active", active);
		if(!particleStack.isEmpty())
			nbt.setTag("particleStack", particleStack.write(new CompoundNBT()));
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
		ApiUtils.checkForNeedlessTicking(this);
		if(!formed||posInMultiblock!=24)
			return;

		if(active)
		{
			rotation += IEConfig.Machines.excavator_speed;
			rotation %= 360;
		}

		if(world.isRemote)
		{
			if(!particleStack.isEmpty())
			{
				ImmersiveEngineering.proxy.spawnBucketWheelFX(this, particleStack);
				particleStack = ItemStack.EMPTY;
			}
		}
		else if(active&&world.getGameTime()%20==0)
		{
			CompoundNBT nbt = new CompoundNBT();
			nbt.setFloat("rotation", rotation);
			MessageTileSync sync = new MessageTileSync(this, nbt);
			ImmersiveEngineering.packetHandler.send(PacketDistributor.TRACKING_CHUNK.with(() -> world.getChunk(pos)), sync);
		}
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public HashMap<String, String> getTextureReplacements()
	{
		//TODO
		//synchronized(digStacks)
		//{
		//	HashMap<String, String> texMap = new HashMap<String, String>();
		//	for(int i = 0; i < this.digStacks.size(); i++)
		//		if(!this.digStacks.get(i).isEmpty())
		//		{
		//			Block b = Block.getBlockFromItem(this.digStacks.get(i).getItem());
		//			IBlockState state = b!=null?b.getStateFromMeta(this.digStacks.get(i).getMetadata()): Blocks.STONE.getDefaultState();
		//			IBakedModel model = Minecraft.getInstance().getBlockRendererDispatcher().getBlockModelShapes().getModelForState(state);
		//			if(model!=null&&model.getParticleTexture()!=null)
		//				texMap.put("dig"+i, model.getParticleTexture().getIconName());
		//		}
		//	return texMap;
		//}
		return new HashMap<>();
	}

	static ArrayList<String> emptyDisplayList = new ArrayList<>();

	@Override
	public ArrayList<String> compileDisplayList()
	{
		return emptyDisplayList;
	}

	@Override
	public void receiveMessageFromServer(CompoundNBT message)
	{
		synchronized(digStacks)
		{
			if(message.hasKey("fill"))
				this.digStacks.set(message.getInt("fill"), ItemStack.read(message.getCompound("fillStack")));
			if(message.hasKey("empty"))
				this.digStacks.set(message.getInt("empty"), ItemStack.EMPTY);
			if(message.hasKey("rotation"))
			{
				int packetRotation = message.getInt("rotation");
				if(Math.abs(packetRotation-rotation) > 5*IEConfig.Machines.excavator_speed)
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
			renderAABB = new AxisAlignedBB(getPos().add(-(facing.getAxis()==Axis.Z?3: 0), -3, -(facing.getAxis()==Axis.X?3: 0)), getPos().add((facing.getAxis()==Axis.Z?4: 1), 4, (facing.getAxis()==Axis.X?4: 1)));
//			else
//				renderAABB = new AxisAlignedBB(getPos(), getPos());
		return renderAABB;
	}

	@Override
	public float[] getBlockBounds()
	{
		if(posInMultiblock==3||posInMultiblock==9||posInMultiblock==11)
			return new float[]{0, .25f, 0, 1, 1, 1};
		else if(posInMultiblock==45||posInMultiblock==37||posInMultiblock==39)
			return new float[]{0, 0, 0, 1, .75f, 1};
		else if(posInMultiblock==21)
			return new float[]{facing==Direction.NORTH?.25f: 0, 0, facing==Direction.WEST?.25f: 0, facing==Direction.SOUTH?.75f: 1, 1, facing==Direction.EAST?.75f: 1};
		else if(posInMultiblock==27)
			return new float[]{facing==Direction.SOUTH?.25f: 0, 0, facing==Direction.EAST?.25f: 0, facing==Direction.NORTH?.75f: 1, 1, facing==Direction.WEST?.75f: 1};
		else if(posInMultiblock==15||posInMultiblock==29)
			return new float[]{facing==Direction.NORTH?.25f: 0, 0, facing==Direction.WEST?.25f: 0, facing==Direction.SOUTH?.75f: 1, 1, facing==Direction.EAST?.75f: 1};
		else if(posInMultiblock==19||posInMultiblock==33)
			return new float[]{facing==Direction.SOUTH?.25f: 0, 0, facing==Direction.EAST?.25f: 0, facing==Direction.NORTH?.75f: 1, 1, facing==Direction.WEST?.75f: 1};
		return new float[]{0, 0, 0, 1, 1, 1};
	}
}