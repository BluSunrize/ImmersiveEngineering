package blusunrize.immersiveengineering.common.blocks.metal;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;

import blusunrize.immersiveengineering.api.ApiUtils;
import blusunrize.immersiveengineering.api.IEProperties;
import blusunrize.immersiveengineering.api.IEProperties.PropertyBoolInverted;
import blusunrize.immersiveengineering.api.TargetingInfo;
import blusunrize.immersiveengineering.api.energy.wires.IImmersiveConnectable;
import blusunrize.immersiveengineering.api.energy.wires.ImmersiveNetHandler.Connection;
import blusunrize.immersiveengineering.api.energy.wires.TileEntityImmersiveConnectable;
import blusunrize.immersiveengineering.api.energy.wires.WireType;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IAdvancedSelectionBounds;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IDirectionalTile;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IDualState;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IHasDummyBlocks;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IMirrorAble;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IUsesBooleanProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class TileEntityTransformer extends TileEntityImmersiveConnectable implements IDirectionalTile, IMirrorAble, IHasDummyBlocks, IAdvancedSelectionBounds, IDualState
{
	WireType secondCable;
	public EnumFacing facing=EnumFacing.NORTH;
	public int dummy=0;
	public boolean onPost=false;

	public static boolean _Immovable()
	{
		return true;
	}

	@Override
	protected boolean canTakeLV()
	{
		return true;
	}
	@Override
	protected boolean canTakeMV()
	{
		return true;
	}

	@Override
	public boolean canConnect()
	{
		return true;
	}
	//	@Override
	//	public boolean canUpdate()
	//	{
	//		return false;
	//	}
	@Override
	public boolean isEnergyOutput()
	{
		return false;
	}
	@Override
	public int outputEnergy(int amount, boolean simulate, int energyType)
	{
		return 0;
	}

	@Override
	public void writeCustomNBT(NBTTagCompound nbt, boolean descPacket)
	{
		super.writeCustomNBT(nbt, descPacket);
		nbt.setInteger("facing", facing.ordinal());
		if(secondCable!=null)
			nbt.setString("secondCable", secondCable.getUniqueName());
		nbt.setInteger("dummy", dummy);
		nbt.setBoolean("postAttached", onPost);
	}
	@Override
	public void readCustomNBT(NBTTagCompound nbt, boolean descPacket)
	{
		super.readCustomNBT(nbt, descPacket);
		facing = EnumFacing.getFront(nbt.getInteger("facing"));
		if(nbt.hasKey("secondCable"))
			secondCable = ApiUtils.getWireTypeFromNBT(nbt, "secondCable");
		else
			secondCable = null;
		dummy = nbt.getInteger("dummy");
		onPost = nbt.getBoolean("postAttached");
	}

	@Override
	public BlockPos getConnectionMaster(WireType cableType, TargetingInfo target)
	{
		return getPos().add(0,-dummy,0);
	}	
	@Override
	public boolean canConnectCable(WireType cableType, TargetingInfo target)
	{
		if(cableType==WireType.STEEL&&!canTakeHV())
			return false;
		if(dummy!=0)
		{
			TileEntity master = worldObj.getTileEntity(getPos().add(0,-dummy,0));
			if(master instanceof TileEntityTransformer)
				return ((TileEntityTransformer)master).canConnectCable(cableType, target);
			return false;
		}
		int tc = getTargetedConnector(target);
		switch(tc)
		{
		case 0:
			return limitType==null && secondCable!=cableType;
		case 1:
			return secondCable==null && limitType!=cableType;
		}
		return false;
	}
	@Override
	public void connectCable(WireType cableType, TargetingInfo target)
	{
		if(dummy!=0)
		{
			TileEntity master = worldObj.getTileEntity(getPos().add(0,-dummy,0));
			if(master instanceof TileEntityTransformer)
				((TileEntityTransformer)master).connectCable(cableType, target);
			return;
		}
		switch(getTargetedConnector(target))
		{
		case 0:
			if(this.limitType==null)
				this.limitType = cableType;
			break;
		case 1:
			if(secondCable==null)
				this.secondCable = cableType;
			break;
		}
	}
	@Override
	public WireType getCableLimiter(TargetingInfo target)
	{
		switch(getTargetedConnector(target))
		{
		case 0:
			return limitType;
		case 1:
			return secondCable;
		}
		return null;
	}
	@Override
	public void removeCable(Connection connection)
	{
		WireType type = connection!=null?connection.cableType:null;
		if(type==null)
		{
			limitType=null;
			secondCable=null;
		}
		if(type==limitType)
			this.limitType = null;
		if(type==secondCable)
			this.secondCable = null;
		worldObj.markBlockForUpdate(getPos());
	}

	@Override
	public Vec3 getRaytraceOffset(IImmersiveConnectable link)
	{
		if(onPost)
			return new Vec3(.5, 1.5, .5);
		return new Vec3(.5, 2.75, .5);
	}
	@Override
	public Vec3 getConnectionOffset(Connection con)
	{
		boolean b = con.cableType==limitType;
		if(onPost)
		{
			if(b)
				return new Vec3(.5+(facing==EnumFacing.EAST?.4375: facing==EnumFacing.WEST?-.4375: 0),1.4375,.5+(facing==EnumFacing.SOUTH?.4375: facing==EnumFacing.NORTH?-.4375: 0));
			else
				return new Vec3(.5+(facing==EnumFacing.EAST?-.0625: facing==EnumFacing.WEST?.0625: 0),.25,.5+(facing==EnumFacing.SOUTH?-.0625: facing==EnumFacing.NORTH?.0625: 0));
		}
		else
		{
			double conRadius = con.cableType.getRenderDiameter()/2;
			double offset = con.cableType==WireType.COPPER?-.0625: con.cableType==WireType.ELECTRUM?.0625: .25; 
			if(facing==EnumFacing.NORTH)
				return new Vec3(b?.8125:.1875, 2.5+offset-conRadius, .5);
			if(facing==EnumFacing.SOUTH)
				return new Vec3(b?.1875:.8125, 2.5+offset-conRadius, .5);
			if(facing==EnumFacing.WEST)
				return new Vec3(.5, 2.5+offset-conRadius, b?.1875:.8125);
			if(facing==EnumFacing.EAST)
				return new Vec3(.5, 2.5+offset-conRadius, b?.8125:.1875);
		}
		return new Vec3(.5,.5,.5);
	}

	public int getTargetedConnector(TargetingInfo target)
	{
		if(onPost)
		{
			if(target.hitY>=.5)
				return 0;
			else
				return 1;
		}
		else
		{
			if(facing==EnumFacing.NORTH)
				if(target.hitX<.5)
					return 1;
				else
					return 0;
			else if(facing==EnumFacing.SOUTH)
				if(target.hitX<.5)
					return 0;
				else
					return 1;
			else if(facing==EnumFacing.WEST)
				if(target.hitZ<.5)
					return 0;
				else
					return 1;
			else if(facing==EnumFacing.EAST)
				if(target.hitZ<.5)
					return 1;
				else
					return 0;
		}
		return -1;
	}

	public WireType getLimiter(int side)
	{
		if(side==0)
			return limitType;
		return secondCable;
	}

	@SideOnly(Side.CLIENT)
	private AxisAlignedBB renderAABB;
	@SideOnly(Side.CLIENT)
	@Override
	public AxisAlignedBB getRenderBoundingBox()
	{
		//		if(renderAABB==null)
		//		{
		//			if(Config.getBoolean("increasedRenderboxes"))
		//				renderAABB = AxisAlignedBB.getBoundingBox(xCoord-16, yCoord-16, zCoord-16, xCoord+17, yCoord+17, zCoord+17);
		//			else if(!dummy)
		//				renderAABB = AxisAlignedBB.getBoundingBox(xCoord, yCoord-1, zCoord, xCoord+1, yCoord+1.5, zCoord+1);
		//			else
		//				renderAABB = AxisAlignedBB.getBoundingBox(xCoord, yCoord, zCoord, xCoord, yCoord, zCoord);
		//		}
		return AxisAlignedBB.fromBounds(getPos().getX()-16,getPos().getY()-16,getPos().getZ()-16, getPos().getX()+16,getPos().getY()+16,getPos().getZ()+16);
	}

	@Override
	public PropertyBoolInverted getBoolProperty(Class<? extends IUsesBooleanProperty> inf)
	{
		return inf==IMirrorAble.class?IEProperties.BOOLEANS[0]:inf==IDualState.class?IEProperties.BOOLEANS[1]:null;
	}
	@Override
	public boolean getIsMirrored()
	{
		if (onPost)
			return false;
		WireType lower = this instanceof TileEntityTransformerHV?WireType.ELECTRUM:WireType.COPPER;
		boolean b = (limitType!=null&&lower.equals(limitType))||(secondCable!=null&&!lower.equals(secondCable));
		return !b;
	}

	@Override
	public EnumFacing getFacing()
	{
		return facing;
	}
	@Override
	public void setFacing(EnumFacing facing)
	{
		this.facing = facing;
	}
	@Override
	public int getFacingLimitation()
	{
		return 2;
	}
	@Override
	public boolean mirrorFacingOnPlacement(EntityLivingBase placer)
	{
		return false;
	}
	@Override
	public boolean canHammerRotate(EnumFacing side, float hitX, float hitY, float hitZ, EntityLivingBase entity)
	{
		return false;
	}

	@Override
	public boolean isDummy()
	{
		return dummy!=0;
	}
	public boolean isOnPost()
	{
		return onPost;
	}
	@Override
	public void placeDummies(BlockPos pos, IBlockState state, EnumFacing side, float hitX, float hitY, float hitZ)
	{
		if (state.getValue(IEProperties.BOOLEANS[1]))
		{
			onPost = true;
			markDirty();
			worldObj.markBlockForUpdate(pos);
		}
		else
			for(int i=1; i<=2; i++)
			{
				worldObj.setBlockState(pos.add(0,i,0), state);
				((TileEntityTransformer)worldObj.getTileEntity(pos.add(0,i,0))).dummy = i;
				((TileEntityTransformer)worldObj.getTileEntity(pos.add(0,i,0))).facing = this.facing;
			}
	}
	@Override
	public void breakDummies(BlockPos pos, IBlockState state)
	{
		if (onPost)
			return;
		for(int i=0; i<=2; i++)
			worldObj.setBlockToAir(getPos().add(0,-dummy,0).add(0,i,0));
	}

	@Override
	public float[] getBlockBounds()
	{
		if(dummy==2)
			return new float[]{facing.getAxis()==Axis.Z?0:.3125f, 0, facing.getAxis()==Axis.X?0:.3125f, facing.getAxis()==Axis.Z?1:.6875f,this instanceof TileEntityTransformerHV?.75f:.5625f,facing.getAxis()==Axis.X?1:.6875f};
		if (onPost)
			return new float[]{facing.getAxis()==Axis.Z?.25F:facing==EnumFacing.WEST?-.375F:.6875F,
					0,
					facing.getAxis()==Axis.X?.25F:facing==EnumFacing.NORTH?-.375F:.6875F,
					facing.getAxis()==Axis.Z?.75F:facing==EnumFacing.EAST?1.375F:.3125F,
					1,
					facing.getAxis()==Axis.X?.75F:facing==EnumFacing.SOUTH?1.375F:.3125F};
		return null;
	}

	@Override
	public float[] getSpecialCollisionBounds()
	{
		return null;
	}
	@Override
	public float[] getSpecialSelectionBounds()
	{
		return null;
	}

	@Override
	public List<AxisAlignedBB> getAdvancedSelectionBounds()
	{
		if(dummy==2)
		{
			WireType lower = this instanceof TileEntityTransformerHV?WireType.ELECTRUM:WireType.COPPER;
			WireType higher = this instanceof TileEntityTransformerHV?WireType.STEEL:WireType.ELECTRUM;
			boolean b = (limitType!=null&&lower.equals(limitType))||(secondCable!=null&&!lower.equals(secondCable));
			double offsetL = lower==WireType.COPPER?0: lower==WireType.ELECTRUM?.0625: .25; 
			double offsetH = higher==WireType.COPPER?0: higher==WireType.ELECTRUM?.0625: .25; 
			if(facing==EnumFacing.NORTH)
				return Lists.newArrayList(new AxisAlignedBB(0,0,.3125, .375,.5+(b?offsetH:offsetL),.6875).offset(getPos().getX(),getPos().getY(),getPos().getZ()), new AxisAlignedBB(.625,0,.3125, 1,.5+(b?offsetL:offsetH),.6875).offset(getPos().getX(),getPos().getY(),getPos().getZ()));
			if(facing==EnumFacing.SOUTH)
				return Lists.newArrayList(new AxisAlignedBB(0,0,.3125, .375,.5+(b?offsetL:offsetH),.6875).offset(getPos().getX(),getPos().getY(),getPos().getZ()), new AxisAlignedBB(.625,0,.3125, 1,.5+(b?offsetH:offsetL),.6875).offset(getPos().getX(),getPos().getY(),getPos().getZ()));
			if(facing==EnumFacing.WEST)
				return Lists.newArrayList(new AxisAlignedBB(.3125,0,0, .6875,.5+(b?offsetL:offsetH),.375).offset(getPos().getX(),getPos().getY(),getPos().getZ()), new AxisAlignedBB(.3125,0,.625, .6875,.5+(b?offsetH:offsetL),1).offset(getPos().getX(),getPos().getY(),getPos().getZ()));
			if(facing==EnumFacing.EAST)
				return Lists.newArrayList(new AxisAlignedBB(.3125,0,0, .6875,.5+(b?offsetH:offsetL),.375).offset(getPos().getX(),getPos().getY(),getPos().getZ()), new AxisAlignedBB(.3125,0,.625, .6875,.5+(b?offsetL:offsetH),1).offset(getPos().getX(),getPos().getY(),getPos().getZ()));
		}
		return null;
	}

	@Override
	public boolean isOverrideBox(AxisAlignedBB box, EntityPlayer player, MovingObjectPosition mop, ArrayList<AxisAlignedBB> list)
	{
		if(box.expand(.002,.002,.002).isVecInside(mop.hitVec))
			return true;	
		return false;
	}
	@Override
	public Set<BlockPos> getIgnored(IImmersiveConnectable other)
	{
		if (onPost)
			return super.getIgnored(other);
		return ImmutableSet.of(pos.up(), pos.up(2));
	}

	@Override
	public boolean getIsSecondState()
	{
		return onPost;
	}
}