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
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IDirectionalTile;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IHammerInteraction;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IHasDummyBlocks;
import blusunrize.immersiveengineering.common.blocks.TileEntityIEBase;
import blusunrize.immersiveengineering.common.util.ChatUtils;
import blusunrize.immersiveengineering.common.util.IEDamageSources;
import blusunrize.immersiveengineering.common.util.IEPotions;
import blusunrize.immersiveengineering.common.util.network.MessageTileSync;
import cofh.api.energy.IEnergyReceiver;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.PotionEffect;
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

public class TileEntityTeslaCoil extends TileEntityIEBase implements ITickable, IFluxReceiver,IEnergyReceiver, IHasDummyBlocks, IDirectionalTile, IBlockBounds, IHammerInteraction
{
	public boolean dummy = false;
	public FluxStorage energyStorage = new FluxStorage(48000);
	public boolean redstoneControlInverted = false;
	public EnumFacing facing = EnumFacing.UP;
	@SideOnly(Side.CLIENT)
	public static ArrayListMultimap<BlockPos,LightningAnimation> effectMap = ArrayListMultimap.create();

	@Override
	public void update()
	{
		if(dummy)
			return;
		int timeKey = getPos().getX()^getPos().getZ();
		int energyDrain = Config.getInt("teslacoil_consumption");
		if(worldObj.getTotalWorldTime()%32==(timeKey&31) && (worldObj.isBlockIndirectlyGettingPowered(getPos())>0^redstoneControlInverted) && energyStorage.getEnergyStored()>=energyDrain)
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
							target.addPotionEffect(new PotionEffect(IEPotions.stunned.getId(),128));
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
				//target up to 4 blocks away
				double tV = (worldObj.rand.nextDouble()-.5)*8;
				double tH = (worldObj.rand.nextDouble()-.5)*8;
				//Minimal distance to the coil is 2 blocks
				tV += tV<0?-2:2;
				tH += tH<0?-2:2;

				BlockPos targetBlock = getPos().add(facing.getAxis()==Axis.X?0:tH, facing.getAxis()==Axis.Y?0:tV, facing.getAxis()==Axis.Y?tV:facing.getAxis()==Axis.X?tH:0);
				double tL=0;
				boolean targetFound = false;
				if(!worldObj.isAirBlock(targetBlock))
				{
					IBlockState state = worldObj.getBlockState(targetBlock);
					state.getBlock().setBlockBoundsBasedOnState(worldObj, targetBlock);
					//					ty = (blockY-getPos().getY())+state.getBlock().getBlockBoundsMaxY();
					if(facing==EnumFacing.UP)
						tL = targetBlock.getY()-getPos().getY() + state.getBlock().getBlockBoundsMaxY();
					else if(facing==EnumFacing.DOWN)
						tL = targetBlock.getY()-getPos().getY() + state.getBlock().getBlockBoundsMinY();
					else if(facing==EnumFacing.NORTH)
						tL = targetBlock.getZ()-getPos().getZ() + state.getBlock().getBlockBoundsMinZ();
					else if(facing==EnumFacing.SOUTH)
						tL = targetBlock.getZ()-getPos().getZ() + state.getBlock().getBlockBoundsMaxZ();
					else if(facing==EnumFacing.WEST)
						tL = targetBlock.getX()-getPos().getX() + state.getBlock().getBlockBoundsMinX();
					else
						tL = targetBlock.getX()-getPos().getX() + state.getBlock().getBlockBoundsMaxX();
					targetFound = true;
				}
				else
				{
					boolean positiveFirst = worldObj.rand.nextBoolean();
					for(int i=0; i<2; i++)
					{
						for(int ll=0;ll<=6;ll++)
						{
							BlockPos targetBlock2 = targetBlock.offset(positiveFirst?facing:facing.getOpposite(), ll);
							if(!worldObj.isAirBlock(targetBlock2))
							{
								IBlockState state = worldObj.getBlockState(targetBlock2);
								state.getBlock().setBlockBoundsBasedOnState(worldObj, targetBlock2);
								tL = facing.getAxis()==Axis.Y?(targetBlock2.getY()-getPos().getY()): facing.getAxis()==Axis.Z?(targetBlock2.getZ()-getPos().getZ()): (targetBlock2.getZ()-getPos().getZ());
								EnumFacing tempF = positiveFirst?facing:facing.getOpposite();
								if(tempF==EnumFacing.UP)
									tL += state.getBlock().getBlockBoundsMaxY();
								else if(tempF==EnumFacing.DOWN)
									tL += state.getBlock().getBlockBoundsMinY();
								else if(tempF==EnumFacing.NORTH)
									tL += state.getBlock().getBlockBoundsMinZ();
								else if(tempF==EnumFacing.SOUTH)
									tL += state.getBlock().getBlockBoundsMaxZ();
								else if(tempF==EnumFacing.WEST)
									tL += state.getBlock().getBlockBoundsMinX();
								else
									tL += state.getBlock().getBlockBoundsMaxX();
								targetFound = true;
								break;
							}
						}
						if(targetFound)
							break;
						positiveFirst = !positiveFirst;
					}
				}

				if(targetFound)
				{
					double tx = facing.getAxis()==Axis.X?tL:tH;
					double ty = facing.getAxis()==Axis.Y?tL:tV;
					double tz = facing.getAxis()==Axis.Y?tV:facing.getAxis()==Axis.X?tH:tL;

					EnumFacing f = null;
					if(facing.getAxis()==Axis.Y)
					{
						if(Math.abs(tz)>Math.abs(tx))
							f = tz<0?EnumFacing.NORTH:EnumFacing.SOUTH;
						else 
							f = tx<0?EnumFacing.WEST:EnumFacing.EAST;
					}
					else if(facing.getAxis()==Axis.Z)
					{
						if(Math.abs(ty)>Math.abs(tx))
							f = ty<0?EnumFacing.DOWN:EnumFacing.UP;
						else 
							f = tx<0?EnumFacing.WEST:EnumFacing.EAST;
					}
					else
					{
						if(Math.abs(ty)>Math.abs(tz))
							f = ty<0?EnumFacing.DOWN:EnumFacing.UP;
						else 
							f = tz<0?EnumFacing.NORTH:EnumFacing.SOUTH;
					}

					double verticalOffset = 1+worldObj.rand.nextDouble()*.25;
					Vec3 coilPos = new Vec3(getPos()).addVector(.5,.5,.5);
					//Vertical offset
					coilPos = coilPos.addVector(facing.getFrontOffsetX()*verticalOffset, facing.getFrontOffsetY()*verticalOffset, facing.getFrontOffsetZ()*verticalOffset);
					//offset to direction
					if(f!=null)
					{
						coilPos = coilPos.addVector(f.getFrontOffsetX()*.375, f.getFrontOffsetY()*.375, f.getFrontOffsetZ()*.375);
						//random side offset
						f = f.rotateAround(facing.getAxis());
						double dShift = (worldObj.rand.nextDouble()-.5)*.75;
						coilPos = coilPos.addVector(f.getFrontOffsetX()*dShift, f.getFrontOffsetY()*dShift, f.getFrontOffsetZ()*dShift);
					}
					effectMap.put(getPos(), new LightningAnimation(coilPos, new Vec3(getPos()).addVector(tx,ty,tz)));
					worldObj.playSound(coilPos.xCoord,coilPos.yCoord,coilPos.zCoord, "immersiveengineering:tesla", 2.5F,0.5F+worldObj.rand.nextFloat(), true);
				}
			}
			this.markDirty();
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
				double dy = target.posY-getPos().getY();
				double dz = target.posZ-getPos().getZ();

				EnumFacing f = null;
				if(facing.getAxis()==Axis.Y)
				{
					if(Math.abs(dz)>Math.abs(dx))
						f = dz<0?EnumFacing.NORTH:EnumFacing.SOUTH;
					else 
						f = dx<0?EnumFacing.WEST:EnumFacing.EAST;
				}
				else if(facing.getAxis()==Axis.Z)
				{
					if(Math.abs(dy)>Math.abs(dx))
						f = dy<0?EnumFacing.DOWN:EnumFacing.UP;
					else 
						f = dx<0?EnumFacing.WEST:EnumFacing.EAST;
				}
				else
				{
					if(Math.abs(dy)>Math.abs(dz))
						f = dy<0?EnumFacing.DOWN:EnumFacing.UP;
					else 
						f = dz<0?EnumFacing.NORTH:EnumFacing.SOUTH;
				}
				double verticalOffset = 1+worldObj.rand.nextDouble()*.25;
				Vec3 coilPos = new Vec3(getPos()).addVector(.5,.5,.5);
				//Vertical offset
				coilPos = coilPos.addVector(facing.getFrontOffsetX()*verticalOffset, facing.getFrontOffsetY()*verticalOffset, facing.getFrontOffsetZ()*verticalOffset);
				//offset to direction
				if(f!=null)
				{
					coilPos = coilPos.addVector(f.getFrontOffsetX()*.375, f.getFrontOffsetY()*.375, f.getFrontOffsetZ()*.375);
					//random side offset
					f = f.rotateAround(facing.getAxis());
					double dShift = (worldObj.rand.nextDouble()-.5)*.75;
					coilPos = coilPos.addVector(f.getFrontOffsetX()*dShift, f.getFrontOffsetY()*dShift, f.getFrontOffsetZ()*dShift);
				}

				effectMap.put(getPos(), new LightningAnimation(coilPos,(EntityLivingBase)target));
				worldObj.playSoundEffect(coilPos.xCoord,coilPos.yCoord,coilPos.zCoord, "immersiveengineering:tesla", 2.5F,0.5F+worldObj.rand.nextFloat());
			}
		}
	}

	@Override
	public void readCustomNBT(NBTTagCompound nbt, boolean descPacket)
	{
		dummy = nbt.getBoolean("dummy");
		redstoneControlInverted = nbt.getBoolean("redstoneInverted");
		facing = EnumFacing.getFront(nbt.getInteger("facing"));
		energyStorage.readFromNBT(nbt);
	}

	@Override
	public void writeCustomNBT(NBTTagCompound nbt, boolean descPacket)
	{
		nbt.setBoolean("dummy", dummy);
		nbt.setBoolean("redstoneInverted", redstoneControlInverted);
		if(facing!=null)
			nbt.setInteger("facing", facing.ordinal());
		energyStorage.writeToNBT(nbt);
	}

	@Override
	public float[] getBlockBounds()
	{
		if(!dummy)
			return null;
		switch(facing)
		{
		case DOWN:
			return new float[]{.125f,.125f,.125f, .875f,1,.875f};
		case UP:
			return new float[]{.125f,0,.125f, .875f,.875f,.875f};
		case NORTH:
			return new float[]{.125f,.125f,.125f, .875f,.875f,1};
		case SOUTH:
			return new float[]{.125f,.125f,0, .875f,.875f,.875f};
		case WEST:
			return new float[]{.125f,.125f,.125f, 1,.875f,.875f};
		case EAST:
			return new float[]{0,.125f,.125f, .875f,.875f,.875f};
		}
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
			TileEntity te = worldObj.getTileEntity(getPos().offset(facing,-1));
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
		return 0;
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
		return dummy;
	}
	@Override
	public void placeDummies(BlockPos pos, IBlockState state, EnumFacing side, float hitX, float hitY, float hitZ)
	{
		worldObj.setBlockState(pos.offset(facing), state);
		((TileEntityTeslaCoil)worldObj.getTileEntity(pos.offset(facing))).dummy = true;
		((TileEntityTeslaCoil)worldObj.getTileEntity(pos.offset(facing))).facing = facing;
	}
	@Override
	public void breakDummies(BlockPos pos, IBlockState state)
	{
		for(int i=0; i<=1; i++)
			if(worldObj.getTileEntity(getPos().offset(facing, dummy?-1:0).offset(facing, i)) instanceof TileEntityTeslaCoil)
				worldObj.setBlockToAir(getPos().offset(facing, dummy?-1:0).offset(facing, i));
	}

	@Override
	public boolean canConnectEnergy(EnumFacing from)
	{
		return !dummy;
	}
	@Override
	public int receiveEnergy(EnumFacing from, int energy, boolean simulate)
	{
		if(dummy)
			return 0;

		int rec = energyStorage.receiveEnergy(energy, simulate);
		markDirty();
		if(rec>0)
			worldObj.markBlockForUpdate(getPos());
		return rec;
	}
	@Override
	public int getEnergyStored(EnumFacing from)
	{
		if(dummy)
		{
			TileEntity te = worldObj.getTileEntity(getPos().offset(facing,-1));
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
			TileEntity te = worldObj.getTileEntity(getPos().offset(facing,-1));
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
				subPoints.add(sub.addVector(offX,offY,offZ));
			}
		}

	}
}