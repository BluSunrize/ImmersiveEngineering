package blusunrize.immersiveengineering.common.blocks.metal;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.google.common.collect.ArrayListMultimap;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.api.energy.immersiveflux.FluxStorage;
import blusunrize.immersiveengineering.api.energy.immersiveflux.IFluxReceiver;
import blusunrize.immersiveengineering.common.Config;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IBlockBounds;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IHammerInteraction;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IHasDummyBlocks;
import blusunrize.immersiveengineering.common.blocks.TileEntityIEBase;
import blusunrize.immersiveengineering.common.util.ChatUtils;
import blusunrize.immersiveengineering.common.util.IEDamageSources;
import blusunrize.immersiveengineering.common.util.network.MessageTileSync;
import cofh.api.energy.IEnergyReceiver;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.ITickable;
import net.minecraft.util.Vec3;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class TileEntityTeslaCoil extends TileEntityIEBase implements ITickable, IFluxReceiver,IEnergyReceiver, IHasDummyBlocks, IBlockBounds, IHammerInteraction
{
	public boolean dummy = false;
	public FluxStorage energyStorage = new FluxStorage(48000);
	public boolean redstoneControlInverted = false;
	@SideOnly(Side.CLIENT)
	public static ArrayListMultimap<BlockPos,LightningAnimation> effectMap = ArrayListMultimap.create();

	@Override
	public void update()
	{
		if(dummy)
			return;
		int timeKey = getPos().getX()^getPos().getZ();
		int energyDrain = Config.getInt("teslacoil_consumption");
		if(!dummy && worldObj.getTotalWorldTime()%32==(timeKey&31) && (worldObj.isBlockIndirectlyGettingPowered(getPos())>0^redstoneControlInverted) && energyStorage.getEnergyStored()>=energyDrain)
		{
			if(!worldObj.isRemote)
				this.energyStorage.extractEnergy(energyDrain,false);

			double radius = 6;
			AxisAlignedBB aabb = AxisAlignedBB.fromBounds(getPos().getX()-radius,getPos().getY()-radius,getPos().getZ()-radius, getPos().getX()+radius,getPos().getY()+radius,getPos().getZ()+radius);
			List<EntityLivingBase> targets = worldObj.getEntitiesWithinAABB(EntityLivingBase.class, aabb);
			if(!targets.isEmpty())
			{
				DamageSource dmgsrc = IEDamageSources.causeTeslaDamage();
				int randomTarget = worldObj.rand.nextInt(targets.size());
				EntityLivingBase target = targets.get(randomTarget);
				if(target!=null)
				{
					if(!worldObj.isRemote)
					{
						energyDrain = Config.getInt("teslacoil_consumption_active");
						if(energyStorage.extractEnergy(energyDrain,true)==energyDrain)
						{
							energyStorage.extractEnergy(energyDrain,false);
							int prevFire = target.fire;
							target.fire = 1;
							target.attackEntityFrom(dmgsrc, (float)Config.getDouble("teslacoil_damage"));
							target.fire = prevFire;
							NBTTagCompound tag = new NBTTagCompound();
							tag.setInteger("targetEntity", target.getEntityId());
							ImmersiveEngineering.packetHandler.sendToAll(new MessageTileSync(this, tag));
						}
					}
				}
			}
			else if(worldObj.isRemote && worldObj.getTotalWorldTime()%128==(timeKey&127))
			{
				Vec3 coilPos = new Vec3(getPos()).addVector(.5,1.5+worldObj.rand.nextDouble()*.25,.5);
				//target up to 4 blocks away
				double tx = (worldObj.rand.nextDouble()-.5)*8;
				double tz = (worldObj.rand.nextDouble()-.5)*8;
				//Minimal distance to the coil is 2 blocks
				tx += tx<0?-2:2;
				tz += tz<0?-2:2;

				int blockY = getPos().getY();
				BlockPos targetBlock = new BlockPos(getPos().getX()+tx,blockY,getPos().getZ()+tz);
				double ty = blockY;
				boolean targetFound = false;
				if(!worldObj.isAirBlock(targetBlock))
				{
					IBlockState state = worldObj.getBlockState(targetBlock);
					state.getBlock().setBlockBoundsBasedOnState(worldObj, targetBlock);
					ty = (blockY-getPos().getY())+state.getBlock().getBlockBoundsMaxY();
					targetFound = true;
				}
				else
				{
					boolean topFirst = worldObj.rand.nextBoolean();
					for(int i=0; i<2; i++)
					{
						for(int yy=0;yy<=6;yy++)
						{
							BlockPos targetBlock2 = targetBlock.add(0,topFirst?yy:-yy,0);
							if(!worldObj.isAirBlock(targetBlock2))
							{
								IBlockState state = worldObj.getBlockState(targetBlock2);
								state.getBlock().setBlockBoundsBasedOnState(worldObj, targetBlock2);
								ty = (targetBlock2.getY()-getPos().getY())+ (topFirst?state.getBlock().getBlockBoundsMinY():state.getBlock().getBlockBoundsMaxY());
								targetFound = true;
								break;
							}
						}
						if(targetFound)
							break;
						topFirst = !topFirst;
					}
				}
				
				if(targetFound)
				{
					EnumFacing f;
					if(Math.abs(tz)>Math.abs(tx))
						f = tz<0?EnumFacing.NORTH:EnumFacing.SOUTH;
					else
						f = tx<0?EnumFacing.WEST:EnumFacing.EAST;
					coilPos = coilPos.addVector(f.getAxis()==Axis.X?f.getFrontOffsetX()*.375:((worldObj.rand.nextDouble()-.5)*.75), 0, f.getAxis()==Axis.Z?f.getFrontOffsetZ()*.375:((worldObj.rand.nextDouble()-.5)*.75));
					effectMap.put(getPos(), new LightningAnimation(coilPos, new Vec3(getPos()).addVector(tx,ty,tz)));
					worldObj.playSound(getPos().getX(),getPos().getY(),getPos().getZ(), "immersiveengineering:tesla", 2.5F,0.5F+worldObj.rand.nextFloat(), true);
				}
			}
		}
	}

	@Override
	public void receiveMessageFromServer(NBTTagCompound message)
	{
		if(message.hasKey("targetEntity"))
		{
			Entity target = worldObj.getEntityByID(message.getInteger("targetEntity"));
			if(target instanceof EntityLivingBase)
			{
				double dx = target.posX-getPos().getX();
				double dz = target.posZ-getPos().getZ();
				EnumFacing f;
				if(Math.abs(dz)>Math.abs(dx))
					f = dz<0?EnumFacing.NORTH:EnumFacing.SOUTH;
				else
					f = dx<0?EnumFacing.WEST:EnumFacing.EAST;
				Vec3 coilPos = new Vec3(getPos()).addVector(.5,1.5+worldObj.rand.nextDouble()*.25,.5);
				coilPos = coilPos.addVector(f.getAxis()==Axis.X?f.getFrontOffsetX()*.375:((worldObj.rand.nextDouble()-.5)*.75), 0, f.getAxis()==Axis.Z?f.getFrontOffsetZ()*.375:((worldObj.rand.nextDouble()-.5)*.75));
				effectMap.put(getPos(), new LightningAnimation(coilPos,(EntityLivingBase)target));
				worldObj.playSoundEffect(getPos().getX()+.5,getPos().getY()+1.5,getPos().getZ()+.5, "immersiveengineering:tesla", 2.5F,0.5F+worldObj.rand.nextFloat());
			}
		}
	}

	@Override
	public void readCustomNBT(NBTTagCompound nbt, boolean descPacket)
	{
		dummy = nbt.getBoolean("dummy");
		redstoneControlInverted = nbt.getBoolean("redstoneInverted");
		energyStorage.readFromNBT(nbt);
	}

	@Override
	public void writeCustomNBT(NBTTagCompound nbt, boolean descPacket)
	{
		nbt.setBoolean("dummy", dummy);
		nbt.setBoolean("redstoneInverted", redstoneControlInverted);
		energyStorage.writeToNBT(nbt);
	}

	@Override
	public float[] getBlockBounds()
	{
		return dummy?new float[]{.125f,0,.125f, .875f,.875f,.875f}:null;
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

	AxisAlignedBB renderBB;
	@Override
	@SideOnly(Side.CLIENT)
	public net.minecraft.util.AxisAlignedBB getRenderBoundingBox()
	{
		if(renderBB==null)
			renderBB = new AxisAlignedBB(getPos().add(-8,-8,-8),getPos().add(8,8,8));
		return renderBB;
	}

	@Override
	public boolean hammerUseSide(EnumFacing side, EntityPlayer player, float hitX, float hitY, float hitZ)
	{
		if(dummy)
		{
			TileEntity te = worldObj.getTileEntity(getPos().add(0,-1,0));
			if(te instanceof TileEntityTeslaCoil)
				return ((TileEntityTeslaCoil)te).hammerUseSide(side, player, hitX, hitY, hitZ);
			return false;
		}
		redstoneControlInverted = !redstoneControlInverted;
		ChatUtils.sendServerNoSpamMessages(player, new ChatComponentTranslation(Lib.CHAT_INFO+"rsControl."+(redstoneControlInverted?"invertedOn":"invertedOff")));
		markDirty();
		worldObj.markBlockForUpdate(getPos());
		return true;
	}

	@Override
	public boolean isDummy()
	{
		return dummy;
	}
	@Override
	public void placeDummies(BlockPos pos, IBlockState state, EnumFacing side, float hitX, float hitY, float hitZ)
	{
		worldObj.setBlockState(pos.add(0,1,0), state);
		((TileEntityTeslaCoil)worldObj.getTileEntity(pos.add(0,1,0))).dummy = true;
	}
	@Override
	public void breakDummies(BlockPos pos, IBlockState state)
	{
		for(int i=0; i<=1; i++)
			if(worldObj.getTileEntity(getPos().add(0,dummy?-1:0,0).add(0,i,0)) instanceof TileEntityTeslaCoil)
				worldObj.setBlockToAir(getPos().add(0,dummy?-1:0,0).add(0,i,0));
	}

	@Override
	public boolean canConnectEnergy(EnumFacing from)
	{
		return !dummy;
	}
	@Override
	public int receiveEnergy(EnumFacing from, int energy, boolean simulate)
	{
		return energyStorage.receiveEnergy(energy, simulate);
	}
	@Override
	public int getEnergyStored(EnumFacing from)
	{
		if(dummy)
		{
			TileEntity te = worldObj.getTileEntity(getPos().add(0,-1,0));
			if(te instanceof TileEntityTeslaCoil)	
				return ((TileEntityTeslaCoil)te).getEnergyStored(from);
			return 0;
		}
		return energyStorage.getEnergyStored();
	}
	@Override
	public int getMaxEnergyStored(EnumFacing from)
	{
		if(dummy)
		{
			TileEntity te = worldObj.getTileEntity(getPos().add(0,-1,0));
			if(te instanceof TileEntityTeslaCoil)	
				return ((TileEntityTeslaCoil)te).getMaxEnergyStored(from);
			return 0;
		}
		return energyStorage.getMaxEnergyStored();
	}

	public static class LightningAnimation
	{
		public Vec3 startPos;
		public EntityLivingBase targetEntity;
		public Vec3 targetPos;
		public int timer = 40;

		public List<Vec3> subPoints = new ArrayList();
		private Vec3 prevTarget;

		public LightningAnimation(Vec3 startPos, EntityLivingBase targetEntity)
		{
			this.startPos = startPos;
			this.targetEntity = targetEntity;
		}
		public LightningAnimation(Vec3 startPos, Vec3 targetPos)
		{
			this.startPos = startPos;
			this.targetPos = targetPos;
		}

		public boolean shoudlRecalculateLightning()
		{
			if(subPoints.isEmpty()||timer%8==0)
				return true;
			boolean b = false;
			Vec3 end = targetEntity!=null?targetEntity.getPositionVector():targetPos;
			if(prevTarget!=null)
				b = prevTarget.distanceTo(end)>1;
				prevTarget = end;
				return b;
		}

		public void createLightning(Random rand)
		{
			subPoints.clear();
			Vec3 end = targetEntity!=null?targetEntity.getPositionVector():targetPos;
			Vec3 dist = end.subtract(startPos);
			double points = 12;
			for(int i=0; i<points; i++)
			{
				Vec3 sub = startPos.addVector(dist.xCoord/points*i, dist.yCoord/points*i, dist.zCoord/points*i);
				//distance to the middle point and by that, distance from the start and end. -1 is start, 1 is end
				double fixPointDist=  (i-points/2)/(points/2);
				//Randomization modifier, closer to start/end means smaller divergence
				double mod = 1-.75*Math.abs(fixPointDist);
				double offX = (rand.nextDouble()-.5)*mod;
				double offY = (rand.nextDouble()-.5)*mod;
				double offZ = (rand.nextDouble()-.5)*mod;
				if(fixPointDist<0)
				{
					offY+=.75*mod*(.75+fixPointDist);//Closer to the coil should arc upwards
					offX = (sub.xCoord-startPos.xCoord)<0?-Math.abs(offX):Math.abs(offX);
					offZ = (sub.zCoord-startPos.zCoord)<0?-Math.abs(offZ):Math.abs(offZ);
				}
				else 
				{
					offY = Math.min(end.yCoord+1*(1-fixPointDist)*-Math.signum(dist.yCoord), offY);//final points should be higher/lower than end, depending on if lightning goes up or down
					offX = Math.abs(offX)*(end.xCoord-sub.xCoord);
					offZ = Math.abs(offZ)*(end.zCoord-sub.zCoord);
				}
				//				fixPointDist
				//				sub = ;
				subPoints.add(sub.addVector(offX,offY,offZ));
			}
		}

	}
}