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
import blusunrize.immersiveengineering.common.blocks.TileEntityMultiblockPart;
import blusunrize.immersiveengineering.common.blocks.multiblocks.MultiblockBucketWheel;
import blusunrize.immersiveengineering.common.util.Utils;
import blusunrize.immersiveengineering.common.util.network.MessageTileSync;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fml.common.network.NetworkRegistry.TargetPoint;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.ArrayList;
import java.util.HashMap;

public class TileEntityBucketWheel extends TileEntityMultiblockPart<TileEntityBucketWheel> implements IHasObjProperty, IDynamicTexture
{
	public float rotation = 0;
	public NonNullList<ItemStack> digStacks = NonNullList.withSize(8, ItemStack.EMPTY);
	public boolean active = false;
	public ItemStack particleStack = ItemStack.EMPTY;
	private static final int[] size = {7, 1, 7};

	public TileEntityBucketWheel()
	{
		super(size);
	}

	@Override
	public ItemStack getOriginalBlock()
	{
		if(pos < 0)
			return ItemStack.EMPTY;
		ItemStack s = pos < 0?ItemStack.EMPTY: MultiblockBucketWheel.instance.getStructureManual()[pos/7][pos%7][0];
		return s.copy();
	}

	@Override
	public void readCustomNBT(NBTTagCompound nbt, boolean descPacket)
	{
		super.readCustomNBT(nbt, descPacket);
		float nbtRot = nbt.getFloat("rotation");
		rotation = (Math.abs(nbtRot-rotation) > 5*IEConfig.Machines.excavator_speed)?nbtRot: rotation; // avoid stuttering due to packet delays
		digStacks = Utils.readInventory(nbt.getTagList("digStacks", 10), 8);
		active = nbt.getBoolean("active");
		particleStack = nbt.hasKey("particleStack")?new ItemStack(nbt.getCompoundTag("particleStack")): ItemStack.EMPTY;
	}

	@Override
	public void writeCustomNBT(NBTTagCompound nbt, boolean descPacket)
	{
		super.writeCustomNBT(nbt, descPacket);
		nbt.setFloat("rotation", rotation);
		nbt.setTag("digStacks", Utils.writeInventory(digStacks));
		nbt.setBoolean("active", active);
		if(!particleStack.isEmpty())
			nbt.setTag("particleStack", particleStack.writeToNBT(new NBTTagCompound()));
	}

	@Override
	protected IFluidTank[] getAccessibleFluidTanks(EnumFacing side)
	{
		return new IFluidTank[0];
	}

	@Override
	protected boolean canFillTankFrom(int iTank, EnumFacing side, FluidStack resources)
	{
		return false;
	}

	@Override
	protected boolean canDrainTankFrom(int iTank, EnumFacing side)
	{
		return false;
	}

	@Override
	public void update()
	{
		ApiUtils.checkForNeedlessTicking(this);
		if(!formed||pos!=24)
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
		else if(active&&world.getTotalWorldTime()%20==0)
		{
			NBTTagCompound nbt = new NBTTagCompound();
			nbt.setFloat("rotation", rotation);
			MessageTileSync sync = new MessageTileSync(this, nbt);
			ImmersiveEngineering.packetHandler.sendToAllAround(sync, new TargetPoint(world.provider.getDimension(),
					getPos().getX(), getPos().getY(), getPos().getZ(), 100));
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public HashMap<String, String> getTextureReplacements()
	{
		synchronized(digStacks)
		{
			HashMap<String, String> texMap = new HashMap<String, String>();
			for(int i = 0; i < this.digStacks.size(); i++)
				if(!this.digStacks.get(i).isEmpty())
				{
					Block b = Block.getBlockFromItem(this.digStacks.get(i).getItem());
					IBlockState state = b!=null?b.getStateFromMeta(this.digStacks.get(i).getMetadata()): Blocks.STONE.getDefaultState();
					IBakedModel model = Minecraft.getMinecraft().getBlockRendererDispatcher().getBlockModelShapes().getModelForState(state);
					if(model!=null&&model.getParticleTexture()!=null)
						texMap.put("dig"+i, model.getParticleTexture().getIconName());
				}
			return texMap;
		}
	}

	static ArrayList<String> emptyDisplayList = new ArrayList<>();

	@Override
	public ArrayList<String> compileDisplayList()
	{
		return emptyDisplayList;
	}

	@Override
	public void receiveMessageFromServer(NBTTagCompound message)
	{
		synchronized(digStacks)
		{
			if(message.hasKey("fill"))
				this.digStacks.set(message.getInteger("fill"), new ItemStack(message.getCompoundTag("fillStack")));
			if(message.hasKey("empty"))
				this.digStacks.set(message.getInteger("empty"), ItemStack.EMPTY);
			if(message.hasKey("rotation"))
			{
				int packetRotation = message.getInteger("rotation");
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

	@SideOnly(Side.CLIENT)
	private AxisAlignedBB renderAABB;

	@Override
	@SideOnly(Side.CLIENT)
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
		if(pos==3||pos==9||pos==11)
			return new float[]{0, .25f, 0, 1, 1, 1};
		else if(pos==45||pos==37||pos==39)
			return new float[]{0, 0, 0, 1, .75f, 1};
		else if(pos==21)
			return new float[]{facing==EnumFacing.NORTH?.25f: 0, 0, facing==EnumFacing.WEST?.25f: 0, facing==EnumFacing.SOUTH?.75f: 1, 1, facing==EnumFacing.EAST?.75f: 1};
		else if(pos==27)
			return new float[]{facing==EnumFacing.SOUTH?.25f: 0, 0, facing==EnumFacing.EAST?.25f: 0, facing==EnumFacing.NORTH?.75f: 1, 1, facing==EnumFacing.WEST?.75f: 1};
		else if(pos==15||pos==29)
			return new float[]{facing==EnumFacing.NORTH?.25f: 0, 0, facing==EnumFacing.WEST?.25f: 0, facing==EnumFacing.SOUTH?.75f: 1, 1, facing==EnumFacing.EAST?.75f: 1};
		else if(pos==19||pos==33)
			return new float[]{facing==EnumFacing.SOUTH?.25f: 0, 0, facing==EnumFacing.EAST?.25f: 0, facing==EnumFacing.NORTH?.75f: 1, 1, facing==EnumFacing.WEST?.75f: 1};
		return new float[]{0, 0, 0, 1, 1, 1};
	}
}