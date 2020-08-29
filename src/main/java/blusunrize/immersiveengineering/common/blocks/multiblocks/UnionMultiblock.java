/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 *
 */

package blusunrize.immersiveengineering.common.blocks.multiblocks;

import blusunrize.immersiveengineering.api.multiblocks.MultiblockHandler.IMultiblock;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.template.PlacementSettings;
import net.minecraft.world.gen.feature.template.Template;
import net.minecraft.world.gen.feature.template.Template.BlockInfo;

import java.util.ArrayList;
import java.util.List;

public class UnionMultiblock implements IMultiblock
{
	private final ResourceLocation name;
	private final List<TransformedMultiblock> parts;

	public UnionMultiblock(ResourceLocation name, List<TransformedMultiblock> parts)
	{
		this.name = name;
		this.parts = parts;
	}

	@Override
	public ResourceLocation getUniqueName()
	{
		return name;
	}

	@Override
	public boolean isBlockTrigger(BlockState state, Direction d)
	{
		return false;
	}

	@Override
	public boolean createStructure(World world, BlockPos pos, Direction side, PlayerEntity player)
	{
		return false;
	}

	@Override
	public List<BlockInfo> getStructure()
	{
		Vec3i min = getMin();
		List<BlockInfo> ret = new ArrayList<>();
		for(TransformedMultiblock part : parts)
			for(BlockInfo i : part.multiblock.getStructure())
				ret.add(new BlockInfo(part.toUnionCoords(i.pos).subtract(min), i.state, i.nbt));
		return ret;
	}

	@Override
	public ItemStack[] getTotalMaterials()
	{
		List<ItemStack> ret = new ArrayList<>();
		for(TransformedMultiblock part : parts)
			for(ItemStack stack : part.multiblock.getTotalMaterials())
			{
				boolean added = false;
				for(ItemStack ex : ret)
					if(ex.equals(stack))
					{
						ex.grow(stack.getCount());
						added = true;
						break;
					}
				if(!added)
					ret.add(stack.copy());
			}
		return ret.toArray(new ItemStack[0]);
	}

	@Override
	public boolean overwriteBlockRender(BlockState state, int iterator)
	{
		return false;
	}

	@Override
	public float getManualScale()
	{
		return 12;
	}

	@Override
	public boolean canRenderFormedStructure()
	{
		return false;
	}

	@Override
	public void renderFormedStructure(MatrixStack transform, IRenderTypeBuffer buffer)
	{

	}

	@Override
	public Vec3i getSize()
	{
		Vec3i max = Vec3i.NULL_VECTOR;
		for(TransformedMultiblock part : parts)
			max = max(max, part.toUnionCoords(part.multiblock.getSize()));
		Vec3i min = getMin();
		return new Vec3i(
				max.getX()-min.getX(),
				max.getY()-min.getY(),
				max.getZ()-min.getZ()
		);
	}

	private Vec3i getMin()
	{
		Vec3i min = Vec3i.NULL_VECTOR;
		for(TransformedMultiblock part : parts)
		{
			//TODO more intelligent approach?
			final Vec3i size = part.multiblock.getSize();
			for(int factorX = 0; factorX < 2; ++factorX)
				for(int factorY = 0; factorY < 2; ++factorY)
					for(int factorZ = 0; factorZ < 2; ++factorZ)
						min = min(min, part.toUnionCoords(new Vec3i(
								size.getX()*factorX,
								size.getY()*factorY,
								size.getZ()*factorZ
						)));
		}
		return min;
	}

	private Vec3i min(Vec3i a, Vec3i b)
	{
		return new Vec3i(
				Math.min(a.getX(), b.getX()),
				Math.min(a.getY(), b.getY()),
				Math.min(a.getZ(), b.getZ())
		);
	}

	private Vec3i max(Vec3i a, Vec3i b)
	{
		return new Vec3i(
				Math.max(a.getX(), b.getX()),
				Math.max(a.getY(), b.getY()),
				Math.max(a.getZ(), b.getZ())
		);
	}

	@Override
	public void disassemble(World world, BlockPos startPos, boolean mirrored, Direction clickDirectionAtCreation)
	{

	}

	@Override
	public BlockPos getTriggerOffset()
	{
		return BlockPos.ZERO;
	}

	public static class TransformedMultiblock
	{
		private final IMultiblock multiblock;
		private final Vec3i offset;
		private final Rotation rotation;

		public TransformedMultiblock(IMultiblock multiblock, Vec3i offset, Rotation rotation)
		{
			this.multiblock = multiblock;
			this.offset = offset;
			this.rotation = rotation;
		}

		public BlockPos toUnionCoords(Vec3i inMultiblockCoords)
		{
			return Template.transformedBlockPos(new PlacementSettings()
					.setRotation(rotation), new BlockPos(inMultiblockCoords)).add(offset);
		}
	}
}
