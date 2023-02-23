/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.render.tile;

import blusunrize.immersiveengineering.api.shader.IShaderItem;
import blusunrize.immersiveengineering.api.shader.ShaderCase;
import blusunrize.immersiveengineering.api.shader.ShaderLayer;
import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.client.render.IEShaderLayerCompositeTexture;
import blusunrize.immersiveengineering.common.blocks.cloth.ShaderBannerBlockEntity;
import blusunrize.immersiveengineering.common.blocks.cloth.ShaderBannerStandingBlock;
import blusunrize.immersiveengineering.common.blocks.cloth.ShaderBannerWallBlock;
import blusunrize.immersiveengineering.common.register.IEBlocks.Cloth;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;
import java.util.HashMap;

public class ShaderBannerRenderer extends IEBlockEntityRenderer<ShaderBannerBlockEntity>
{
	private final ModelPart clothModel;
	private final ModelPart standingModel;
	private final ModelPart crossbar;

	public ShaderBannerRenderer(BlockEntityRendererProvider.Context ctx)
	{
		ModelPart modelpart = ctx.bakeLayer(ModelLayers.BANNER);
		this.clothModel = modelpart.getChild("flag");
		this.standingModel = modelpart.getChild("pole");
		this.crossbar = modelpart.getChild("bar");
	}

	@Override
	public void render(ShaderBannerBlockEntity te, float partialTicks, PoseStack matrixStack, MultiBufferSource bufferIn, int combinedLightIn, int combinedOverlayIn)
	{
		long time = te.getLevelNonnull().getGameTime();
		matrixStack.pushPose();

		// Check which of the two blocks we are so we can calculate the orientation.
		if(te.getState().getBlock()==Cloth.SHADER_BANNER.get())
		{
			// Standing banner, we have 16 different rotations.
			int orientation = te.getState().getValue(ShaderBannerStandingBlock.ROTATION);
			matrixStack.translate(0.5F, 0.5F, 0.5F);
			float f1 = (float)(orientation*360)/16.0F;
			matrixStack.mulPose(new Quaternionf().rotateY(-f1 * Mth.DEG_TO_RAD));
			standingModel.visible = true;
		}
		else
		{
			// Must be the wall banner, attaches to the side of the block with no support pillar.
			assert te.getState().getBlock()==Cloth.SHADER_BANNER_WALL.get();

			Direction facing = te.getState().getValue(ShaderBannerWallBlock.FACING);
			float rotation = facing.toYRot();

			matrixStack.translate(0.5F, -1/6f, 0.5F);
			matrixStack.mulPose(new Quaternionf().rotateY(-rotation * Mth.DEG_TO_RAD));
			matrixStack.translate(0.0F, -0.3125F, -0.4375F);
			standingModel.visible = false;
		}

		BlockPos blockpos = te.getBlockPos();
		float f3 = (float)(blockpos.getX()*7+blockpos.getY()*9+blockpos.getZ()*13)+(float)time+partialTicks;
		clothModel.xRot = (-0.0125F+0.01F*Mth.cos(f3*(float)Math.PI*0.02F))*(float)Math.PI;
		clothModel.y = -32.0F;
		ResourceLocation resourcelocation = this.getBannerResourceLocation(te);

		if(resourcelocation!=null)
		{
			matrixStack.pushPose();

			matrixStack.scale(2f/3, -2f/3, -2f/3);
			VertexConsumer builder;
			builder = bufferIn.getBuffer(RenderType.entitySolid(resourcelocation));
			this.clothModel.render(matrixStack, builder, combinedLightIn, combinedOverlayIn);
			builder = ModelBakery.BANNER_BASE.buffer(bufferIn, RenderType::entitySolid);
			this.crossbar.render(matrixStack, builder, combinedLightIn, combinedOverlayIn);
			this.standingModel.render(matrixStack, builder, combinedLightIn, combinedOverlayIn);

			matrixStack.popPose();
		}

		matrixStack.popPose();
	}

	private static final ResourceLocation BASE_TEXTURE = new ResourceLocation("textures/entity/banner_base.png");
	private static final HashMap<ResourceLocation, ResourceLocation> CACHE = new HashMap<>();

	@Nullable
	private ResourceLocation getBannerResourceLocation(ShaderBannerBlockEntity bannerObj)
	{
		return getShaderResourceLocation(bannerObj.shader.getShaderItem(), bannerObj.shader.getShaderType());
	}

	@Nullable
	public static ResourceLocation getShaderResourceLocation(ItemStack shader, ResourceLocation shaderType)
	{
		ResourceLocation name = null;
		ShaderCase sCase = null;
		if(!shader.isEmpty()&&shader.getItem() instanceof IShaderItem)
		{
			IShaderItem iShaderItem = ((IShaderItem)shader.getItem());
			name = iShaderItem.getShaderName(shader);
			if(CACHE.containsKey(name))
				return CACHE.get(name);
			sCase = iShaderItem.getShaderCase(shader, shaderType);
		}

		if(sCase!=null)
		{
			ShaderLayer[] layers = sCase.getLayers();
			ResourceLocation textureLocation = new ResourceLocation(name.getNamespace(), "bannershader/"+name.getPath());
			ClientUtils.mc().getTextureManager().register(textureLocation, new IEShaderLayerCompositeTexture(BASE_TEXTURE, layers));
			CACHE.put(name, textureLocation);
			return textureLocation;
		}
		return null;
	}
}
