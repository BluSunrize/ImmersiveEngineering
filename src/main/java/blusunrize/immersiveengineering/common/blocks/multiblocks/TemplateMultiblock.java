/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.multiblocks;

import blusunrize.immersiveengineering.api.MultiblockHandler;
import blusunrize.immersiveengineering.api.crafting.IngredientStack;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IDirectionalTile;
import blusunrize.immersiveengineering.common.blocks.generic.MultiblockPartTileEntity;
import blusunrize.immersiveengineering.common.util.IELogger;
import blusunrize.immersiveengineering.common.util.Utils;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.Mirror;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.*;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.template.PlacementSettings;
import net.minecraft.world.gen.feature.template.Template;
import net.minecraft.world.gen.feature.template.Template.BlockInfo;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

//TODO support tags/oredict
public abstract class TemplateMultiblock implements MultiblockHandler.IMultiblock
{
	private final ResourceLocation loc;
	private final BlockPos masterFromOrigin;
	public final BlockPos triggerFromOrigin;
	@Nullable
	private Template template;
	@Nullable
	private IngredientStack[] materials;
	private BlockState trigger = Blocks.AIR.getDefaultState();

	public TemplateMultiblock(ResourceLocation loc, BlockPos masterFromOrigin, BlockPos triggerFromOrigin)
	{
		this.loc = loc;
		this.masterFromOrigin = masterFromOrigin;
		this.triggerFromOrigin = triggerFromOrigin;
	}

	@Nonnull
	private Template getTemplate()
	{
		if(template==null)//TODO reset on resource reload
		{
			try
			{
				template = StaticTemplateManager.loadStaticTemplate(loc);
				List<Template.BlockInfo> blocks = template.blocks.get(0);
				for(int i = 0; i < blocks.size(); i++)
				{
					Template.BlockInfo info = blocks.get(i);
					if(info.pos.equals(triggerFromOrigin))
						trigger = info.state;
					if(info.state==Blocks.AIR.getDefaultState())
					{
						blocks.remove(i);
						i--;
					}
				}
				materials = null;
			} catch(IOException e)
			{
				throw new RuntimeException(e);
			}
		}
		return template;
	}

	//TODO make all of these non-final (currently final to make porting easier)

	@Override
	public final ResourceLocation getUniqueName()
	{
		return loc;
	}

	@Override
	public final boolean isBlockTrigger(BlockState state)
	{
		getTemplate();
		return state==trigger;
	}

	private static final List<Mirror> MIRROR_STATES = ImmutableList.of(Mirror.NONE, Mirror.LEFT_RIGHT);

	@Override
	public final boolean createStructure(World world, BlockPos pos, Direction side, PlayerEntity player)
	{
		if(side.getAxis()==Axis.Y)
			side = Direction.fromAngle(player.rotationYaw);
		Rotation rot = Utils.getRotationBetweenFacings(Direction.NORTH, side.getOpposite());
		if(rot==null)
			return false;
		Template template = getTemplate();
		mirrorLoop:
		for(Mirror mirror : MIRROR_STATES)
		{
			PlacementSettings placeSet = new PlacementSettings().setMirror(mirror).setRotation(rot);
			BlockPos origin = pos.subtract(Template.transformedBlockPos(placeSet, triggerFromOrigin));
			for(Template.BlockInfo info : template.blocks.get(0))
			{
				BlockPos realRelPos = Template.transformedBlockPos(placeSet, info.pos);
				BlockPos here = origin.add(realRelPos);

				BlockState inWorld = world.getBlockState(here);
				if(info.state!=inWorld)
					continue mirrorLoop;
				//TODO is this still necessary?
				if(info.nbt!=null)
				{
					//Check facing using TE's
					TileEntity teStored = TileEntity.create(info.nbt);
					TileEntity teWorld = world.getTileEntity(here);
					if(teStored instanceof IDirectionalTile&&teWorld instanceof IDirectionalTile)
					{
						IDirectionalTile dirStored = (IDirectionalTile)teStored;
						IDirectionalTile dirWorld = (IDirectionalTile)teWorld;
						if(dirStored.getFacing()!=dirWorld.getFacing())
							continue mirrorLoop;
					}
				}
			}
			form(world, origin, rot, mirror, side);
			return true;
		}
		return false;
	}

	protected void form(World world, BlockPos pos, Rotation rot, Mirror mirror, Direction sideHit)
	{
		BlockPos masterPos = withSettingsAndOffset(pos, masterFromOrigin, mirror, rot);
		for(BlockInfo block : getStructure())
		{
			BlockPos actualPos = withSettingsAndOffset(pos, block.pos, mirror, rot);
			replaceStructureBlock(block, world, actualPos, mirror!=Mirror.NONE, sideHit,
					actualPos.subtract(masterPos));
		}
	}

	protected abstract void replaceStructureBlock(BlockInfo into, World world, BlockPos actualPos, boolean mirrored, Direction clickDirection, Vec3i offsetFromMaster);

	@Override
	public final List<BlockInfo> getStructure()
	{
		return getTemplate().blocks.get(0);
	}

	@Override
	public final Vec3i getSize()
	{
		return getTemplate().getSize();
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public boolean overwriteBlockRender(BlockState state, int iterator)
	{
		return false;
	}

	public static BlockPos withSettingsAndOffset(BlockPos origin, BlockPos relative, Mirror mirror, Rotation rot)
	{
		PlacementSettings settings = new PlacementSettings().setMirror(mirror).setRotation(rot);
		return origin.add(Template.transformedBlockPos(settings, relative));
	}

	public static BlockPos withSettingsAndOffset(BlockPos origin, BlockPos relative, boolean mirrored, Direction facing)
	{
		Rotation rot = Utils.getRotationBetweenFacings(Direction.NORTH, facing);
		if(rot==null)
			return origin;
		return withSettingsAndOffset(origin, relative, mirrored?Mirror.LEFT_RIGHT: Mirror.NONE,
				rot);
	}

	@Override
	public final IngredientStack[] getTotalMaterials()
	{
		if(materials==null)
		{
			List<BlockInfo> structure = getStructure();
			List<IngredientStack> ret = new ArrayList<>(structure.size());
			RayTraceResult rtr = new BlockRayTraceResult(Vec3d.ZERO, Direction.DOWN, BlockPos.ZERO, false);
			for(BlockInfo info : structure)
			{
				ItemStack picked = Utils.getPickBlock(info.state, rtr, Minecraft.getInstance().player);
				boolean added = false;
				for(IngredientStack existing : ret)
					if(existing.matchesItemStackIgnoringSize(picked))
					{
						++existing.inputSize;
						added = true;
						break;
					}
				if(!added)
					ret.add(new IngredientStack(picked));
			}
			materials = ret.toArray(new IngredientStack[0]);
		}
		return materials;
	}

	@Override
	public final void disassemble(World world, BlockPos origin, boolean mirrored, Direction clickDirectionAtCreation)
	{
		Mirror mirror = mirrored?Mirror.LEFT_RIGHT: Mirror.NONE;
		Rotation rot = Utils.getRotationBetweenFacings(Direction.NORTH, clickDirectionAtCreation);
		Preconditions.checkNotNull(rot);
		for(BlockInfo block : getStructure())
		{
			BlockPos actualPos = withSettingsAndOffset(origin, block.pos, mirror, rot);
			prepareBlockForDisassembly(world, actualPos);
			world.setBlockState(actualPos, block.state);
		}
	}

	protected void prepareBlockForDisassembly(World world, BlockPos pos)
	{
		TileEntity te = world.getTileEntity(pos);
		if(te instanceof MultiblockPartTileEntity)
			((MultiblockPartTileEntity)te).formed = false;
		else
			IELogger.logger.error("Expected multiblock TE at {}", pos);
	}

	@Override
	public BlockPos getTriggerOffset()
	{
		return triggerFromOrigin;
	}
}
