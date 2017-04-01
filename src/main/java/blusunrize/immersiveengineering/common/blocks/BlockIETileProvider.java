package blusunrize.immersiveengineering.common.blocks;

import blusunrize.immersiveengineering.api.IEProperties;
import blusunrize.immersiveengineering.api.energy.wires.IImmersiveConnectable;
import blusunrize.immersiveengineering.api.energy.wires.ImmersiveNetHandler;
import blusunrize.immersiveengineering.api.energy.wires.TileEntityImmersiveConnectable;
import blusunrize.immersiveengineering.api.shader.CapabilityShader;
import blusunrize.immersiveengineering.client.models.IOBJModelCallback;
import blusunrize.immersiveengineering.common.CommonProxy;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.*;
import blusunrize.immersiveengineering.common.util.Utils;
import blusunrize.immersiveengineering.common.util.inventory.IEInventoryHandler;
import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.AxisDirection;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.client.model.obj.OBJModel.OBJState;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.common.property.Properties;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;

public abstract class BlockIETileProvider<E extends Enum<E> & BlockIEBase.IBlockEnum> extends BlockIEBase<E> implements ITileEntityProvider, IColouredBlock
{
	private boolean hasColours = false;

	public BlockIETileProvider(String name, Material material, PropertyEnum<E> mainProperty, Class<? extends ItemBlockIEBase> itemBlock, Object... additionalProperties)
	{
		super(name, material, mainProperty, itemBlock, additionalProperties);
	}

	@Override
	public List<ItemStack> getDrops(IBlockAccess world, BlockPos pos, IBlockState state, int fortune)
	{
		return super.getDrops(world, pos, state, fortune);
	}

	@Override
	public void breakBlock(World world, BlockPos pos, IBlockState state)
	{
		TileEntity tile = world.getTileEntity(pos);
		if(tile != null && ( !(tile instanceof ITileDrop) || !((ITileDrop)tile).preventInventoryDrop()) && tile.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null))
		{
			IItemHandler h = tile.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
			if (h instanceof IEInventoryHandler)
				for (int i = 0;i<h.getSlots();i++)
					if (h.getStackInSlot(i)!=null)
					{
						spawnAsEntity(world, pos, h.getStackInSlot(i));
						((IEInventoryHandler) h).setStackInSlot(i, null);
					}
		}
		if(tile instanceof IHasDummyBlocks)
		{
			((IHasDummyBlocks)tile).breakDummies(pos, state);
		}
		if(tile instanceof IImmersiveConnectable)
			if(!world.isRemote||!Minecraft.getMinecraft().isSingleplayer())
				ImmersiveNetHandler.INSTANCE.clearAllConnectionsFor(Utils.toCC(tile),world, !world.isRemote&&world.getGameRules().getBoolean("doTileDrops"));
		super.breakBlock(world, pos, state);
		world.removeTileEntity(pos);
	}

	@Override
	public void harvestBlock(World world, EntityPlayer player, BlockPos pos, IBlockState state, TileEntity tile, ItemStack stack)
	{
		if(tile instanceof ITileDrop)
		{
			ItemStack s = ((ITileDrop)tile).getTileDrop(player, state);
			if(s!=null)
			{
				spawnAsEntity(world, pos, s);
				return;
			}
		}
		if(tile instanceof IAdditionalDrops)
		{
			Collection<ItemStack> stacks = ((IAdditionalDrops)tile).getExtraDrops(player, state);
			if(stacks!=null && !stacks.isEmpty())
				for(ItemStack s : stacks)
					if(s!=null)
						spawnAsEntity(world, pos, s);
		}
		super.harvestBlock(world, player, pos, state, tile, stack);
	}

	@Override
	public boolean canEntityDestroy(IBlockState state, IBlockAccess world, BlockPos pos, Entity entity)
	{
		TileEntity tile = world.getTileEntity(pos);
		if(tile instanceof IEntityProof)
			return ((IEntityProof)tile).canEntityDestroy(entity);
		return super.canEntityDestroy(state, world, pos, entity);
	}

	@Override
	public ItemStack getPickBlock(IBlockState state, RayTraceResult target, World world, BlockPos pos, EntityPlayer player)
	{
		TileEntity tile = world.getTileEntity(pos);
		if(tile instanceof ITileDrop)
		{
			ItemStack s = ((ITileDrop)tile).getTileDrop(player, world.getBlockState(pos));
			if(s!=null)
				return s;
		}
		Item item = Item.getItemFromBlock(this);
		return item == null ? null : new ItemStack(item, 1, this.damageDropped(world.getBlockState(pos)));
	}


	@Override
	public boolean eventReceived(IBlockState state, World worldIn, BlockPos pos, int eventID, int eventParam)
	{
		super.eventReceived(state, worldIn, pos, eventID, eventParam);
		TileEntity tileentity = worldIn.getTileEntity(pos);
		return tileentity != null && tileentity.receiveClientEvent(eventID, eventParam);
	}

	protected EnumFacing getDefaultFacing()
	{
		return EnumFacing.NORTH;
	}

	@Override
	public IBlockState getActualState(IBlockState state, IBlockAccess world, BlockPos pos)
	{
		state = super.getActualState(state, world, pos);
		TileEntity tile = world.getTileEntity(pos);

		if(tile instanceof IAttachedIntegerProperies)
		{
			for(String s : ((IAttachedIntegerProperies)tile).getIntPropertyNames())
				state = applyProperty(state, ((IAttachedIntegerProperies)tile).getIntProperty(s),  ((IAttachedIntegerProperies)tile).getIntPropertyValue(s));
		}

		if(tile instanceof IDirectionalTile && (state.getPropertyNames().contains(IEProperties.FACING_ALL) || state.getPropertyNames().contains(IEProperties.FACING_HORIZONTAL)))
		{
			PropertyDirection prop = state.getPropertyNames().contains(IEProperties.FACING_HORIZONTAL)?IEProperties.FACING_HORIZONTAL: IEProperties.FACING_ALL;
			state = applyProperty(state, prop, ((IDirectionalTile)tile).getFacing());
		}
		else if(state.getPropertyNames().contains(IEProperties.FACING_HORIZONTAL))
			state = state.withProperty(IEProperties.FACING_HORIZONTAL, getDefaultFacing());
		else if(state.getPropertyNames().contains(IEProperties.FACING_ALL))
			state = state.withProperty(IEProperties.FACING_ALL, getDefaultFacing());

		if(tile instanceof IActiveState)
		{
			IProperty boolProp = ((IActiveState) tile).getBoolProperty(IActiveState.class);
			if(state.getPropertyNames().contains(boolProp))
				state = applyProperty(state, boolProp, ((IActiveState) tile).getIsActive());
		}

		if(tile instanceof IDualState)
		{
			IProperty boolProp = ((IDualState) tile).getBoolProperty(IDualState.class);
			if(state.getPropertyNames().contains(boolProp))
				state = applyProperty(state, boolProp, ((IDualState) tile).getIsSecondState());
		}

		if(tile instanceof TileEntityMultiblockPart)
			state = applyProperty(state, IEProperties.MULTIBLOCKSLAVE, ((TileEntityMultiblockPart)tile).isDummy());
		else if(tile instanceof IHasDummyBlocks)
			state = applyProperty(state, IEProperties.MULTIBLOCKSLAVE, ((IHasDummyBlocks)tile).isDummy());

		if(tile instanceof IMirrorAble)
			state = applyProperty(state, ((IMirrorAble)tile).getBoolProperty(IMirrorAble.class), ((IMirrorAble)tile).getIsMirrored());

		return state;
	}

	@Override
	public boolean rotateBlock(World world, BlockPos pos, EnumFacing axis)
	{
		TileEntity tile = world.getTileEntity(pos);
		if(tile instanceof IDirectionalTile)
		{
			if(!((IDirectionalTile)tile).canRotate(axis))
				return false;
			IBlockState state = world.getBlockState(pos);
			if(state.getPropertyNames().contains(IEProperties.FACING_ALL) || state.getPropertyNames().contains(IEProperties.FACING_HORIZONTAL))
			{
				PropertyDirection prop = state.getPropertyNames().contains(IEProperties.FACING_HORIZONTAL)?IEProperties.FACING_HORIZONTAL: IEProperties.FACING_ALL;
				EnumFacing f = ((IDirectionalTile)tile).getFacing();
				int limit = ((IDirectionalTile)tile).getFacingLimitation();

				if(limit==0)
					f = EnumFacing.VALUES[(f.ordinal() + 1) % EnumFacing.VALUES.length];
				else if(limit==1)
					f = axis.getAxisDirection()==AxisDirection.POSITIVE?f.rotateAround(axis.getAxis()).getOpposite():f.rotateAround(axis.getAxis());
				else if(limit == 2 || limit == 5)
					f = axis.getAxisDirection()==AxisDirection.POSITIVE?f.rotateY():f.rotateYCCW();
				if(f != ((IDirectionalTile)tile).getFacing())
				{
					EnumFacing old = ((IDirectionalTile)tile).getFacing();
					((IDirectionalTile)tile).setFacing(f);
					((IDirectionalTile)tile).afterRotation(old,f);
					state = applyProperty(state, prop, ((IDirectionalTile)tile).getFacing());
					world.setBlockState(pos, state.cycleProperty(prop));
				}
			}
		}
		return false;
	}

	@Override
	public IBlockState getExtendedState(IBlockState state, IBlockAccess world, BlockPos pos)
	{
		state = super.getExtendedState(state, world, pos);
		if(state instanceof IExtendedBlockState)
		{
			IExtendedBlockState extended = (IExtendedBlockState)state;
			TileEntity te = world.getTileEntity(pos);
			if(te!=null)
			{
				if(te instanceof IConfigurableSides)
					for(int i = 0; i < 6; i++)
						if(extended.getUnlistedNames().contains(IEProperties.SIDECONFIG[i]))
							extended = extended.withProperty(IEProperties.SIDECONFIG[i], ((IConfigurableSides)te).getSideConfig(i));
				if(te instanceof IAdvancedHasObjProperty)
					extended = extended.withProperty(Properties.AnimationProperty, ((IAdvancedHasObjProperty)te).getOBJState());
				else if(te instanceof IHasObjProperty)
					extended = extended.withProperty(Properties.AnimationProperty, new OBJState(((IHasObjProperty)te).compileDisplayList(), true));
				if(te instanceof IDynamicTexture)
					extended = extended.withProperty(IEProperties.OBJ_TEXTURE_REMAP, ((IDynamicTexture)te).getTextureReplacements());
				if(te instanceof IOBJModelCallback)
					extended = extended.withProperty(IOBJModelCallback.PROPERTY, (IOBJModelCallback)te);
				if(te.hasCapability(CapabilityShader.SHADER_CAPABILITY, null))
					extended = extended.withProperty(CapabilityShader.BLOCKSTATE_PROPERTY, te.getCapability(CapabilityShader.SHADER_CAPABILITY, null));
				if(te instanceof IPropertyPassthrough && ((IExtendedBlockState)state).getUnlistedNames().contains(IEProperties.TILEENTITY_PASSTHROUGH))
					extended = extended.withProperty(IEProperties.TILEENTITY_PASSTHROUGH, te);
				if(te instanceof TileEntityImmersiveConnectable && ((IExtendedBlockState)state).getUnlistedNames().contains(IEProperties.CONNECTIONS))
					extended = extended.withProperty(IEProperties.CONNECTIONS, ((TileEntityImmersiveConnectable)te).genConnBlockstate());
			}
			state = extended;
		}

		return state;
	}

	@Override
	public void onIEBlockPlacedBy(World world, BlockPos pos, IBlockState state, EnumFacing side, float hitX, float hitY, float hitZ, EntityLivingBase placer, ItemStack stack)
	{
		TileEntity tile = world.getTileEntity(pos);

		if(tile instanceof IDirectionalTile)
		{
			EnumFacing f = ((IDirectionalTile)tile).getFacingForPlacement(placer, pos, side, hitX, hitY, hitZ);
			((IDirectionalTile)tile).setFacing(f);
			if(tile instanceof IAdvancedDirectionalTile)
				((IAdvancedDirectionalTile)tile).onDirectionalPlacement(side, hitX, hitY, hitZ, placer);
		}
		if(tile instanceof IHasDummyBlocks)
		{
			((IHasDummyBlocks)tile).placeDummies(pos, state, side, hitX, hitY, hitZ);
		}
		if(tile instanceof ITileDrop)
		{
			((ITileDrop)tile).readOnPlacement(placer, stack);
		}
	}

	@Override
	public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, ItemStack heldItem, EnumFacing side, float hitX, float hitY, float hitZ)
	{
		TileEntity tile = world.getTileEntity(pos);
		if(tile instanceof IConfigurableSides && Utils.isHammer(heldItem) && !world.isRemote)
		{
			int iSide = player.isSneaking()?side.getOpposite().ordinal():side.ordinal();
			if(((IConfigurableSides)tile).toggleSide(iSide, player))
				return true;
		}
		if(tile instanceof IDirectionalTile && Utils.isHammer(heldItem) && ((IDirectionalTile)tile).canHammerRotate(side, hitX, hitY, hitZ, player) && !world.isRemote)
		{
			EnumFacing f = ((IDirectionalTile)tile).getFacing();
			int limit = ((IDirectionalTile)tile).getFacingLimitation();

			if(limit==0)
				f = EnumFacing.VALUES[(f.ordinal() + 1) % EnumFacing.VALUES.length];
			else if(limit==1)
				f = player.isSneaking()?f.rotateAround(side.getAxis()).getOpposite():f.rotateAround(side.getAxis());
			else if(limit == 2 || limit == 5)
				f = player.isSneaking()?f.rotateYCCW():f.rotateY();
			((IDirectionalTile)tile).setFacing(f);
			tile.markDirty();
			world.notifyBlockUpdate(pos,state,state,3);
			world.addBlockEvent(tile.getPos(), tile.getBlockType(), 255, 0);
			return true;
		}
		if(tile instanceof IHammerInteraction && Utils.isHammer(heldItem) && !world.isRemote)
		{
			boolean b = ((IHammerInteraction)tile).hammerUseSide(side, player, hitX, hitY, hitZ);
			if(b)
				return b;
		}
		if(tile instanceof IPlayerInteraction)
		{
			boolean b = ((IPlayerInteraction)tile).interact(side, player, hand, heldItem, hitX, hitY, hitZ);
			if(b)
				return b;
		}
		if(tile instanceof IGuiTile && hand == EnumHand.MAIN_HAND && !player.isSneaking())
		{
			TileEntity master = ((IGuiTile)tile).getGuiMaster();
			if(!world.isRemote && master!=null && ((IGuiTile)master).canOpenGui(player))
				CommonProxy.openGuiForTile(player,(TileEntity & IGuiTile)master);
			return true;
		}
		return false;
	}

	@Override
	public void neighborChanged(IBlockState state, World world, BlockPos pos, Block blockIn)
	{
		TileEntity tile = world.getTileEntity(pos);
		if(tile instanceof INeighbourChangeTile && !tile.getWorld().isRemote)
			((INeighbourChangeTile)tile).onNeighborBlockChange(pos);
	}

	@Override
	public int getLightValue(IBlockState state, IBlockAccess world, BlockPos pos)
	{
		TileEntity te = world.getTileEntity(pos);
		if(te instanceof ILightValue)
			return ((ILightValue)te).getLightValue();
		return 0;
	}

	public BlockIETileProvider setHasColours()
	{
		this.hasColours = true;
		return this;
	}
	@Override
	public boolean hasCustomBlockColours()
	{
		return hasColours;
	}
	@Override
	public int getRenderColour(IBlockState state, @Nullable IBlockAccess worldIn, @Nullable BlockPos pos, int tintIndex)
	{
		if(worldIn!=null && pos!=null)
		{
			TileEntity tile = worldIn.getTileEntity(pos);
			if(tile instanceof IColouredTile)
				return ((IColouredTile)tile).getRenderColour(tintIndex);
		}
		return 0xffffff;
	}

	@Override
	public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess world, BlockPos pos)
	{
		if(world.getBlockState(pos).getBlock()!=this)
			return FULL_BLOCK_AABB;
		else
		{
			TileEntity te = world.getTileEntity(pos);
			if(te instanceof IBlockBounds)
			{
				float[] bounds = ((IBlockBounds)te).getBlockBounds();
				if(bounds!=null)
					return new AxisAlignedBB(bounds[0],bounds[1],bounds[2],bounds[3],bounds[4],bounds[5]);
			}
		}
		return super.getBoundingBox(state, world, pos);
	}
	@Override
	public void addCollisionBoxToList(IBlockState state, World world, BlockPos pos, AxisAlignedBB mask, List<AxisAlignedBB> list, Entity ent)
	{
		TileEntity te = world.getTileEntity(pos);
		if(te instanceof IAdvancedCollisionBounds)
		{
			List<AxisAlignedBB> bounds = ((IAdvancedCollisionBounds)te).getAdvancedColisionBounds();
			if(bounds!=null && !bounds.isEmpty())
			{
				for(AxisAlignedBB aabb : bounds)
					if(aabb!=null && mask.intersectsWith(aabb))
						list.add(aabb);
				return;
			}
		}
		super.addCollisionBoxToList(state, world, pos, mask, list, ent);
	}
	@Override
	public RayTraceResult collisionRayTrace(IBlockState state, World world, BlockPos pos, Vec3d start, Vec3d end)
	{
		TileEntity te = world.getTileEntity(pos);
		if(te instanceof IAdvancedSelectionBounds)
		{
			List<AxisAlignedBB> list = ((IAdvancedSelectionBounds)te).getAdvancedSelectionBounds();
			if(list!=null && !list.isEmpty())
			{
				for(AxisAlignedBB aabb : list)
				{
					RayTraceResult mop = this.rayTrace(pos, start, end, aabb.offset(-pos.getX(),-pos.getY(),-pos.getZ()));
					if(mop!=null)
						return mop;
				}
				return null;
			}
		}
		return super.collisionRayTrace(state, world, pos, start, end);
	}
//	public RayTraceResult doRaytrace(World world, BlockPos pos, Vec3d start, Vec3d end)
//	{
//		start = start.addVector((double)(-pos.getX()), (double)(-pos.getY()), (double)(-pos.getZ()));
//		end = end.addVector((double)(-pos.getX()), (double)(-pos.getY()), (double)(-pos.getZ()));
//		Vec3d vec3 = start.getIntermediateWithXValue(end, this.minX);
//		Vec3d vec31 = start.getIntermediateWithXValue(end, this.maxX);
//		Vec3d vec32 = start.getIntermediateWithYValue(end, this.minY);
//		Vec3d vec33 = start.getIntermediateWithYValue(end, this.maxY);
//		Vec3d vec34 = start.getIntermediateWithZValue(end, this.minZ);
//		Vec3d vec35 = start.getIntermediateWithZValue(end, this.maxZ);
//
//		if(!this.isVecInsideYZBounds(vec3))
//			vec3 = null;
//		if(!this.isVecInsideYZBounds(vec31))
//			vec31 = null;
//		if(!this.isVecInsideXZBounds(vec32))
//			vec32 = null;
//		if(!this.isVecInsideXZBounds(vec33))
//			vec33 = null;
//		if(!this.isVecInsideXYBounds(vec34))
//			vec34 = null;
//		if(!this.isVecInsideXYBounds(vec35))
//			vec35 = null;
//
//		Vec3d vec36 = null;
//
//		if(vec3 != null && (vec36 == null || start.squareDistanceTo(vec3) < start.squareDistanceTo(vec36)))
//			vec36 = vec3;
//		if(vec31 != null && (vec36 == null || start.squareDistanceTo(vec31) < start.squareDistanceTo(vec36)))
//			vec36 = vec31;
//		if(vec32 != null && (vec36 == null || start.squareDistanceTo(vec32) < start.squareDistanceTo(vec36)))
//			vec36 = vec32;
//		if(vec33 != null && (vec36 == null || start.squareDistanceTo(vec33) < start.squareDistanceTo(vec36)))
//			vec36 = vec33;
//		if(vec34 != null && (vec36 == null || start.squareDistanceTo(vec34) < start.squareDistanceTo(vec36)))
//			vec36 = vec34;
//		if(vec35 != null && (vec36 == null || start.squareDistanceTo(vec35) < start.squareDistanceTo(vec36)))
//			vec36 = vec35;
//
//		if (vec36 == null)
//			return null;
//		else
//		{
//			EnumFacing enumfacing = null;
//			if(vec36 == vec3)
//				enumfacing = EnumFacing.WEST;
//			if(vec36 == vec31)
//				enumfacing = EnumFacing.EAST;
//			if(vec36 == vec32)
//				enumfacing = EnumFacing.DOWN;
//			if(vec36 == vec33)
//				enumfacing = EnumFacing.UP;
//			if(vec36 == vec34)
//				enumfacing = EnumFacing.NORTH;
//			if(vec36 == vec35)
//				enumfacing = EnumFacing.SOUTH;
//			return new RayTraceResult(vec36.addVector((double)pos.getX(), (double)pos.getY(), (double)pos.getZ()), enumfacing, pos);
//		}
//	}
//	protected boolean isVecInsideYZBounds(Vec3d point)
//	{
//		return point != null && (point.yCoord >= this.minY && point.yCoord <= this.maxY && point.zCoord >= this.minZ && point.zCoord <= this.maxZ);
//	}
//	protected boolean isVecInsideXZBounds(Vec3d point)
//	{
//		return point != null && (point.xCoord >= this.minX && point.xCoord <= this.maxX && point.zCoord >= this.minZ && point.zCoord <= this.maxZ);
//	}
//	protected boolean isVecInsideXYBounds(Vec3d point)
//	{
//		return point != null && (point.xCoord >= this.minX && point.xCoord <= this.maxX && point.yCoord >= this.minY && point.yCoord <= this.maxY);
//	}


	@Override
	public boolean hasComparatorInputOverride(IBlockState state)
	{
		return true;
	}
	@Override
	public int getComparatorInputOverride(IBlockState state, World world, BlockPos pos)
	{
		TileEntity te = world.getTileEntity(pos);
		if(te instanceof IEBlockInterfaces.IComparatorOverride)
			return ((IEBlockInterfaces.IComparatorOverride)te).getComparatorInputOverride();
		return 0;
	}


	@Override
	public int getWeakPower(IBlockState blockState, IBlockAccess world, BlockPos pos, EnumFacing side)
	{
		TileEntity te = world.getTileEntity(pos);
		if(te instanceof IEBlockInterfaces.IRedstoneOutput)
			return ((IEBlockInterfaces.IRedstoneOutput) te).getWeakRSOutput(blockState, side);
		return 0;
	}

	@Override
	public int getStrongPower(IBlockState blockState, IBlockAccess world, BlockPos pos, EnumFacing side)
	{
		TileEntity te = world.getTileEntity(pos);
		if(te instanceof IEBlockInterfaces.IRedstoneOutput)
			return ((IEBlockInterfaces.IRedstoneOutput) te).getStrongRSOutput(blockState, side);
		return 0;
	}

	@Override
	public boolean canProvidePower(IBlockState state)
	{
		return true;
	}

	@Override
	public boolean canConnectRedstone(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing side)
	{
		TileEntity te = world.getTileEntity(pos);
		if(te instanceof IEBlockInterfaces.IRedstoneOutput)
			return ((IEBlockInterfaces.IRedstoneOutput) te).canConnectRedstone(state, side);
		return false;
	}

	@Override
	public void onEntityCollidedWithBlock(World world, BlockPos pos, IBlockState state, Entity entity)
	{
		TileEntity te = world.getTileEntity(pos);
		if(te instanceof TileEntityIEBase)
			((TileEntityIEBase)te).onEntityCollision(world, entity);
	}
}