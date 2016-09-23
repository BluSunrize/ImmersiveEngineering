package blusunrize.immersiveengineering.common.blocks.metal;

import blusunrize.immersiveengineering.api.AdvancedAABB;
import blusunrize.immersiveengineering.api.fluid.IFluidPipe;
import blusunrize.immersiveengineering.client.models.IOBJModelCallback;
import blusunrize.immersiveengineering.common.IEContent;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.*;
import blusunrize.immersiveengineering.common.blocks.TileEntityIEBase;
import blusunrize.immersiveengineering.common.blocks.wooden.BlockTypes_WoodenDecoration;
import blusunrize.immersiveengineering.common.util.Utils;
import blusunrize.immersiveengineering.common.util.chickenbones.Matrix4;
import com.google.common.base.Function;
import com.google.common.collect.Lists;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.client.model.obj.OBJModel.OBJState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.model.TRSRTransformation;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.FluidTankProperties;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.oredict.OreDictionary;

import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static java.util.Collections.newSetFromMap;

public class TileEntityFluidPipe extends TileEntityIEBase implements IFluidPipe, IAdvancedHasObjProperty, IOBJModelCallback<IBlockState>, IColouredTile, IPlayerInteraction, IHammerInteraction, IAdvancedSelectionBounds, IAdvancedCollisionBounds
{
	static ConcurrentHashMap<BlockPos, Set<DirectionalFluidOutput>> indirectConnections = new ConcurrentHashMap<BlockPos, Set<DirectionalFluidOutput>>();
	public static ArrayList<Function<ItemStack, Boolean>> validPipeCovers = new ArrayList();
	public static ArrayList<Function<ItemStack, Boolean>> climbablePipeCovers = new ArrayList();
	static{
		final ArrayList<ItemStack> scaffolds = Lists.newArrayList(
				new ItemStack(IEContent.blockWoodenDecoration, 1, BlockTypes_WoodenDecoration.SCAFFOLDING.getMeta()),
				new ItemStack(IEContent.blockMetalDecoration1, 1, BlockTypes_MetalDecoration1.STEEL_SCAFFOLDING_0.getMeta()),
				new ItemStack(IEContent.blockMetalDecoration1, 1, BlockTypes_MetalDecoration1.STEEL_SCAFFOLDING_1.getMeta()),
				new ItemStack(IEContent.blockMetalDecoration1, 1, BlockTypes_MetalDecoration1.STEEL_SCAFFOLDING_2.getMeta()),
				new ItemStack(IEContent.blockMetalDecoration1, 1, BlockTypes_MetalDecoration1.ALUMINUM_SCAFFOLDING_0.getMeta()),
				new ItemStack(IEContent.blockMetalDecoration1, 1, BlockTypes_MetalDecoration1.ALUMINUM_SCAFFOLDING_1.getMeta()),
				new ItemStack(IEContent.blockMetalDecoration1, 1, BlockTypes_MetalDecoration1.ALUMINUM_SCAFFOLDING_2.getMeta()));
		TileEntityFluidPipe.validPipeCovers.add(new Function<ItemStack, Boolean>()
		{
			@Nullable
			@Override
			public Boolean apply(@Nullable ItemStack input)
			{
				if(input == null)
					return Boolean.FALSE;
				for(ItemStack stack : scaffolds)
					if(OreDictionary.itemMatches(stack, input, false))
						return Boolean.TRUE;
				return Boolean.FALSE;
			}
		});
		TileEntityFluidPipe.climbablePipeCovers.add(new Function<ItemStack, Boolean>()
		{
			@Nullable
			@Override
			public Boolean apply(@Nullable ItemStack input)
			{
				if(input == null)
					return Boolean.FALSE;
				for(ItemStack stack : scaffolds)
					if(OreDictionary.itemMatches(stack, input, false))
						return Boolean.TRUE;
				return Boolean.FALSE;
			}
		});
	}

	public int[] sideConfig = new int[] {0,0,0,0,0,0};
	public ItemStack pipeCover = null;

	public static Set<DirectionalFluidOutput> getConnectedFluidHandlers(BlockPos node, World world)
	{
		if(indirectConnections.containsKey(node))
			return indirectConnections.get(node);

		ArrayList<BlockPos> openList = new ArrayList();
		ArrayList<BlockPos> closedList = new ArrayList();
		Set<DirectionalFluidOutput> fluidHandlers = Collections.newSetFromMap(new ConcurrentHashMap<DirectionalFluidOutput, Boolean>());
		openList.add(node);
		while(!openList.isEmpty() && closedList.size()<1024)
		{
			BlockPos next = openList.get(0);
			if(world.isBlockLoaded(next))
			{
				TileEntity pipeTile = world.getTileEntity(next);
				if(!closedList.contains(next) && (pipeTile instanceof IFluidPipe))
				{
					if(pipeTile instanceof TileEntityFluidPipe)
						closedList.add(next);
					IFluidTankProperties[] tankInfo;
					for(int i=0; i<6; i++)
					{
						//						boolean b = (te instanceof TileEntityFluidPipe)? (((TileEntityFluidPipe) te).sideConfig[i]==0): (((TileEntityFluidPump) te).sideConfig[i]==1);
						EnumFacing fd = EnumFacing.getFront(i);
						if(((IFluidPipe)pipeTile).hasOutputConnection(fd))
						{
							BlockPos nextPos = next.offset(fd);
							if(world.isBlockLoaded(nextPos))
							{
								TileEntity adjacentTile = world.getTileEntity(nextPos);
								if(adjacentTile!=null)
									if(adjacentTile instanceof TileEntityFluidPipe)
										openList.add(nextPos);
									else if(adjacentTile.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, fd.getOpposite()))
									{
										IFluidHandler handler = adjacentTile.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, fd.getOpposite());
										tankInfo = handler.getTankProperties();
										if(tankInfo != null && tankInfo.length > 0)
											fluidHandlers.add(new DirectionalFluidOutput(handler, adjacentTile, fd));
									}
							}
						}
					}
				}
			}
			openList.remove(0);
		}
		if(FMLCommonHandler.instance().getEffectiveSide() == Side.SERVER)
		{
			if(!indirectConnections.containsKey(node))
			{
				indirectConnections.put(node, newSetFromMap(new ConcurrentHashMap<DirectionalFluidOutput, Boolean>()));
				indirectConnections.get(node).addAll(fluidHandlers);
			}
		}
		return fluidHandlers;
	}

	@Override
	public void invalidate()
	{
		super.invalidate();
		if (!worldObj.isRemote)
			indirectConnections.clear();
	}


	@Override
	public void onEntityCollision(World world, Entity entity)
	{
		if(!(entity instanceof EntityLivingBase) || ((EntityLivingBase)entity).isOnLadder() || pipeCover==null)
			return;
		else
		{
//			boolean climb = false;
//			for(Function<ItemStack,Boolean> f : climbablePipeCovers)
//				if(f!=null && f.apply(pipeCover)==Boolean.TRUE)
//				{
//					climb = true;
//					break;
//				}
//			if(!climb)
//				return;
			float f5 = 0.15F;
			if(entity.motionX<-f5)
				entity.motionX=-f5;
			if(entity.motionX>f5)
				entity.motionX=f5;
			if(entity.motionZ<-f5)
				entity.motionZ=-f5;
			if(entity.motionZ>f5)
				entity.motionZ=f5;

			entity.fallDistance=0f;
			if(entity.motionY<-.15)
				entity.motionY = -0.15D;

			if(entity.motionY<0 && entity instanceof EntityPlayer && entity.isSneaking())
			{
				entity.motionY=.05;
				return;
			}
			if(entity.isCollidedHorizontally)
				entity.motionY=.2;
		}
	}

	@Override
	public void readCustomNBT(NBTTagCompound nbt, boolean descPacket)
	{
		sideConfig = nbt.getIntArray("sideConfig");
		if(sideConfig==null || sideConfig.length!=6)
			sideConfig = new int[]{0,0,0,0,0,0};
		pipeCover = ItemStack.loadItemStackFromNBT(nbt.getCompoundTag("pipeCover"));
	}

	@Override
	public void writeCustomNBT(NBTTagCompound nbt, boolean descPacket)
	{
		nbt.setIntArray("sideConfig", sideConfig);
		if(pipeCover != null)
			nbt.setTag("pipeCover", (pipeCover.writeToNBT(new NBTTagCompound())));
	}


	boolean canOutputPressurized(TileEntity output, boolean consumePower)
	{
		if(output instanceof IFluidPipe)
			return ((IFluidPipe)output).canOutputPressurized(consumePower);
		return false;
	}

	PipeFluidHandler[] sidedHandlers = {new PipeFluidHandler(this, EnumFacing.DOWN),new PipeFluidHandler(this, EnumFacing.UP),new PipeFluidHandler(this, EnumFacing.NORTH),new PipeFluidHandler(this, EnumFacing.SOUTH),new PipeFluidHandler(this, EnumFacing.WEST),new PipeFluidHandler(this, EnumFacing.EAST)};
	@Override
	public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing)
	{
		if(capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY && facing!=null&&sideConfig[facing.ordinal()]==0)
			return true;
		return super.hasCapability(capability, facing);
	}
	@Override
	public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing)
	{
		if(capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY && facing!=null&&sideConfig[facing.ordinal()]==0)
			return (T)sidedHandlers[facing.ordinal()];
		return super.getCapability(capability, facing);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public TextureAtlasSprite getTextureReplacement(IBlockState object, String material)
	{
		return null;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public boolean shouldRenderGroup(IBlockState object, String group)
	{
		return true;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public Matrix4 handlePerspective(IBlockState Object, TransformType cameraTransformType, Matrix4 perspective)
	{
		return perspective;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public List<BakedQuad> modifyQuads(IBlockState object, List<BakedQuad> quads)
	{
		if(pipeCover != null)
		{
			Block b = Block.getBlockFromItem(pipeCover.getItem());
			IBlockState state = b != null ? b.getStateFromMeta(pipeCover.getMetadata()) : Blocks.STONE.getDefaultState();
			IBakedModel model = Minecraft.getMinecraft().getBlockRendererDispatcher().getBlockModelShapes().getModelForState(state);
			if(model != null)
			{
				for(EnumFacing facing : EnumFacing.VALUES)
					quads.addAll(model.getQuads(state, facing, 0));
				quads.addAll(model.getQuads(state, null, 0));
			}
		}
		return quads;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public String getCacheKey(IBlockState object)
	{
		return getRenderCacheKey();
	}

	@Override
	@SideOnly(Side.CLIENT)
	public com.google.common.base.Optional<TRSRTransformation> applyTransformations(IBlockState object, String group, com.google.common.base.Optional<TRSRTransformation> transform)
	{
		return transform;
	}

	static class PipeFluidHandler implements IFluidHandler
	{
		TileEntityFluidPipe pipe;
		EnumFacing facing;

		public PipeFluidHandler(TileEntityFluidPipe pipe, EnumFacing facing)
		{
			this.pipe = pipe;
			this.facing = facing;
		}

		@Override
		public IFluidTankProperties[] getTankProperties()
		{
			return new IFluidTankProperties[]{new FluidTankProperties(null, 1000, true, false)};
		}
		@Override
		public int fill(FluidStack resource, boolean doFill)
		{
//		if(resource==null || from==null || sideConfig[from.ordinal()]!=0 || worldObj.isRemote)
//			return 0;
			if(resource == null)
				return 0;
			int canAccept = resource.amount;
			if(canAccept <= 0)
				return 0;
			ArrayList<DirectionalFluidOutput> outputList = new ArrayList(getConnectedFluidHandlers(pipe.getPos(), pipe.worldObj));

			BlockPos ccFrom2 = new BlockPos(pipe.getPos().offset(facing));
			if(outputList.size() < 1)
//NO OUTPUTS!
				return 0;
			BlockPos ccFrom = new BlockPos(pipe.getPos().offset(facing));
			int sum = 0;
			HashMap<DirectionalFluidOutput, Integer> sorting = new HashMap<DirectionalFluidOutput, Integer>();
			for(DirectionalFluidOutput output : outputList)
			{
				BlockPos cc = Utils.toCC(output.containingTile);
				if(!cc.equals(ccFrom) && pipe.worldObj.isBlockLoaded(cc) && !pipe.equals(output.containingTile))
				//&& output.output.canFill(output.direction.getOpposite(), resource.getFluid()))
				{
					int limit = (resource.tag != null && resource.tag.hasKey("pressurized")) || pipe.canOutputPressurized(output.containingTile, false) ? 1000 : 50;
					int tileSpecificAcceptedFluid = Math.min(limit, canAccept);
					int temp = output.output.fill(Utils.copyFluidStackWithAmount(resource, tileSpecificAcceptedFluid, true), false);
					if(temp > 0)
					{
						sorting.put(output, temp);
						sum += temp;
					}
				}
			}
			if(sum > 0)
			{
				int f = 0;
				for(DirectionalFluidOutput output : sorting.keySet())
				{
					int limit = (resource.tag != null && resource.tag.hasKey("pressurized")) || pipe.canOutputPressurized(output.containingTile, false) ? 1000 : 50;
					int tileSpecificAcceptedFluid = Math.min(limit, canAccept);
					float prio = sorting.get(output) / (float) sum;
					int amount = (int) (tileSpecificAcceptedFluid * prio);
					int r = output.output.fill(Utils.copyFluidStackWithAmount(resource, amount, true), doFill);
					if(r > 50)
						pipe.canOutputPressurized(output.containingTile, true);
					f += r;
					canAccept -= r;
					if(canAccept <= 0)
						break;
				}
				return f;
			}
			return 0;
		}

		@Nullable
		@Override
		public FluidStack drain(FluidStack resource, boolean doDrain)
		{
			return null;
		}
		@Nullable
		@Override
		public FluidStack drain(int maxDrain, boolean doDrain)
		{
			return null;
		}
	}
	public static class DirectionalFluidOutput
	{
		IFluidHandler output;
		EnumFacing direction;
		TileEntity containingTile;
		public DirectionalFluidOutput(IFluidHandler output, TileEntity containingTile, EnumFacing direction)
		{
			this.output = output;
			this.direction = direction;
			this.containingTile = containingTile;
		}
	}

	public byte getConnectionByte()
	{
		byte connections = 0;
		IFluidTankProperties[] tankInfo;
		for(int i=5; i>=0; i--)
		{
			//			TileEntity con = worldObj.getTileEntity(xCoord+(i==4?-1: i==5?1: 0),yCoord+(i==0?-1: i==1?1: 0),zCoord+(i==2?-1: i==3?1: 0));
			EnumFacing dir = EnumFacing.getFront(i);
			TileEntity con = worldObj.getTileEntity(getPos().offset(dir));
			connections <<= 1;
			if(sideConfig[i]==0 && con!=null && con.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, dir.getOpposite()))
			{
				IFluidHandler handler = con.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, dir.getOpposite());
				tankInfo = handler.getTankProperties();
				if(tankInfo!=null && tankInfo.length>0)
					connections |= 1;
			}
		}
		return connections;
	}
	public byte getAvailableConnectionByte()
	{
		byte connections = 0;
		IFluidTankProperties[] tankInfo;
		for(int i=5; i>=0; i--)
		{
			//			TileEntity con = worldObj.getTileEntity(xCoord+(i==4?-1: i==5?1: 0),yCoord+(i==0?-1: i==1?1: 0),zCoord+(i==2?-1: i==3?1: 0));
			EnumFacing dir = EnumFacing.getFront(i);
			TileEntity con = worldObj.getTileEntity(getPos().offset(dir));
			connections <<= 1;
			if(con!=null && con.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, dir.getOpposite()))
			{
				IFluidHandler handler = con.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, dir.getOpposite());
				tankInfo = handler.getTankProperties();
				if(tankInfo!=null && tankInfo.length>0)
					connections |= 1;
			}
		}
		return connections;
	}
	public int getConnectionStyle(int connection)
	{
		if(sideConfig[connection]==-1)
			return 0;
		byte thisConnections = getConnectionByte();
		if((thisConnections&(1<<connection))==0)
			return 0;

		if(thisConnections!=3&&thisConnections!=12&&thisConnections!=48)
			return 1;
		//		TileEntity con = worldObj.getTileEntity(xCoord+(connection==4?-1: connection==5?1: 0),yCoord+(connection==0?-1: connection==1?1: 0),zCoord+(connection==2?-1: connection==3?1: 0));
		TileEntity con = worldObj.getTileEntity(getPos().offset(EnumFacing.getFront(connection)));
		if(con instanceof TileEntityFluidPipe)
		{
			byte tileConnections = ((TileEntityFluidPipe)con).getConnectionByte();
			if(thisConnections==tileConnections)
				return 0;
		}
		return 1;
	}

	public void toggleSide(int side)
	{
		sideConfig[side]++;
		if(sideConfig[side]>0)
			sideConfig[side] = -1;
		markDirty();

		EnumFacing fd = EnumFacing.getFront(side);
		TileEntity connected = worldObj.getTileEntity(getPos().offset(fd));
		if(connected instanceof TileEntityFluidPipe)
		{
			((TileEntityFluidPipe)connected).sideConfig[fd.getOpposite().ordinal()] = sideConfig[side];
			connected.markDirty();
			worldObj.addBlockEvent(getPos().offset(fd), getBlockType(), 0,0);
		}
		worldObj.addBlockEvent(getPos(), getBlockType(), 0,0);
	}
	@Override
	public boolean receiveClientEvent(int id, int arg)
	{
		if(id==0)
		{
			this.markContainingBlockForUpdate(null);
			return true;
		}
		return false;
	}

	@Override
	public float[] getBlockBounds()
	{
		return null;
	}
	@Override
	public List<AxisAlignedBB> getAdvancedColisionBounds()
	{
		List<AxisAlignedBB> list = Lists.newArrayList();
		if(pipeCover != null)
		{
			list.add(new AxisAlignedBB(0, 0, 0, 1, 1, 1).expandXyz(-.03125f).offset(getPos()));
			return list;
		}
		byte connections = getConnectionByte();
		if(/*connections==16||connections==32||*/connections==48)
		{
			list.add(new AxisAlignedBB(0, .25f, .25f, 1, .75f, .75f).offset(getPos()));
			if((connections&16) == 0)
				list.add(new AxisAlignedBB(0, .125f, .125f, .125f, .875f, .875f).offset(getPos()));
			if((connections&32) == 0)
				list.add(new AxisAlignedBB(.875f, .125f, .125f, 1, .875f, .875f).offset(getPos()));
		}
		else if(/*connections==4||connections==8||*/connections==12)
		{
			list.add(new AxisAlignedBB(.25f, .25f, 0, .75f, .75f, 1).offset(getPos()));
			if((connections&4) == 0)
				list.add(new AxisAlignedBB(.125f, .125f, 0, .875f, .875f, .125f).offset(getPos()));
			if((connections&8) == 0)
				list.add(new AxisAlignedBB(.125f, .125f, .875f, .875f, .875f, 1).offset(getPos()));
		}
		else if(/*connections==1||connections==2||*/connections==3)
		{
			list.add(new AxisAlignedBB(.25f, 0, .25f, .75f, 1, .75f).offset(getPos()));
			if((connections&1) == 0)
				list.add(new AxisAlignedBB(.125f, 0, .125f, .875f, .125f, .875f).offset(getPos()));
			if((connections&2) == 0)
				list.add(new AxisAlignedBB(.125f, .875f, .125f, .875f, 1, .875f).offset(getPos()));
		}
		else
		{
			list.add(new AxisAlignedBB(.25f, .25f, .25f, .75f, .75f, .75f).offset(getPos()));
			for(int i=0; i<6; i++)
			{
				if((connections & 0x1)==1)
					list.add(new AxisAlignedBB(i == 4 ? 0 : i == 5 ? .875f : .125f, i == 0 ? 0 : i == 1 ? .875f : .125f, i == 2 ? 0 : i == 3 ? .875f : .125f, i == 4 ? .125f : i == 5 ? 1 : .875f, i == 0 ? .125f : i == 1 ? 1 : .875f, i == 2 ? .125f : i == 3 ? 1 : .875f).offset(getPos()));
				connections >>= 1;
			}
		}
		return list;
	}
	@Override
	public List<AxisAlignedBB> getAdvancedSelectionBounds()
	{
		List<AxisAlignedBB> list = Lists.newArrayList();
		byte connections = getAvailableConnectionByte();
		byte availableConnections = getConnectionByte();
		double[] baseAABB = pipeCover != null ? new double[]{.002, .998, .002, .998, .002, .998} : new double[]{.25, .75, .25, .75, .25, .75};
		for(int i=0; i<6; i++)
		{
			double depth = getConnectionStyle(i)==0?.25:.125;
			double size = getConnectionStyle(i)==0?.25:.125;
			//			if(pipeCover!=null)
			//				size = 0;
			if((connections & 0x1)==1)
				list.add(new AdvancedAABB(new AxisAlignedBB(i == 4 ? 0 : i == 5 ? 1 - depth : size, i == 0 ? 0 : i == 1 ? 1 - depth : size, i == 2 ? 0 : i == 3 ? 1 - depth : size, i == 4 ? depth : i == 5 ? 1 : 1 - size, i == 0 ? depth : i == 1 ? 1 : 1 - size, i == 2 ? depth : i == 3 ? 1 : 1 - size).offset(getPos()), EnumFacing.getFront(i)));
			if((availableConnections & 0x1)==1)
				baseAABB[i] += i%2==1?.125: -.125;
			baseAABB[i] = Math.min(Math.max(baseAABB[i], 0), 1);
			availableConnections = (byte)(availableConnections>>1);
			connections = (byte)(connections>>1);
		}
		list.add(new AdvancedAABB(new AxisAlignedBB(baseAABB[4], baseAABB[0], baseAABB[2], baseAABB[5], baseAABB[1], baseAABB[3]).offset(getPos()), null));
		return list;
	}

	@Override
	public boolean isOverrideBox(AxisAlignedBB box, EntityPlayer player, RayTraceResult mop, ArrayList<AxisAlignedBB> list)
	{
		if(box instanceof AdvancedAABB)
		{
			if(box.expand(.002,.002,.002).isVecInside(mop.hitVec))
			{
				AxisAlignedBB changedBox = ((AdvancedAABB)box).fd!=null?box.expand(((AdvancedAABB)box).fd.getFrontOffsetX()!=0?0:.03125, ((AdvancedAABB)box).fd.getFrontOffsetY()!=0?0:.03125, ((AdvancedAABB)box).fd.getFrontOffsetZ()!=0?0:.03125): box;
				list.add(changedBox);
				return true;
			}
		}
		return false;
	}

	static HashMap<String, OBJState> cachedOBJStates = new HashMap<String, OBJState>();
	static String[] CONNECTIONS = new String[]{
			"con_yMin", "con_yMax", "con_zMin", "con_zMax", "con_xMin", "con_xMax"
	};

	String getRenderCacheKey()
	{
		byte connections = getConnectionByte();
		String key = "";
		for(int i=0; i<6; i++)
		{
			if((connections&(1<<i))!=0)
				key += getConnectionStyle(i)==1?"2":"1";
			else
				key += "0";
		}
		if(pipeCover != null)
			key += "scaf:" + pipeCover.getItem().getRegistryName();
		return key;
	}

	@Override
	public OBJState getOBJState()
	{
		byte connections = getConnectionByte();
		String key = getRenderCacheKey();
		if(!cachedOBJStates.containsKey(key))
		{
			ArrayList<String> parts = new ArrayList();
			Matrix4 rotationMatrix = new Matrix4(TRSRTransformation.identity().getMatrix());//new Matrix4();

//			if(pipeCover!=null)
//				parts.add("cover");
			int totalConnections = Integer.bitCount(connections);
			boolean straightY = (connections&3)==3;
			boolean straightZ = (connections&12)==12;
			boolean straightX = (connections&48)==48;
			switch(totalConnections)
			{
				case 0://stub
					parts.add("center");
					break;
				case 1://stopper
					parts.add("stopper");

					//default: y-
					if((connections&2)!=0)//y+
						rotationMatrix.rotate(Math.PI, 0,0,1);
					else if((connections&4)!=0)//z-
						rotationMatrix.rotate(Math.PI/2, 1,0,0);
					else if((connections&8)!=0)//z+
						rotationMatrix.rotate(-Math.PI/2, 1,0,0);
					else if((connections&16)!=0)//x-
						rotationMatrix.rotate(-Math.PI/2, 0,0,1);
					else if((connections&32)!=0)//x+
						rotationMatrix.rotate(Math.PI/2, 0,0,1);
					parts.add("con_yMin");
					break;
				case 2://straight or curve
					if(straightY)
					{
						parts.add("pipe_y");
						if(getConnectionStyle(0)==1)
							parts.add("con_yMin");
						if(getConnectionStyle(1)==1)
							parts.add("con_yMax");
					}
					else if(straightZ)
					{
						parts.add("pipe_z");
						if(getConnectionStyle(2)==1)
							parts.add("con_zMin");
						if(getConnectionStyle(3)==1)
							parts.add("con_zMax");
					}
					else if(straightX)
					{
						parts.add("pipe_x");
						if(getConnectionStyle(4)==1)
							parts.add("con_xMin");
						if(getConnectionStyle(5)==1)
							parts.add("con_xMax");
					}
					else
					{
						parts.add("curve");
						parts.add("con_yMin");
						parts.add("con_zMin");
						byte connectTo = (byte)(connections&60);
						if((connections&3)!=0)//curve to top or bottom
						{
							if(connectTo==16)//x-
								rotationMatrix.rotate(Math.PI/2, 0,1,0);
							else if(connectTo==32)//x+
								rotationMatrix.rotate(-Math.PI/2, 0,1,0);
							else if(connectTo==8)//z+
								rotationMatrix.rotate(Math.PI, 0,1,0);
							if((connections&2)!=0)//flip to top
								rotationMatrix.rotate(Math.PI, 0,0,1);

							//default: Curve to z-
						}
						else//curve to horizontal
						{
							rotationMatrix.rotate(-Math.PI/2, 0,0,1);
							if(connectTo==40)//z+ to x+
								rotationMatrix.rotate(Math.PI, 1,0,0);
							else if(connectTo==24)//z+ to x-
								rotationMatrix.rotate(-Math.PI/2, 1,0,0);
							else if(connectTo==36)//z- to x+
								rotationMatrix.rotate(Math.PI/2, 1,0,0);
							//default: z- to x-
						}
					}
					break;
				case 3://tcross or tcurve
					if(straightX||straightZ||straightY)//has straight connect
					{
						parts.add("tcross");
						parts.add("con_yMin");
						parts.add("con_zMin");
						parts.add("con_zMax");
						if(straightX)
						{
							rotationMatrix.rotate(Math.PI/2, 0,1,0);
							if((connections&4)!=0)//z-
								rotationMatrix.rotate(Math.PI/2, 0,0,1);
							else if((connections&8)!=0)//z+
								rotationMatrix.rotate(-Math.PI/2, 0,0,1);
							else if((connections&2)!=0)//y+
								rotationMatrix.rotate(Math.PI, 0,0,1);
							//default: Curve to y-
						}
						else if(straightY)
						{
							rotationMatrix.rotate(Math.PI/2, 1,0,0);
							if((connections&16)!=0)//x-
								rotationMatrix.rotate(-Math.PI/2, 0,0,1);
							else if((connections&32)!=0)//x+
								rotationMatrix.rotate(Math.PI/2, 0,0,1);
							else if((connections&8)!=0)//z+
								rotationMatrix.rotate(Math.PI, 0,0,1);
							//default: Curve to z-
						}
						else //default:z straight
						{
							if((connections&16)!=0)//x-
								rotationMatrix.rotate(-Math.PI/2, 0,0,1);
							else if((connections&32)!=0)//x+
								rotationMatrix.rotate(Math.PI/2, 0,0,1);
							else if((connections&2)!=0)//y+
								rotationMatrix.rotate(Math.PI, 0,0,1);
							//default: Curve to y-
						}
					}
					else //tcurve
					{
						parts.add("tcurve");
						parts.add("con_yMin");
						parts.add("con_zMin");
						parts.add("con_xMax");
						//default y-, z-, x+
						if((connections&8)!=0)//z+
						{
							if((connections&16)!=0)//x-
								rotationMatrix.rotate(Math.PI, 0,1,0);
							else
								rotationMatrix.rotate(-Math.PI/2, 0,1,0);
						}
						else//z-
						{
							if((connections&16)!=0)//x-
								rotationMatrix.rotate(Math.PI/2, 0,1,0);
						}
						if((connections&2)!=0)//y+
							rotationMatrix.rotate(Math.PI/2, 0,0,1);
					}
					break;
				case 4://cross or complex tcross
					boolean cross = (straightX&&straightZ)||(straightX&&straightY)||(straightZ&&straightY);
					if(cross)
					{
						parts.add("cross");
						parts.add("con_yMin");
						parts.add("con_yMax");
						parts.add("con_zMin");
						parts.add("con_zMax");
						if(!straightY)//x and z
							rotationMatrix.rotate(Math.PI/2, 0,0,1);
						else if(straightX)//x and y
							rotationMatrix.rotate(Math.PI/2, 0,1,0);
					}
					else
					{
						parts.add("tcross2");
						parts.add("con_yMin");
						parts.add("con_zMin");
						parts.add("con_zMax");
						parts.add("con_xMax");
						if(straightZ)
						{
							//default y- z+- x+
							if((connections&16)!=0)//x-
								rotationMatrix.rotate(Math.PI, 0,1,0);
							if((connections&2)!=0)//y+
								rotationMatrix.rotate(Math.PI/2, 0,0,1);
						}
						else if(straightY)
						{
							rotationMatrix.rotate(Math.PI / 2, 1, 0, 0);
							//default y+- z- x+
							if((connections&8)!=0)//z+
							{
								rotationMatrix.rotate(Math.PI/2, 0,0,1);
								if((connections&16)!=0)//x-
									rotationMatrix.rotate(Math.PI/2, 0,0,1);
							}
							else if((connections&16)!=0)//x-
								rotationMatrix.rotate(-Math.PI/2, 0,0,1);
						}
						else
						{
							rotationMatrix.rotate(Math.PI/2, 0,1,0);
							//default y- z- x+-
							if((connections&8)!=0)//z+
								rotationMatrix.rotate(Math.PI, 0,1,0);
							if((connections&2)!=0)//y+
								rotationMatrix.rotate(Math.PI/2, 0,0,1);
						}
					}
					break;
				case 5://complete tcross
					parts.add("tcross3");
					parts.add("con_yMin");
					parts.add("con_yMax");
					parts.add("con_zMin");
					parts.add("con_zMax");
					parts.add("con_xMax");
					//default y+- z+- x+
					if(straightZ)
					{
						if(straightY)
						{
							if((connections&16)!=0)//x-
								rotationMatrix.rotate(Math.PI, 0,1,0);
						}
						else if(straightX)
							rotationMatrix.rotate(((connections&2)!=0)?(Math.PI/2):(-Math.PI/2), 0,0,1);
					}
					else if(straightX)
					{
						rotationMatrix.rotate(Math.PI/2, 0,1,0);
						if((connections&8)!=0)//z+
							rotationMatrix.rotate(Math.PI, 0,1,0);
					}
					break;
				case 6://Full Crossing
					parts.add("con_yMin");
					parts.add("con_yMax");
					parts.add("con_zMin");
					parts.add("con_zMax");
					parts.add("con_xMin");
					parts.add("con_xMax");

					break;
			}
			//			connetionParts
			//			for(int i=0; i<6; i++)
			//				if(((TileEntityFluidPipe)tile).getConnectionStyle(i)==1)
			//					connectionCaps.add(CONNECTIONS[i]);

			Matrix4 tempMatr = new Matrix4();
			tempMatr.m03 = tempMatr.m13 = tempMatr.m23 = .5f;
			rotationMatrix.leftMultiply(tempMatr);
			tempMatr.invert();
			rotationMatrix = rotationMatrix.multiply(tempMatr);

			cachedOBJStates.put(key, new OBJState(parts, true, new TRSRTransformation(rotationMatrix.toMatrix4f())));
		}
		return cachedOBJStates.get(key);
	}

	//	@Override
//	public HashMap<String, String> getTextureReplacements()
//	{
//		if(pipeCover!=null)
//		{
//			HashMap<String,String> map = new HashMap<String,String>();
////			map.put("cover","minecraft:blocks/stone");
//			Block b = Block.getBlockFromItem(pipeCover.getItem());
//			IBlockState state = b!=null?b.getStateFromMeta(pipeCover.getMetadata()): Blocks.STONE.getDefaultState();
//			IBakedModel model = Minecraft.getMinecraft().getBlockRendererDispatcher().getBlockModelShapes().getModelForState(state);
//			if(model!=null && model.getParticleTexture()!=null)
//				map.put("cover", model.getParticleTexture().getIconName());
//
//			return map;
//		}
//		return null;
//	}
	public static OBJState getStateFromKey(String key)
	{
		if(!cachedOBJStates.containsKey(key))
		{
			ArrayList<String> parts = new ArrayList();
			Matrix4 rotationMatrix = new Matrix4(TRSRTransformation.identity().getMatrix());//new Matrix4();

			byte connections = (byte)Integer.parseInt(key.replace("2","1"), 2);
			int totalConnections = Integer.bitCount(connections);
			boolean straightY = (connections&3)==3;
			boolean straightZ = (connections&12)==12;
			boolean straightX = (connections&48)==48;
			switch(totalConnections)
			{
				case 0://stub
					parts.add("center");
					break;
				case 1://stopper
					parts.add("stopper");

					//default: y-
					if((connections&2)!=0)//y+
						rotationMatrix.rotate(Math.PI, 0,0,1);
					else if((connections&4)!=0)//z-
						rotationMatrix.rotate(Math.PI/2, 1,0,0);
					else if((connections&8)!=0)//z+
						rotationMatrix.rotate(-Math.PI/2, 1,0,0);
					else if((connections&16)!=0)//x-
						rotationMatrix.rotate(-Math.PI/2, 0,0,1);
					else if((connections&32)!=0)//x+
						rotationMatrix.rotate(Math.PI/2, 0,0,1);
					parts.add("con_yMin");
					break;
				case 2://straight or curve
					if(straightY)
					{
						parts.add("pipe_y");
						if(key.charAt(5)=='2')
							parts.add("con_yMin");
						if(key.charAt(4)==1)
							parts.add("con_yMax");
					}
					else if(straightZ)
					{
						parts.add("pipe_z");
						if(key.charAt(3)=='2')
							parts.add("con_zMin");
						if(key.charAt(2)=='2')
							parts.add("con_zMax");
					}
					else if(straightX)
					{
						parts.add("pipe_x");
						if(key.charAt(1)=='2')
							parts.add("con_xMin");
						if(key.charAt(0)=='2')
							parts.add("con_xMax");
					}
					else
					{
						parts.add("curve");
						parts.add("con_yMin");
						parts.add("con_zMin");
						byte connectTo = (byte)(connections&60);
						if((connections&3)!=0)//curve to top or bottom
						{
							if(connectTo==16)//x-
								rotationMatrix.rotate(Math.PI/2, 0,1,0);
							else if(connectTo==32)//x+
								rotationMatrix.rotate(-Math.PI/2, 0,1,0);
							else if(connectTo==8)//z+
								rotationMatrix.rotate(Math.PI, 0,1,0);
							if((connections&2)!=0)//flip to top
								rotationMatrix.rotate(Math.PI, 0,0,1);

							//default: Curve to z-
						}
						else//curve to horizontal
						{
							rotationMatrix.rotate(-Math.PI/2, 0,0,1);
							if(connectTo==40)//z+ to x+
								rotationMatrix.rotate(Math.PI, 1,0,0);
							else if(connectTo==24)//z+ to x-
								rotationMatrix.rotate(-Math.PI/2, 1,0,0);
							else if(connectTo==36)//z- to x+
								rotationMatrix.rotate(Math.PI/2, 1,0,0);
							//default: z- to x-
						}
					}
					break;
				case 3://tcross or tcurve
					if(straightX||straightZ||straightY)//has straight connect
					{
						parts.add("tcross");
						parts.add("con_yMin");
						parts.add("con_zMin");
						parts.add("con_zMax");
						if(straightX)
						{
							rotationMatrix.rotate(Math.PI/2, 0,1,0);
							if((connections&4)!=0)//z-
								rotationMatrix.rotate(Math.PI/2, 0,0,1);
							else if((connections&8)!=0)//z+
								rotationMatrix.rotate(-Math.PI/2, 0,0,1);
							else if((connections&2)!=0)//y+
								rotationMatrix.rotate(Math.PI, 0,0,1);
							//default: Curve to y-
						}
						else if(straightY)
						{
							rotationMatrix.rotate(Math.PI/2, 1,0,0);
							if((connections&16)!=0)//x-
								rotationMatrix.rotate(-Math.PI/2, 0,0,1);
							else if((connections&32)!=0)//x+
								rotationMatrix.rotate(Math.PI/2, 0,0,1);
							else if((connections&8)!=0)//z+
								rotationMatrix.rotate(Math.PI, 0,0,1);
							//default: Curve to z-
						}
						else //default:z straight
						{
							if((connections&16)!=0)//x-
								rotationMatrix.rotate(-Math.PI/2, 0,0,1);
							else if((connections&32)!=0)//x+
								rotationMatrix.rotate(Math.PI/2, 0,0,1);
							else if((connections&2)!=0)//y+
								rotationMatrix.rotate(Math.PI, 0,0,1);
							//default: Curve to y-
						}
					}
					else //tcurve
					{
						parts.add("tcurve");
						parts.add("con_yMin");
						parts.add("con_zMin");
						parts.add("con_xMax");
						//default y-, z-, x+
						if((connections&8)!=0)//z+
						{
							if((connections&16)!=0)//x-
								rotationMatrix.rotate(Math.PI, 0,1,0);
							else
								rotationMatrix.rotate(-Math.PI/2, 0,1,0);
						}
						else//z-
						{
							if((connections&16)!=0)//x-
								rotationMatrix.rotate(Math.PI/2, 0,1,0);
						}
						if((connections&2)!=0)//y+
							rotationMatrix.rotate(Math.PI/2, 0,0,1);
					}
					break;
				case 4://cross or complex tcross
					boolean cross = (straightX&&straightZ)||(straightX&&straightY)||(straightZ&&straightY);
					if(cross)
					{
						parts.add("cross");
						parts.add("con_yMin");
						parts.add("con_yMax");
						parts.add("con_zMin");
						parts.add("con_zMax");
						if(!straightY)//x and z
							rotationMatrix.rotate(Math.PI/2, 0,0,1);
						else if(straightX)//x and y
							rotationMatrix.rotate(Math.PI/2, 0,1,0);
					}
					else
					{
						parts.add("tcross2");
						parts.add("con_yMin");
						parts.add("con_zMin");
						parts.add("con_zMax");
						parts.add("con_xMax");
						if(straightZ)
						{
							//default y- z+- x+
							if((connections&16)!=0)//x-
								rotationMatrix.rotate(Math.PI, 0,1,0);
							if((connections&2)!=0)//y+
								rotationMatrix.rotate(Math.PI/2, 0,0,1);
						}
						else if(straightY)
						{
							//default y+- z- x+
							if((connections&8)!=0)//z+
							{
								rotationMatrix.rotate(Math.PI/2, 0,0,1);
								if((connections&16)!=0)//x-
									rotationMatrix.rotate(Math.PI/2, 0,0,1);
							}
							else if((connections&16)!=0)//x-
								rotationMatrix.rotate(-Math.PI/2, 0,0,1);
						}
						else
						{
							rotationMatrix.rotate(Math.PI/2, 0,1,0);
							//default y- z- x+-
							if((connections&8)!=0)//z+
								rotationMatrix.rotate(Math.PI, 0,1,0);
							if((connections&2)!=0)//y+
								rotationMatrix.rotate(Math.PI/2, 0,0,1);
						}
					}
					break;
				case 5://complete tcross
					parts.add("tcross3");
					parts.add("con_yMin");
					parts.add("con_yMax");
					parts.add("con_zMin");
					parts.add("con_zMax");
					parts.add("con_xMax");
					//default y+- z+- x+
					if(straightZ)
					{
						if(straightY)
						{
							if((connections&16)!=0)//x-
								rotationMatrix.rotate(Math.PI, 0,1,0);
						}
						else if(straightX)
							rotationMatrix.rotate(((connections&2)!=0)?(Math.PI/2):(-Math.PI/2), 0,0,1);
					}
					else if(straightX)
					{
						rotationMatrix.rotate(Math.PI/2, 0,1,0);
						if((connections&8)!=0)//z+
							rotationMatrix.rotate(Math.PI, 0,1,0);
					}
					break;
				case 6://Full Crossing
					break;
			}
			//			connetionParts
			//			for(int i=0; i<6; i++)
			//				if(((TileEntityFluidPipe)tile).getConnectionStyle(i)==1)
			//					connectionCaps.add(CONNECTIONS[i]);

			Matrix4 tempMatr = new Matrix4();
			tempMatr.m03 = tempMatr.m13 = tempMatr.m23 = .5f;
			rotationMatrix.leftMultiply(tempMatr);
			tempMatr.invert();
			rotationMatrix = rotationMatrix.multiply(tempMatr);

			cachedOBJStates.put(key, new OBJState(parts, true, new TRSRTransformation(rotationMatrix.toMatrix4f())));
		}
		return cachedOBJStates.get(key);
	}

	@Override
	public int getRenderColour()
	{
		return 0xff00ff;
	}

	@Override
	public boolean interact(EnumFacing side, EntityPlayer player, EnumHand hand, ItemStack heldItem, float hitX, float hitY, float hitZ)
	{
		if(heldItem == null && player.isSneaking() && pipeCover != null)
		{
			if(!worldObj.isRemote && worldObj.getGameRules().getBoolean("doTileDrops"))
			{
				EntityItem entityitem = player.dropItem(pipeCover.copy(), false);
				if(entityitem != null)
					entityitem.setNoPickupDelay();
			}
			pipeCover = null;
			this.markContainingBlockForUpdate(null);
			worldObj.addBlockEvent(getPos(), getBlockType(), 255, 0);
			return true;
		} else if(heldItem != null && !player.isSneaking())
			for(Function<ItemStack, Boolean> func : validPipeCovers)
				if(func.apply(heldItem) == Boolean.TRUE)
				{
					if(!OreDictionary.itemMatches(pipeCover, heldItem, true))
					{
						if(!worldObj.isRemote && pipeCover != null && worldObj.getGameRules().getBoolean("doTileDrops"))
						{
							EntityItem entityitem = player.dropItem(pipeCover.copy(), false);
							if(entityitem != null)
								entityitem.setNoPickupDelay();
						}
						pipeCover = Utils.copyStackWithAmount(heldItem, 1);
						if((--heldItem.stackSize) <= 0)
							heldItem = null;
						this.markContainingBlockForUpdate(null);
						worldObj.addBlockEvent(getPos(), getBlockType(), 255, 0);
						return true;
					}
				}
		return false;
	}
	@Override
	public boolean hammerUseSide(EnumFacing side, EntityPlayer player, float hitX, float hitY, float hitZ)
	{
		if(worldObj.isRemote)
			return true;
		EnumFacing fd = null;
		List<AxisAlignedBB> boxes = this.getAdvancedSelectionBounds();
		for(AxisAlignedBB box : boxes)
			if(box instanceof AdvancedAABB)
			{
				if(box.expand(.002,.002,.002).isVecInside(new Vec3d(getPos().getX()+hitX, getPos().getY()+hitY, getPos().getZ()+hitZ)))
					if(box instanceof AdvancedAABB)
						fd = ((AdvancedAABB)box).fd;
			}
		if(fd!=null)
		{
			toggleSide(fd.ordinal());
			this.markContainingBlockForUpdate(null);
			TileEntityFluidPipe.indirectConnections.clear();
			return true;
		}
		return false;
	}

	@Override
	public boolean canOutputPressurized(boolean consumePower)
	{
		return false;
	}
	@Override
	public boolean hasOutputConnection(EnumFacing side)
	{
		return side != null && sideConfig[side.ordinal()] == 0;
	}
}