package blusunrize.immersiveengineering.common.blocks.metal;

import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.api.TargetingInfo;
import blusunrize.immersiveengineering.api.energy.wires.IImmersiveConnectable;
import blusunrize.immersiveengineering.api.energy.wires.ImmersiveNetHandler;
import blusunrize.immersiveengineering.api.energy.wires.ImmersiveNetHandler.Connection;
import blusunrize.immersiveengineering.api.energy.wires.TileEntityImmersiveConnectable;
import blusunrize.immersiveengineering.api.energy.wires.WireType;
import blusunrize.immersiveengineering.client.models.IOBJModelCallback;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.*;
import blusunrize.immersiveengineering.common.util.Utils;
import blusunrize.immersiveengineering.common.util.chickenbones.Matrix4;
import com.google.common.base.Optional;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.common.model.TRSRTransformation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

public class TileEntityConnectorRedstone extends TileEntityImmersiveConnectable implements ITickable, IDirectionalTile, IRedstoneOutput, IHammerInteraction, IBlockBounds, IBlockOverlayText, IOBJModelCallback<IBlockState>
{
	public EnumFacing facing = EnumFacing.DOWN;
	public int ioMode = 0; // 0 - input, 1 -output
	public int redstoneChannel = 0;

	public RedstoneWireNetwork wireNetwork = new RedstoneWireNetwork().add(this);
	private boolean loaded = false;

	@Override
	public void update()
	{
		if(hasWorldObj() && !worldObj.isRemote && !loaded)
		{
			loaded = true;
			wireNetwork.removeFromNetwork(null);
		}
	}

	@Override
	public int getStrongRSOutput(IBlockState state, EnumFacing side)
	{
		if(!isRSOutput() || side != this.facing.getOpposite())
			return 0;
		return wireNetwork != null ? wireNetwork.getPowerOutput(redstoneChannel) : 0;
	}

	@Override
	public int getWeakRSOutput(IBlockState state, EnumFacing side)
	{
		if(!isRSOutput())
			return 0;
		return wireNetwork != null ? wireNetwork.getPowerOutput(redstoneChannel) : 0;
	}

	@Override
	public boolean canConnectRedstone(IBlockState state, EnumFacing side)
	{
		return false;
	}

	public boolean isRSInput()
	{
		return ioMode == 0;
	}

	public boolean isRSOutput()
	{
		return ioMode == 1;
	}

	@Override
	public boolean hammerUseSide(EnumFacing side, EntityPlayer player, float hitX, float hitY, float hitZ)
	{
		//Sneaking iterates through colours, normal hammerign toggles in and out
		if(player.isSneaking())
			redstoneChannel = (redstoneChannel + 1) % 16;
		else
			ioMode = ioMode == 0 ? 1 : 0;
		markDirty();
		wireNetwork.updateValues();
		wireNetwork.notifyOfChange(this);
		worldObj.addBlockEvent(getPos(), this.getBlockType(), 254, 0);
		return true;
	}

	@Override
	public boolean canConnectCable(WireType cableType, TargetingInfo target)
	{
		if(cableType != WireType.REDSTONE)
			return false;
		return limitType == null || limitType == cableType;
	}

	@Override
	public void connectCable(WireType cableType, TargetingInfo target, IImmersiveConnectable other)
	{
		super.connectCable(cableType, target, other);
		if(other instanceof TileEntityConnectorRedstone)
			if(((TileEntityConnectorRedstone) other).wireNetwork != wireNetwork)
				wireNetwork.mergeNetwork(((TileEntityConnectorRedstone) other).wireNetwork);
	}

	@Override
	public void removeCable(ImmersiveNetHandler.Connection connection)
	{
		super.removeCable(connection);
		wireNetwork.removeFromNetwork(this);
	}

	@Override
	public EnumFacing getFacing()
	{
		return this.facing;
	}

	@Override
	public void setFacing(EnumFacing facing)
	{
		this.facing = facing;
	}

	@Override
	public int getFacingLimitation()
	{
		return 0;
	}

	@Override
	public boolean mirrorFacingOnPlacement(EntityLivingBase placer)
	{
		return true;
	}

	@Override
	public boolean canHammerRotate(EnumFacing side, float hitX, float hitY, float hitZ, EntityLivingBase entity)
	{
		return false;
	}


	@Override
	public void writeCustomNBT(NBTTagCompound nbt, boolean descPacket)
	{
		super.writeCustomNBT(nbt, descPacket);
		nbt.setInteger("facing", facing.ordinal());
		nbt.setInteger("ioMode", ioMode);
		nbt.setInteger("redstoneChannel", redstoneChannel);
	}

	@Override
	public void readCustomNBT(NBTTagCompound nbt, boolean descPacket)
	{
		super.readCustomNBT(nbt, descPacket);
		facing = EnumFacing.getFront(nbt.getInteger("facing"));
		ioMode = nbt.getInteger("ioMode");
		redstoneChannel = nbt.getInteger("redstoneChannel");
	}

	@Override
	public Vec3d getRaytraceOffset(IImmersiveConnectable link)
	{
		EnumFacing side = facing.getOpposite();
		return new Vec3d(.5 + side.getFrontOffsetX() * .0625, .5 + side.getFrontOffsetY() * .0625, .5 + side.getFrontOffsetZ() * .0625);
	}

	@Override
	public Vec3d getConnectionOffset(Connection con)
	{
		EnumFacing side = facing.getOpposite();
		double conRadius = con.cableType.getRenderDiameter() / 2;
		return new Vec3d(.5 - conRadius * side.getFrontOffsetX(), .5 - conRadius * side.getFrontOffsetY(), .5 - conRadius * side.getFrontOffsetZ());
	}

	@SideOnly(Side.CLIENT)
	private AxisAlignedBB renderAABB;

	@SideOnly(Side.CLIENT)
	@Override
	public AxisAlignedBB getRenderBoundingBox()
	{
		int inc = getRenderRadiusIncrease();
		return new AxisAlignedBB(this.pos.getX() - inc, this.pos.getY() - inc, this.pos.getZ() - inc, this.pos.getX() + inc + 1, this.pos.getY() + inc + 1, this.pos.getZ() + inc + 1);
	}

	int getRenderRadiusIncrease()
	{
		return WireType.REDSTONE.getMaxLength();
	}

	@Override
	public float[] getBlockBounds()
	{
		float length = .625f;
		float wMin = .3125f;
		float wMax = .6875f;
		switch(facing.getOpposite())
		{
			case UP:
				return new float[]{wMin, 0, wMin, wMax, length, wMax};
			case DOWN:
				return new float[]{wMin, 1 - length, wMin, wMax, 1, wMax};
			case SOUTH:
				return new float[]{wMin, wMin, 0, wMax, wMax, length};
			case NORTH:
				return new float[]{wMin, wMin, 1 - length, wMax, wMax, 1};
			case EAST:
				return new float[]{0, wMin, wMin, length, wMax, wMax};
			case WEST:
				return new float[]{1 - length, wMin, wMin, 1, wMax, wMax};
		}
		return new float[]{0, 0, 0, 1, 1, 1};
	}

	@SideOnly(Side.CLIENT)
	@Override
	public TextureAtlasSprite getTextureReplacement(IBlockState object, String material)
	{
		return null;
	}

	@SideOnly(Side.CLIENT)
	@Override
	public boolean shouldRenderGroup(IBlockState object, String group)
	{
		if("io_out".equals(group))
			return this.ioMode == 1;
		else if("io_in".equals(group))
			return this.ioMode == 0;
		return true;
	}

	@SideOnly(Side.CLIENT)
	@Override
	public Optional<TRSRTransformation> applyTransformations(IBlockState object, String group, Optional<TRSRTransformation> transform)
	{
		return transform;
	}

	@SideOnly(Side.CLIENT)
	@Override
	public Matrix4 handlePerspective(IBlockState Object, TransformType cameraTransformType, Matrix4 perspective)
	{
		return perspective;
	}

	@SideOnly(Side.CLIENT)
	@Override
	public int getRenderColour(IBlockState object, String group)
	{
		if("coloured".equals(group))
			return EnumDyeColor.byMetadata(this.redstoneChannel).getMapColor().colorValue;
		return 0xffffff;
	}

	@Override
	public String[] getOverlayText(EntityPlayer player, RayTraceResult mop, boolean hammer)
	{
		if(!hammer)
			return null;
		return new String[]{
				I18n.format(Lib.DESC_INFO + "redstoneChannel", I18n.format("item.fireworksCharge." + EnumDyeColor.byMetadata(redstoneChannel).getUnlocalizedName())),
				I18n.format(Lib.DESC_INFO + "blockSide.io." + this.ioMode)
		};
	}

	@Override
	public boolean useNixieFont(EntityPlayer player, RayTraceResult mop)
	{
		return false;
	}

	static class RedstoneWireNetwork
	{
		public byte[] channelValues = new byte[16];
		public List<WeakReference<TileEntityConnectorRedstone>> connectors = new ArrayList();

		public RedstoneWireNetwork add(TileEntityConnectorRedstone connector)
		{
			connectors.add(new WeakReference<>(connector));
			return this;
		}

		public void mergeNetwork(RedstoneWireNetwork wireNetwork)
		{
			for(WeakReference<TileEntityConnectorRedstone> connectorRef : wireNetwork.connectors)
			{
				TileEntityConnectorRedstone connector = connectorRef.get();
				if(connector != null)
					connector.wireNetwork = add(connector);
			}
			for(WeakReference<TileEntityConnectorRedstone> connectorRef : wireNetwork.connectors)
			{
				TileEntityConnectorRedstone connector = connectorRef.get();
				if(connector != null)
					notifyOfChange(connector);
			}
		}

		public void removeFromNetwork(TileEntityConnectorRedstone removedConnector)
		{
			BlockPos removedCC = Utils.toCC(removedConnector);
			for(WeakReference<TileEntityConnectorRedstone> connectorRef : connectors)
			{
				TileEntityConnectorRedstone connector = connectorRef.get();
				if(connector != null)
					connector.wireNetwork = new RedstoneWireNetwork().add(connector);
			}
			for(WeakReference<TileEntityConnectorRedstone> connectorRef : connectors)
			{
				TileEntityConnectorRedstone connector = connectorRef.get();
				if(connector != null)
				{
					BlockPos conCC = Utils.toCC(connector);
					Set<ImmersiveNetHandler.Connection> connections = ImmersiveNetHandler.INSTANCE.getConnections(connector.getWorld(), conCC);
					if(connections != null)
						for(ImmersiveNetHandler.Connection connection : connections)
						{
							BlockPos node = connection.start;
							if(node.equals(conCC))
								node = connection.end;
							if(!node.equals(removedCC))
							{
								TileEntity nodeTile = connector.getWorld().getTileEntity(node);
								if(nodeTile instanceof TileEntityConnectorRedstone)
									if(connector.wireNetwork != ((TileEntityConnectorRedstone) nodeTile).wireNetwork)
										connector.wireNetwork.mergeNetwork(((TileEntityConnectorRedstone) nodeTile).wireNetwork);
							}
						}
					if(!connector.isInvalid())
						notifyOfChange(connector);
				}
			}
		}

		public void updateValues()
		{
			byte[] oldValues = channelValues;
			channelValues = new byte[16];
			for(WeakReference<TileEntityConnectorRedstone> connectorRef : connectors)
			{
				TileEntityConnectorRedstone connector = connectorRef.get();
				if(connector != null && connector.isRSInput())
				{
//						if (ProjectRedAPI.transmissionAPI != null)
//						{
//							for (ForgeDirection direction : ForgeDirection.VALID_DIRECTIONS)
//							{
//								byte[] values = ProjectRedAPI.transmissionAPI.getBundledInput(connector.getWorldObj(), connector.xCoord, connector.yCoord, connector.zCoord, direction.getOpposite().ordinal());
//								if (values != null)
//								{
//									for (int i = 0; i < values.length; i++)
//									{
//										channelValues[i] = (byte) Math.max((values[i] & 255) / 16f, channelValues[i]);
//									}
//								}
//							}
//						}
//						if (Loader.isModLoaded("ComputerCraft")) CCCompat.updateRedstoneValues(this, connector);
					int val = connector.getWorld().isBlockIndirectlyGettingPowered(connector.getPos());
					channelValues[connector.redstoneChannel] = (byte) Math.max(val, channelValues[connector.redstoneChannel]);
				}
			}
			if(!Arrays.equals(oldValues, channelValues))
				for(WeakReference<TileEntityConnectorRedstone> connectorRef : connectors)
				{
					TileEntityConnectorRedstone connector = connectorRef.get();
					if(connector != null)
						notifyOfChange(connector);
				}
		}

		public int getPowerOutput(int redstoneChannel)
		{
			return channelValues[redstoneChannel];
		}

		public void notifyOfChange(TileEntityConnectorRedstone tile)
		{
			tile.markContainingBlockForUpdate(null);
			tile.markBlockForUpdate(tile.getPos().offset(tile.facing), null);
		}

		public byte[] getByteValues()
		{
			byte[] values = new byte[16];
			for(int i = 0; i < values.length; i++)
				values[i] = (byte) (channelValues[i] * 16);
			return values;
		}
	}
}