/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.multiblocks;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.IEProperties;
import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.common.blocks.IEBlocks.Multiblocks;
import blusunrize.immersiveengineering.common.blocks.metal.MetalPressTileEntity;
import blusunrize.immersiveengineering.common.util.IELogger;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Quaternion;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms.TransformType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate.StructureBlockInfo;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class MetalPressMultiblock extends IETemplateMultiblock
{
	public MetalPressMultiblock()
	{
		super(new ResourceLocation(ImmersiveEngineering.MODID, "multiblocks/metal_press"),
				new BlockPos(1, 1, 0), new BlockPos(1, 1, 0), new BlockPos(3, 3, 1),
				() -> Multiblocks.metalPress.defaultBlockState());
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public boolean canRenderFormedStructure()
	{
		return true;
	}

	@OnlyIn(Dist.CLIENT)
	private static ItemStack renderStack;

	@Override
	@OnlyIn(Dist.CLIENT)
	public void renderFormedStructure(PoseStack transform, MultiBufferSource buffer)
	{
		if(renderStack==null)
			renderStack = new ItemStack(Multiblocks.metalPress);
		transform.scale(4, 4, 4);
		transform.translate(.375, .375, .125f);
		transform.mulPose(new Quaternion(0, -45, 0, true));
		transform.mulPose(new Quaternion(-20, 0, 0, true));

		ClientUtils.mc().getItemRenderer().renderStatic(
				renderStack,
				TransformType.GUI,
				0xf000f0,
				OverlayTexture.NO_OVERLAY,
				transform, buffer,
				0
		);
	}

	@Override
	public float getManualScale()
	{
		return 13;
	}

	@Override
	public Direction transformDirection(Direction original)
	{
		return original.getClockWise();
	}

	@Override
	public Direction untransformDirection(Direction transformed)
	{
		return transformed.getCounterClockWise();
	}

	@Override
	public BlockPos multiblockToModelPos(BlockPos posInMultiblock)
	{
		return super.multiblockToModelPos(new BlockPos(
				posInMultiblock.getZ()+1,
				posInMultiblock.getY(),
				1-posInMultiblock.getX()
		));
	}

	@Override
	protected void replaceStructureBlock(StructureBlockInfo info, Level world, BlockPos actualPos, boolean mirrored, Direction clickDirection, Vec3i offsetFromMaster)
	{
		Direction mbDirection;
		if(mirrored)
			mbDirection = transformDirection(clickDirection);
		else
			mbDirection = transformDirection(clickDirection.getOpposite());
		BlockState state = Multiblocks.metalPress.defaultBlockState();
		if(!offsetFromMaster.equals(Vec3i.ZERO))
			state = state.setValue(IEProperties.MULTIBLOCKSLAVE, true);
		world.setBlockAndUpdate(actualPos, state);
		BlockEntity curr = world.getBlockEntity(actualPos);
		if(curr instanceof MetalPressTileEntity)
		{
			MetalPressTileEntity tile = (MetalPressTileEntity)curr;
			tile.formed = true;
			tile.offsetToMaster = new BlockPos(offsetFromMaster);
			tile.posInMultiblock = info.pos;
			tile.setFacing(mbDirection);
			tile.setChanged();
			world.blockEvent(actualPos, world.getBlockState(actualPos).getBlock(), 255, 0);
		}
		else
			IELogger.logger.error("Expected metal press TE at {} during placement", actualPos);
	}
}