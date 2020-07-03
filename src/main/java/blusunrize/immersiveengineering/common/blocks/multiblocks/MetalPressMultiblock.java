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
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Quaternion;
import net.minecraft.util.math.vector.Vector3i;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.template.Template.BlockInfo;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class MetalPressMultiblock extends IETemplateMultiblock
{
	public MetalPressMultiblock()
	{
		super(new ResourceLocation(ImmersiveEngineering.MODID, "multiblocks/metal_press"),
				new BlockPos(1, 1, 0), new BlockPos(1, 1, 0), () -> Multiblocks.metalPress.getDefaultState());
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
	public void renderFormedStructure(MatrixStack transform, IRenderTypeBuffer buffer)
	{
		if(renderStack==null)
			renderStack = new ItemStack(Multiblocks.metalPress);
		transform.scale(4, 4, 4);
		transform.translate(.375, .375, .125f);
		transform.rotate(new Quaternion(0, -45, 0, true));
		transform.rotate(new Quaternion(-20, 0, 0, true));

		ClientUtils.mc().getItemRenderer().renderItem(
				renderStack,
				TransformType.GUI,
				0xf000f0,
				OverlayTexture.NO_OVERLAY,
				transform, buffer
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
		return original.rotateY();
	}

	@Override
	public Direction untransformDirection(Direction transformed)
	{
		return transformed.rotateYCCW();
	}

	@Override
	protected void replaceStructureBlock(BlockInfo info, World world, BlockPos actualPos, boolean mirrored, Direction clickDirection, Vector3i offsetFromMaster)
	{
		Direction mbDirection;
		if(mirrored)
			mbDirection = transformDirection(clickDirection);
		else
			mbDirection = transformDirection(clickDirection.getOpposite());
		BlockState state = Multiblocks.metalPress.getDefaultState();
		if(!offsetFromMaster.equals(Vector3i.NULL_VECTOR))
			state = state.with(IEProperties.MULTIBLOCKSLAVE, true);
		world.setBlockState(actualPos, state);
		TileEntity curr = world.getTileEntity(actualPos);
		if(curr instanceof MetalPressTileEntity)
		{
			MetalPressTileEntity tile = (MetalPressTileEntity)curr;
			tile.formed = true;
			tile.offsetToMaster = new BlockPos(offsetFromMaster);
			tile.posInMultiblock = info.pos;
			tile.setFacing(mbDirection);
			tile.markDirty();
			world.addBlockEvent(actualPos, world.getBlockState(actualPos).getBlock(), 255, 0);
		}
		else
			IELogger.logger.error("Expected metal press TE at {} during placement", actualPos);
	}
}