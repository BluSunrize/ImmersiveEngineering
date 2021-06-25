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
import blusunrize.immersiveengineering.common.blocks.IEBlocks.Cloth;
import blusunrize.immersiveengineering.common.blocks.cloth.ShaderBannerStandingBlock;
import blusunrize.immersiveengineering.common.blocks.cloth.ShaderBannerTileEntity;
import blusunrize.immersiveengineering.common.blocks.cloth.ShaderBannerWallBlock;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.model.ModelBakery;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.client.renderer.tileentity.BannerTileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Quaternion;
import net.minecraft.util.math.vector.Vector3f;

import javax.annotation.Nullable;
import java.util.HashMap;

public class ShaderBannerRenderer extends TileEntityRenderer<ShaderBannerTileEntity>
{
	private final ModelRenderer clothModel = BannerTileEntityRenderer.getModelRender();
	private final ModelRenderer standingModel = new ModelRenderer(64, 64, 44, 0);
	private final ModelRenderer crossbar;

	public ShaderBannerRenderer(TileEntityRendererDispatcher rendererDispatcherIn)
	{
		super(rendererDispatcherIn);
		this.standingModel.addBox(-1.0F, -30.0F, -1.0F, 2.0F, 42.0F, 2.0F, 0.0F);
		this.crossbar = new ModelRenderer(64, 64, 0, 42);
		this.crossbar.addBox(-10.0F, -32.0F, -1.0F, 20.0F, 2.0F, 2.0F, 0.0F);

	}

	@Override
	public void render(ShaderBannerTileEntity te, float partialTicks, MatrixStack matrixStack, IRenderTypeBuffer bufferIn, int combinedLightIn, int combinedOverlayIn)
	{
		long time = te.getWorldNonnull().getGameTime();
		matrixStack.push();

		// Check which of the two blocks we are so we can calculate the orientation.
		if(te.getState().getBlock() == Cloth.shaderBanner.get())
		{
			// Standing banner, we have 16 different rotations.
			int orientation = te.getState().get(ShaderBannerStandingBlock.ROTATION);
			matrixStack.translate(0.5F, 0.5F, 0.5F);
			float f1 = (float)(orientation*360)/16.0F;
			matrixStack.rotate(new Quaternion(new Vector3f(0.0F, 1.0F, 0.0F), -f1, true));
			standingModel.showModel = true;
		}
		else
		{
			// Must be the wall banner, attaches to the side of the block with no support pillar.
			assert te.getState().getBlock() == Cloth.shaderBannerWall.get();

			Direction facing = te.getState().get(ShaderBannerWallBlock.FACING);
			float rotation = facing.getHorizontalAngle();

			matrixStack.translate(0.5F, -1/6f, 0.5F);
			matrixStack.rotate(new Quaternion(new Vector3f(0.0F, 1.0F, 0.0F), -rotation, true));
			matrixStack.translate(0.0F, -0.3125F, -0.4375F);
			standingModel.showModel = false;
		}

		BlockPos blockpos = te.getPos();
		float f3 = (float)(blockpos.getX()*7+blockpos.getY()*9+blockpos.getZ()*13)+(float)time+partialTicks;
		clothModel.rotateAngleX = (-0.0125F+0.01F*MathHelper.cos(f3*(float)Math.PI*0.02F))*(float)Math.PI;
		clothModel.rotationPointY = -32.0F;
		ResourceLocation resourcelocation = this.getBannerResourceLocation(te);

		if(resourcelocation!=null)
		{
			matrixStack.push();

			matrixStack.scale(2f/3, -2f/3, -2f/3);
			IVertexBuilder builder;
			builder = bufferIn.getBuffer(RenderType.getEntitySolid(resourcelocation));
			this.clothModel.render(matrixStack, builder, combinedLightIn, combinedOverlayIn);
			builder = ModelBakery.LOCATION_BANNER_BASE.getBuffer(bufferIn, RenderType::getEntitySolid);
			this.crossbar.render(matrixStack, builder, combinedLightIn, combinedOverlayIn);
			this.standingModel.render(matrixStack, builder, combinedLightIn, combinedOverlayIn);

			matrixStack.pop();
		}

		matrixStack.pop();
	}

	private static final ResourceLocation BASE_TEXTURE = new ResourceLocation("textures/entity/banner_base.png");
	private static final HashMap<ResourceLocation, ResourceLocation> CACHE = new HashMap<>();

	@Nullable
	private ResourceLocation getBannerResourceLocation(ShaderBannerTileEntity bannerObj)
	{
		ResourceLocation name = null;
		ShaderCase sCase = null;
		ItemStack shader = bannerObj.shader.getShaderItem();
		if(!shader.isEmpty()&&shader.getItem() instanceof IShaderItem)
		{
			IShaderItem iShaderItem = ((IShaderItem)shader.getItem());
			name = iShaderItem.getShaderName(shader);
			if(CACHE.containsKey(name))
				return CACHE.get(name);
			sCase = iShaderItem.getShaderCase(shader, null, bannerObj.shader.getShaderType());
		}

		if(sCase!=null)
		{
			ShaderLayer[] layers = sCase.getLayers();
			ResourceLocation textureLocation = new ResourceLocation(name.getNamespace(), "bannershader/"+name.getPath());
			ClientUtils.mc().getTextureManager().loadTexture(textureLocation, new IEShaderLayerCompositeTexture(BASE_TEXTURE, layers));
			CACHE.put(name, textureLocation);
			return textureLocation;
		}
		return null;
	}
}
